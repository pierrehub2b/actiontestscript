package com.ats.tools.logger.levels;

import com.ats.script.actions.Action;
import com.google.gson.JsonObject;

import java.io.PrintStream;

public class FullLevelLogger extends WarningLevelLogger {

	private final static String LABEL = "ACTION";
	
	public FullLevelLogger(PrintStream out, String level) {
		super(out, level);
	}

	@Override
	public void action(Action action, String testName, int line) {
		print(LABEL, action.getActionLogs(testName, line, new JsonObject()));
	}
}