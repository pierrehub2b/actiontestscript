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
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionScripting extends ActionReturnVariable {

	public static final String JAVASCRIPT_LABEL = "javascript";
	public static final String SCRIPT_LABEL = "scripting";

	private CalculatedValue jsCode;

	public ActionScripting() {}

	public ActionScripting(ScriptLoader script, int stopPolicy, ArrayList<String> options, String code, Variable variable, ArrayList<String> objectArray) {
		super(script, stopPolicy, options, objectArray, variable);
		setJsCode(new CalculatedValue(script, code));
	}

	public ActionScripting(Script script, int stopPolicy, int maxTry, int delay, SearchedElement element, CalculatedValue jsCode, Variable variable) {
		super(script, stopPolicy, maxTry, delay, element, variable);
		setJsCode(jsCode);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		String variableCode = "null";
		if(getVariable() != null) {
			variableCode = getVariable().getName();
		}
		
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ").append(jsCode.getJavaCode()).append(", ").append(variableCode).append(")");
		return codeBuilder;
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		ArrayList<String> keywords = super.getKeywords();
		keywords.add(jsCode.getKeywords());
		return keywords;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
		
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);

		if(variable != null) {
			final Object result = getTestElement().executeScript(status, jsCode.getCalculated(), true);
			if(result != null) {
				updateVariableValue(result.toString());
			}
		}else {
			getTestElement().executeScript(status, jsCode.getCalculated(), false);
		}
	
		status.endAction();
		ts.getRecorder().updateScreen(0, status.getDuration(), jsCode.getCalculated());
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("code", jsCode.getCalculated());
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getJsCode() {
		return jsCode;
	}

	public void setJsCode(CalculatedValue jsCode) {
		this.jsCode = jsCode;
	}
}