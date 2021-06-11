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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Operators;
import com.google.gson.JsonObject;

public class ActionAssertValue extends ActionExecute {

	public static final String SCRIPT_LABEL = "check-value";

	private CalculatedValue value1;
	private CalculatedValue value2;

	private Operators operatorExec = new Operators();

	public ActionAssertValue() {}

	public ActionAssertValue(ScriptLoader script, int stopPolicy, String compairData) {
		super(script, stopPolicy);

		final String[] operatorData = operatorExec.initData(compairData);
		setValue1(new CalculatedValue(script, operatorData[0]));
		setValue2(new CalculatedValue(script, operatorData[1]));
	}

	public ActionAssertValue(Script script, int stopPolicy, String operator, CalculatedValue value1, CalculatedValue value2) {
		super(script, stopPolicy);
		setOperator(operator);
		setValue1(value1);
		setValue2(value2);
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
		codeBuilder.append(operatorExec.getJavaCode())
		.append(", ")
		.append(value1.getJavaCode())
		.append(", ")
		.append(value2.getJavaCode())
		.append(")");
		return codeBuilder;
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		final ArrayList<String> keywords = super.getKeywords();
		keywords.add(value1.getKeywords());
		keywords.add(value2.getKeywords());
		return keywords;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		
		super.execute(ts, testName, testLine);
		
		final String calculated1 = value1.getCalculated();
		final String calculated2 = value2.getCalculated();
		
		final String errorDescription = operatorExec.check(calculated1, calculated2);
		
		if(errorDescription == null) {
			status.setNoError(calculated1);
		}else {

			final StringBuilder builder = new StringBuilder("Value1 '");
			builder.append(calculated1)
			.append("' ")
			.append(errorDescription)
			.append(" value2 '")
			.append(calculated2)
			.append("'");

			status.setError(ActionStatus.VALUES_COMPARE_FAIL, builder.toString(), new String[]{calculated1, calculated2});
		}
		
		status.endDuration();
		ts.getRecorder().update(status.getCode(), status.getDuration(), calculated1, calculated2);
		
		return true;
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
		this.operatorExec.updatePattern(value2.getCalculated());
	}

	public String getOperator() {
		return operatorExec.getType();
	}

	public void setOperator(String operator) {
		this.operatorExec.setType(operator);
	}
}
