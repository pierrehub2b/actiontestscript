package com.ats.tools;

import java.util.regex.Pattern;

public final class Operators {

	public static final String EQUAL = "=";
	public static final String LOWER = "<";
	public static final String GREATER = ">";

	public static final String DIFFERENT = "<>";
	public static final String LOWER_EQUALS = "<=";
	public static final String GREATER_EQUALS = ">=";

	private static final String REGEXP = "=~";

	//public static final Pattern SINGLE_PATTERN = Pattern.compile("(.*)(" + EQUAL + "|" + LOWER + "|" + GREATER + ")(.*)");
	//public static final Pattern DOUBLE_PATTERN = Pattern.compile("(.*)(" + REGEXP + "|" + DIFFERENT + "|" + LOWER_EQUALS + "|" + GREATER_EQUALS + ")(.*)");

	public static final Pattern REGEXP_PATTERN = Pattern.compile("(.*)" + REGEXP + "(.*)");
	public static final Pattern EQUAL_PATTERN = Pattern.compile("(.*)" + EQUAL + "(.*)");

	public static String getJavaCode(String operator) {

		String code = Operators.class.getSimpleName() + ".";

		switch (operator) {

		case LOWER:
			code += "LOWER";
			break;
		case GREATER:
			code += "GREATER";
			break;
		case DIFFERENT:
			code += "DIFFERENT";
			break;
		case LOWER_EQUALS:
			code += "LOWER_EQUALS";
			break;
		case GREATER_EQUALS:
			code += "GREATER_EQUALS";
			break;
		default:
			code += "EQUAL";
		}

		return code;
	}

}