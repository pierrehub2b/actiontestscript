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

public class ConditionalValue {

	public static final String EQUALS = "=";
	public static final String DIFFERENT = "<>";

	private Variable variable;
	private String operator = EQUALS;
	private CalculatedValue value;
	
	private boolean execGo = false;

	public ConditionalValue(Script script, String variableName, String value) {
		this.setVariable(script.getVariable(variableName, false));
		this.setValue(new CalculatedValue(script, value));
	}

	public ConditionalValue(String o, Variable v, CalculatedValue c) {
		this.operator = o;
		this.variable = v;
		this.value = c;
	}
	
	public boolean isExecGo() {
		execGo = variable.getCalculatedValue().equals(value.getCalculated());
		execGo = (execGo && ConditionalValue.EQUALS.equals(operator)) || (!execGo && ConditionalValue.DIFFERENT.equals(operator));
		
		return execGo;
	}
	
	public StringBuilder getLog() {
		final StringBuilder sb = new StringBuilder("\"condition\":{\"variable\":\"")
		.append(variable.getName()).append("\", \"value\":\"").append(variable.getCalculatedValue())
		.append("\", \"type\":\"").append(operator).append("\", \"compareTo\":\"").append(value.getCalculated())
		.append("\", \"continue\":").append(execGo).append("}");
		
		return sb;
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