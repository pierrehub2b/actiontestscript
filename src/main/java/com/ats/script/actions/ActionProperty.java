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

public class ActionProperty extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "property";

	private String name;
	private Variable variable;

	public ActionProperty() {}

	public ActionProperty(Script script, boolean stop, ArrayList<String> options, String name, String varName, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setName(name);
		setVariable(script.getVariable(varName, true));
	}

	public ActionProperty(Script script, boolean stop, int maxTry, SearchedElement element, String name, Variable variable) {
		super(script, stop, maxTry, element);
		setName(name);
		setVariable(variable);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", \"" + name + "\", " + variable.getName() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);
		
		if(status.isPassed()) {
			String attributeValue = getTestElement().getAttribute(name);

			if(attributeValue == null) {
				status.setPassed(false);
				status.setCode(ActionStatus.ATTRIBUTE_NOT_SET);
				status.setData(name);
				status.setMessage("Attribute '" + name + "' not found !");
				
				ts.updateVisualStatus(ActionStatus.ATTRIBUTE_NOT_SET, name, status.getDuration() + "");
				
			}else {
				status.setMessage(attributeValue);
				variable.updateValue(attributeValue);
				
				ts.updateVisualStatus(0, name, attributeValue);
			}
		}
		
		status.updateDuration();

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

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
}