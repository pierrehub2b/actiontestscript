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

import com.ats.element.AtsBaseElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.tools.Operators;
import com.ats.tools.Utils;
import com.google.common.base.Strings;

public class CalculatedProperty implements Comparable<CalculatedProperty>{

	private final String defaultName = "name";

	private CalculatedValue value;
	private String name = defaultName;

	private Operators operatorExec = new Operators();

	public CalculatedProperty() {}

	public CalculatedProperty(Script script, String data) {
		final String[] operatorData = operatorExec.initData(data);
		setName(operatorData[0]);
		setValue(new CalculatedValue(script, operatorData[1]));
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
		return ActionTestScript.JAVA_PROPERTY_FUNCTION_NAME + "(" + operatorExec.getJavaCode() + ", \"" + name + "\", " + value.getJavaCode() + ")";
	}

	public Predicate<AtsBaseElement> getPredicate(Predicate<AtsBaseElement> predicate){
		return operatorExec.getPredicate(predicate, name, value);
	}

	public int checkProperty(ActionStatus status, String data) {

		final String errorDescription = operatorExec.check(data, value.getCalculated());
		final String shortValue = Utils.truncateString(data, 200);

		if(errorDescription == null) {
			status.setNoError(shortValue);
			return 0;
		}else {

			final StringBuilder builder = new StringBuilder("property '");
			builder.append(name)
			.append("' with actual value '")
			.append(shortValue)
			.append("' ")
			.append(errorDescription)
			.append(" '")
			.append(value.getCalculated())
			.append("'");

			status.setError(ActionStatus.ATTRIBUTE_CHECK_FAIL, builder.toString(), new String[]{shortValue, getValue().getCalculated()});
			return ActionStatus.ATTRIBUTE_CHECK_FAIL;
		}
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
		return operatorExec.isRegexp();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getOperator() {
		return operatorExec.getType();
	}

	public void setOperator(String operator) {
		this.operatorExec.setType(operator);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(!Strings.isNullOrEmpty(name)) {
			this.name = name;
		}
	}

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;
		this.operatorExec.updatePattern(value.getCalculated());
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
