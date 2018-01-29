package com.ats.driver;

public class BrowserProperties {

	private String name;
	private String path;
	private int wait;

	public BrowserProperties(String name, String path, int wait) {
		this.name = name;
		this.path = path;
		this.wait = wait;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public int getWait() {
		return wait;
	}
}