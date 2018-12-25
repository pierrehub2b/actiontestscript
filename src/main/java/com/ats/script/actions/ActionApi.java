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

import java.util.regex.Matcher;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionApi extends Action {

	public static final String SCRIPT_LABEL = "api";

	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";
	public static final String SOAP = "SOAP";

	private CalculatedValue method;
	private CalculatedValue data;

	private String type = GET;

	public ActionApi() {}

	public ActionApi(Script script, String method, String parameters) {
		super(script);

		this.method = new CalculatedValue(script, method);

		final Matcher objectMatcher = Script.OBJECT_PATTERN.matcher(parameters);

		if (objectMatcher.find()) {
			if(objectMatcher.groupCount() >= 1){
				setType(objectMatcher.group(1).trim());
				if(objectMatcher.groupCount() >= 2){
					setData(new CalculatedValue(script, objectMatcher.group(2)));
				}
			}
		}
	}

	public ActionApi(Script script, String type, CalculatedValue method, CalculatedValue data) {
		super(script);
		setType(type);
		setMethod(method);
		setData(data);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {

		StringBuilder codeBuilder = new StringBuilder(super.getJavaCode()).append("\"").append(type).append("\", ").append(method.getJavaCode()).append(", ");

		if(data != null){
			codeBuilder.append(data.getJavaCode());
		}else {
			codeBuilder.append("new ");
			codeBuilder.append(CalculatedValue.class.getName());
			codeBuilder.append("[0]");
		}

		codeBuilder.append(")");
		return codeBuilder.toString();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);

		if(ts.getCurrentChannel() != null){
			ts.getCurrentChannel().api(status, this);
		}

		status.endDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getMethod() {
		return method;
	}

	public void setMethod(CalculatedValue endPoint) {
		this.method = endPoint;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type.toUpperCase();
	}

	public CalculatedValue getData() {
		return data;
	}

	public void setData(CalculatedValue value) {
		this.data = value;
	}
}