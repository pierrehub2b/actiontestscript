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

package com.ats.generator.variables;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.google.gson.JsonObject;

public class ConditionalValue {

	public static final String EQUALS = "=";
	public static final String DIFFERENT = "<>";

	private Variable variable;
	private String operator = EQUALS;
	private CalculatedValue value;
	
	private boolean exec = false;

	public ConditionalValue(Script script, String variableName, String value) {
		this.setVariable(script.getVariable(variableName, false));
		this.setValue(new CalculatedValue(script, value));
	}
	
	public ConditionalValue(Script script, String variableName, String value, String operator) {
		this(script, variableName, value);
		this.setOperator(operator);
	}

	public ConditionalValue(String op, Variable va, CalculatedValue cv) {
		this.operator = op;
		this.variable = va;
		this.value = cv;
	}
	
	public boolean isExec() {
		final boolean isEquals = variable.getCalculatedValue().equals(value.getCalculated());
		exec = (isEquals && ConditionalValue.EQUALS.equals(operator)) || (!isEquals && ConditionalValue.DIFFERENT.equals(operator));
		return exec;
	}
	
	public JsonObject getLog() {
		return getLog(new JsonObject());
	}
	
	public JsonObject getLog(JsonObject log) {
		
		final JsonObject varData = new JsonObject();
		varData.addProperty(variable.getName(), variable.getCalculatedValue());
		
		final JsonObject compareData = new JsonObject();
		compareData.addProperty("operator", operator);
		compareData.addProperty("value", value.getCalculated());
		
		final JsonObject data = new JsonObject();
		data.add("variable", varData);
		data.add("compare", compareData);
		data.addProperty("continue", exec);
		
		log.add("condition", data);
		return log;
	}

	public StringBuilder getJavaCode(StringBuilder builder, int codeLine) {

		final StringBuilder codeBuilder = 
				new StringBuilder("if(")
				.append(ActionTestScript.JAVA_CONDITION_FUNCTION).append("(")
				.append(this.getClass().getSimpleName()).append(".");

		if(DIFFERENT.equals(operator)) {
			codeBuilder.append("DIFFERENT");
		}else {
			codeBuilder.append("EQUALS");
		}

		codeBuilder.append(", ")
		.append(codeLine)
		.append(", ")
		.append(variable.getName()).append(", ")
		.append(value.getJavaCode())
		.append(")) ")
		.append(builder);
		return codeBuilder;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public Variable getVariable() {
		return variable;
	}
	public void setVariable(Variable variable) {
		this.variable = variable;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public CalculatedValue getValue() {
		return value;
	}
	public void setValue(CalculatedValue value) {
		this.value = value;
	}
}