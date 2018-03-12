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
package com.github.qlefevre.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class TestCrawlerController {



	@Test
	public void testPostResponse() throws Exception {

		//  "http://176.170.247.89/crawler/response";
		String url = "http://localhost:8080/crawler/response";

		byte[] data = null;
		try (InputStream is = TestCrawlerController.class.getResourceAsStream("/postResults.json")) {
			data = IOUtils.toByteArray(is);
		}
		HttpResponse<JsonNode> jsonResponse = Unirest.post(url).header("accept", "application/json")
				.body(new String(data)).asJson();
		System.out.println(jsonResponse.getBody().toString());
	}

}
