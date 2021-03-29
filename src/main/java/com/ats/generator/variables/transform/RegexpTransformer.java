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

package com.ats.generator.variables.transform;

import com.ats.executor.ActionTestScript;
import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexpTransformer extends Transformer {

	private int group = 0;
	private String pattern = "(.*)";

	public RegexpTransformer() {} // Needed for serialization

	public RegexpTransformer(String pattern, int group) {
		setPattern(pattern);
		setGroup(group);
	}

	public RegexpTransformer(String data) {

		int lastComa = data.lastIndexOf(",");

		try {
			setGroup(getInt(data.substring(lastComa + 1).trim()));
		}catch (IndexOutOfBoundsException e) {}

		try {
			setPattern(data.substring(0, lastComa).trim());
		}catch (IndexOutOfBoundsException e) {}
	}

	@Override
	public String getJavaCode() {
		return ActionTestScript.JAVA_REGEX_FUNCTION_NAME + "(\"" + StringEscapeUtils.escapeJava(pattern) + "\", " + group + ")";
	}

	@Override
	public String format(String data) {
		if(data.length() > 0) {
			try {
				final Pattern patternComp = Pattern.compile(pattern);
				final Matcher m = patternComp.matcher(data);

				if(m.find()) {
					return m.group(group);
				}
			}catch(PatternSyntaxException e) {
				return "#REGEXP_ERROR# (Pattern syntax error) " + pattern;
			}catch(IndexOutOfBoundsException e) {
				return "#REGEXP_ERROR# (Group index out of bound) " + pattern;
			}
		}
		return "";
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}