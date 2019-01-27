package com.ats.tools.logger;

public interface IExecutionLogger {
	public void sendInfo(String message, String value);
	public void sendError(String message, String value);
	public void sendWarning(String message, String value);
}
