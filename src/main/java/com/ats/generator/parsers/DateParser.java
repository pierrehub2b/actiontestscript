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

package com.ats.generator.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateParser {

	private static final Pattern DATE_PATTERN = Pattern.compile("&date(?:\\((\\d{1,2})\\/(\\d{1,2})\\/(\\d{1,4})\\))?(?:(\\+|\\-)(\\d+)?(\\w))?");
	
	private static final String JAVA_DATE_START_FUNCTION = "LocalDate.now()";
	private static final String JAVA_DATE_END_FUNCTION = ".format(DateTimeFormatter.ofPattern(\"MM/dd/yyyy\"))";
	
	public static String getJavaCode(String data) {
		
		String code = JAVA_DATE_START_FUNCTION;
		if(data == null) return code + JAVA_DATE_END_FUNCTION;
		
		final Matcher dateMatcher = DATE_PATTERN.matcher(data);
		
		while (dateMatcher.find()) {
						
			final String fixedMonth = dateMatcher.group(1);
			final String fixedDay = dateMatcher.group(2);
			final String fixedYear = dateMatcher.group(3);
			
			if(fixedMonth != null && fixedDay != null && fixedYear != null){
				code = "LocalDate.parse(\"" + fixedMonth + "/" + fixedDay + "/" + fixedYear + "\")";
			}
						
			final boolean addValue = "+".equals(dateMatcher.group(4));
			String increment = dateMatcher.group(5);
			String incrementType = dateMatcher.group(6);
			
			if(increment != null && incrementType != null){
				
				if(addValue){
					code += ".plus";
				}else{
					code += ".minus";
				}
				
				if("y".equals(incrementType.toLowerCase())){
					code += "Years";
				}else if("m".equals(incrementType.toLowerCase())){
					code += "Months";
				}else{
					code += "Days";
				}
				code += "(" + increment + ")";
			}
		}
		return code + JAVA_DATE_END_FUNCTION;
	}
}