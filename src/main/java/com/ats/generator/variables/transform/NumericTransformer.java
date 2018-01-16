package com.ats.generator.variables.transform;

import org.mariuszgromada.math.mxparser.Expression;

import com.ats.executor.ActionTestScript;

public class NumericTransformer extends Transformer {

	private int decimal = -1;
	
	public NumericTransformer() {} // Needed for serialization
	
	public NumericTransformer(int dp) {
		setDecimal(dp);
	}
	
	public NumericTransformer(String ... data) {
		if(data.length > 0){
			setDecimal(getInt(data[0].replace("dp", "").trim()));
		}
	}

	@Override
	public String getJavaCode() {
		return ActionTestScript.JAVA_NUMERIC_FUNCTION_NAME + "(" + decimal + ")";
	}
	
	@Override
	public String format(String data) {

		if(decimal > -1) {
			data = "round(" + data + "," + decimal + ")";
		}
		
		Expression e = new Expression(data);
		Double result = e.calculate();
		
		return result.toString();
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getDecimal() {
		return decimal;
	}

	public void setDecimal(int decimal) {
		this.decimal = decimal;
	}
}