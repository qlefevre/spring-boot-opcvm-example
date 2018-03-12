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
/**
 * 
 */
package com.github.qlefevre.opcvm.util;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author quentin
 *
 */
public class MailUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);
	
	
	public static  String extractMail(Principal user){
		String mail = null;
		if (user != null) {
			mail = user.getName();
			/*if (user instanceof OAuth2Authentication) {
				OAuth2Authentication oAuth2 = (OAuth2Authentication) user;
				Map<String, String> details = (Map<String, String>) oAuth2.getUserAuthentication().getDetails();
				mail = details.get("email");
			}*/
		}
		return mail;
	}
	
	public static void sendMail(String to,String subject,String content){
		
		final String username = "alertesopcvm@gmail.com";
		final String password = System.getProperty("mail.password");
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(MessageFormat.format("Mail {0} / Password {1}", username,password));
		}
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,null);

		try {

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(content,"utf-8", "html");

			Transport transport = session.getTransport("smtp");
			String mfrom = "alertesopcvm@gmail.com";
			transport.connect("smtp.gmail.com", mfrom, password);
			transport.sendMessage(message, message.getAllRecipients());
			

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}
