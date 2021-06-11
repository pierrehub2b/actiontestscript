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

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionSelect extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_SELECT = "select";
	
	public static final String SELECT_TEXT = "text";
	public static final String SELECT_VALUE = "value";
	public static final String SELECT_INDEX = "index";

	private CalculatedProperty selectValue;

	public ActionSelect() {}

	public ActionSelect(ScriptLoader script, String data, int stopPolicy, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, stopPolicy, options, objectArray);
		setSelectValue(new CalculatedProperty(script, data));
	}

	public ActionSelect(Script script, int stopPolicy, int maxTry, int delay, SearchedElement element, CalculatedProperty selectValue) {
		super(script, stopPolicy, maxTry, delay, element);
		setSelectValue(selectValue);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ")
		.append(selectValue.getJavaCode())
		.append(")");
		return codeBuilder;
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		getTestElement().select(status, selectValue);
		
		status.endAction();
		ts.getRecorder().updateScreen(0, status.getDuration());
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("select", selectValue.getValue().getCalculated());
		data.addProperty("regexp", selectValue.isRegexp());
		data.addProperty("name", selectValue.getName());
		return super.getActionLogs(scriptName, scriptLine, data);
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		ArrayList<String> keywords = super.getKeywords();
		keywords.addAll(selectValue.getKeywords());
		return keywords;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedProperty getSelectValue() {
		return selectValue;
	}

	public void setSelectValue(CalculatedProperty value) {
		this.selectValue = value;
	}
}