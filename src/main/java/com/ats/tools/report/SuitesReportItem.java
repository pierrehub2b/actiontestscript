package com.ats.tools.report;

import java.util.Map;

import org.testng.TestRunner;

public class SuitesReportItem {

	public String name;
	public Map<String, String> parameters;
	public String[] tests;
	public String logLevel = "";
	
	public SuitesReportItem(TestRunner runner) {
		this.name = runner.getSuite().getName();
		this.parameters = runner.getTest().getAllParameters();
		this.tests = runner.getTest().getClasses().stream().map(c -> c.getName()).toArray(String[]::new);
	
		final Map<String, String> params = runner.getTest().getAllParameters();
		if(params != null) {
			this.logLevel = params.getOrDefault("ats.log.level", "");
		}
	}
}