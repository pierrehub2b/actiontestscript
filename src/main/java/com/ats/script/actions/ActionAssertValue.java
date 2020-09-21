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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Operators;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionAssertValue extends ActionExecute {

	public static final String SCRIPT_LABEL = "check-value";

	private CalculatedValue value1;
	private CalculatedValue value2;

	private boolean regexp = false;

	public ActionAssertValue() {}

	public ActionAssertValue(ScriptLoader script, boolean stop, String compairData) {
		super(script, stop);

		if(!splitData(script, Operators.REGEXP_PATTERN.matcher(compairData), true)) {
			splitData(script, Operators.EQUAL_PATTERN.matcher(compairData), false);
		}
	}

	public ActionAssertValue(Script script, boolean stop, boolean regexp, CalculatedValue value1, CalculatedValue value2) {
		super(script, stop);
		setRegexp(regexp);
		setValue1(value1);
		setValue2(value2);
	}

	private boolean splitData(ScriptLoader script, Matcher m, boolean regexp) {
		if(m.find()) {
			setRegexp(regexp);
			setValue1(new CalculatedValue(script, m.group(1).trim()));
			setValue2(new CalculatedValue(script, m.group(2).trim()));
			return true;
		}
		return false;
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("value1", value1.getCalculated());
		data.addProperty("value2", value2.getCalculated());
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(regexp)
		.append(", ")
		.append(value1.getJavaCode())
		.append(", ")
		.append(value2.getJavaCode())
		.append(")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts, String testName, int testLine) {
		
		super.execute(ts, testName, testLine);
		
		final String value1Calculated = value1.getCalculated();
		final String value2Calculated = value2.getCalculated();
		
		String regexpLabel = "";
		
		if(isRegexp()) {
			regexpLabel = "regexp -> ";
			status.setPassed(Pattern.compile(value2Calculated).matcher(value1Calculated).matches());
		}else {
			status.setPassed(value1Calculated.equals(value2Calculated));
		}

		int error = 0;
		if(!status.isPassed()) {
			status.setCode(ActionStatus.VALUES_COMPARE_FAIL);
			status.setMessage("'" + value1Calculated + "' does not match '" + regexpLabel + value2Calculated + "'");
			error = ActionStatus.VALUES_COMPARE_FAIL;
		}
		
		status.endDuration();
		ts.getRecorder().update(error, status.getDuration(), value1Calculated, regexpLabel + value2Calculated);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getValue1() {
		return value1;
	}

	public void setValue1(CalculatedValue value1) {
		this.value1 = value1;
	}

	public CalculatedValue getValue2() {
		return value2;
	}

	public void setValue2(CalculatedValue value2) {
		this.value2 = value2;
	}

	public boolean isRegexp() {
		return regexp;
	}

	public void setRegexp(boolean regexp) {
		this.regexp = regexp;
	}
}
