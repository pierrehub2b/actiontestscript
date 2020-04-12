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

import java.util.regex.Matcher;

import org.apache.commons.text.StringEscapeUtils;

public class BaseValue {

	protected String value;
	protected String codeValue;
	
	private String replace;
	protected String defaultValue;
	
	public BaseValue(Matcher m) {
		replace = m.group(0);
		value = StringEscapeUtils.escapeJava(m.group(1).trim());
		codeValue = value;
		defaultValue = m.group(2).trim();
	}
	
	public String getReplace() {
		return replace;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String getNoComma() {
		return replace.replace(",", "\n");
	}

	public String getCode() {
		StringBuilder codeBuilder = new StringBuilder("(");
		codeBuilder.append(codeValue);
						
		if(defaultValue.length() > 0) {
			codeBuilder.append(", \"");
			codeBuilder.append(defaultValue);
			codeBuilder.append("\"");
		}
		
		codeBuilder.append(")");
		return codeBuilder.toString();
	}
}