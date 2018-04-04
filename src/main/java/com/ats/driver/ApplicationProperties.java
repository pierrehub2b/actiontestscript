package com.ats.driver;

public class ApplicationProperties {
	
	private String name;
	private String path;
	
	public ApplicationProperties(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
}