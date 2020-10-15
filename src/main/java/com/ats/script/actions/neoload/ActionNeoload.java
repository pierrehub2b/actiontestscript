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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.ATS;
import com.ats.script.Script;
import com.ats.script.actions.Action;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ActionNeoload extends Action {

	public static final String SCRIPT_NEOLOAD_LABEL = "neoload";

	private static final String USER_AGENT = "ATS-Neoload-Recoder/" + ATS.VERSION;

	private OkHttpClient client;

	private String apiUrl;

	public ActionNeoload() {}

	public ActionNeoload(Script script) {
		super(script);
	}

	private void initClient() {
		client = new okhttp3.OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.cache(null).build();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		ts.getCurrentChannel().neoloadAction(this, testName, testLine);
		return true;
	}

	public void executeRequest(Channel channel, String designApiUrl) {
		initClient();
		apiUrl = designApiUrl;
		status.setPassed(true);
		status.setMessage(designApiUrl);
	}

	protected boolean postDesignData(Object data) {
		return postData(new Gson().toJson(getDesignRequest(data)));
	}

	protected boolean postData(String data) {

		final RequestBody body = RequestBody.create(null, data);

		final Builder request = 
				new Builder().url(apiUrl).post(body)
				.addHeader("User-Agent", USER_AGENT)
				.addHeader("Accept", "application/json")
				.addHeader("Content-Type", "application/json");

		try {
			
			final Response response = client.newCall(request.build()).execute();
			if(response.code() >= 200 && response.code() < 300) {
				status.setPassed(true);
			}else {
				status.setError(ActionStatus.NEOLOAD_POST_ERROR, response.message());
			}
			response.close();
			
		} catch (IOException e) {
			status.setError(ActionStatus.NEOLOAD_POST_ERROR, e.getMessage());
		}
		
		return status.isPassed();
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