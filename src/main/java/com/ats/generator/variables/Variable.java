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
import com.ats.generator.variables.transform.Transformer;

import java.util.regex.Pattern;

public class Variable implements Comparable<Variable>{

	public static final String SCRIPT_LABEL = "var";
	public static final int SCRIPT_LABEL_LENGTH = SCRIPT_LABEL.length();
	public static final Pattern SCRIPT_PATTERN = Pattern.compile("\\$var\\s*?\\((\\w+)\\)", Pattern.CASE_INSENSITIVE);

	private boolean calculation = true;
	private String name = "";

	private CalculatedValue value;
	private Transformer transformation;
	private String data = null;

	public Variable() {}

	public Variable(String name, CalculatedValue value) {
		this.setName(formatVariableName(name));
		this.setValue(value);
	}

	public Variable(String name, CalculatedValue value, Transformer transformer) {
		this.setName(formatVariableName(name));
		this.setValue(value);
		this.setTransformation(transformer);
	}

	public static final String formatVariableName(String value){
		return value.replaceAll("[^A-Za-z0-9_]", "");
	}
	
	@Override
	public String toString() {
		return getCalculatedValue();
	}

	public String getCalculatedValue() {

		String result = data; 
		if(result == null) {
			result = value.getCalculated();
		}

		if(transformation == null) {
			return result;
		}else {
			return transformation.format(result);
		}
	}	

	public void setData(String value) {
		data = value;
	}
	
	//----------------------------------------------------------------------------------------------------------------------------

	@Override
	public int compareTo(Variable variable) {
		return Boolean.valueOf(isCalculation()).compareTo(Boolean.valueOf(variable.isCalculation()));
	}
	
	public boolean equals(CalculatedValue clv) {
		return clv.getCalculated().equals(getCalculatedValue());
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode(){

		StringBuilder codeBuilder = new StringBuilder(this.getClass().getSimpleName());
		codeBuilder.append(" ");
		codeBuilder.append(getName());
		codeBuilder.append(" = ");
		codeBuilder.append(ActionTestScript.JAVA_VAR_FUNCTION_NAME);
		codeBuilder.append("(\"");
		codeBuilder.append(getName());
		codeBuilder.append("\"");

		if(isCalculation()) {
			codeBuilder.append(", ");
			codeBuilder.append(value.getJavaCode());
		}

		if(transformation != null) {
			codeBuilder.append(", ");
			codeBuilder.append(transformation.getJavaCode());
		}
		codeBuilder.append(")");

		return codeBuilder.toString();
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

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;
	}

	public Transformer getTransformation() {
		return transformation;
	}

	public void setTransformation(Transformer value) {
		this.transformation = value;
	}

	public boolean isCalculation() {
		return calculation;
	}

	public void setCalculation(boolean value) {
		this.calculation = value;
	}
}