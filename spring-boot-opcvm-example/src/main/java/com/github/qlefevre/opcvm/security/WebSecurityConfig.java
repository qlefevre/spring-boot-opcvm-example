/*******************************************************************************
 * Copyright 2018  Quentin Lefèvre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.qlefevre.opcvm.security;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CompositeFilter;

import com.github.qlefevre.opcvm.repository.RoleRepository;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private DataSource datasource;
    
    @Autowired
    private OAuth2ClientContext oauth2ClientContext;
 
	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void configure(WebSecurity web)
            throws Exception {
			web.ignoring().antMatchers(HttpMethod.POST, "/crawler/**")
			.antMatchers("/ajax/**","/css/**","/js/**","/img/**");
	}
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/motdepasseoublie","/compte","/opcvm/**","/crawler/**","/recherche**","/login**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/alertes").hasRole("USER")
                //.anyRequest().authenticated()
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/")
                                                                       
			.invalidateHttpSession(true)                                             
                .permitAll();
    }
    

    
    private Filter ssoFilter() {
		OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/google");
		OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);
		googleFilter.setRestTemplate(googleTemplate);
		UserInfoTokenServices googleTokenServices = new UserInfoTokenServices(googleResource().getUserInfoUri(),google().getClientId());
		googleTokenServices.setRestTemplate(googleTemplate);
		googleTokenServices.setPrincipalExtractor(mailPrincipalExtractor());
		googleTokenServices.setAuthoritiesExtractor(mailAuthoritiesExtractor());
		googleFilter.setTokenServices(googleTokenServices);
		
		OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/facebook");
		OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oauth2ClientContext);
		facebookFilter.setRestTemplate(facebookTemplate);
		UserInfoTokenServices liveTokenServices = new UserInfoTokenServices(facebookResource().getUserInfoUri(),facebook().getClientId());
		liveTokenServices.setRestTemplate(facebookTemplate);
		liveTokenServices.setPrincipalExtractor(mailPrincipalExtractor());
		facebookFilter.setTokenServices(liveTokenServices);
		
		CompositeFilter compositeFilter = new CompositeFilter();
		compositeFilter.setFilters(Arrays.asList(googleFilter,facebookFilter));
		
		return compositeFilter;
	}
    
    private PrincipalExtractor mailPrincipalExtractor(){
    	return new PrincipalExtractor() {
			@Override
			public Object extractPrincipal(Map<String, Object> map) {
				if(map.containsKey("email")){
					return map.get("email");
				}
				return null;
			}
		};
    }
    
    private AuthoritiesExtractor mailAuthoritiesExtractor(){
    	return new AuthoritiesExtractor() {
			@Override
			public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
				String mail = null;
				if(map.containsKey("email")){
					mail = (String)map.get("email");
				}
				List<GrantedAuthority> autorities = Collections.emptyList();
				if(mail != null){
					autorities = roleRepository.findAllByEmail(mail).stream().map(role -> new SimpleGrantedAuthority(role.getRole())).collect(Collectors.toList());
				}
				return autorities;
			}
			
		};
    }
    
    
    
	@Bean
	@ConfigurationProperties("google.client")
	public AuthorizationCodeResourceDetails google() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("google.resource")
	public ResourceServerProperties googleResource() {
		return new ResourceServerProperties();
	}


	@Bean
	@ConfigurationProperties("facebook.client")
	public AuthorizationCodeResourceDetails facebook() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("facebook.resource")
	public ResourceServerProperties facebookResource() {
		return new ResourceServerProperties();
	}

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    
    	JdbcUserDetailsManager userDetailsService = new JdbcUserDetailsManager();
        userDetailsService.setDataSource(datasource);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
 
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
        userDetailsService.setUsersByUsernameQuery("select email,password, enabled from user where email=?");
        userDetailsService.setAuthoritiesByUsernameQuery("select email, role from role where email=?");
        userDetailsService.setUserExistsSql("select email from user where email = ?");
    }
}