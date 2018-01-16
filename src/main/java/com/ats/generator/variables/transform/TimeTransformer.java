package com.ats.generator.variables.transform;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;

public class TimeTransformer extends Transformer {

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final Pattern PATTERN_HOUR_TRANSFORM = Pattern.compile("(-?\\d+)([hms])");

	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	
	public TimeTransformer() {} // Needed for serialization

	public TimeTransformer(String ... data) {
		if(data.length > 0){
			Matcher dataMatcher;
			for(String item : data){
				dataMatcher = PATTERN_HOUR_TRANSFORM.matcher(item);
				if(dataMatcher.find()){
					if("h".equals(dataMatcher.group(2))){
						setHour(getInt(dataMatcher.group(1)));
					}else if("m".equals(dataMatcher.group(2))){
						setMinute(getInt(dataMatcher.group(1)));
					}else if("s".equals(dataMatcher.group(2))){
						setSecond(getInt(dataMatcher.group(1)));
					}
				}
			}
		}
	}
	
	public static String getNowValue() {
		LocalTime time = LocalTime.now();
		return time.format(TIME_FORMATTER);
	}

	@Override
	public String getJavaCode() {
		
		StringJoiner joiner = new StringJoiner(", ");
		if(hour != 0) {
			joiner.add("\"" + hour + "h\"");
		}
		
		if(minute != 0) {
			joiner.add("\"" + minute + "m\"");
		}
		
		if(second != 0) {
			joiner.add("\"" + second + "s\"");
		}
		
		return ActionTestScript.JAVA_TIME_FUNCTION_NAME + "(" + joiner.toString() + ")";
	}
	
	@Override
	public String format(String data) {
		
		try {
			LocalTime time = LocalTime.parse(data, TIME_FORMATTER);
			time = time.plusHours(hour).plusMinutes(minute).plusSeconds(second);
			return time.toString();
			
		} catch (DateTimeParseException e) {}
		
		return "";
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}
}