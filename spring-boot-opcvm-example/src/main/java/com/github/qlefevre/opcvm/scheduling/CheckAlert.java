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
package com.github.qlefevre.opcvm.scheduling;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.github.qlefevre.opcvm.domain.Alert;
import com.github.qlefevre.opcvm.domain.Opcvm;
import com.github.qlefevre.opcvm.repository.AlertRepository;
import com.github.qlefevre.opcvm.repository.OpcvmRepository;
import com.github.qlefevre.opcvm.util.MailUtil;
import com.github.qlefevre.opcvm.util.OpcvmUtil;

@Component
public class CheckAlert {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckAlert.class);
	
	@Autowired
    private AlertRepository alertRepository;

    @Autowired
    private OpcvmRepository opcvmRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
	private SpringTemplateEngine templateEngine;
	
    //@Scheduled(fixedRate = 60000)
    @Scheduled(cron = "0 30 8 * * *")
    public void checkAlerts() {
        Date today = new Date();
        Date yesterday = DateUtils.addDays(today, -1);
        List<Alert> alerts = alertRepository.findAlertsTop20ByModificationDateLessThanAndEndDateGreaterThanOrderByIsinAsc(yesterday, today);
		while (CollectionUtils.isNotEmpty(alerts)) {
			if (CollectionUtils.isNotEmpty(alerts)) {
				System.out.println(alerts);
			}
			for (Alert alert : alerts) {
				alert.setModificationDate(today);
				checkAlert(alert);
			}
			alertRepository.save(alerts);
			
			alerts  = alertRepository.findAlertsTop20ByModificationDateLessThanAndEndDateGreaterThanOrderByIsinAsc(yesterday, today);
			 
		}

    }

    private void checkAlert(Alert alert) {
        switch (alert.getType()) {
            case TRIGGER_ALERT:
                checkTriggerAlert(alert);
                break;
            case CHANGE_ALERT:
                checkChangeAlert(alert);
                break;
            case TUNNEL_ALERT:
            	checkTunnelAlert(alert);
            	break;
            default:
                LOGGER.error(MessageFormat.format("Unable to handle alert of type {0} - {1}", alert.getType(), alert.toString()));
        }
    }

    private void checkTriggerAlert(Alert pAlert) {
        Opcvm opcvm = opcvmRepository.findOpcvmByIsin(pAlert.getIsin()).get();
        boolean alert = false;
        String content=null;
        switch (pAlert.getTrend()) {
            case DOWN:
                alert = opcvm.getQuote() < pAlert.getValue();
                content = getMessage("alert.trigger.down");
                break;
            case UP:
                alert = opcvm.getQuote() > pAlert.getValue();
                content = getMessage("alert.trigger.up");
                break;
        }
        if (alert) {
           Object[] parameters = new Object[]{null,pAlert.getValue(),opcvm.getQuote(),opcvm.getChange()};
           sendMail(pAlert, opcvm,content,parameters);
        }
    }

    private void checkChangeAlert(Alert pAlert) {
        Opcvm opcvm = opcvmRepository.findOpcvmByIsin(pAlert.getIsin()).get();
        boolean alert = false;
        String content=null;
        switch (pAlert.getTrend()) {
            case DOWN:
                alert = opcvm.getChange() < pAlert.getValue();
                content = getMessage("alert.change.down");
                break;
            case UP:
                alert = opcvm.getChange() > pAlert.getValue();
                content = getMessage("alert.change.up");
                break;
        }
        if (alert) {
        	Object[] parameters = new Object[]{null,pAlert.getValue(),opcvm.getQuote(),opcvm.getChange()};
            sendMail(pAlert, opcvm,content,parameters);
        }
    }
    
    private void checkTunnelAlert(Alert pAlert) {
        Opcvm opcvm = opcvmRepository.findOpcvmByIsin(pAlert.getIsin()).get();
        boolean alert = opcvm.getQuote() < pAlert.getMinValue() || opcvm.getQuote() > pAlert.getMaxValue();
        if (alert) {
        	String content = getMessage("alert.tunnel");
        	Object[] parameters = new Object[]{null,opcvm.getQuote(),opcvm.getChange(),pAlert.getMinValue(),pAlert.getMaxValue()};
            sendMail(pAlert,opcvm,content,parameters);
        }
    }
    
    private void sendMail(Alert alert, Opcvm opcvm,String message, Object[] parameters){
    	 String opcvmTitle = opcvm.getName();
    	 String alertTitle = OpcvmUtil.toAction(alert);
    	 if(StringUtils.isNotEmpty(opcvmTitle)){
    		 alertTitle = " ("+alertTitle+")";
    	 }
    	 String subject = MessageFormat.format(getMessage("alert.subject"), opcvmTitle+alertTitle);
    	 parameters[0] = opcvmTitle;
    	 String content = MessageFormat.format(message, parameters);
    	 
    	 Context context = new Context();
    	 context.setVariable("website", "https://spring-boot-opcvm-example");
 		 context.setVariable("opcvm", opcvm);
 		 context.setVariable("content", content);
 		 context.setVariable("alerte", OpcvmUtil.toAction(alert, OpcvmUtil.alertColors(Arrays.asList(alert),false)));
 		 content = templateEngine.process("mail_alerte", context);
    	 
    	 MailUtil.sendMail(alert.getEmail(), subject, content);
    }
 

    private String getMessage(String message){
    	return messageSource.getMessage(message, null,Locale.getDefault());
    }
}
