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
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionAssertProperty extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "check-property";

	private CalculatedProperty value;
	private String attributeValue;

	public ActionAssertProperty() {}

	public ActionAssertProperty(ScriptLoader script, boolean stop, ArrayList<String> options, String data, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setValue(new CalculatedProperty(script, data));
	}

	public ActionAssertProperty(Script script, boolean stop, int maxTry, int delay, SearchedElement element, CalculatedProperty property) {
		super(script, stop, maxTry, delay, element);
		setValue(property);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ").append(value.getJavaCode()).append(")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);

		if(status.isPassed()) {
			
			getTestElement().updateScreen();
			
			int maxCheckTry = getActionMaxTry();
			int checkValue = checkProperty(value.getName());
			
			while(checkValue < 0 && maxCheckTry > 0) {
				getCurrentChannel().sendWarningLog("Assert property", maxCheckTry + " try");
				getCurrentChannel().progressiveWait(getActionMaxTry() - maxCheckTry);
				
				checkValue = checkProperty(value.getName());
				maxCheckTry--;
			}
			
			status.endDuration();
			
			if(checkValue == ActionStatus.ATTRIBUTE_NOT_SET) {
				ts.getRecorder().update(ActionStatus.ATTRIBUTE_NOT_SET, status.getDuration(), value.getName());
			}else {
				ts.getRecorder().update(checkValue, status.getDuration(), value.getExpectedResult(), attributeValue);
			}
		}
	}
	
	private int checkProperty(String name) {
		
		attributeValue = getTestElement().getAttribute(status, name);
		
		if(attributeValue == null) {
			status.setError(ActionStatus.ATTRIBUTE_NOT_SET, "attribute '" + name + "' not found", value.getName());
			return ActionStatus.ATTRIBUTE_NOT_SET;
		}else {
							
			final boolean passed = value.checkProperty(attributeValue);
			final String shortAttribute = value.getShortActualValue();
			
			if(passed) {
				status.setNoError(shortAttribute);
				return 0;
			}else {
				status.setError(ActionStatus.ATTRIBUTE_CHECK_FAIL, value.getExpectedResultLogs(), new String[]{shortAttribute, value.getValue().getCalculated()});
				return ActionStatus.ATTRIBUTE_CHECK_FAIL;
			}
		}
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("property", value.getName());
		data.addProperty("value", attributeValue);
		return super.getActionLogs(scriptName, scriptLine, data);
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedProperty getValue() {
		return value;
	}

	public void setValue(CalculatedProperty value) {
		this.value = value;
	}
}