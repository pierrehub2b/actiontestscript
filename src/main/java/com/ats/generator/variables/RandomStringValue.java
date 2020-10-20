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

import java.util.Random;
import java.util.regex.Matcher;

import com.ats.tools.Utils;

public class RandomStringValue extends BaseValue {
	
	private static final String UPP_KEY = "upp";
	private static final String LOW_KEY = "low";
	private static final String NUM_KEY = "num";
	
	public RandomStringValue(Matcher m) {
		super(m);
	}

	public RandomStringValue(int len, String type) {
		super(len + "", type);
	}

	public int getValue() {
		return Utils.string2Int(value, 10);
	}
	
	public String exec() {
		
		final int len = Utils.string2Int(value, 10);
		String baseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		if(!UPP_KEY.equals(defaultValue)){
			if(NUM_KEY.equals(defaultValue)){
				baseChars = "0123456789";
			}else if(LOW_KEY.equals(defaultValue)){
				baseChars = baseChars.toLowerCase();
			}else if(defaultValue != null && defaultValue.length() > 0){
				baseChars = defaultValue;
			}else{
				baseChars += baseChars.toLowerCase();
			}
		}
		
		final StringBuilder result = new StringBuilder();
        final Random rnd = new Random();
        while (result.length() < len) { // length of the random string.
            int index = (int) (rnd.nextFloat() * baseChars.length());
            result.append(baseChars.charAt(index));
        }
        return result.toString();
	}
}