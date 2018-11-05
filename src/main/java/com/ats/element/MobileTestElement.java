package com.ats.element;

public class MobileTestElement {

	private String id;
	private int offsetX;
	private int offsetY;
	
	public MobileTestElement(String id, int offsetX, int offsetY) {
		this.id = id;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
		
	public String getId() {
		return id;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}
}
