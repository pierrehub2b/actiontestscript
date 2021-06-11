package com.ats.generator.variables.transform;

import java.util.StringJoiner;

public class TableTransformer extends Transformer {
	
	private int[] rows = null;
	private int random = -1;

	public TableTransformer() {} // Needed for serialization

	public TableTransformer(String ... data) {

	}
	
	@Override
	public String getJavaCode() {
		
		final StringJoiner joiner = new StringJoiner(", ");
		
		if(random > -1) {

		}
		
		if(rows != null) {

		}

		return "(" + joiner.toString() + ")";
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public int getRandom() {
		return random;
	}

	public void setRandom(int random) {
		this.random = random;
	}
	
	public int[] getRows() {
		return rows;
	}

	public void setRow(int[] value) {
		this.rows = value;
	}
}