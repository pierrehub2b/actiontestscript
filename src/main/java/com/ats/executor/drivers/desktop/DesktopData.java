package com.ats.executor.drivers.desktop;

public class DesktopData {

	private String name;
	private String value;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value.substring(1);
	}

	public void setValue(String value) {
		this.value = value;
	}
}