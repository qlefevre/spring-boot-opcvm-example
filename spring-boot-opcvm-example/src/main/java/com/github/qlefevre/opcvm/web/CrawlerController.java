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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.qlefevre.opcvm.crawler.Result;
import com.github.qlefevre.opcvm.domain.QuoteExtractor;
import com.github.qlefevre.opcvm.domain.QuoteResult;
import com.github.qlefevre.opcvm.domain.QuoteSupplier;
import com.github.qlefevre.opcvm.domain.QuoteUrl;
import com.github.qlefevre.opcvm.repository.QuoteExtractorRepository;
import com.github.qlefevre.opcvm.repository.QuoteResultRepository;
import com.github.qlefevre.opcvm.repository.QuoteSupplierRepository;
import com.github.qlefevre.opcvm.repository.QuoteUrlRepository;
import com.github.qlefevre.opcvm.util.NetworkUtil;

@Controller
public class CrawlerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerController.class);

	@Autowired
	private QuoteUrlRepository quoteUrlRepository;

	@Autowired
	private QuoteExtractorRepository quoteExtractorRepository;

	@Autowired
	private QuoteSupplierRepository quoteSupplierRepository;

	@Autowired
	private QuoteResultRepository quoteResultRepository;

	@RequestMapping("/crawler/request")
	@ResponseBody
	public Object crawlerRequest(@RequestHeader HttpHeaders headers) {
		Map<String, Object> vRequest = new LinkedHashMap<>();
		if (isMidWeek()) {
			String requestHeaders = NetworkUtil.getHttpHeaders(headers);
			vRequest.put("headers", requestHeaders);
			
			Date date = new Date();
			
			// Fournisseurs disponibles et isins à scanner
			List<QuoteSupplier> vSuppliers = quoteSupplierRepository.findQuoteSuppliersByEnabledAndStartHourLessThanEqual(true,date.getHours());
			List<String> vDomains = vSuppliers.stream().map(QuoteSupplier::getDomain).collect(Collectors.toList());

			// Urls et Extractors
			List<QuoteUrl> vQuoteUrls = new ArrayList<QuoteUrl>();
			for(String vDomain : vDomains){
				vQuoteUrls.addAll(quoteUrlRepository.findQuoteUrlsByDomainOrderByIsinAscDomainAsc(vDomain,"%"+vDomain+"%"));
			}
			
			List<QuoteExtractor> vExtractors = quoteExtractorRepository.findQuoteExtractorsByDomainIn(vDomains);

			// Objet JSON
			List<String> vUrls = vQuoteUrls.stream().map(vQuoteUrl -> vQuoteUrl.getUrl()).collect(Collectors.toList());

			vRequest.put("extractors", vExtractors);
			vRequest.put("urls", vUrls);
		}
		return vRequest;
	}
	
	@RequestMapping(value = "/crawler/response", method = RequestMethod.POST)
	@ResponseBody
	public List<Result> crawlerResponse(@RequestBody String resultsBody) {
		List<Result> results = Collections.emptyList();
		if (isMidWeek()) {
			try {
				// Résultats par Isin
				ObjectMapper mapper = new ObjectMapper();
				results = Arrays.asList((Result[]) mapper.readValue(resultsBody, Result[].class));
				results.sort(Comparator.comparing(Result::getIsin).thenComparing(Result::getUrl));
				List<QuoteResult> quoteResults = results.stream().map(result -> new QuoteResult(result.getUrl(), result.getIsin(),
						result.getName(), result.getQuote(), result.getChange())).collect(Collectors.toList());
				quoteResultRepository.save(quoteResults);
			} catch (Exception vEx) {
				LOGGER.error("Crawler Response Error", vEx);
			}
		}
		
		return results;
	}

	private boolean isMidWeek() {
		Calendar calendar = Calendar.getInstance();
		return true || !(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
	}


}