package com.ats.generator.variables;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.script.Script;
import com.ats.tools.Utils;

public class CalculatedValue{


	private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$param\\s*?\\((\\d+),?(\\s*?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENV_PATTERN = Pattern.compile("\\$env\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE);

	private static final Pattern TODAY_PATTERN = Pattern.compile("^\\$today$", Pattern.CASE_INSENSITIVE);
	private static final Pattern NOW_PATTERN = Pattern.compile("^\\$now$", Pattern.CASE_INSENSITIVE);
	private static final Pattern UUID_PATTERN = Pattern.compile("^\\$uuid$", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern KEY_REGEXP = Pattern.compile("\\$key\\s?\\((\\w+)\\-?([^\\)]*)?\\)");

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
	private String data;
	private String calculated;

	private String javaCode = "";
	private Object[] dataList;

	public CalculatedValue() {}

	public CalculatedValue(String value) {
		this.setData(value);
	}

	public CalculatedValue(ActionTestScript actionTestScript, Object[] data) {
		this.dataList = data;
	}

	public CalculatedValue(Script script, String dataValue) {

		dataValue = Utils.escapeAts(dataValue);
		
		this.setScript(script);
		this.setData(dataValue);

		this.javaCode = StringEscapeUtils.escapeJava(dataValue);

		if(dataValue.length() > 0){

			Matcher mv = Variable.SCRIPT_PATTERN.matcher(dataValue);
			while (mv.find()) {
				String variableName = mv.group(1);
				dataValue = dataValue.replace(mv.group(0), script.getVariableValue(variableName));
				javaCode = javaCode.replace(mv.group(0), "\", " + variableName + ", \"");
			}

			mv = PARAMETER_PATTERN.matcher(dataValue);
			while (mv.find()) {
				String replace = mv.group(0);
				String value = mv.group(1).trim();

				String defaultValue = mv.group(2).trim();

				int index = 0;
				try{
					index = Integer.parseInt(value);
				}catch(NumberFormatException e){}

				StringBuilder codeBuilder = new StringBuilder("(");
				codeBuilder.append(index);

				if(defaultValue.length() > 0) {
					codeBuilder.append(", \"");
					codeBuilder.append(defaultValue);
					codeBuilder.append("\"");
				}

				codeBuilder.append(")");
				javaCode = javaCode.replace(replace, "\", " + ActionTestScript.JAVA_PARAM_FUNCTION_NAME + codeBuilder.toString() + ", \"");

				String paramValue = script.getParameterValue(index, defaultValue);
				dataValue = dataValue.replace(replace, paramValue);
			}

			mv = ENV_PATTERN.matcher(dataValue);
			while (mv.find()) {

				String replace = mv.group(0);
				String[] envData = mv.group(1).split(",");

				String value = envData[0].trim();
				String defaultValue = "";
				
				if(envData.length > 1) {
					defaultValue = envData[1].trim();
				}

				StringBuilder codeBuilder = new StringBuilder("(\"");
				codeBuilder.append(value);
				codeBuilder.append("\"");

				if(defaultValue.length() > 0) {
					defaultValue = defaultValue.trim();
					codeBuilder.append(", \"");
					codeBuilder.append(defaultValue);
					codeBuilder.append("\"");
				}

				String envValue = script.getEnvironmentValue(value, defaultValue);
				dataValue = dataValue.replace(replace, envValue);

				codeBuilder.append(")");
				javaCode = javaCode.replace(replace, "\", " + ActionTestScript.JAVA_ENV_FUNCTION_NAME + codeBuilder.toString() + ", \"");

			}

			mv = TODAY_PATTERN.matcher(dataValue);
			while (mv.find()) {
				String replace = mv.group(0);
				dataValue = dataValue.replace(replace, DateTransformer.getTodayValue());
				javaCode = javaCode.replace(replace, "\", " + ActionTestScript.JAVA_TODAY_FUNCTION_NAME + "(), \"");
			}

			mv = NOW_PATTERN.matcher(dataValue);
			while (mv.find()) {
				String replace = mv.group(0);
				dataValue = dataValue.replace(replace, TimeTransformer.getNowValue());
				javaCode = javaCode.replace(replace, "\", " + ActionTestScript.JAVA_NOW_FUNCTION_NAME + "(), \"");
			}

			mv = UUID_PATTERN.matcher(dataValue);
			while (mv.find()) {
				String replace = mv.group(0);
				dataValue = dataValue.replace(replace, UUID.randomUUID().toString());
				javaCode = javaCode.replace(replace, "\", " + ActionTestScript.JAVA_UUID_FUNCTION_NAME + "(), \"");
			}

			mv = KEY_REGEXP.matcher(dataValue);
			while (mv.find()) {
				String replace = mv.group(0);
				String value = mv.group(1).trim().toUpperCase();

				String spareKey = mv.group(2);
				if(spareKey.length() > 0) {
					javaCode = javaCode.replace(replace, "\", " + "Keys.chord(Keys." + value + ", \"" + spareKey.toLowerCase() + "\"), \"");
				}else {
					javaCode = javaCode.replace(replace, "\", " + "Keys." + value + ", \"");
				}
			}

			this.setCalculated(dataValue);
		}
	}

	public void dispose() {
		script = null;
		dataList = null;
	}

	public String getJavaCode(){

		String value = javaCode;
		//String value = Utils.atsStringValue(javaCode);

		/*value = value.replaceAll(SPACE_PATTERN, " ");
		value = value.replaceAll(TAB_PATTERN, "\t");
		value = value.replaceAll(EQUAL_PATTERN, "=");
		value = value.replaceAll(COMMA_PATTERN, ",");
		value = value.replaceAll(BREAK_PATTERN, " ");*/

		value = "\"" + value + "\"";

		value = unnecessaryStartQuotes.matcher(value).replaceFirst("");
		value = unnecessaryEndQuotes.matcher(value).replaceFirst("");
		value = unnecessaryMiddleQuotes.matcher(value).replaceAll("");
		
		return ActionTestScript.JAVA_VALUE_FUNCTION_NAME + "(" + value + ")";
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

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

	public String getCalculated() {
		if(calculated == null){
			if(dataList != null) {
				StringBuilder builder = new StringBuilder("");
				for(Object obj : dataList) {
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
	
	public ArrayList<SendKeyData> getCalculatedText(){

		ArrayList<SendKeyData> chainKeys = new ArrayList<SendKeyData>();
		
		if(calculated != null){
			
			addTextChain(chainKeys, calculated);
			
		}else {	
			
			if(dataList != null) {
				for(Object obj : dataList) {
					if (obj instanceof Variable) {
						chainKeys.add(new SendKeyData(((Variable)obj).getCalculatedValue()));
					}else {
						chainKeys.add(new SendKeyData(obj.toString()));
					}
				}
			}else {
				chainKeys.add(new SendKeyData(data));
			}
		}
		
		return chainKeys;
	}
	
	private void addTextChain(ArrayList<SendKeyData> chain, String s){

		int start = 0;		

		Matcher match = KEY_REGEXP.matcher(s);
		while(match.find()) {

			int end = match.start();
			if(end > 0) {
				SendKeyData sendKey = new SendKeyData(s.substring(start, end));
				chain.add(sendKey);
			}

			start = match.end();
			chain.add(new SendKeyData(match.group(1), match.group(2)));
		}

		SendKeyData sendKey = null;
		if(start == 0) {
			sendKey = new SendKeyData(s);
		}else if(start != s.length()){
			sendKey = new SendKeyData(s.substring(start));
		}
		
		if(sendKey != null) {
			chain.add(sendKey);
		}
	}

	public void setCalculated(String value) {
		this.calculated = value;
	}
}