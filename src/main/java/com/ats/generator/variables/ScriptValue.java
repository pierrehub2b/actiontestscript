package com.ats.generator.variables;

import com.ats.tools.Utils;

public class ScriptValue {
	
	private String value = "";
	
	public ScriptValue(String value) {
		this.value = value;
	}
	
	public int toInt() {
		return Utils.string2Int(value);
	}
	
	public double toDouble() {
		return Utils.string2Double(value);
	}
	
	public boolean toBoolean() {
		return Boolean.valueOf(value);
	}
	
	@Override
	public String toString() {
		return value;
	}
}