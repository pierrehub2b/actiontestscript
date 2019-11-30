package com.ats.tools.logger.levels;

import java.io.PrintStream;

public class WarningLevelLogger extends InfoLevelLogger {

	public WarningLevelLogger(PrintStream out, String level) {
		super(out, level);
	}

	@Override
	public void warning(String message) {
		print("WARNING", message);
	}
}