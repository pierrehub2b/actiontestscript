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

package com.ats.script.actions.neoload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.ats.driver.AtsManager;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;
import com.ats.script.actions.Action;
import com.google.gson.Gson;

public class ActionNeoload extends Action {

	public static final String SCRIPT_NEOLOAD_LABEL = "neoload";
	
	private static final String USER_AGENT = "ATS-Neoload-Recoder/" + AtsManager.getVersion();

	private CloseableHttpClient httpClient;
	private String apiUrl;

	public ActionNeoload() {}

	public ActionNeoload(Script script) {
		super(script);
	}

	private void initClient() {
		final Builder configBuilder = RequestConfig.custom()
				.setConnectTimeout(30000)
				.setConnectionRequestTimeout(30000)
				.setSocketTimeout(30000);

		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(configBuilder.build()).build();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		ts.getCurrentChannel().neoloadAction(this);
	}

	public void executeRequest(Channel channel, String designApiUrl) {
		initClient();
		apiUrl = designApiUrl;
		status.setPassed(true);
		status.setMessage(designApiUrl);
	}
	
	protected boolean postDesignData(Object data) {
		return postDesignDataString(new Gson().toJson(getDesignRequest(data)));
	}
	
	protected boolean postDesignDataString(String data) {
		return postData(data.getBytes(StandardCharsets.UTF_8));
	}

	private boolean postData(byte[] dataByte) {
		
		try {
			final HttpPost postRequest = new HttpPost(apiUrl);
			postRequest.addHeader("Accept", "application/json");
			postRequest.addHeader("Content-Type", "application/json");
			postRequest.addHeader("Cache-Control", "nocache");
			postRequest.addHeader("Pragma", "nocache");
			postRequest.addHeader("User-Agent", USER_AGENT);
			postRequest.addHeader("Connection", "keepalive");
						
			postRequest.setEntity(new ByteArrayEntity(dataByte));
			
			final HttpResponse response = httpClient.execute(postRequest);
			final int responseCode = response.getStatusLine().getStatusCode();
			if(responseCode >= 200 && responseCode < 300) {
				status.setPassed(true);
				return true;
			}else {
				status.setPassed(false);
				status.setMessage(response.getStatusLine().getReasonPhrase());
			}
		}catch (IOException | IllegalArgumentException e) {
			status.setPassed(false);
			status.setMessage(e.getMessage());
		}
		
		return false;
	}
		
	//--------------------------------------------------------
	// Json serialization
	//--------------------------------------------------------

	protected class DesignRequest {
		public Object d;
		public DesignRequest(Object data) {
			this.d = data;
		}
	}
	
	protected DesignRequest getDesignRequest(Object data) {
		return new DesignRequest(data);
	}
}