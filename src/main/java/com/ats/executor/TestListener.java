package com.ats.executor;

import org.testng.TestListenerAdapter;

public class TestListener extends TestListenerAdapter {
	/*@Override
    public void onTestFailure(ITestResult tr) {
        Throwable th = tr.getThrowable();
        if (th != null) {
            System.out.println(th.getMessage());
            tr.setThrowable(null);
        }
    }*/
}
