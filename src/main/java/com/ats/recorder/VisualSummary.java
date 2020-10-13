package com.ats.recorder;

public class VisualSummary {
	
	private boolean passed;
	private String data = "";
	private String suiteName = "";
	private String testName = "";
	private int actions = 0;
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public boolean isPassed() {
		return passed;
	}
	
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public int getActions() {
		return actions;
	}

	public void setActions(int actions) {
		this.actions = actions;
	}

}