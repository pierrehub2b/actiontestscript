package com.ats.tools.logger.levels;

import java.io.PrintStream;

import com.ats.script.actions.Action;
import com.google.gson.JsonObject;

public class FullLevelLogger extends WarningLevelLogger {

	public FullLevelLogger(PrintStream out, String level) {
		super(out, level);
	}

	@Override
	public void action(Action action, String testName, int line) {
		print("ACTION", action.getActionLogs(testName, line, new JsonObject()));
	}
}