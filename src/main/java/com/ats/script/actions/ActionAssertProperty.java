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
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.logger.MessageCode;

public class ActionAssertProperty extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "check-property";

	private CalculatedProperty value;

	public ActionAssertProperty() {}

	public ActionAssertProperty(ScriptLoader script, boolean stop, ArrayList<String> options, String data, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setValue(new CalculatedProperty(script, data));
	}

	public ActionAssertProperty(Script script, boolean stop, int maxTry, SearchedElement element, CalculatedProperty property) {
		super(script, stop, maxTry, element);
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
		final Channel channel = ts.getCurrentChannel();

		if(status.isPassed()) {
			
			getTestElement().updateScreen();
			
			String attributeValue = getTestElement().getAttribute(status, value.getName());
												
			if(attributeValue == null) {
				status.setPassed(false);
				status.setCode(ActionStatus.ATTRIBUTE_NOT_SET);
				status.setData(value.getName());
				status.setMessage("Attribute '" + value.getName() + "' not found !");

				status.endDuration();
				ts.getRecorder().update(ActionStatus.ATTRIBUTE_NOT_SET, status.getDuration(), value.getName());

			}else {
					
				final String expectedResult = value.getExpectedResult();
				
				boolean passed = value.checkProperty(attributeValue);
				int currentTry = getActionMaxTry();
				
				while(!passed && currentTry > 0) {
					passed = value.checkProperty(attributeValue);
					
					channel.sendLog(MessageCode.PROPERTY_TRY_ASSERT, "Assert property value", currentTry);
					channel.progressiveWait(currentTry);
					currentTry--;
				}

				status.endDuration();
				
				if(passed) {
		
					status.setPassed(true);
					status.setMessage(attributeValue);
					
					ts.getRecorder().update(0, status.getDuration(), expectedResult, attributeValue);

				}else {
					status.setPassed(false);
					status.setCode(ActionStatus.ATTRIBUTE_CHECK_FAIL);
					status.setData(new String[]{attributeValue, value.getValue().getCalculated()});
					status.setMessage("Expected result : '" + expectedResult + "'");
					
					ts.getRecorder().update(ActionStatus.ATTRIBUTE_CHECK_FAIL, status.getDuration(), expectedResult, attributeValue);
				}
			}
		}
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