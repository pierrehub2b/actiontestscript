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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Operators;

public class ActionAssertCount extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_COUNT = "check-count";

	private final Pattern COUNT_PATTERN = Pattern.compile("(.*)(\\d+) ?(\\-?\\+?)");

	private int value = 1;
	private String operator = Operators.EQUAL;

	public ActionAssertCount() {}

	public ActionAssertCount(ScriptLoader script, boolean stop, ArrayList<String> options, ArrayList<String> objectArray, String data) {
		super(script, stop, options, objectArray);

		Matcher m = COUNT_PATTERN.matcher(data);
		if(m.matches()) {
			try {
				setValue(Integer.parseInt(m.group(2).trim())); 
			}catch(NumberFormatException e) {}

			if(m.groupCount() > 2) {
				switch (m.group(3).trim()) {
				case "+":
					setOperator(Operators.GREATER_EQUALS);
					break;
				case "-":
					setOperator(Operators.LOWER_EQUALS);
					break;
				case "!":
					setOperator(Operators.DIFFERENT);
					break;
				}
			}
		}
	}

	public ActionAssertCount(Script script, boolean stop, int maxTry, SearchedElement element, String operator, int value) {
		super(script, stop, maxTry, element);
		setOperator(operator);
		setValue(value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + Operators.getJavaCode(operator) + ", " + value + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts, operator, value);
	}

	@Override
	public void terminateExecution(ActionTestScript ts) {

		int count = getTestElement().checkOccurrences(status, operator, value);

		String expectedResult = "occurences " + operator + " " + count;
		
		if(status.isPassed()) {
			ts.updateVisualStatus(0, expectedResult, value + "");
		}else {
			ts.updateVisualStatus(ActionStatus.OCCURRENCES_ERROR, expectedResult, value + "");
		}
		
		status.getElement().setFoundElements(null);
		status.updateDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}
