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

import com.ats.tools.Utils;

import java.util.regex.Pattern;

public abstract class Transformer {

	public static final String REGEXP = "regexp";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String NUMERIC = "numeric";
	public static final String TABLE = "table";

	public static final Pattern TRANSFORM_PATTERN = Pattern.compile("(" + REGEXP + "|" + DATE + "|" + TIME + "|" + NUMERIC + "|" + TABLE + ") ?\\[(.*)\\]");

	protected int getInt(String value){
		return Utils.string2Int(value);
	}

	public static Transformer createTransformer(String type, String data) {
		switch(type) {
			case REGEXP:
				return new RegexpTransformer(data);
			case DATE:
				return new DateTransformer(data.split(","));
			case TIME:
				return new TimeTransformer(data.split(","));
			case NUMERIC:
				return new NumericTransformer(data);
			case TABLE:
				return new TableTransformer(data.split(","));
			default:
				return null;
		}
	}
	
	public String getJavaCode(){
		return "";
	}
	
	public String format(String data) {
		return "";
	}
}