package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionWindowClose extends ActionWindow {

	public static final String SCRIPT_CLOSE_LABEL = SCRIPT_LABEL + "close";

	public ActionWindowClose() {}

	public ActionWindowClose(Script script, int num) {
		super(script, num);
	}

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + getNum() + ")";
	}

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		ts.closeWindow(status, getNum());
		status.updateDuration();
		ts.updateVisualImage();
	}
}