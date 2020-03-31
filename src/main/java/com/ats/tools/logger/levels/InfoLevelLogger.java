package com.ats.tools.logger.levels;

import java.io.PrintStream;

public class InfoLevelLogger extends ErrorLevelLogger {

	private final static String LABEL = "INFO";
	
	public InfoLevelLogger(PrintStream out, String level) {
		super(out, level);
	}
	
	@Override
	public void info(String message) {
		print(LABEL, new StringBuilder(message));
	}
}