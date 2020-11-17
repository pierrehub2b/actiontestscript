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

package com.ats.recorder;

import java.util.ArrayList;

import com.ats.executor.ScriptStatus;

public class ReportSummary {
	
	public static final String EMPTY_VALUE = "[empty]";
	
	private int status = 1;
	private String data = "";
	private String suiteName = "";
	private String testName = "";
	private int actions = 0;
	
	private ReportSummaryError error;
	
	public void appendData(String value) {
		data += value + "<br>";
	}
	
	public void setFailData(String script, int line, String message) {
		status = 0;
		error = new ReportSummaryError(script, line, message);
	}
	
	public Object[] toData(ScriptStatus st) {
		
		if(data == null || data.isEmpty()) {
			data = EMPTY_VALUE;
		}
		
		final ArrayList<Object> result = new ArrayList<Object>();
		result.add(st.isPassed());
		result.add(st.getActions());
		result.add(st.getSuiteName());
		result.add(st.getTestName());
		result.add(data);
		
		if(status == 0 && error != null) {
			result.add(error.getScriptName());
			result.add(error.getLine());
			result.add(error.getMessage());
		}

		return result.toArray();
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int value) {
		this.status = value;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public int getActions() {
		return actions;
	}

	public void setActions(int actions) {
		this.actions = actions;
	}

	public ReportSummaryError getError() {
		return error;
	}

	public void setError(ReportSummaryError error) {
		this.error = error;
	}
}