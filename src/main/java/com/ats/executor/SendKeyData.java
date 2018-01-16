package com.ats.executor;

import org.openqa.selenium.Keys;

public class SendKeyData {

	private static final String KEY_DOWN_SHIFT = "SHIFT";
	private static final String KEY_DOWN_ALT = "ALT";
	private static final String KEY_DOWN_CONTROL = "CONTROL";

	private CharSequence sequence = "";
	private boolean altDown = false;
	private boolean shiftDown = false;
	private boolean controlDown = false;

	public SendKeyData(String ... keyData) {
		for (String key : keyData) {
			if(KEY_DOWN_SHIFT.equals(key)) {
				this.shiftDown = true;
			}else if(KEY_DOWN_ALT.equals(key)) {
				this.altDown = true;
			}else if(KEY_DOWN_CONTROL.equals(key)) {
				this.controlDown = true;
			}else {
				try {
					sequence = Keys.valueOf(key);
				}catch(IllegalArgumentException e) {
					sequence = key.toLowerCase();
				}
			}
		}
	}

	public SendKeyData(String data) {
		this.sequence = data;
	}

	public void setSequence(CharSequence value) {
		this.sequence = value;
	}

	public CharSequence getSequence() {
		
		CharSequence result = sequence;
		
		if(shiftDown) {
			result = Keys.chord(Keys.SHIFT, result);
		}
		
		if(altDown) {
			result = Keys.chord(Keys.ALT, result);
		}
		
		if(controlDown) {
			result = Keys.chord(Keys.CONTROL, result);
		}
		
		return result;
	}

}