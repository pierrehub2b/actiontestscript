package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionPropertySet extends Action {
	
	public static final String SCRIPT_LABEL = "system-set";
	
	private String name;
	private String value;
	
	public ActionPropertySet() { }
	
	public ActionPropertySet(Script script, String name, String value) {
		super(script);
		setName(name);
		setValue(value);
	}
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder builder = super.getJavaCode();
		builder.append("\"" + name + "\"").append(", ").append("\"" + value + "\"").append(")");
		return builder;
	}
	
	@Override
	public void execute(ActionTestScript ts, String testName, int line) {
		super.execute(ts, testName, line);
		ts.getCurrentChannel().setSysProperty(getName(), getValue());
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
