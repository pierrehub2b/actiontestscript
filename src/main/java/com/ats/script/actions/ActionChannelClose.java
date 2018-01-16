package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionChannelClose extends ActionChannel {

	public static final String SCRIPT_CLOSE_LABEL = SCRIPT_LABEL + "close";
	
	public ActionChannelClose() {}

	public ActionChannelClose(Script script, String name) {
		super(script, name);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		ts.updateVisualValue(getName());
		
		status.resetDuration();
		ts.closeChannel(status, getName());
		status.updateDuration();
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + "\"" + getName() + "\")";
	}
}