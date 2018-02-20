package com.ats.generator.objects;

public class BoundData {
	
	private int value;
		
	public BoundData() {}
	
	public BoundData(int value) {
		setValue(value);
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
}