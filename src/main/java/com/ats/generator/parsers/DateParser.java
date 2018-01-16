package com.ats.generator.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateParser {

	private static final Pattern DATE_PATTERN = Pattern.compile("&date(?:\\((\\d{1,2})\\/(\\d{1,2})\\/(\\d{1,4})\\))?(?:(\\+|\\-)(\\d+)?(\\w))?");
	
	private static final String JAVA_DATE_START_FUNCTION = "LocalDate.now()";
	private static final String JAVA_DATE_END_FUNCTION = ".format(DateTimeFormatter.ofPattern(\"MM/dd/yyyy\"))";
	
	public static String getJavaCode(String data) {
		
		String code = JAVA_DATE_START_FUNCTION;
		
		Matcher dateMatcher = DATE_PATTERN.matcher(data);
		
		while (dateMatcher.find()) {
						
			String fixedMonth = dateMatcher.group(1);
			String fixedDay = dateMatcher.group(2);
			String fixedYear = dateMatcher.group(3);
			
			if(fixedMonth != null && fixedDay != null && fixedYear != null){
				code = "LocalDate.parse(\"" + fixedMonth + "/" + fixedDay + "/" + fixedYear + "\")";
			}
						
			boolean addValue = "+".equals(dateMatcher.group(4));
			String increment = dateMatcher.group(5);
			String incrementType = dateMatcher.group(6);
			
			if(increment != null && incrementType != null){
				
				if(addValue){
					code += ".plus";
				}else{
					code += ".minus";
				}
				
				if("y".equals(incrementType.toLowerCase())){
					code += "Years";
				}else if("m".equals(incrementType.toLowerCase())){
					code += "Months";
				}else{
					code += "Days";
				}
				code += "(" + increment + ")";
			}
		}
		return code + JAVA_DATE_END_FUNCTION;
	}
}