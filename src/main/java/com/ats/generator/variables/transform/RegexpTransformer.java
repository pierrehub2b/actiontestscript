package com.ats.generator.variables.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import com.ats.executor.ActionTestScript;

public class RegexpTransformer extends Transformer {

	private int group = 0;
	private String pattern = "(.*)";

	public RegexpTransformer() {} // Needed for serialization

	public RegexpTransformer(String pattern, int group) {
		setPattern(pattern);
		setGroup(group);
	}

	public RegexpTransformer(String ... data) {
		if(data.length > 1){
			setPattern(data[0].trim());
			setGroup(getInt(data[1].trim()));
		}
	}

	@Override
	public String getJavaCode() {
		return ActionTestScript.JAVA_REGEX_FUNCTION_NAME + "(\"" + StringEscapeUtils.escapeJava(pattern) + "\", " + group + ")";
	}

	@Override
	public String format(String data) {

		String result = "";
		if(data.length() > 0) {
			Pattern patternComp = Pattern.compile(pattern);
			Matcher m = patternComp.matcher(data);

			if(m.find()) {
				try {
					result = m.group(group);
				}catch(IndexOutOfBoundsException e) {}
			}
		}
		return result;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}