package com.ats.generator.variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomStringValue extends BaseValue {

	public static final Pattern RND_PATTERN = Pattern.compile("\\$rndstring\\s*?\\((\\d+),?(\\w{0,3}?[^\\)]*)?\\)", Pattern.CASE_INSENSITIVE);
	
	public static final String UPP_KEY = "upp";
	public static final String LOW_KEY = "low";
	
	public RandomStringValue(Matcher m) {
		super(m);
		if(!UPP_KEY.equals(defaultValue) && !LOW_KEY.equals(defaultValue)) {
			defaultValue = "";
		}
	}

	public int getValue() {
		try {
			return Integer.parseInt(value);
		}catch(NumberFormatException e) {}
		
		return 10;
	}
}