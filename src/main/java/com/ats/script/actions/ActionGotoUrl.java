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

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionGotoUrl extends ActionExecute {

	public static final String SCRIPT_LABEL = "goto-url";
	public static final String SCRIPT_LABEL_SIMPLE = "goto";
	public static final String SCRIPT_LABEL_OPEN = "open-url";

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
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(url.getJavaCode()).append(")");
		return codeBuilder;
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		ArrayList<String> keywords = super.getKeywords();
		keywords.add(url.getKeywords());
		return keywords;
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {

		super.execute(ts, testName, testLine);
		
		if(status.isPassed()) {
			
			final String urlString = url.getCalculated();
			if(ts.getCurrentChannel() != null){
				ts.getCurrentChannel().navigate(status, urlString);
			}
			
			status.endAction();
			
			ts.getRecorder().updateScreen(0, status.getDuration(), urlString);
		}
		
		return true;
	}

	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("url", url.getCalculated());
		return super.getActionLogs(scriptName, scriptLine, data);
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