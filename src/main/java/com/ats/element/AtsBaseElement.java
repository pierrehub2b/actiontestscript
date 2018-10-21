package com.ats.element;

import java.util.Map;

public class AtsBaseElement {
	
	protected String id;
	protected String tag;
	protected Double width;
	protected Double height;
	protected Double x;
	protected Double y;
	protected Map<String, String> attributes;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}
	
	//----------------------------------------------------------------------------------------
	// Predicate search
	//----------------------------------------------------------------------------------------

	public String getAttribute(String key) {
		return attributes.get(key);
	}
}
