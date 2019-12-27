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

import com.ats.script.Script;

public class ConditionalValue {

	private Variable variable;
	private String operator = "=";
	private CalculatedValue value;

	public ConditionalValue(Script script, String variableName, String value) {
		this.setVariable(script.getVariable(variableName, false));
		this.setValue(new CalculatedValue(script, value));
	}

	public boolean isPassed() {
		return true;
	}

	public StringBuilder getJavaCode(StringBuilder builder) {
		final StringBuilder codeBuilder = 
				new StringBuilder("if(")
				.append(variable.getName())
				.append(".equals(")
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