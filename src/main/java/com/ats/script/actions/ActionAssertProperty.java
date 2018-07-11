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
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionAssertProperty extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_PROPERTY = "check-property";

	private CalculatedProperty value;

	public ActionAssertProperty() {}

	public ActionAssertProperty(ScriptLoader script, boolean stop, ArrayList<String> options, ArrayList<String> objectArray, String propertyData) {
		super(script, stop, options, objectArray);
		setValue(new CalculatedProperty(script, propertyData));
	}
	
	public ActionAssertProperty(Script script, boolean stop, int maxTry, SearchedElement element, CalculatedProperty property) {
		super(script, stop, maxTry, element);
		setValue(property);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + value.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		String attributeValue = getTestElement().getAttribute(value.getName());
		
		if(attributeValue == null) {
			status.setPassed(false);
			status.setCode(ActionStatus.ATTRIBUTE_NOT_SET);
			status.setData(value.getName());
			status.setMessage("Attribute '" + value.getName() + "' not found !");
			
			ts.updateVisualStatus(false);
			ts.updateVisualValue(value.getName(), "Attribute not found");
		}else {
			if(value.checkProperty(attributeValue)) {
				status.setPassed(true);
				status.setMessage(attributeValue);
				
				ts.updateVisualStatus(true);
				ts.updateVisualValue(value.getName() + " = " + value.getValue().getCalculated(), attributeValue);
			}else {
				status.setPassed(false);
				status.setCode(ActionStatus.ATTRIBUTE_CHECK_FAIL);
				status.setData(new String[]{attributeValue, value.getValue().getCalculated()});
				status.setMessage("Attribute value '" + attributeValue + "' do not match expected value '" + value.getValue().getCalculated() + "'");
				
				ts.updateVisualStatus(false);
				ts.updateVisualValue(value.getName() + " = " + value.getValue().getCalculated(), attributeValue);
			}
		}

		status.updateDuration();
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