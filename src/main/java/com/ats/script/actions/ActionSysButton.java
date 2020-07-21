package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.ScriptLoader;

import java.util.ArrayList;

public class ActionSysButton extends Action {
	
	public static final String SCRIPT_LABEL = "sysbutton";
	
	public static final String SOUND_UP = "sound-up";
	public static final String SOUND_DOWN = "sound-down";
	public static final String POWER = "power";
	public static final String BACK = "back";
	public static final String HOME = "home";
	public static final String MENU = "menu";
	public static final String PREVIEW = "preview";
	
	private ArrayList<String> buttonTypes;
	
	public ActionSysButton(ScriptLoader script, ArrayList<String> buttonTypes) {
		super(script);
		this.buttonTypes = buttonTypes;
	}
	
	@Override
	public void execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);
		
		if (status.isPassed()) {
			if (ts.getCurrentChannel() != null) {
				ts.getCurrentChannel().buttonClick(buttonTypes);
			}
			status.endDuration();
		}
	}
	
	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode();
	}
}
