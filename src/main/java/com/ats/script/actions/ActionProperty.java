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
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;
import com.google.gson.JsonObject;

public class ActionProperty extends ActionReturnVariable {
	
	public static final String SCRIPT_LABEL = "property";
	
	private String name;
	
	public ActionProperty() {}
	
	public ActionProperty(Script script, boolean stop, ArrayList<String> options, String name, Variable variable, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray, variable);
		setName(name);
	}
	
	public ActionProperty(Script script, boolean stop, int maxTry, int delay, SearchedElement element, String name, Variable variable) {
		super(script, stop, maxTry, delay, element, variable);
		setName(name);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", \"")
				.append(name)
				.append("\", ")
				.append(variable.getName())
				.append(")");
		return codeBuilder;
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		
		if (status.isPassed()) {
			
			final String attributeValue = getTestElement().getAttribute(status, name);
			status.endDuration();
			
			if (attributeValue == null) {
				status.setError(ActionStatus.ATTRIBUTE_NOT_SET, "attribute '" + name + "' not found", name);
				ts.getRecorder().update(ActionStatus.ATTRIBUTE_NOT_SET, status.getDuration(), name);
			} else {
				status.setMessage(attributeValue);
				updateVariableValue(attributeValue);
				ts.getRecorder().update(0, status.getDuration(), name, attributeValue);
			}
		}
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("property", name);
		return super.getActionLogs(scriptName, scriptLine, data);
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public String getName() {
		return name;
	}
	
	public void setName(String value) {
		this.name = value;
	}
}