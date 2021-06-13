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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.testng.TestRunner;

import com.google.gson.JsonObject;

import io.netty.util.CharsetUtil;

public class ScriptStatus {

	private long start;
	private int actions;
	private boolean passed;

	private String testName = "";
	private String suiteName = "";
	
	private String errorScript = "";
	private String errorInfo = "";

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
	
	public String getErrorScript() {
		return errorScript;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void endLogs(ActionTestScript ts, TestRunner runner) {
		final JsonObject logs = new JsonObject();
		final long duration = System.currentTimeMillis() - start;

		logs.addProperty("name", testName);
		logs.addProperty("suite", suiteName);
		logs.addProperty("duration", duration);
		logs.addProperty("passed", passed);
		logs.addProperty("actions", actions);

		final String logsContent = logs.toString();

		ts.sendScriptInfo("Script terminated -> " + logsContent);

		final Path output = Paths.get(runner.getOutputDirectory(), "../ats-scripts.json");

		PrintWriter out = null;
		BufferedWriter bufWriter;

		try{
			bufWriter =
					Files.newBufferedWriter(
							output,
							CharsetUtil.UTF_8,
							StandardOpenOption.WRITE, 
							StandardOpenOption.APPEND,
							StandardOpenOption.CREATE);
			out = new PrintWriter(bufWriter, true);
			out.println(logsContent);
			out.close();
		}catch(IOException e){

		}
	}

	public boolean isSuiteExecution() {
		return testName != null && suiteName != null && testName.length() != 0 && suiteName.length() != 0;
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
	
	public void failedAt(String actionClass, String script, int line, String app, int errorCode, String errorMessage) {
		
		passed = false;

		final JsonObject logs = new JsonObject();
		logs.addProperty("app", app);
		logs.addProperty("errorCode", errorCode);
		logs.addProperty("errorMessage", errorMessage);

		final StringBuilder sb = 
				new StringBuilder(actionClass)
				.append(" (")
				.append(script)
				.append(":")
				.append(line)
				.append(")");

		errorScript = sb.toString();
		errorInfo = logs.toString();
	}
}