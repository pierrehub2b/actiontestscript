package com.ats.generator.objects.mouse;

import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;

public class Mouse {

	public static final String OVER = "over";
	public static final String CLICK = "click";
	public static final String DOUBLE_CLICK = CLICK + "-double";
	public static final String RIGHT_CLICK = CLICK + "-right";
	public static final String WHEEL_CLICK = CLICK + "-wheel";

	public static final String DRAG = "drag";
	public static final String DROP = "drop";
	
	//public static final String SCROLL = "scroll";

	private String type = "undefined";
	private MouseDirection position;

	public Mouse() {
		setPosition(new MouseDirection());
	}
	
	public Mouse(String type) {
		setType(type);
		setPosition(new MouseDirection());
	}

	public Mouse(MouseDirectionData hpos, MouseDirectionData vpos) {
		setPosition(new MouseDirection(hpos, vpos));
	}
	
	public Mouse(String type, MouseDirectionData hpos, MouseDirectionData vpos) {
		setType(type);
		setPosition(new MouseDirection(hpos, vpos));
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MouseDirection getPosition() {
		return position;
	}

	public void setPosition(MouseDirection position) {
		this.position = position;
	}
}