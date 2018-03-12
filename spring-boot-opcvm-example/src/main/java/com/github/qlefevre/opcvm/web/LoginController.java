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
package com.github.qlefevre.opcvm.web;

import java.security.Principal;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.github.qlefevre.opcvm.domain.User;
import com.github.qlefevre.opcvm.repository.UserRepository;
import com.github.qlefevre.opcvm.util.MailUtil;
import com.github.qlefevre.opcvm.util.NetworkUtil;

@Controller
public class LoginController {

    @Autowired
	private UserRepository userRepository;
    
    @Autowired
	private SpringTemplateEngine templateEngine;

    @SuppressWarnings("deprecation")
	@RequestMapping(value="/motdepasseoublie",method=RequestMethod.POST)
    public String forgotPassword(@ModelAttribute User user, Model model, Principal principal){	
    	String email = user.getEmail();
    	Optional<User> userOptional = userRepository.findByEmail(email);
    	
    	String param = null;
    	if(userOptional.isPresent()){
    		param = "resetpasswordsend"; 
    		user = userOptional.get();
    		user.setResetToken(RandomStringUtils.randomAlphanumeric(50));
    		userRepository.save(user);
    		String button = "Modifier le mot de passe";
    		String subject = "Changez votre mot de passe en 1 clic"; 
    		String content = "Bonjour,"
    		+ "<br/> Vous avez demandé à réinitialiser votre mot de passe. "
    		+ "<br/> Changez votre mot de passe en cliquant sur le bouton ci-dessous : ";
    		String link = "https://"+NetworkUtil.SITENAME+"/compte?resettoken="+user.getResetToken();
    		String info = "Attention, pour des raisons de sécurité vous ne pourrez utiliser ce lien qu'une seule fois. " 
    		+ "<br/> En cas d'erreur, cliquez à nouveau sur le lien \"mot de passe oublié\" à l'entrée du site " 
    		+ "pour recevoir un lien tout frais.vous avez demandé à réinitialiser votre mot de passe.";
    		sendMessage(email, subject, content, info,button, link);
    	}else{
    		param = "unknownmail";
    	}
    	
    	return "redirect:/login" + (param == null ? "" : "?"+param);
    }
    
    private void sendMessage(String email, String subject, String content, String info, String button,String link){
    	 Context context = new Context();
    	 context.setVariable("website", "http://localhost");
 		 context.setVariable("content", content);
 		 context.setVariable("info", info);
 		 context.setVariable("link", link);
 		 context.setVariable("subject", subject);
 		 context.setVariable("button", button);
 		 content = templateEngine.process("mail_message", context);
    	 
    	 MailUtil.sendMail(email, subject, content);
    }
    
    @RequestMapping(value="/motdepasseoublie",method=RequestMethod.GET)
    public String forgotPassword(Model model){	
    	model.addAttribute("user", new User());
    	return "forgotpassword";
    }
    
    @SuppressWarnings("deprecation")
    @RequestMapping(value="/compte",method=RequestMethod.POST)
    public String account( @RequestParam(value="resettoken",required=false) String resetToken,@RequestParam("email") String email,@RequestParam("password1") String password1,@RequestParam("password2") String password2, Model model, Principal principal){	
    	String param = null;
    	User user;
    	 if(password1 != null && password1.equals(password2)){
    		
    		if(StringUtils.isNotEmpty(resetToken)){
    			user = userRepository.findByEmail(email).get();
    			param = "passwordchanged";
    		}else{
    			user = new User();
    			user.setEmail(email);
    			user.setEnabled(false);
    			user.setConfirmationToken(RandomStringUtils.randomAlphanumeric(50));
    		}
    		PasswordEncoder encoder = new BCryptPasswordEncoder();
    		String vEncodedPassword = encoder.encode(password1);
    		user.setPassword(vEncodedPassword);
    		userRepository.save(user);
    		
    		if(StringUtils.isEmpty(resetToken)){
    			String button = "Confirmer l'adresse mail";
    			String subject = "Bienvenue chez mysite.com"; 
        		String content = "Bonjour,"
    			+ "<br/>Merci de vous être inscrit sur le site ! Veuillez confirmer votre adresse email en cliquant sur le lien suivant.";
    			String link = "https://"+NetworkUtil.SITENAME+"/compte?confirmationtoken="+user.getConfirmationToken();
    		
    			sendMessage(email, subject, content, null,button, link);
    		}
    	}
    	return "redirect:/login" + (param == null ? "" : "?"+param);
    }

 	@RequestMapping(value = "/compte",method=RequestMethod.GET)
 	public String account(Model model,@RequestParam(value="confirmationtoken",required=false) String confirmationToken, @RequestParam(value="resettoken",required=false) String resetToken) {
 		
		if (StringUtils.isNotEmpty(confirmationToken)) {
			Optional<User> userOptional = userRepository.findByConfirmationToken(confirmationToken);
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				user.setEnabled(true);
				userRepository.save(user);
				return "redirect:/login?confirm";
			}
			return "redirect:/login";
		} else if (StringUtils.isNotEmpty(resetToken)) {
			Optional<User> user = userRepository.findByResetToken(StringUtils.defaultString(resetToken));
			if (user.isPresent()) { // Token found in DB
				model.addAttribute("resetToken", resetToken);
				model.addAttribute("email", user.get().getEmail());
				return "compte";
			}
			return "redirect:/login";
		}
		return "compte";
 	}
    
    @RequestMapping("/login")
    public String login(Model model) {
        return "login";
    }

}
