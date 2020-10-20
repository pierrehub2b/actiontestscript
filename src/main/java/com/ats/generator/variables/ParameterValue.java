package com.ats.generator.variables;

import java.util.regex.Matcher;

public class ParameterValue extends BaseValue {
		
	public ParameterValue(Matcher m) {
		super(m);
		
		try {
			Integer.parseInt(codeValue);
		}catch (NumberFormatException e) {
			codeValue = "\"" + codeValue + "\"";
		}
	}
	
	public String getValue() {
		return value;
	}
}