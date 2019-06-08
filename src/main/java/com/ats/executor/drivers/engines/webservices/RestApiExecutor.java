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
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.config.RequestConfig.Builder;
import com.ats.executor.ActionStatus;
import com.ats.script.actions.ActionApi;

public class RestApiExecutor extends ApiExecutor {

	private HttpRequestBase request;
	private CloseableHttpClient httpClient;

	public RestApiExecutor(HttpHost proxy, int timeout, int maxTry, String authentication, String authenticationValue, String wsUrl) {

		super(timeout, maxTry, authentication, authenticationValue);

		if(!wsUrl.endsWith("/")) {
			wsUrl += "/";
		}
		this.setUri(wsUrl);

		final Builder requestBuilder = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout);

		if(proxy != null) {
			requestBuilder.setProxy(proxy);
		}

		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestBuilder.build()).build();
	}

	private String getContentType(String value) {
		value = value.trim();
		if((value.startsWith("[") && value.endsWith("]")) || (value.startsWith("{") && value.endsWith("}"))){
			return "application/json";
		}else if(value.startsWith("<") && value.endsWith(">")){
			return "application/xml";
		}
		return "application/x-www-form-urlencoded";
	}

	@Override
	public void execute(ActionStatus status, final ActionApi api) {

		super.execute(status, api);

		final String fullUri = uri.resolve(api.getMethod().getCalculated()).toString();
		String parameters = "";
		if(api.getData() != null) {
			parameters = api.getData().getCalculated();
		}

		final String apiType = api.getType().toUpperCase();
		if(ActionApi.GET.equals(apiType) || ActionApi.DELETE.equals(apiType)) {

			if(parameters.length() > 0) {
				parameters = "/" + URLEncoder.encode(parameters, StandardCharsets.UTF_8);
			}

			if(ActionApi.GET.equals(apiType)) {
				request = new HttpGet(fullUri + parameters);
			}else {
				request = new HttpDelete(fullUri + parameters);
			}

		}else {
			addHeader("Content-Type", getContentType(parameters));

			if(ActionApi.PATCH.equals(apiType)) {
				request = new HttpPatch(fullUri);
			}else if(ActionApi.PUT.equals(apiType)) {
				request = new HttpPut(fullUri);
			}else {
				request = new HttpPost(fullUri);
			}
			((HttpEntityEnclosingRequestBase)request).setEntity(new ByteArrayEntity(parameters.getBytes(StandardCharsets.UTF_8)));
		}

		headerProperties.forEach((k,v) -> request.addHeader(k, v));

		int max = maxTry;
		while(!executeRequest(status) && max > 0) {
			max--;
		}
	}

	private boolean executeRequest(ActionStatus status) {
		try {
			final HttpResponse response = httpClient.execute(request);
			final int responseCode = response.getStatusLine().getStatusCode();
			if(responseCode >= 200 && responseCode < 300) {
				final Header[] contentType = response.getHeaders("Content-Type");
				if(contentType != null && contentType.length > 0) {
					parseResponse(contentType[0].getValue(), EntityUtils.toString(response.getEntity()));
				}
			}else {
				status.setCode(ActionStatus.WEB_DRIVER_ERROR);
				status.setMessage("REST response error : (code " + responseCode + ") " + response.getStatusLine().getReasonPhrase());
				status.setPassed(false);
			}
			return true;
			
		} catch (IOException e) {
			status.setCode(ActionStatus.WEB_DRIVER_ERROR);
			status.setMessage("Execute REST action error : " + e.getMessage());
			status.setPassed(false);
		}
		return false;
	}
}
