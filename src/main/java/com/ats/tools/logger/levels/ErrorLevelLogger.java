package com.ats.tools.logger.levels;

import java.io.PrintStream;

public class ErrorLevelLogger extends AtsLogger {

	private final static String LABEL = "ERROR";
	
	public ErrorLevelLogger(PrintStream out, String level) {
		super(out, level);
	}
	
	@Override
	public void error(String message) {
		print(LABEL,  new StringBuilder(message));
	}
}