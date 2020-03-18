package com.ats.tools.logger.levels;

import java.io.PrintStream;

public class ErrorLevelLogger extends LevelLoggerBase {

	public ErrorLevelLogger(PrintStream out, String level) {
		super(out, level);
	}
	
	@Override
	public void error(String message) {
		print("ERROR",  new StringBuilder(message));
	}
}