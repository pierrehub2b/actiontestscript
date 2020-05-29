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
import java.util.regex.Pattern;

import com.ats.tools.Utils;

public class RandomStringValue extends BaseValue {

	public static final Pattern RND_PATTERN = Pattern.compile("\\$rnd(?:string)?\\s*?\\((\\d+),?(\\w{0,3}?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	
	public static final String UPP_KEY = "upp";
	public static final String LOW_KEY = "low";
	public static final String NUM_KEY = "num";
	
	public RandomStringValue(Matcher m) {
		super(m);
		if(!UPP_KEY.equals(defaultValue) && !LOW_KEY.equals(defaultValue) && !NUM_KEY.equals(defaultValue)) {
			defaultValue = "";
		}
	}

	public int getValue() {
		return Utils.string2Int(value, 10);
	}
}