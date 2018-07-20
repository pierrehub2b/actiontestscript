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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Operators;

public class ActionAssertValue extends ActionExecute {

	public static final String SCRIPT_LABEL_VALUE = "check-value";

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

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + regexp + ", " + value1.getJavaCode() + ", " + value2.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		
		super.execute(ts);
		if(isRegexp()) {
			Matcher m = Pattern.compile(value2.getCalculated()).matcher(value1.getCalculated());
			status.setPassed(m.matches());
		}else {
			status.setPassed(value1.getCalculated().equals(value2.getCalculated()));
		}

		if(status.isPassed()) {
			ts.updateVisualStatus(0);
		}else {
			status.setCode(ActionStatus.VALUES_COMPARE_FAIL);
			status.setMessage("Value1 : '" + value1.getCalculated() + "' does not match Value2 : '" + value2.getCalculated() + "'");
			
			ts.updateVisualStatus(ActionStatus.VALUES_COMPARE_FAIL, value1.getCalculated(), value2.getCalculated());
		}
		
		status.updateDuration();
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
