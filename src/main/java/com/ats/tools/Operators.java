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

import java.util.regex.Pattern;

public final class Operators {

	public static final String EQUAL = "=";
	public static final String LOWER = "<";
	public static final String GREATER = ">";

	public static final String DIFFERENT = "<>";
	public static final String LOWER_EQUALS = "<=";
	public static final String GREATER_EQUALS = ">=";

	private static final String REGEXP = "=~";

	public static final Pattern REGEXP_PATTERN = Pattern.compile("(.*)" + REGEXP + "(.*)");
	public static final Pattern EQUAL_PATTERN = Pattern.compile("(.*)" + EQUAL + "(.*)");

	public static String getJavaCode(String operator) {

		String code = Operators.class.getSimpleName() + ".";

		switch (operator) {

		case LOWER:
			code += "LOWER";
			break;
		case GREATER:
			code += "GREATER";
			break;
		case DIFFERENT:
			code += "DIFFERENT";
			break;
		case LOWER_EQUALS:
			code += "LOWER_EQUALS";
			break;
		case GREATER_EQUALS:
			code += "GREATER_EQUALS";
			break;
		default:
			code += "EQUAL";
		}

		return code;
	}

}