package com.ats.generator.objects.mouse;

import org.openqa.selenium.Keys;

import com.ats.generator.objects.MouseDirectionData;

public class MouseKey extends Mouse {
	
	private Keys key;

	public MouseKey(String type, Keys key) {
		super(type);
		setKey(key);
	}

	public MouseKey(String type, Keys key, MouseDirectionData hpos, MouseDirectionData vpos) {
		super(type, hpos, vpos);
		setKey(key);
	}

	public Keys getKey() {
		return key;
	}

	public void setKey(Keys key) {
		this.key = key;
	}

}
