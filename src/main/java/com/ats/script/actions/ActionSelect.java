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

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionSelect extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_SELECT = "select";
	
	public static final String SELECT_TEXT = "text";
	public static final String SELECT_VALUE = "value";
	public static final String SELECT_INDEX = "index";

	private CalculatedProperty selectValue;

	public ActionSelect() {}

	public ActionSelect(ScriptLoader script, String data, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setSelectValue(new CalculatedProperty(script, data));
	}

	public ActionSelect(Script script, boolean stop, int maxTry, int delay, SearchedElement element, CalculatedProperty selectValue) {
		super(script, stop, maxTry, delay, element);
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
		
		status.endDuration();
		ts.getRecorder().updateScreen(0, status.getDuration());
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