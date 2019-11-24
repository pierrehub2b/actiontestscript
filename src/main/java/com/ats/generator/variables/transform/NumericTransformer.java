/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.ats.generator.variables.transform;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.commons.lang3.StringUtils;
import org.mariuszgromada.math.mxparser.Expression;

import com.ats.executor.ActionTestScript;

public class NumericTransformer extends Transformer {

	private DecimalFormat formatter = new DecimalFormat();

	private int decimal = -1;
	private boolean comma = false;

	public NumericTransformer() {} // Needed for serialization

	public NumericTransformer(int dp) {
		setDecimal(dp);
		setComma(false);
	}

	public NumericTransformer(int dp, boolean useComma) {
		setDecimal(dp);
		setComma(useComma);
	}

	public NumericTransformer(String data) {
		if(data.contains(",")) {
			data = data.replace(",", "");
			setComma(true);
		}
		setDecimal(getInt(data.replace("dp", "").trim()));
	}

	@Override
	public String getJavaCode() {
		return ActionTestScript.JAVA_NUMERIC_FUNCTION_NAME + "(" + decimal + ", " + comma + ")";
	}

	@Override
	public String format(String data) {

		formatter.setGroupingSize(0);
		data = StringUtils.replace(data, " ", "");
	
		if(decimal > -1) {
			data = "round(" + data + "," + decimal + ")";
			formatter.setMinimumFractionDigits(decimal);
		}else {
			formatter.setMaximumFractionDigits( 12 );
		}
		return formatter.format(new Expression(data).calculate());
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

	public boolean getComma() {
		return comma;
	}

	public void setComma(boolean value) {
		
		this.comma = value;
		
		final DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
		if(value) {
			decimalSymbols.setDecimalSeparator(',');
		}else {
			decimalSymbols.setDecimalSeparator('.');
		}
		this.formatter.setDecimalFormatSymbols(decimalSymbols);
	}
}