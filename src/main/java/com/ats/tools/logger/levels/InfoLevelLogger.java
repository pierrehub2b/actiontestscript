package com.ats.tools.logger.levels;

import java.io.PrintStream;

public class InfoLevelLogger extends ErrorLevelLogger {

	public InfoLevelLogger(PrintStream out, String level) {
		super(out, level);
	}
	
	@Override
	public void info(String message) {
		print("INFO", new StringBuilder(message));
	}
}