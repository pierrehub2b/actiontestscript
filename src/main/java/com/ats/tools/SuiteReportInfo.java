package com.ats.tools;

import java.util.Map;

import org.testng.TestRunner;

public class SuiteReportInfo {

	public String name;
	public Map<String, String> parameters;
	public String[] tests;
	
	public SuiteReportInfo(TestRunner runner) {
		this.name = runner.getSuite().getName();
		this.parameters = runner.getTest().getAllParameters();
		this.tests = runner.getTest().getClasses().stream().map(c -> c.getName()).toArray(String[]::new);
	}
}