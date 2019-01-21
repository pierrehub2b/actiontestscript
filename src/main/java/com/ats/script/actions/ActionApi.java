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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionApi extends Action {
	
	public static final String SCRIPT_LABEL = "api-";
	private static final int SCRIPT_LABEL_LENGTH = SCRIPT_LABEL.length();

	public static final String GET = "get";
	public static final String POST = "post";
	public static final String PUT = "put";
	public static final String PATCH = "patch";
	public static final String DELETE = "delete";
	
	public static final String SOAP = "soap";
	public static final String REST = "rest";
	
	public static final String SCRIPT_LABEL_GET = SCRIPT_LABEL + GET;
	public static final String SCRIPT_LABEL_DELETE = SCRIPT_LABEL + DELETE;

	private CalculatedValue method;
	private CalculatedValue data;

	private String type = GET;

	public ActionApi() {}

	public ActionApi(Script script, String type, ArrayList<String> parameters) {
		super(script);

		setType(type.substring(SCRIPT_LABEL_LENGTH));
		
		if(parameters.size() > 0) {
			setMethod(new CalculatedValue(script, parameters.get(0).trim()));
			if(parameters.size() > 1) {
				setData(new CalculatedValue(script, parameters.get(1).trim()));
			}else {
				setData(new CalculatedValue(script, ""));
			}
		}else {
			setMethod(new CalculatedValue(script, ""));
			setData(new CalculatedValue(script, ""));
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

		if(ts.getCurrentChannel() != null){
			setStatus(new ActionStatus(ts.getCurrentChannel()));
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
		type = type.toLowerCase();
		if(POST.equals(type) || PUT.equals(type) || DELETE.equals(type) || SOAP.equals(type) || PATCH.equals(type)) {
			this.type = type;
		}else {
			this.type = GET;
		}
	}

	public CalculatedValue getData() {
		return data;
	}

	public void setData(CalculatedValue value) {
		this.data = value;
	}
}