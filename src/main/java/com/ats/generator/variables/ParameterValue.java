package com.ats.generator.variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.tools.Utils;

public class ParameterValue extends BaseValue {
	
	public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$param\\s*?\\((\\d+),?(\\s*?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	
	public ParameterValue(Matcher m) {
		super(m);
	}
	
	public int getValue() {
		return Utils.string2Int(value);
	}
}