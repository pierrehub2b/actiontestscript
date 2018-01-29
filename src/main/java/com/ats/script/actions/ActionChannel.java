package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionChannel extends Action{

	public static final String SCRIPT_LABEL = "channel-";

	private String name = "";

	public ActionChannel() {}

	public ActionChannel(Script script, String name) {
		super(script);
		setName(name);
	}

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}