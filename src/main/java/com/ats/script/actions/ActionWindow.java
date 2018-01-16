package com.ats.script.actions;

import com.ats.script.Script;

public class ActionWindow extends Action {

	public static final String SCRIPT_LABEL = "window-";

	private int num;
	
	public ActionWindow() {}

	public ActionWindow(Script script, int num) {
		super(script);
		setNum(num);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
}