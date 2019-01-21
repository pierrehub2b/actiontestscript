/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.ats.executor.drivers.engines.webservices;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.ats.executor.ActionStatus;
import com.ats.script.actions.ActionApi;

public class RestApiExecutor extends AbstractApiExecutor {

	private HttpRequestBase request;
	private CloseableHttpClient httpClient;

	public RestApiExecutor(String wsUrl) {

		if(!wsUrl.endsWith("/")) {
			wsUrl += "/";
		}
		this.setUri(wsUrl);

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(20000)
				.setConnectionRequestTimeout(10000)
				.setSocketTimeout(5000).build();

		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
	}

	@Override
	public void execute(ActionStatus status, ActionApi api) {

		super.execute(status, api);

		final URI fullUri = uri.resolve(api.getMethod().getCalculated());
		String parameters = api.getData().getCalculated().trim();

		switch (api.getType()) {

		case ActionApi.POST:
			
			request = new HttpPost(fullUri);
			
			if((parameters.startsWith("[") && parameters.endsWith("]")) || (parameters.startsWith("{") && parameters.endsWith("}"))){
				request.addHeader("Content-Type", "application/json");
			}else if(parameters.startsWith("<") && parameters.endsWith(">")){
				request.addHeader("Content-Type", "application/xml");
			}else {
				request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			}
			((HttpPost)request).setEntity(new ByteArrayEntity(api.getData().getCalculated().getBytes(StandardCharsets.UTF_8)));

			break;
		case ActionApi.DELETE:
			
			if(parameters.length() > 0) {
				parameters = URLEncoder.encode(parameters, StandardCharsets.UTF_8);
				request = new HttpDelete(fullUri.resolve(parameters));
			}else {
				request = new HttpDelete(fullUri);
			}
			
			break;
		case ActionApi.PUT:
			
			request = new HttpPut(fullUri);
			
			if((parameters.startsWith("[") && parameters.endsWith("]")) || (parameters.startsWith("{") && parameters.endsWith("}"))){
				request.addHeader("Content-Type", "application/json");
			}else if(parameters.startsWith("<") && parameters.endsWith(">")){
				request.addHeader("Content-Type", "application/xml");
			}else {
				request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			}
			((HttpPut)request).setEntity(new ByteArrayEntity(api.getData().getCalculated().getBytes(StandardCharsets.UTF_8)));
			
			break;
		case ActionApi.PATCH:
			
			request = new HttpPatch(fullUri);
			
			if((parameters.startsWith("[") && parameters.endsWith("]")) || (parameters.startsWith("{") && parameters.endsWith("}"))){
				request.addHeader("Content-Type", "application/json");
			}else if(parameters.startsWith("<") && parameters.endsWith(">")){
				request.addHeader("Content-Type", "application/xml");
			}else {
				request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			}
			((HttpPatch)request).setEntity(new ByteArrayEntity(api.getData().getCalculated().getBytes(StandardCharsets.UTF_8)));
			
			break;
		default:

			if(parameters.length() > 0) {
				parameters = URLEncoder.encode(parameters, StandardCharsets.UTF_8);
				request = new HttpGet(fullUri.resolve(parameters));
			}else {
				request = new HttpGet(fullUri);
			}
			
			break;
		}

		request.addHeader("Accept", "application/json");

		try {
			final HttpResponse response = httpClient.execute(request);
			final Header[] contentType = response.getHeaders("Content-Type");

			if(contentType != null && contentType.length > 0) {
				parseResponse(contentType[0].getValue(), EntityUtils.toString(response.getEntity()));
			}

		} catch (IOException e) {
			status.setCode(ActionStatus.WEB_DRIVER_ERROR);
			status.setMessage("Execute rest action error : " + e.getMessage());
			status.setPassed(false);
		}
	}
}
