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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.actions.neoload.ActionNeoload;

public class ActionChannelStart extends ActionChannel {

	public static final String SCRIPT_START_LABEL = SCRIPT_LABEL + "start";
	public static final String BASIC_AUTHENTICATION = "Basic";

	private CalculatedValue application;
	private ArrayList<CalculatedValue> arguments = new ArrayList<CalculatedValue>();

	private boolean neoload = false;
	private String authentication = "";
	private String authenticationValue = "";
	
	public ActionChannelStart() {
		super();
	}
	
	public ActionChannelStart(Script script, String name, ArrayList<String> options, CalculatedValue value, ArrayList<String> dataArray) {
		super(script, name);
		setApplication(value);
		
		if(dataArray != null) {
			options.addAll(dataArray);
		}
		options.forEach(o -> parseOptions(script, o.trim()));
	}
	
	private void parseOptions(Script script, String value){
		if(ActionNeoload.SCRIPT_NEOLOAD_LABEL.equalsIgnoreCase(value)) {
			setNeoload(true);
		}else if(BASIC_AUTHENTICATION.equalsIgnoreCase(value)){
			setAuthentication(BASIC_AUTHENTICATION);
		}else if(BASIC_AUTHENTICATION.equalsIgnoreCase(authentication)) {
			setAuthenticationValue(value);
		}else {
			arguments.add(new CalculatedValue(script, value));
		}
	}
	
	public ActionChannelStart(Script script, String name, CalculatedValue value, String options) {
		this(script, name, new ArrayList<>(Arrays.asList(options.split(","))), value, null);
	}
	
	public ActionChannelStart(Script script, String name, CalculatedValue value, String options, CalculatedValue ...calculatedValues) {
		this(script, name, new ArrayList<>(Arrays.asList(options.split(","))), value, null);
		this.setArguments(new ArrayList<CalculatedValue>(Arrays.asList(calculatedValues)));
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		setStatus(new ActionStatus(ts.getCurrentChannel()));
		ts.getChannelManager().startChannel(status, this);
	}
		
	@Override
	public StringBuilder getActionLogsData() {
		final Channel channel = status.getChannel();
		return super.getActionLogsData()
				.append(", \"app\":\"").append(application.getCalculated()).append("\"")
				.append(", \"appVersion\":\"").append(channel.getApplicationVersion()).append("\"")
				.append(", \"os\":\"").append(channel.getOs()).append("\"");
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		
		final StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append("\"")
		.append(getName())
		.append("\", ")
		.append(application.getJavaCode())
		.append(", ");
				
		final StringJoiner optionsJoiner = new StringJoiner(", ", "\"", "\"");
		if(neoload) {
			optionsJoiner.add(ActionNeoload.SCRIPT_NEOLOAD_LABEL);
		}
		
		if(authentication != null && authentication.length() > 0) {
			optionsJoiner.add(authentication);
			optionsJoiner.add(authenticationValue);
		}
		
		codeBuilder.append(optionsJoiner.toString());
		
		if(arguments.size() > 0) {
			codeBuilder.append(", ");
			
			final StringJoiner argumentsJoiner = new StringJoiner(", ");
			for(CalculatedValue calc : arguments) {
				argumentsJoiner.add(calc.getJavaCode());
			}
			codeBuilder.append(argumentsJoiner.toString());
		}
		
		codeBuilder.append(")");
		return codeBuilder;
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
	
	public boolean isNeoload() {
		return neoload;
	}

	public void setNeoload(boolean value) {
		this.neoload = value;
	}
		
	public ArrayList<CalculatedValue> getArguments() {
		return arguments;
	}

	public void setArguments(ArrayList<CalculatedValue> arguments) {
		this.arguments = arguments;
	}
}