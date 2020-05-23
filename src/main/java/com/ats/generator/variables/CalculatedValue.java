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

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.Keys;

import com.ats.crypto.Password;
import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.script.ProjectData;
import com.ats.script.Script;
import com.ats.tools.Utils;

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

		Matcher mv = Variable.SCRIPT_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final String replace = mv.group(0);
			final String variableName = mv.group(1);
			dataValue = dataValue.replace(replace, script.getVariableValue(variableName));
			rawJavaCode = rawJavaCode.replace(replace, "\", " + variableName + ", \"");
		}

		mv = ParameterValue.PARAMETER_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final ParameterValue sp = new ParameterValue(mv);
			dataValue = dataValue.replace(sp.getReplace(), script.getParameterValue(sp.getValue(), sp.getDefaultValue()));
			rawJavaCode = rawJavaCode.replace(sp.getReplace(), "\", " + ActionTestScript.JAVA_PARAM_FUNCTION_NAME + sp.getCode() + ", \"");
		}

		mv = EnvironmentValue.ENV_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final EnvironmentValue sp = new EnvironmentValue(mv);
			dataValue = dataValue.replace(sp.getReplace(), script.getEnvironmentValue(sp.getValue(), sp.getDefaultValue()));
			rawJavaCode = rawJavaCode.replace(sp.getReplace(), "\", " + ActionTestScript.JAVA_ENV_FUNCTION_NAME + sp.getCode() + ", \"");
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

		mv = RandomStringValue.RND_PATTERN.matcher(dataValue);
		while (mv.find()) {
			final RandomStringValue rds = new RandomStringValue(mv);
			dataValue = dataValue.replace(rds.getReplace(), script.getRandomStringValue(rds.getValue(), rds.getDefaultValue()));
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
			rawJavaCode = rawJavaCode.replace(mv.group(0), ProjectData.getAssetsImageJavaCode(mv.group(1)));
		}

		mv = ASSET_PATTERN.matcher(dataValue);
		while (mv.find()) {
			rawJavaCode = rawJavaCode.replace(mv.group(0), ProjectData.getAssetsJavaCode(mv.group(1)));
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

	public String getTextKeyJavaCode() {
		final Matcher mv = KEY_REGEXP.matcher(rawJavaCode);
		while (mv.find()) {

			final String replace = mv.group(0);
			final String value = mv.group(1).trim().toUpperCase();
			final String spareKey = mv.group(2);

			if(spareKey.length() > 0) {
				rawJavaCode = rawJavaCode.replace(replace, "\", " + "Keys.chord(Keys." + value + ", \"" + spareKey.toLowerCase() + "\"), \"");
			}else {
				rawJavaCode = rawJavaCode.replace(replace, "\", " + "Keys." + value + ", \"");
			}
		}
		return getJavaCode();
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

		if(calculated != null){
			addCalculatedTextChain(script, chainKeys, calculated);
		}else {	
			String strValue = "";
			if(dataList != null) {
				for(Object obj : dataList) {

					if (obj instanceof Variable) {
						strValue += ((Variable)obj).getCalculatedValue();
					}else if(obj instanceof Password) {
						crypted = true;
						strValue += ((Password)obj).getValue();
					}else {

						final byte[] b = obj.toString().getBytes();
						final boolean isKey = b.length > 0 && b[0] < 65;

						if(obj instanceof Keys || isKey) {
							if(strValue != "") {
								addTextChain(script, chainKeys, strValue);
								strValue = "";
							}
							addTextChain(script, chainKeys, obj.toString());
						} else {
							strValue += obj.toString();
						}
					}
				}

				if(strValue != "") {
					addTextChain(script, chainKeys, strValue);
					strValue = "";
				}
			}else {
				addTextChain(script, chainKeys, data);
			}
		}

		return chainKeys;
	}

	private void addCalculatedTextChain(ActionTestScript script, ArrayList<SendKeyData> chain, String s){

		int start = 0;		

		final Matcher match = KEY_REGEXP.matcher(s);
		while(match.find()) {

			int end = match.start();
			if(end > 0) {
				addTextChain(script, chain, s.substring(start, end));
			}

			start = match.end();
			addTextChain(chain, match.group(1), match.group(2));
		}

		if(start == 0) {
			addTextChain(script, chain, s);
		}else if(start != s.length()){
			addTextChain(script, chain, s.substring(start));
		}
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