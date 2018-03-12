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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.github.qlefevre.opcvm.domain.Opcvm;
import com.github.qlefevre.opcvm.domain.QuoteHistory;
import com.github.qlefevre.opcvm.domain.QuoteResult;
import com.github.qlefevre.opcvm.repository.OpcvmRepository;
import com.github.qlefevre.opcvm.repository.QuoteHistoryRepository;
import com.github.qlefevre.opcvm.repository.QuoteResultRepository;
import com.github.qlefevre.opcvm.util.MailUtil;

@Component
public class CheckOpcvm {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckOpcvm.class);

    @Autowired
    private OpcvmRepository opcvmRepository;
    
	@Autowired
	private QuoteHistoryRepository quoteHistoryRepository;
	
	@Autowired
	private QuoteResultRepository quoteResultRepository;
    
    @Autowired
	private SpringTemplateEngine templateEngine;
	
    @Scheduled(cron = "0 20 8 * * *")
    //@Scheduled(fixedDelay=60000)
	public void checkOpcvms() {
		List<QuoteResult> results = new ArrayList<>();
		List<Opcvm> opcvms = new ArrayList<>();

		try {
			// Résultats par Isin
			for(QuoteResult quote : quoteResultRepository.findAll()){
				results.add(quote);
			}
			results.sort(Comparator.comparing(QuoteResult::getIsin).thenComparing(QuoteResult::getUrl));
			Map<String, List<QuoteResult>> resultByIsins = results.stream().collect(Collectors.groupingBy(QuoteResult::getIsin));

			// Vérification des données et enregistrement
			List<QuoteHistory> quotes = new ArrayList<>();
			for (String isin : resultByIsins.keySet()) {
				QuoteResult result = checkQuoteAndChange(resultByIsins.get(isin));
				if (result != null) {
					QuoteHistory quoteHistory = new QuoteHistory();
					quoteHistory.setChange(result.getChange());
					quoteHistory.setQuote(result.getQuote());
					quoteHistory.setDate(new Date());
					quoteHistory.setIsin(isin);
					quotes.add(quoteHistory);
					Opcvm opcvm = opcvmRepository.findOpcvmByIsin(isin).get();
					opcvm.setChange(result.getChange());
					opcvm.setQuote(result.getQuote());
					opcvms.add(opcvm);
				}
			}
			quoteHistoryRepository.save(quotes);
			opcvmRepository.save(opcvms);
			quoteResultRepository.deleteAll();
			opcvms.sort(Comparator.comparing(Opcvm::getIsin));

		} catch (Exception vEx) {
			LOGGER.error("Crawler Response Error", vEx);
		}

		// Mail content
		Date date = new Date();
		Context context = new Context();
		context.setVariable("results", results);
		context.setVariable("opcvms", opcvms);
		context.setVariable("date", date);
		String contentCrawler = templateEngine.process("mail_crawler", context);
		String contentOpcvm = templateEngine.process("mail_opcvm", context);

		// Envoi un mail d'information
		String titleCrawler = "Opcvm Crawler " + new SimpleDateFormat("dd/MM/yyyy HH'h'mm").format(date);
		String titleOpcvm = "Opcvm " + new SimpleDateFormat("dd/MM/yyyy HH'h'mm").format(date);
		MailUtil.sendMail("qlefevre@gmail.com", titleCrawler, contentCrawler);
		MailUtil.sendMail("qlefevre@gmail.com", titleOpcvm, contentOpcvm);

	}
    
	private QuoteResult checkQuoteAndChange(List<QuoteResult> results) {
		QuoteResult resultCheck = null;
		if (CollectionUtils.isNotEmpty(results) && results.size() > 1) {
			Map<String,Integer> ocurrenceMap = new HashMap<>();
			Map<String,QuoteResult> resultMap = new HashMap<>();
			for(QuoteResult result : results){
				final String key = Double.toString(result.getQuote()) + Double.toString(result.getChange());
				if(!ocurrenceMap.containsKey(key)){
					ocurrenceMap.put(key,0);
				}
				ocurrenceMap.put(key,ocurrenceMap.get(key)+1);
				resultMap.put(key, result);	
			}
			List<Map.Entry<String,Integer>> entries = ocurrenceMap.entrySet().stream().sorted((entry1,entry2) -> Integer.compare(entry1.getValue(), entry2.getValue())).collect(Collectors.toList());
			Map.Entry<String,Integer> highestOccurenceEntry = entries.get(entries.size()-1);
			if(highestOccurenceEntry.getValue()>1){
				resultCheck = resultMap.get(highestOccurenceEntry.getKey());
			}
		}
		return resultCheck;
	}
}
