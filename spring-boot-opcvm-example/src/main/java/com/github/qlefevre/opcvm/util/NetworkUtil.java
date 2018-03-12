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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;

import com.google.common.net.InternetDomainName;

public class NetworkUtil {
	
	public static final String SITENAME = "localhost";
	
	private static final List<String> HTTPHEADERS_NOT_ALLOWED = Arrays.asList("host","cookie","referer");

	public static String getDomain(String url){
		try {
			//
			String host = new URL(url).getHost();
			return InternetDomainName.from(host).topPrivateDomain().toString();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getHttpHeaders(HttpHeaders headers){
		String result = headers.keySet().stream().filter(header -> !HTTPHEADERS_NOT_ALLOWED.contains(header.toLowerCase()))
				.map(header -> header+": "+headers.getFirst(header)).collect(Collectors.joining("\n"));
		return result;
	}
	
}
