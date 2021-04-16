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

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ats.element.AtsBaseElement;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.tools.Operators;
import com.ats.tools.Utils;

public class CalculatedProperty implements Comparable<CalculatedProperty>{

	private CalculatedValue value;
	private String name = "id";
	private Pattern regexpPattern;

	private String operator = Operators.EQUAL;

	public CalculatedProperty() {}

	public CalculatedProperty(Script script, String data) {

		final String[] operatorData = Operators.getData(data);
		if(operatorData != null){
			setOperator(operatorData[0]);
			setName(operatorData[1]);
			setValue(new CalculatedValue(script, operatorData[2]));
		}else{
			setValue(new CalculatedValue(script, "true"));
		}
	}

	public CalculatedProperty(String name, String data) {
		setName(name);
		setValue(new CalculatedValue(data));
	}

	public CalculatedProperty(String operator, String name, CalculatedValue value) {
		setOperator(operator);
		setName(name);
		setValue(value);
	}

	public CalculatedProperty(String name, CalculatedValue calc) {
		setName(name);
		setValue(calc);
	}

	public void dispose() {
		value.dispose();
		value = null;
	}

	public String getJavaCode(){
		return ActionTestScript.JAVA_PROPERTY_FUNCTION_NAME + "(" + Operators.getJavaCode(operator) + ", \"" + name + "\", " + value.getJavaCode() + ")";
	}

	public Predicate<AtsBaseElement> getPredicate(Predicate<AtsBaseElement> predicate){
		if(Operators.REGEXP.equals(operator)){
			return predicate.and(p -> regexpMatch(p.getAttribute(name)));
		}else {
			return predicate.and(p -> textEquals(p.getAttribute(name)));
		}
	}

	private String actualValue;
	public boolean checkProperty(String data) {
		this.actualValue = data;

		switch (operator) {

		case Operators.REGEXP :
			return regexpMatch(data);
			
		case Operators.DIFFERENT :
			return !textEquals(data);
			
		case Operators.GREATER :
			
			try {
				return Double.parseDouble(data) > Double.parseDouble(value.getCalculated());
			}catch (NumberFormatException e) {}
			return false;
			
		case Operators.LOWER :
			
			try {
				return Double.parseDouble(data) < Double.parseDouble(value.getCalculated());
			}catch (NumberFormatException e) {}
			return false;
			
		case Operators.GREATER_EQUAL :
			
			try {
				return Double.parseDouble(data) >= Double.parseDouble(value.getCalculated());
			}catch (NumberFormatException e) {}
			return false;

		case Operators.LOWER_EQUAL :
			
			try {
				return Double.parseDouble(data) <= Double.parseDouble(value.getCalculated());
			}catch (NumberFormatException e) {}
			return false;
			
		default :
			return textEquals(data);
		}
	}

	public boolean textEquals(String data){
		if(data == null) {
			return false;
		}
		return data.equals(value.getCalculated());
	}

	public boolean regexpMatch(String data){
		if(data == null) {
			return false;
		}
		return regexpPattern.matcher(data).matches();
	}

	public String getExpectedResultLogs() {

		final StringBuilder builder = new StringBuilder("property '");
		builder.append(name).append("' with actual value '").append(actualValue);

		if(operator.equals(Operators.REGEXP)) {
			builder.append("' do not match '");
		}else {
			builder.append("' is not equals to '");
		}

		builder.append(value.getCalculated()).append("'");

		return builder.toString();
	}

	public String getShortActualValue() {
		return Utils.truncateString(actualValue, 200);
	}

	public String getExpectedResult() {
		final String result = getName();
		if(isRegexp()) {
			return result + " match " + getValue().getCalculated();
		}else {
			return result + " == " + getValue().getCalculated();
		}
	}

	public boolean isRegexp() {
		return Operators.REGEXP.equals(operator);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;

		try {
			regexpPattern = Pattern.compile(value.getCalculated());
		}catch(PatternSyntaxException e) {
			regexpPattern = Pattern.compile(".*");
		}
	}

	//----------------------------------------------------------------------------------------------------------------------------

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(CalculatedProperty prop) {
		return toString().compareTo(prop.toString());
	}
}
