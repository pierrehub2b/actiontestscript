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

package com.ats.executor;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

public class ScriptStatus {

	private long start;
	private int actions;
	private boolean passed;

	private String testName;
	private String suiteName;
	
	private ArrayList<String> errorStack = new ArrayList<String>();
	
	public ScriptStatus() {
		start = System.currentTimeMillis();
		actions = 0;
		passed = true;
	}

	public ScriptStatus(String script, String suite) {
		this();
		testName = script;
		suiteName = suite;
	}
	
	public void addCallscriptStack(String value) {
		errorStack.add(value);
	}

	public void addAction() {
		actions++;
	}

	public void failed() {
		passed = false;
	}
	
	public boolean isPassed() {
		return passed;
	}
	
	public String getTestName() {
		return testName;
	}
	
	public String getSuiteName() {
		return suiteName;
	}
	
	public int getActions() {
		return actions;
	}

	public String endLogs() {
		final JsonObject logs = new JsonObject();
		logs.addProperty("name", testName);
		logs.addProperty("suite", suiteName);
		logs.addProperty("duration", System.currentTimeMillis() - start);
		logs.addProperty("passed", passed);
		logs.addProperty("actions", actions);

		return logs.toString();
	}

	public boolean isSuiteExecution() {
		return testName != null && suiteName != null;
	}

	public void addErrorStack(String value) {
		errorStack.add(value);
	}

	public String getCallscriptStack() {
		if(errorStack.size() > 0) {
			return errorStack.stream().collect(Collectors.joining(" <- "));
		}
		return null;
	}
}