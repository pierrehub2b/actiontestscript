package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionChannelSwitch extends ActionChannel {

	public static final String SCRIPT_SWITCH_LABEL = "switch";
	private static final String JAVA_FUNCTION_NAME_SWITCH = "switchChannel";
	
	public ActionChannelSwitch() {}

	public ActionChannelSwitch(Script script, String name) {
		super(script, name);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		ts.updateVisualValue(getName());
		
		status.resetDuration();
		ts.switchChannel(status, getName());
		status.updateDuration();
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + JAVA_FUNCTION_NAME_SWITCH + "(\"" + getName() + "\");";
	}
}