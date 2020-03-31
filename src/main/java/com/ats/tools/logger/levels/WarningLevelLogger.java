package com.ats.tools.logger.levels;

import java.io.PrintStream;

public class WarningLevelLogger extends InfoLevelLogger {

	private final static String LABEL = "WARNING";
	
	public WarningLevelLogger(PrintStream out, String level) {
		super(out, level);
	}

	@Override
	public void warning(String message) {
		print(LABEL, new StringBuilder(message));
	}
}