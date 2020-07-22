package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionSetProperty extends Action {
	
	public static final String SCRIPT_LABEL = "system-set";
	
	private String name;
	private String value;
	
	public ActionSetProperty() { }
	
	public ActionSetProperty(Script script, String name, String value) {
		super(script);
		setName(name);
		setValue(value);
	}
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder builder = super.getJavaCode();
		builder.append(", ").append(name).append(", ").append(value);
		return builder;
	}
	
	@Override
	public void execute(ActionTestScript ts, String testName, int line) {
		super.execute(ts, testName, line);
		ts.getCurrentChannel().setAttribute(getName(), getValue());
		
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
