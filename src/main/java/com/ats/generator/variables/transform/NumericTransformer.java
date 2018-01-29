package com.ats.generator.variables.transform;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.mariuszgromada.math.mxparser.Expression;

import com.ats.executor.ActionTestScript;

public class NumericTransformer extends Transformer {

	private static DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
	private DecimalFormat formatter = new DecimalFormat();
	
	private int decimal = -1;
	private String pattern;

	public NumericTransformer() {} // Needed for serialization

	public NumericTransformer(int dp) {
		setDecimal(dp);
	}

	public NumericTransformer(int dp, String ... data) {
		setDecimal(dp);
		if(data.length > 0){
			setPattern(data[0]);
		}
	}

	public NumericTransformer(String ... data) {
		if(data.length > 0){
			setDecimal(getInt(data[0].replace("dp", "").trim()));
			if(data.length > 1){
				setPattern(data[1]);
			}
		}
	}

	@Override
	public String getJavaCode() {
		return ActionTestScript.JAVA_NUMERIC_FUNCTION_NAME + "(" + decimal + ")";
	}

	@Override
	public String format(String data) {

	    decimalSymbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(decimalSymbols);
				
		if(decimal > -1) {
			data = "round(" + data + "," + decimal + ")";
			formatter.setMinimumFractionDigits(decimal);
		}else {
			formatter.setMaximumFractionDigits( 12 );
		}

		Expression e = new Expression(data);
		Double result = e.calculate();

		return formatter.format(result);
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

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}