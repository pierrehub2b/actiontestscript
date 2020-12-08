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

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.script.actions.ActionApi;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

public class RestApiExecutor extends ApiExecutor {

	private static final String contentTypeHeader = "Content-Type";

	public RestApiExecutor(PrintStream logStream, OkHttpClient client, int timeout, int maxTry, Channel channel, String wsUrl) {

		super(logStream, client, timeout, maxTry, channel);

		if(!wsUrl.endsWith("/")) {
			wsUrl += "/";
		}
		this.setUri(wsUrl);
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

		final String fullUri = getMethodUri().toString();

		String parameters = "";
		if(api.getData() != null) {
			parameters = api.getData().getCalculated();
		}

		boolean addContentType = true;
		final Builder requestBuilder = new Builder();
		for (Entry<String,String> header : headerProperties.entrySet()) {
			if(contentTypeHeader.equals(header.getKey())) {
				addContentType = false;
			}
			requestBuilder.addHeader(header.getKey(), header.getValue());
		}

		final String apiType = api.getType().toUpperCase();
		if(ActionApi.GET.equals(apiType) || ActionApi.DELETE.equals(apiType)) {

			if(parameters.length() > 0) {
				parameters = "/" + URLEncoder.encode(parameters, StandardCharsets.UTF_8);
			}

			requestBuilder.url(fullUri + parameters);

			if(ActionApi.GET.equals(apiType)) {
				requestBuilder.get();
			}else {
				requestBuilder.delete();
			}

		}else {

			requestBuilder.url(fullUri);
			if(addContentType) {
				requestBuilder.addHeader(contentTypeHeader, getContentType(parameters));
			}

			final RequestBody body = RequestBody.create(null, parameters);

			if(ActionApi.PATCH.equals(apiType)) {
				requestBuilder.patch(body);
			}else if(ActionApi.PUT.equals(apiType)) {
				requestBuilder.put(body);
			}else {
				requestBuilder.post(body);
			}
		}

		executeRequest(status, requestBuilder.build());
	}
}