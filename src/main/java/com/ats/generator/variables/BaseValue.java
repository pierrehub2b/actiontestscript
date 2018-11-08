package com.ats.generator.variables;

import java.util.regex.Matcher;

public class BaseValue {

	protected String value;
	protected String codeValue;
	
	private String replace;
	protected String defaultValue;
	
	public BaseValue(Matcher m) {
		replace = m.group(0);
		value = m.group(1).trim();
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