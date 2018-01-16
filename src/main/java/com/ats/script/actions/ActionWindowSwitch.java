package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionWindowSwitch extends ActionWindow {

	public static final String SCRIPT_SWITCH_LABEL = SCRIPT_LABEL + "switch";
	
	public ActionWindowSwitch() {}

	public ActionWindowSwitch(Script script, int num) {
		super(script, num);
	}
	
	@Override
	public String getJavaCode() {
		return super.getJavaCode() + getNum() + ")";
	}
	
	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		ts.switchWindow(status, getNum());
		status.updateDuration();
		ts.updateVisualImage();
	}
}