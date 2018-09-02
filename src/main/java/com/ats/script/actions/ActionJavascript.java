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
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionJavascript extends ActionReturnVariable {

	public static final String SCRIPT_LABEL = "javascript";

	private CalculatedValue jsCode;

	public ActionJavascript() {}

	public ActionJavascript(ScriptLoader script, boolean stop, ArrayList<String> options, String code, Variable variable, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray, variable);
		setJsCode(new CalculatedValue(script, code));
	}

	public ActionJavascript(Script script, boolean stop, int maxTry, SearchedElement element, CalculatedValue jsCode, Variable variable) {
		super(script, stop, maxTry, element, variable);
		setJsCode(jsCode);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		String variableCode = "null";
		if(getVariable() != null) {
			variableCode = getVariable().getName();
		}
		return super.getJavaCode() + ", " + jsCode.getJavaCode() + ", " + variableCode + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		status.startDuration();
		Object result = getTestElement().executeScript(status, jsCode.getCalculated());
		status.endDuration();
		
		if(getVariable() != null && result != null) {
			updateVariableValue(result.toString());
		}

		ts.getRecorder().updateScreen(0, status.getDuration(), jsCode.getCalculated());
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