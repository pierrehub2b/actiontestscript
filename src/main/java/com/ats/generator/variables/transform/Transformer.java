package com.ats.generator.variables.transform;

import java.util.regex.Pattern;

public abstract class Transformer {

	public static final String REGEXP = "regexp";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String NUMERIC = "numeric";

	public static final Pattern TRANSFORM_PATTERN = Pattern.compile("(" + REGEXP + "|" + DATE + "|" + TIME + "|" + NUMERIC + ") ?\\[(.*)\\]");

	protected int getInt(String value){
		try{
			return Integer.parseInt(value);
		}catch (NumberFormatException e){
			return 0;
		}
	}

	public static Transformer createTransformer(String type, String ... data) {
		switch(type) {
			case REGEXP:
				return new RegexpTransformer(data);
			case DATE:
				return new DateTransformer(data);
			case TIME:
				return new TimeTransformer(data);
			case NUMERIC:
				return new NumericTransformer(data);
			default:
				return null;
		}
	}
	
	public String getJavaCode(){
		return "";
	}
	
	public String format(String data) {
		return "";
	}
}