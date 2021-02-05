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

import com.ats.crypto.Password;
import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.script.Project;
import com.ats.script.Script;
import com.ats.tools.Utils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatedValue{
	
	private static final Pattern TODAY_PATTERN = Pattern.compile("\\$today", Pattern.CASE_INSENSITIVE);
	private static final Pattern NOW_PATTERN = Pattern.compile("\\$now", Pattern.CASE_INSENSITIVE);
	private static final Pattern UUID_PATTERN = Pattern.compile("\\$uuid", Pattern.CASE_INSENSITIVE);
	private static final Pattern PGAV_PATTERN = Pattern.compile("\\$pgav", Pattern.CASE_INSENSITIVE);
	private static final Pattern ITERATION_PATTERN = Pattern.compile("\\$iteration", Pattern.CASE_INSENSITIVE);

	public static final Pattern KEY_REGEXP = Pattern.compile("\\$key\\s?\\((\\w+)\\-?([^\\)]*)?\\)");

	public static final Pattern ASSET_PATTERN = Pattern.compile("\\$asset\\s*?\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE);
	public static final Pattern IMAGE_PATTERN = Pattern.compile("\\$image\\s*?\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE);

	private static final Pattern PASSWORD_DATA = Pattern.compile("\\$pass\\s*?\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern SYS_PATTERN = Pattern.compile("\\$sys\\s*?\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE);
	public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$param\\s*?\\((\\w+),?(\\s*?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	public static final Pattern ENV_PATTERN = Pattern.compile("\\$env\\s*?\\(([\\w.]+),?(\\s*?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern RND_PATTERN = Pattern.compile("\\$rnd(?:string)?\\s*?\\((\\d+),?(\\w{0,3}?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$var\\s*?\\(([^\\)\\.]*)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern GLOBAL_VARIABLE_PATTERN = Pattern.compile("\\$var\\s*?\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE);
	
	//-----------------------------------------------------------------------------------------------------
	// variable and parameter management
	//-----------------------------------------------------------------------------------------------------

	private static final Pattern unnecessaryStartQuotes = Pattern.compile("^\"\", ?");
	private static final Pattern unnecessaryMiddleQuotes = Pattern.compile(" \"\",");
	private static final Pattern unnecessaryEndQuotes = Pattern.compile(", \"\"$");

	//-----------------------------------------------------------------------------------------------------
	// instance data
	//-----------------------------------------------------------------------------------------------------

	private Script script;
	private String data = "";
	private String calculated;

	private String rawJavaCode = "";
	private Object[] dataList;

	private boolean crypted = false;

	public CalculatedValue() {}

	public CalculatedValue(String value) {
		setData(value);
	}

	public CalculatedValue(int value) {
		this(value + "");
	}

	public CalculatedValue(ActionTestScript actionTestScript, Object[] data) {
		dataList = data;
	}

	public CalculatedValue(Script script) {
		setScript(script);
		setCalculated("");
	}
	
	public CalculatedValue(Script script, String dataValue) {
		setScript(script);
		if(dataValue.length() > 0){
			dataValue = Utils.unescapeAts(dataValue);
			setData(dataValue);
			setCalculated(initCalculated(dataValue));
		}
	}

	private String initCalculated(String dataValue) {

		rawJavaCode = StringEscapeUtils.escapeJava(dataValue);
		String fullValue = dataValue;

		Matcher mv = VARIABLE_PATTERN.matcher(dataValue);
		while (mv.find()) {
			
			final String replace = mv.group(0);
			final String variableName = mv.group(1);
			
			dataValue = dataValue.replace(replace, script.getVariableValue(variableName));
			rawJavaCode = rawJavaCode.replace(replace, "\", " + variableName + ", \"");
		}
		
		mv = GLOBAL_VARIABLE_PATTERN.matcher(dataValue);
		while (mv.find()) {
			
			final String replace = mv.group(0);
			final String variableName = mv.group(1);
			
			dataValue = script.getGlobalVariableValue(variableName);
			rawJavaCode = rawJavaCode.replace(replace, "\", " + ActionTestScript.JAVA_GLOBAL_VAR_FUNCTION_NAME + "(\"" + variableName + "\"), \"");
		}

		mv = SYS_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final String replace = mv.group(0);
			final String value = StringEscapeUtils.escapeJava(mv.group(1).trim());
			dataValue = dataValue.replace(replace, script.getSystemValue(value));
			rawJavaCode = rawJavaCode.replace(replace, "\"," + ActionTestScript.JAVA_SYSTEM_FUNCTION_NAME + "(\"" + value + "\"), \"");
		}

		mv = PARAMETER_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final ParameterValue sp = new ParameterValue(mv);
			dataValue = dataValue.replace(sp.getReplace(), script.getParameterValue(sp.getValue(), sp.getDefaultValue()));
			rawJavaCode = fullValue.replace(sp.getReplace(), "\", " + ActionTestScript.JAVA_PARAM_FUNCTION_NAME + sp.getCode() + ", \"");			
		}

		mv = ENV_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final EnvironmentValue sp = new EnvironmentValue(mv);
			dataValue = dataValue.replace(sp.getReplace(), script.getEnvironmentValue(sp.getValue(), sp.getDefaultValue()));
			rawJavaCode = fullValue.replace(sp.getReplace(), "\", " + ActionTestScript.JAVA_ENV_FUNCTION_NAME + sp.getCode() + ", \"");
		}

		mv = TODAY_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final String replace = mv.group(0);
			dataValue = dataValue.replace(replace, DateTransformer.getTodayValue());
			rawJavaCode = rawJavaCode.replace(replace, "\", " + ActionTestScript.JAVA_TODAY_FUNCTION_NAME + "(), \"");
		}

		mv = NOW_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final String replace = mv.group(0);
			dataValue = dataValue.replace(replace, TimeTransformer.getNowValue());
			rawJavaCode = rawJavaCode.replace(replace, "\", " + ActionTestScript.JAVA_NOW_FUNCTION_NAME + "(), \"");
		}

		mv = UUID_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final String replace = mv.group(0);
			dataValue = dataValue.replace(replace, UUID.randomUUID().toString());
			rawJavaCode = rawJavaCode.replace(replace, "\", " + ActionTestScript.JAVA_UUID_FUNCTION_NAME + "(), \"");
		}

		mv = RND_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final RandomStringValue rds = new RandomStringValue(mv);
			dataValue = dataValue.replace(rds.getReplace(), rds.exec());
			rawJavaCode = rawJavaCode.replace(rds.getReplace(), "\", " + ActionTestScript.JAVA_RNDSTRING_FUNCTION_NAME + rds.getCode() + ", \"");
		}

		mv = PGAV_PATTERN.matcher(dataValue);
		while (mv.find()) {
			rawJavaCode = rawJavaCode.replace(mv.group(0), "\", " + ActionTestScript.JAVA_GAV_FUNCTION_NAME + "(), \"");
		}

		mv = ITERATION_PATTERN.matcher(dataValue);
		while (mv.find()) {
			rawJavaCode = rawJavaCode.replace(mv.group(0), "\", " + ActionTestScript.JAVA_ITERATION_FUNCTION_NAME + "(), \"");
		}

		mv = IMAGE_PATTERN.matcher(dataValue);
		while (mv.find()) {
			rawJavaCode = rawJavaCode.replace(mv.group(0), Project.getAssetsImageJavaCode(mv.group(1)));
		}

		mv = ASSET_PATTERN.matcher(dataValue);
		while (mv.find()) {
			rawJavaCode = rawJavaCode.replace(mv.group(0), Project.getAssetsJavaCode(mv.group(1)));
		}

		mv = PASSWORD_DATA.matcher(dataValue);
		while (mv.find()) {
			crypted = true;
			rawJavaCode = rawJavaCode.replace(mv.group(0), "\", new " + Password.class.getCanonicalName() + "(this, \"" + mv.group(1) + "\"), \"");
		}

		return dataValue;
	}

	public boolean isCrypted() {
		return crypted;
	}

	public void dispose() {
		script = null;
		dataList = null;
	}

	//-----------------------------------------------------------------------------------------------------
	// java code
	//-----------------------------------------------------------------------------------------------------

	public String getJavaCode(){

		String value = "\"" + rawJavaCode + "\"";

		value = unnecessaryStartQuotes.matcher(value).replaceFirst("");
		value = unnecessaryEndQuotes.matcher(value).replaceFirst("");
		value = unnecessaryMiddleQuotes.matcher(value).replaceAll("");

		return ActionTestScript.JAVA_VALUE_FUNCTION_NAME + "(" + value + ")";
	}

	//-----------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------

	private String uncrypt(ActionTestScript script, String value) {
		final Matcher mv = PASSWORD_DATA.matcher(value);
		while (mv.find()) {
			crypted = true;
			value = value.replace(mv.group(0), script.getPassword(mv.group(1)));
		}
		return value;
	}	

	private void addTextChain(ActionTestScript script, ArrayList<SendKeyData> list, String value) {
		list.add(new SendKeyData(uncrypt(script, value)));
	}

	private void addTextChain(ArrayList<SendKeyData> list, String value1, String value2) {
		list.add(new SendKeyData(value1, value2));
	}

	public ArrayList<SendKeyData> getCalculatedText(ActionTestScript script){

		final ArrayList<SendKeyData> chainKeys = new ArrayList<SendKeyData>();
		
		final String calc = getCalculated();
		
		int start = 0;		

		final Matcher match = KEY_REGEXP.matcher(calc);
		while(match.find()) {

			int end = match.start();
			if(end > 0) {
				addTextChain(script, chainKeys, calc.substring(start, end));
			}

			start = match.end();
			addTextChain(chainKeys, match.group(1), match.group(2));
		}

		if(start == 0) {
			addTextChain(script, chainKeys, calc);
		}else if(start != calc.length()){
			addTextChain(script, chainKeys, calc.substring(start));
		}

		return chainKeys;
	}
	
	public String getDataListItem() {
		if(dataList != null && dataList.length > 0) {
			return dataList[0].toString();
		}
		return calculated;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getCalculated() {
		if(calculated == null){
			if(dataList != null) {
				final StringBuilder builder = new StringBuilder("");
				for(final Object obj : dataList) {
					if (obj instanceof Variable) {
						builder.append(((Variable) obj).getCalculatedValue());
					}else {
						builder.append(obj);
					}
				}
				return builder.toString();
			}else {
				return data;
			}
		}
		return calculated;
	}

	public void setCalculated(String value) {
		this.calculated = value;
	}

	public String getData() {
		return data;
	}

	public void setData(String value) {
		if(value == null){
			value = "";
		}
		this.data = value;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}
}