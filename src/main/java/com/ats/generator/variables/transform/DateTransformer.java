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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;

public class DateTransformer extends Transformer {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final Pattern PATTERN_DAY_TRANSFORM = Pattern.compile("(-?\\d+)([ymd])");
	
	private int year = 0;
	private int month = 0;
	private int day = 0;
		
	public DateTransformer() {} // Needed for serialization
	
	public DateTransformer(String ... data) {
		if(data.length > 0){
			Matcher dataMatcher;
			for(String item : data){
				dataMatcher = PATTERN_DAY_TRANSFORM.matcher(item);
				if(dataMatcher.find()){
					if("y".equals(dataMatcher.group(2))){
						setYear(getInt(dataMatcher.group(1)));
					}else if("m".equals(dataMatcher.group(2))){
						setMonth(getInt(dataMatcher.group(1)));
					}else if("d".equals(dataMatcher.group(2))){
						setDay(getInt(dataMatcher.group(1)));
					}
				}
			}
		}
	}
	
	public static String getTodayValue() {
		final LocalDate date = LocalDate.now();
		return date.format(DATE_FORMATTER);
	}

	@Override
	public String getJavaCode() {
		
		StringJoiner joiner = new StringJoiner(", ");
		if(year != 0) {
			joiner.add("\"" + year + "y\"");
		}
		
		if(month != 0) {
			joiner.add("\"" + month + "m\"");
		}
		
		if(day != 0) {
			joiner.add("\"" + day + "d\"");
		}
		
		return ActionTestScript.JAVA_DATE_FUNCTION_NAME + "(" + joiner.toString() + ")";
	}
	
	@Override
	public String format(String data) {
		try {
			LocalDate date = LocalDate.parse(data, DATE_FORMATTER);
			date = date.plusYears(year).plusMonths(month).plusDays(day);
			return date.toString();

		} catch (DateTimeParseException e) {}
		
		return "";
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}
}