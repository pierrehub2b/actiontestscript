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
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Operators;
import com.ats.tools.Utils;

public class ActionAssertCount extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "check-count";

	private final Pattern COUNT_PATTERN = Pattern.compile("(\\d+) ?(\\-?\\+?=?)");

	private int value = 1;
	private String operator = Operators.EQUAL;

	public ActionAssertCount() {}

	public ActionAssertCount(ScriptLoader script, boolean stop, ArrayList<String> options, String data, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);

		final Matcher m = COUNT_PATTERN.matcher(data);
		if(m.matches()) {
			
			setValue(Utils.string2Int(m.group(1).trim(), 1));

			if(m.groupCount() > 1) {
				switch (m.group(2).trim()) {
				case "+":
					setOperator(Operators.GREATER_EQUAL);
					break;
				case "-":
					setOperator(Operators.LOWER_EQUAL);
					break;
				case "!":
					setOperator(Operators.DIFFERENT);
					break;
				}
			}
		}
	}

	public ActionAssertCount(Script script, boolean stop, int maxTry, int delay, SearchedElement element, String operator, int value) {
		super(script, stop, maxTry, delay, element);
		setOperator(operator);
		setValue(value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ")
		.append(Operators.getJavaCode(operator))
		.append(", ")
		.append(value)
		.append(")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts, operator, value);
	}

	@Override
	public void terminateExecution(ActionTestScript ts) {
		getTestElement().checkOccurrences(ts, status, operator, value);
		status.getElement().setFoundElements(null);
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
