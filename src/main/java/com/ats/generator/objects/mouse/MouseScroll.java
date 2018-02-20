package com.ats.generator.objects.mouse;

import com.ats.generator.objects.MouseDirectionData;

public class MouseScroll extends Mouse {

	private int value;
	
	public MouseScroll(int value) {
		super();
		setValue(value);
	}
	
	public MouseScroll(int value, MouseDirectionData hpos, MouseDirectionData vpos) {
		super(hpos, vpos);
		setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
