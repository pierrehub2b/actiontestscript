package com.ats.script.actions;

import com.ats.script.Script;

public class ActionExecute extends Action {

	public static final String NO_FAIL_LABEL = "nofail";
	
	protected boolean stop = true;
	
	public ActionExecute() {}
	
	public ActionExecute(Script script, boolean stop) {
		super(script);
		setStop(stop);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + stop + ", ";
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}