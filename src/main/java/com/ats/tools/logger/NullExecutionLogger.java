package com.ats.tools.logger;

public class NullExecutionLogger implements IExecutionLogger {

	@Override
	public void sendInfo(String message, String value) {
	}

	@Override
	public void sendError(String message, String value) {
	}

	@Override
	public void sendWarning(String message, String value) {
	}
}
