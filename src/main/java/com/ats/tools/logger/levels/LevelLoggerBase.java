package com.ats.tools.logger.levels;

import java.io.PrintStream;

import com.ats.script.actions.Action;
import com.ats.tools.logger.NullPrintStream;

public class LevelLoggerBase {

	protected PrintStream out;
	
	public LevelLoggerBase() {
		this.out = new NullPrintStream();
	}
	
	public LevelLoggerBase(PrintStream out, String level) {
		this.out = out;
		print("LOGGER", level);
	}
		
	public void log(String type, String message) {
		print(type, message);
	}
	
	protected void print(String type, String data) {
		out.println("[ATS-" + type + "] " + data);
	}
	
	public void warning(String message) {}
	public void info(String message) {}
	public void error(String message) {}
	
	public void action(Action action, String testName, int line) {}
}