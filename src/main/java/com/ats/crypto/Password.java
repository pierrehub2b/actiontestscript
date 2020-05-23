package com.ats.crypto;

import com.ats.executor.ActionTestScript;

public class Password {
	
	private ActionTestScript script;
	private String name;

	public Password(ActionTestScript script, String name) {
		this.script = script;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "$pass(" + name + ")";
	}
	
	public String getValue() {
		return script.getPassword(name);
	}
}