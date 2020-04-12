package com.ats.generator.variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterValue extends BaseValue {
	
	public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$param\\s*?\\((\\w+),?(\\s*?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	
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