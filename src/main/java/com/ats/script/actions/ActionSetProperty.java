package com.ats.script.actions;

import com.ats.executor.ActionTestScript;

public class ActionSetProperty extends ActionExecuteElement {
	
	public static final String SCRIPT_LABEL = "system-set";
	
	private String name;
	private String value;
	
	/* public ActionSetProperty(Script script, String name, String value) {
		super(script);
	} */
	
	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode();
	}
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
