package com.ats.generator.variables;

import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.transform.Transformer;

public class Variable implements Comparable<Variable>{

	public static final String SCRIPT_LABEL = "var";
	public static final Pattern SCRIPT_PATTERN = Pattern.compile("\\$var\\s*?\\((\\w+)\\)", Pattern.CASE_INSENSITIVE);

	private boolean calculation = true;
	private String name = "";

	private CalculatedValue value;
	private Transformer transformation;
	private String data = null;

	public Variable() {}

	public Variable(String name, CalculatedValue value) {
		this.setName(formatVariableName(name));
		this.setValue(value);
	}

	public Variable(String name, CalculatedValue value, Transformer transformer) {
		this.setName(formatVariableName(name));
		this.setValue(value);
		this.setTransformation(transformer);
	}

	public static final String formatVariableName(String value){
		return value.replaceAll("[^A-Za-z0-9_]", "");
	}

	public String getCalculatedValue() {

		String result = data; 
		if(result == null) {
			result = value.getCalculated();
		}

		if(transformation == null) {
			return result;
		}else {
			return transformation.format(result);
		}
	}	

	public void updateValue(String attributeValue) {
		data = attributeValue;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode(){

		StringBuilder codeBuilder = new StringBuilder(this.getClass().getSimpleName());
		codeBuilder.append(" ");
		codeBuilder.append(getName());
		codeBuilder.append(" = ");
		codeBuilder.append(ActionTestScript.JAVA_VAR_FUNCTION_NAME);
		codeBuilder.append("(\"");
		codeBuilder.append(getName());
		codeBuilder.append("\"");

		if(isCalculation()) {
			codeBuilder.append(", ");
			codeBuilder.append(value.getJavaCode());
		}

		if(transformation != null) {
			codeBuilder.append(", ");
			codeBuilder.append(transformation.getJavaCode());
		}
		codeBuilder.append(")");

		return codeBuilder.toString();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;
	}

	public Transformer getTransformation() {
		return transformation;
	}

	public void setTransformation(Transformer value) {
		this.transformation = value;
	}

	public boolean isCalculation() {
		return calculation;
	}

	public void setCalculation(boolean value) {
		this.calculation = value;
	}

	//----------------------------------------------------------------------------------------------------------------------------

	@Override
	public int compareTo(Variable variable) {
		return Boolean.valueOf(isCalculation()).compareTo(Boolean.valueOf(variable.isCalculation()));
	}

}