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

package com.ats.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Operators {

	public static final String REGEXP = "=~";
	public static final String DIFFERENT = "<>";
	public static final String LOWER_EQUAL = "<=";
	public static final String GREATER_EQUAL = ">=";
	
	public static final String EQUAL = "=";
	public static final String LOWER = "<";
	public static final String GREATER = ">";

	public static final Pattern REGEXP_PATTERN = Pattern.compile("(.*)" + REGEXP + "(.*)");
	public static final Pattern DIFFERENT_PATTERN = Pattern.compile("(.*)" + DIFFERENT + "(.*)");
	public static final Pattern LOWER_EQUAL_PATTERN = Pattern.compile("(.*)" + LOWER_EQUAL + "(.*)");
	public static final Pattern GREATER_EQUAL_PATTERN = Pattern.compile("(.*)" + GREATER_EQUAL + "(.*)");
	public static final Pattern EQUAL_PATTERN = Pattern.compile("(.*)" + EQUAL + "(.*)");
	public static final Pattern LOWER_PATTERN = Pattern.compile("(.*)" + LOWER + "(.*)");
	public static final Pattern GREATER_PATTERN = Pattern.compile("(.*)" + GREATER + "(.*)");
	
	private String type = EQUAL;
	
	public Operators() {}
	
	public Operators(String value) {
		setType(value);
	}

	public static String getJavaCode(String operator) {

		final String code = Operators.class.getSimpleName() + ".";

		switch (operator) {

		case LOWER:
			return code + "LOWER";
		case GREATER:
			return code + "GREATER";
		case DIFFERENT:
			return code + "DIFFERENT";
		case LOWER_EQUAL:
			return code + "LOWER_EQUAL";
		case GREATER_EQUAL:
			return code + "GREATER_EQUAL";
		case REGEXP:
			return code + "REGEXP";
		default:
			return code + "EQUAL";
		}
	}
	
	public static String[] getData(String data) {
		Matcher objectMatcher = REGEXP_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {REGEXP, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		objectMatcher = DIFFERENT_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {DIFFERENT, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		objectMatcher = LOWER_EQUAL_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {LOWER_EQUAL, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		objectMatcher = GREATER_EQUAL_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {GREATER_EQUAL, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		objectMatcher = EQUAL_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {EQUAL, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		objectMatcher = LOWER_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {LOWER, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		objectMatcher = GREATER_PATTERN.matcher(data);
		if(objectMatcher.find()) {
			return new String[] {GREATER, objectMatcher.group(1).trim(), objectMatcher.group(2).trim()};
		}
		
		return null;
	}
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}