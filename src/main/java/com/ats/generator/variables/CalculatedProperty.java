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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ats.element.AtsBaseElement;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.tools.Operators;

public class CalculatedProperty implements Comparable<CalculatedProperty>{

	private CalculatedValue value;
	private String name = "id";
	private boolean regexp = false;

	private Pattern regexpPattern;

	public CalculatedProperty() {}

	public CalculatedProperty(Script script, String data) {

		Matcher objectMatcher = Operators.REGEXP_PATTERN.matcher(data);
		boolean dataFound = objectMatcher.find();

		if (dataFound) {
			setRegexp(true);
		}else {
			objectMatcher = Operators.EQUAL_PATTERN.matcher(data);
			dataFound = objectMatcher.find();
		}

		if(dataFound && objectMatcher.groupCount() >= 2){

			setName(objectMatcher.group(1).trim());
			setValue(new CalculatedValue(script, objectMatcher.group(2).trim()));

		}else{
			setValue(new CalculatedValue(script, "true"));
		}
	}

	public CalculatedProperty(String name, String data) {
		setName(name);
		setValue(new CalculatedValue(data));
	}

	public CalculatedProperty(boolean isRegexp, String name, CalculatedValue value) {
		setRegexp(isRegexp);
		setName(name);
		setValue(value);
	}

	public void dispose() {
		value.dispose();
		value = null;
	}

	public String getJavaCode(){
		return ActionTestScript.JAVA_PROPERTY_FUNCTION_NAME + "(" + isRegexp() + ", \"" + name + "\", " + value.getJavaCode() + ")";
	}

	public Predicate<AtsBaseElement> getPredicate(Predicate<AtsBaseElement> predicate){
		if(isRegexp()) {
			return predicate.and(p -> regexpMatch(p.getAttribute(name)));
		}else {
			return predicate.and(p -> textEquals(p.getAttribute(name)));
		}
	}

	public boolean checkProperty(String data) {
		if(isRegexp()){
			return regexpMatch(data);
		}else{
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

	public String getExpectedResult() {
		final String result = getName();
		if(isRegexp()) {
			return result + " match " + getValue().getCalculated();
		}else {
			return result + " == " + getValue().getCalculated();
		}
	}
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public boolean isRegexp(){
		return regexp;
	}

	public void setRegexp(boolean value){
		this.regexp = value;
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
