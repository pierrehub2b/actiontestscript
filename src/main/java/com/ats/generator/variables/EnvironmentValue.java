package com.ats.generator.variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentValue extends BaseValue {

	public static final Pattern ENV_PATTERN = Pattern.compile("\\$env\\s*?\\((\\w+),?(\\s*?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	
	public EnvironmentValue(Matcher m) {
		super(m);
		codeValue = "\"" + codeValue + "\"";
	}

	public String getValue() {
		return value;
	}
}
