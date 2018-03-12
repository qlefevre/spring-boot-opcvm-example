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
package com.github.qlefevre.opcvm.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.collections.CollectionUtils;

import com.github.qlefevre.opcvm.domain.Alert;
import com.github.qlefevre.opcvm.domain.QuoteHistory;

public class OpcvmUtil {

	public static String COLOR_GREEN = "#008000";
	public static String COLOR_RED = "#ff0000";
	public static String COLOR_BLUE = "#104e8b";

	public static List<String> COLOR_RAINBOW = Arrays.asList("#7d218c", "#C57E00", "#6C240E");

	public static String toAction(Alert alert) {
		String trend = alert.isTrendUp() ? ">" : "<";
		switch (alert.getType()) {
		case TRIGGER_ALERT:
			return "valeur " + trend + " " + alert.getValue() + "€";
		case CHANGE_ALERT:
			return "variation " + trend + " " + alert.getChange() + "%";
		case TUNNEL_ALERT:
			return alert.getMinValue() + "€ <  valeur  < " + alert.getMaxValue() + "€";
		default:
		}
		return "";
	}

	public static String toAction(Alert alert, Map<Alert, String> alertColors) {
		String trend = alert.isTrendUp() ? "&gt;" : "&lt;";
		String styleColor = alertColors.get(alert);
		switch (alert.getType()) {
		case TRIGGER_ALERT:
			return "valeur <span class=\"action-valeur\" style=\"color:" + styleColor + "\" >" + trend + " "
					+ alert.getValue() + "€</span>";
		case CHANGE_ALERT:
			return "variation <span class=\"action-valeur\"  style=\"color:" + styleColor + "\">" + trend + " "
					+ alert.getChange() + "%</span>";
		case TUNNEL_ALERT:
			return "<span class=\"action-valeur\"  style=\"color:" + styleColor + "\">" + alert.getMinValue()
					+ "€ &lt; </span> valeur <span class=\"action-valeur\"  style=\"color:" + styleColor + "\"> &lt; "
					+ alert.getMaxValue() + "€</span>";
		default:
		}
		return "";
	}

	public static Map<Alert, String> alertColors(List<Alert> alerts, boolean multipleColor) {
		Map<Alert, String> colors = new HashMap<>();
		Set<String> alreadyGivenColors = new HashSet<>();
		String color;
		for (Alert alert : alerts) {
			switch (alert.getType()) {
			case TRIGGER_ALERT:
				color = TrendType.UP.equals(alert.getTrend()) ? COLOR_GREEN : COLOR_RED;
				if (multipleColor) {
					color = getColor(color, COLOR_RAINBOW, alreadyGivenColors);
				}
				colors.put(alert, color);
				break;
			case CHANGE_ALERT:
				colors.put(alert, TrendType.UP.equals(alert.getTrend()) ? COLOR_GREEN : COLOR_RED);
				break;
			case TUNNEL_ALERT:
				color = COLOR_BLUE;
				if (multipleColor) {
					color = getColor(color, COLOR_RAINBOW, alreadyGivenColors);
				}
				colors.put(alert, color);
				break;
			default:
				colors.put(alert, "");
			}
		}
		return colors;
	}

	public static String getColor(String defaultColor, List<String> colors, Set<String> alreadyGivenColors) {
		String color = null;
		if (!alreadyGivenColors.contains(defaultColor)) {
			color = defaultColor;
		}
		if (color == null) {
			for (String nColor : colors) {
				if (!alreadyGivenColors.contains(nColor)) {
					color = nColor;
					break;
				}
			}
		}
		if (color == null) {
			color = colors.get(colors.size() - 1);
		}
		alreadyGivenColors.add(color);
		return color;
	}

	public static String lightenColor(String color, double lighten) {
		int r = Math.min(255, Integer.valueOf(color.substring(1, 3), 16) + (int) (255 * lighten));
		int g = Math.min(255, Integer.valueOf(color.substring(3, 5), 16) + (int) (255 * lighten));
		int b = Math.min(255, Integer.valueOf(color.substring(5, 7), 16) + (int) (255 * lighten));
		color = "#" + Integer.toHexString(r + 512).substring(1);
		color += Integer.toHexString(g + 512).substring(1);
		color += Integer.toHexString(b + 512).substring(1);
		return color;
	}

	public static List<String> lightenColors(List<String> colors) {
		return colors.stream().map(color -> lightenColor(color, 0.2)).collect(Collectors.toList());
	}

	public static double getMinValue(List<QuoteHistory> quoteHistoryList, Collection<Alert> alertes) {
		return getMinMaxValue(quoteHistoryList, alertes, true);
	}

	public static double getMaxValue(List<QuoteHistory> quoteHistoryList, Collection<Alert> alertes) {
		return getMinMaxValue(quoteHistoryList, alertes, false);
	}

	private static double getMinMaxValue(List<QuoteHistory> quoteHistoryList, Collection<Alert> alertes, boolean min) {
		DoubleStream doubleStream = quoteHistoryList.stream().mapToDouble(QuoteHistory::getQuote);
		double rValue = min ? doubleStream.min().getAsDouble() : doubleStream.max().getAsDouble();

		if (CollectionUtils.isNotEmpty(alertes)) {
			Collection<Alert> filterAlerts = alertes.stream()
					.filter(alert -> AlertType.TRIGGER_ALERT.equals(alert.getType())).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(filterAlerts)) {
				doubleStream = filterAlerts.stream().mapToDouble(Alert::getValue);
				double triggerValue = min ? doubleStream.min().getAsDouble() : doubleStream.max().getAsDouble();
				rValue = min ? Math.min(rValue, triggerValue) : Math.max(rValue, triggerValue);

			}

			filterAlerts = alertes.stream().filter(alert -> AlertType.TUNNEL_ALERT.equals(alert.getType()))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(filterAlerts)) {
				doubleStream = alertes.stream().filter(alert -> AlertType.TUNNEL_ALERT.equals(alert.getType()))
						.mapToDouble(min ? Alert::getMinValue : Alert::getMaxValue);
				double tunnelValue = min ? doubleStream.min().getAsDouble() : doubleStream.max().getAsDouble();
				rValue = min ? Math.min(rValue, tunnelValue) : Math.max(rValue, tunnelValue);
			}
		}
		return rValue;
	}

}
