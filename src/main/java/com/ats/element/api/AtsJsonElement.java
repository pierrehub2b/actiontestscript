package com.ats.element.api;

import java.util.Map;

import com.google.gson.JsonElement;

public class AtsJsonElement extends AtsApiElement {

	private JsonElement element;
	
	public AtsJsonElement(JsonElement element, String tag, Map<String, String> attributes) {
		super(tag, attributes);
		this.element = element;		
	}

	public JsonElement getElement() {
		return element;
	}

	public void setElement(JsonElement element) {
		this.element = element;
	}
}