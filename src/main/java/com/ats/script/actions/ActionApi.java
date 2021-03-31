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
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.tools.Utils;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class ActionApi extends Action {

	public static final String SCRIPT_LABEL = "api-";
	private static final int SCRIPT_LABEL_LENGTH = SCRIPT_LABEL.length();

	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String PATCH = "PATCH";
	public static final String DELETE = "DELETE";

	public static final String SOAP = "SOAP";
	public static final String REST = "REST";
	
	private static final String CACHE_LABEL = "cache";

	private CalculatedValue method;
	private CalculatedValue data;
	private boolean useCache = false;

	private List<CalculatedProperty> header;

	private String type = GET;
	
	private int port = -1;

	public ActionApi() {}

	public ActionApi(Script script, String type, String options, String method, String headerData, ArrayList<String> data) {
		super(script);

		setType(type.substring(SCRIPT_LABEL_LENGTH));
		setMethod(new CalculatedValue(script, method));

		this.header = new ArrayList<CalculatedProperty>();
		if(headerData.length() > 0) {
			Arrays.stream(headerData.split(",")).forEach(s -> this.header.add(new CalculatedProperty(script, s)));
		}

		if(data.size() > 0) {
			setData(new CalculatedValue(script, data.get(0).trim()));
		}
		
		if(options.contains(CACHE_LABEL)) {
			this.useCache = true;
			options.replace(CACHE_LABEL, "");
		}
		
		this.port = Utils.string2Int(options.replace(",", ""), -1);
	}
	
	public ActionApi(Script script, String type, CalculatedValue method, CalculatedValue data) {
		this(script, type, -1, false, method, data, new CalculatedProperty[0]);
	}

	public ActionApi(Script script, String type, int port, boolean cache, CalculatedValue method, CalculatedValue data, CalculatedProperty ... headerData) {
		super(script);
		setType(type);
		setPort(port);
		setUseCache(cache);
		setMethod(method);
		setData(data);
		setHeader(new ArrayList<CalculatedProperty>(Arrays.asList(headerData)));
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {

		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append("\"")
		.append(type)
		.append("\", ")
		.append(port)
		.append(", ")
		.append(useCache)
		.append(", ")
		.append(method.getJavaCode())
		.append(", ");

		if(data != null){
			codeBuilder.append(data.getJavaCode());
		}else {
			codeBuilder.append("null");
		}

		if(header != null && header.size() > 0){

			codeBuilder.append(", ");

			StringJoiner joiner = new StringJoiner(", ");
			for (CalculatedProperty head : header){
				joiner.add(head.getJavaCode());
			}

			codeBuilder.append(joiner.toString());
		}

		codeBuilder.append(")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);
		getCurrentChannel().api(status, this);
		status.endDuration();
		return true;
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("type", type);
		data.addProperty("method", method.getCalculated());
		
		final Object statusData = status.getData();
		if(statusData == null) {
			data.addProperty(status.getMessage(), "");
		}else {
			data.addProperty(status.getMessage(), statusData.toString());
		}
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public List<CalculatedProperty> getHeader() {
		return header;
	}

	public void setHeader(List<CalculatedProperty> data) {
		this.header = data;
	}

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
		if(POST.equalsIgnoreCase(type) || PUT.equalsIgnoreCase(type) || DELETE.equalsIgnoreCase(type) || SOAP.equalsIgnoreCase(type) || PATCH.equalsIgnoreCase(type)) {
			this.type = type.toUpperCase();
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

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean value) {
		this.useCache = value;
	}
}