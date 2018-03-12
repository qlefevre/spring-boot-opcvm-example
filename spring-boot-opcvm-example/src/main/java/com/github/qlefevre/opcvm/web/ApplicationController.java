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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.qlefevre.opcvm.domain.Alert;
import com.github.qlefevre.opcvm.domain.Opcvm;
import com.github.qlefevre.opcvm.domain.QuoteHistory;
import com.github.qlefevre.opcvm.repository.AlertRepository;
import com.github.qlefevre.opcvm.repository.OpcvmRepository;
import com.github.qlefevre.opcvm.repository.QuoteHistoryRepository;
import com.github.qlefevre.opcvm.util.AlertType;
import com.github.qlefevre.opcvm.util.MailUtil;
import com.github.qlefevre.opcvm.util.OpcvmUtil;
import com.google.common.collect.ImmutableMap;

@Controller
public class ApplicationController {

	@Autowired
	private OpcvmRepository opcvmRepository;

	@Autowired
	private QuoteHistoryRepository quoteHistoryRepository;

	@Autowired
	private AlertRepository alertRepository;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("YYYY-MM-dd");
    
    private static final int DEFAULT_PAGEABLE_SIZE = 20;

	@RequestMapping("/")
	public String index(Model model, Principal user, @RequestParam(value = "r", required = false) String request,
			@PageableDefault(size = DEFAULT_PAGEABLE_SIZE, page = 0) Pageable pageable) {
		request = StringUtils.defaultString(request).trim().replaceAll("\\s+", " ");
		String requestSql = "%" + request.replace(' ', '%') + "%";
		List<Opcvm> opcvms = opcvmRepository.findTop20ByIsinLikeOrNameLike(requestSql, requestSql,pageable);
		int count = (int)opcvmRepository.countByIsinLikeOrNameLike(requestSql, requestSql);
		model.addAttribute("recherche", request);
		model.addAttribute("opcvms", opcvms);
		model.addAttribute("page", pageable.getPageNumber());
		model.addAttribute("paginationstart",0);
		model.addAttribute("paginationend", count/DEFAULT_PAGEABLE_SIZE);
		addUser(model, user);
		return "index";
	}
	
	@RequestMapping("/avertissement-legal")
	public String warningLegal(Model model, Principal user) {
		addUser(model, user);
		return "avertissement-legal";
	}
	
	@RequestMapping("/ajouter-une-alerte")
	public String documentation(Model model, Principal user) {
		addUser(model, user);
		return "documentation";
	}

	@RequestMapping("/alertes")
	public String alerts(Model model, Principal user) {
		addAlerts(model, user, null);
		addUser(model, user);
		return "alertes";
	}

	@RequestMapping("/alerte/{id}")
	public String alert(Model model, Principal user, @PathVariable long id) {
		return doEdit(model, user, false, id, null);
	}

	@RequestMapping("/nouvellealerte/{isin}")
	public String newAlert(Model model, Principal user, @PathVariable String isin) {
		return doEdit(model, user, true, -1L, isin);
	}

	@RequestMapping("/alerte/supprimer/{id}")
	public String deleteAlert(Model model, Principal user, @PathVariable long id) {
		String email = MailUtil.extractMail(user);
		Optional<Alert> alert = alertRepository.findOneByIdAndEmail(id, email);
		if (alert.isPresent()) {
			alertRepository.delete(alert.get());
		}
		return "redirect:/alertes";
	}

	private String doEdit(Model model, Principal user, boolean newAlert, Long id, String isin) {
		addUser(model, user);
		String email = MailUtil.extractMail(user);

		Opcvm opcvm = null;
		Alert alerte = null;
		if (newAlert) {
			opcvm = opcvmRepository.findOpcvmByIsin(isin).get();
			alerte = new Alert();
			alerte.setType(AlertType.TRIGGER_ALERT);
			alerte.setIsin(isin);
			alerte.setEmail(email);
			alerte.setValue(opcvm.getQuote());
			alerte.setMinValue(opcvm.getQuote() - 1);
			alerte.setMaxValue(opcvm.getQuote() + 1);
		} else {
			alerte = alertRepository.findOneByIdAndEmail(id, email).get(); // securité
			opcvm = opcvmRepository.findOpcvmByIsin(alerte.getIsin()).get();
		}

		model.addAttribute("alerte", alerte);
		model.addAttribute("opcvm", opcvm);

		addOpcvmChart(model, alerte.getIsin(), Collections.emptyMap());
		return "alerte";
	}

	@PostMapping("/alerte")
	public String alerte(@ModelAttribute Alert alert, Principal user) {
		String email = user != null ? MailUtil.extractMail(user) : null;
		// Sécurité petit malin

		if (alert.getId() == null || alertRepository.findOneByIdAndEmail(alert.getId(), email) != null) {

			Calendar cal = Calendar.getInstance();
			Date date = cal.getTime();
			alert.setCreationDate(date);
			cal.add(Calendar.DAY_OF_YEAR, -1);
			date = cal.getTime();
			alert.setModificationDate(date);
			cal.add(Calendar.DAY_OF_YEAR, alert.getValidityDuration() + 1);
			date = cal.getTime();
			alert.setEndDate(date);
			alert.setEmail(email);

			alertRepository.save(alert);
		}
		return "redirect:/alertes";
	}

	@RequestMapping("/opcvm/{isin}")
	public String opcvm(@PathVariable String isin, Model model, Principal user) {
		Optional<Opcvm> opcvmOptional = opcvmRepository.findOpcvmByIsin(isin);
		if(opcvmOptional.isPresent()){
		Opcvm opcvm = opcvmOptional.get();
		model.addAttribute("opcvm", opcvm);
		Map<Alert, String> alerts = addAlerts(model, user, isin);
		addOpcvmChart(model, isin, alerts);
		addUser(model, user);
		return "opcvm";
		}else{
			return "redirect:/";
		}
	}

	private Map<Alert, String> addAlerts(Model model, Principal user, String isin) {
		String email = MailUtil.extractMail(user);
		List<Alert> alerts = alertRepository.findAlertsByEmailAndIsinLikeOrderByIsinAscEndDateAsc(email,
				isin == null ? "%" : isin);
		Map<Alert, String> alertColors = OpcvmUtil.alertColors(alerts, isin != null);
		List<String> isins = alerts.stream().map(Alert::getIsin).collect(Collectors.toList());
		List<Opcvm> opcvms = opcvmRepository.findAllByIsinIn(isins);
		Map<String, String> isin2Descriptions = opcvms.stream()
				.collect(Collectors.toMap(Opcvm::getIsin, Opcvm::getName));
		Map<Long, String> alert2Actions = alerts.stream()
				.collect(Collectors.toMap(Alert::getId, alerte -> OpcvmUtil.toAction(alerte, alertColors)));
		model.addAttribute("isin2descriptions", isin2Descriptions);
		model.addAttribute("alert2actions", alert2Actions);
		model.addAttribute("alertes", alerts);
		return alertColors;
	}

	private void addUser(Model model, Principal user) {
		model.addAttribute("username", user != null ? MailUtil.extractMail(user) : null);
		model.addAttribute("connected", user != null);
	}

	private void addOpcvmChart(Model model, String isin, Map<Alert, String> alerts) {
		List<QuoteHistory> quoteHistoryList = quoteHistoryRepository.findQuoteHistorysByIsinOrderByDateAsc(isin);
		Object[] data = quoteHistoryList.stream()
				.map(quote -> ImmutableMap.of("period", SDF.format(quote.getDate()), "value", quote.getQuote()))
				.toArray();
		double minValue = OpcvmUtil.getMinValue(quoteHistoryList, alerts.keySet());
		double maxValue = OpcvmUtil.getMaxValue(quoteHistoryList, alerts.keySet());
		int ymin = ((int) minValue / 5) * 5;
		int ymax = (((int) maxValue / 5) + 1) * 5;
		model.addAttribute("data", data);
		model.addAttribute("min_value", minValue);
		model.addAttribute("max_value", maxValue);
		model.addAttribute("ymin", ymin);
		model.addAttribute("ymax", ymax);
		model.addAttribute("isin", isin);
		List<Object> alertValues = new ArrayList<>();
		List<String> colorValues = new ArrayList<>();
		if (!CollectionUtils.isEmpty(alerts)) {
			alertValues.addAll(alerts.keySet().stream().filter(alert -> AlertType.TRIGGER_ALERT.equals(alert.getType()))
					.map(Alert::getValue).collect(Collectors.toList()));
			colorValues.addAll(alerts.keySet().stream().filter(alert -> AlertType.TRIGGER_ALERT.equals(alert.getType()))
					.map(alert -> alerts.get(alert)).collect(Collectors.toList()));

			alertValues.addAll(alerts.keySet().stream().filter(alert -> AlertType.TUNNEL_ALERT.equals(alert.getType()))
					.map(alert -> new Object[] { alert.getMinValue(), alert.getMaxValue() })
					.flatMap(array -> Arrays.stream(array)).collect(Collectors.toList()));
			colorValues.addAll(alerts.keySet().stream().filter(alert -> AlertType.TUNNEL_ALERT.equals(alert.getType()))
					.map(alert -> new String[] { alerts.get(alert), alerts.get(alert) })
					.flatMap(array -> Arrays.stream(array)).collect(Collectors.toList()));
		}
		model.addAttribute("alertevaleurs", alertValues);
		model.addAttribute("alertecouleurs", colorValues);
		model.addAttribute("alertecouleursclaires", OpcvmUtil.lightenColors(colorValues));
	}

}