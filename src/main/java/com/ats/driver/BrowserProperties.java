package com.ats.driver;

public class BrowserProperties extends ApplicationProperties {

	private int wait;

	public BrowserProperties(String name, String path, int wait) {
		super(name, path);
		this.wait = wait;
	}

	public int getWait() {
		return wait;
	}
}