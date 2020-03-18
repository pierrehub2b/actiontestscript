package com.ats.executor;

import com.google.gson.JsonObject;

public class ScriptStatus {

	private long start;
	private int actions;
	private boolean passed;

	private String testName;
	private String suiteName;

	public ScriptStatus() {
		start = System.currentTimeMillis();
		actions = 0;
		passed = true;
	}

	public ScriptStatus(String script, String suite) {
		this();
		testName = script;
		suiteName = suite;
	}

	public void addAction() {
		actions++;
	}

	public void failed() {
		passed = false;
	}

	public String endLogs() {
		final JsonObject logs = new JsonObject();
		logs.addProperty("name", testName);
		logs.addProperty("suite", suiteName);
		logs.addProperty("duration", System.currentTimeMillis() - start);
		logs.addProperty("passed", passed);
		logs.addProperty("actions", actions);

		return logs.toString();
	}

	public boolean isSuiteExecution() {
		return testName != null && suiteName != null;
	}
}
