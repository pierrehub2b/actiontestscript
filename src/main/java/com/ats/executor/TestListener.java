package com.ats.executor;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class TestListener extends TestListenerAdapter {

	@Override
	public void onTestSuccess(ITestResult tr) {
		final ScriptStatus status = ((ActionTestScript)tr.getInstance()).getStatus();
		if(!status.isPassed()) {
			tr.setStatus(ITestResult.FAILURE);
		}
		super.onTestSuccess(tr);
	}
	
}