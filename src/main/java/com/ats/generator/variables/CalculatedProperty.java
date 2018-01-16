package com.ats.generator.variables;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.tools.Operators;
import com.ats.tools.Utils;

public class CalculatedProperty implements Comparable<CalculatedProperty>{

	private CalculatedValue value;
	private String name = "id";
	private boolean regexp = false;

	private Pattern regexpPattern;

	public CalculatedProperty() {}

	public CalculatedProperty(Script script, String data) {

		Matcher objectMatcher = Operators.REGEXP_PATTERN.matcher(data);
		boolean dataFound = objectMatcher.find();

		if (dataFound) {
			setRegexp(true);
		}else {
			objectMatcher = Operators.EQUAL_PATTERN.matcher(data);
			dataFound = objectMatcher.find();
		}

		if(dataFound && objectMatcher.groupCount() >= 2){

			setName(objectMatcher.group(1).trim());
			setValue(new CalculatedValue(script, Utils.atsStringValue(objectMatcher.group(2).trim())));

		}else{
			setValue(new CalculatedValue(script, "true"));
		}
	}

	public CalculatedProperty(String name, String data) {
		setName(name);
		setValue(new CalculatedValue(data));
	}
	
	public CalculatedProperty(boolean isRegexp, String name, CalculatedValue value) {
		setRegexp(isRegexp);
		setName(name);
		setValue(value);
	}
	
	public void dispose() {
		value.dispose();
		value = null;
	}

	public String getJavaCode(){
		return ActionTestScript.JAVA_PROPERTY_FUNCTION_NAME + "(" + isRegexp() + ", \"" + name + "\", " + value.getJavaCode() + ")";
	}

	public Predicate<Map<String, Object>> getPredicate(Predicate<Map<String, Object>> predicate){
		if(isRegexp()){
			predicate = predicate.and(
					p -> matchRegexp(
							p.get(name).toString().trim()));
		}else{
			predicate = predicate.and(p -> matchText(p.get(name)));
		}
		return predicate;
	}

	public boolean checkProperty(Object data) {
		if(isRegexp()){
			return matchRegexp((String)data);
		}else{
			return matchText(data);
		}
	}

	public boolean matchText(Object obj){
		String textValue = "";
		if(obj != null && obj.toString() != null){
			textValue = obj.toString().trim();
		}
		return textValue.equals(value.getCalculated());
	}	

	public boolean matchRegexp(String s){
		Matcher m = getRegexpPattern().matcher(s);
		return m.matches();
	}

	private Pattern getRegexpPattern(){
		if(regexpPattern == null){
			regexpPattern = Pattern.compile(value.getCalculated());
		}
		return regexpPattern;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public boolean isRegexp(){
		return regexp;
	}

	public void setRegexp(boolean value){
		this.regexp = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;
	}

	//----------------------------------------------------------------------------------------------------------------------------

	public String toString() {
		return name;
	}

	@Override
	public int compareTo(CalculatedProperty prop) {
		return toString().compareTo(prop.toString());
	}
}
