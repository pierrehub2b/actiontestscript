package com.ats.tools.logger.levels;

import java.io.PrintStream;

import com.ats.generator.variables.ConditionalValue;

public class InfoLevelLogger extends ErrorLevelLogger {

	public InfoLevelLogger(PrintStream out, String level) {
		super(out, level);
	}
	
	@Override
	public void info(String message) {
		print("INFO", message);
	}
	
	@Override
	public void conditionExec(String scriptName, ConditionalValue condition) {
		print("INFO", condition.getLogData(scriptName));
	}
}