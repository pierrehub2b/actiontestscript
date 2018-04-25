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

package com.ats.script.actions;

import java.net.MalformedURLException;
import java.net.URL;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionGotoUrl extends ActionExecute {

	public static final String SCRIPT_LABEL = "goto-url";

	public static final String NEXT = "next";
	public static final String REFRESH = "refresh";
	public static final String BACK = "back";

	private CalculatedValue url;

	public ActionGotoUrl() {}

	public ActionGotoUrl(Script script, boolean stop, CalculatedValue url) {
		super(script, stop);
		setUrl(url);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + url.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);

		if(NEXT.equals(url.getCalculated()) || REFRESH.equals(url.getCalculated()) || BACK.equals(url.getCalculated())) {
			ts.navigate(status, url.getCalculated());
			ts.updateVisualImage();
		}else {
			
			String urlString = url.getCalculated();
			if(!urlString.startsWith("https://") && !urlString.startsWith("http://") && !urlString.startsWith("file://") ) {
				urlString = "http://" + urlString;
			}

			try {

				ts.navigate(status, new URL(urlString), false);
				ts.updateVisualValue(urlString);
				ts.updateVisualImage();

			} catch (MalformedURLException e) {
				status.setPassed(false);
				status.setData(urlString);
				status.setCode(ActionStatus.MALFORMED_GOTO_URL);
			} 
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getUrl() {
		return url;
	}

	public void setUrl(CalculatedValue url) {
		this.url = url;
	}
}