package com.ats.tools.logger.levels;

import java.io.PrintStream;

import com.ats.script.actions.Action;

public class FullLevelLogger extends WarningLevelLogger {

	public FullLevelLogger(PrintStream out, String level) {
		super(out, level);
	}

	@Override
	public void action(Action action, String testName, int line) {
		print("ACTION", action.getActionLogs(testName, line, new StringBuilder()).toString());
	}
}