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
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.actions.neoload.ActionNeoload;
import com.ats.script.actions.performance.ActionPerformance;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class ActionChannelStart extends ActionChannel {

	public static final String SCRIPT_START_LABEL = SCRIPT_LABEL + "start";
	public static final String BASIC_AUTHENTICATION = "Basic";
	
	public static final int PERF = 1;
	public static final int NEOLOAD = 2;

	private CalculatedValue application;
	private ArrayList<CalculatedValue> arguments = new ArrayList<CalculatedValue>();

	private int performance = 0;

	private String authentication = "";
	private String authenticationValue = "";

	public ActionChannelStart() {
		super();
	}

	public ActionChannelStart(Script script, String name, List<String> options, CalculatedValue value, List<String> dataArray) {
		super(script, name);
		setApplication(value);

		if(dataArray != null) {
			options.addAll(dataArray);
		}
		options.forEach(o -> parseOptions(script, o.trim()));
	}

	private void parseOptions(Script script, String value){
		if(ActionNeoload.SCRIPT_NEOLOAD_LABEL.equalsIgnoreCase(value)) {
			setPerformance(NEOLOAD);
		}else if(ActionPerformance.SCRIPT_PERFORMANCE_LABEL.equalsIgnoreCase(value)) {
			setPerformance(PERF);
		}else if(BASIC_AUTHENTICATION.equalsIgnoreCase(value)){
			setAuthentication(BASIC_AUTHENTICATION);
		}else if(BASIC_AUTHENTICATION.equalsIgnoreCase(authentication)) {
			setAuthenticationValue(value);
		}else {
			arguments.add(new CalculatedValue(script, value));
		}
	}

	public ActionChannelStart(Script script, String name, CalculatedValue value, String[] options) {
		this(script, name, Arrays.asList(options), value, null);
	}

	public ActionChannelStart(Script script, String name, CalculatedValue value, String[] options, CalculatedValue ...calculatedValues) {
		this(script, name, Arrays.asList(options), value, null);
		this.setArguments(new ArrayList<CalculatedValue>(Arrays.asList(calculatedValues)));
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		setStatus(ts.getCurrentChannel().newActionStatus(testName, testLine));
		ts.getChannelManager().startChannel(status, this);
		return true;
	}

	@Override
	public JsonObject getActionLogsData() {
		final Channel channel = status.getChannel();
		final JsonObject data = super.getActionLogsData();

		data.addProperty("app", application.getCalculated());
		data.addProperty("appVersion", channel.getApplicationVersion());
		data.addProperty("os", channel.getOs());

		return data;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {

		final StringBuilder codeBuilder = 
				super.getJavaCode()
				.append("\"").append(getName()).append("\", ")
				.append(application.getJavaCode())
				.append(", new String[]{");

		final ArrayList<String> options = new ArrayList<String>();
		if(performance == PERF) {
			options.add(String.format("\"%s\"", ActionPerformance.SCRIPT_PERFORMANCE_LABEL));
		}else if(performance == NEOLOAD) {
			options.add(String.format("\"%s\"", ActionNeoload.SCRIPT_NEOLOAD_LABEL));
		}

		if(authentication != null && authentication.length() > 0) {
			options.add(String.format("\"%s\"", authentication));
			options.add(String.format("\"%s\"", authenticationValue));
		}

		codeBuilder.append(String.join(", ", options)).append("}");

		if(arguments.size() > 0) {
			codeBuilder.append(", ");

			final StringJoiner argumentsJoiner = new StringJoiner(", ");
			for(CalculatedValue calc : arguments) {
				argumentsJoiner.add(calc.getJavaCode());
			}
			codeBuilder.append(argumentsJoiner.toString());
		}

		return codeBuilder.append(")");
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getApplication() {
		return application;
	}

	public void setApplication(CalculatedValue value) {
		this.application = value;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String value) {
		this.authentication = value;
	}

	public String getAuthenticationValue() {
		return authenticationValue;
	}

	public void setAuthenticationValue(String value) {
		this.authenticationValue = value;
	}

	public int getPerformance() {
		return performance;
	}

	public void setPerformance(int value) {
		this.performance = value;
	}

	public ArrayList<CalculatedValue> getArguments() {
		return arguments;
	}

	public void setArguments(ArrayList<CalculatedValue> arguments) {
		this.arguments = arguments;
	}
}