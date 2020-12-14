package com.ats.tools.report;

import java.util.Map;

import org.testng.TestRunner;

public class SuitesReportItem {

	public String name;
	public String description;
	public Map<String, String> parameters;
	public String[] tests;
	public String logLevel = "";
	
	public SuitesReportItem(TestRunner runner) {
		this.name = runner.getSuite().getName();
		this.parameters = runner.getTest().getAllParameters();
		this.tests = runner.getTest().getClasses().stream().map(c -> c.getName()).toArray(String[]::new);
	
		if(this.parameters != null) {
			setLogLevel(this.parameters.remove("ats.log.level"));
			setDescription(this.parameters.remove("ats.suite.description"));
		}
	}
	
	private void setLogLevel(String value) {
		if(value != null) {
			logLevel = value;
		}
	}
	
	private void setDescription(String value) {
		if(value != null) {
			description = value;
		}
	}
}