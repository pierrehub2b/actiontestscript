package com.ats.generator.objects;

public class MouseDirectionData {

	private Cartesian type;
	private int value = 0;

	public MouseDirectionData() {}

	public MouseDirectionData(String name, int value) {
		this.setName(name);
		this.setValue(value);
	}
	
	public MouseDirectionData(String name, String value) {
		this.setName(name);
		try{
			this.setValue(Integer.parseInt(value));
		}catch (NumberFormatException e){}
	}
	
	public MouseDirectionData(Cartesian type, int value) {
		this.type = type;
		this.setValue(value);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode() {
		
		if(value == 0 && (Cartesian.MIDDLE.equals(type) || Cartesian.CENTER.equals(type))){
			return "";
		}
		return type.getJavacode() + ", " + value;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public String getName() {
		return type.toString();
	}

	public void setName(String value) {
		
		this.type = Cartesian.valueOf(value.toUpperCase());
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}