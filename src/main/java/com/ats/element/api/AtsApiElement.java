package com.ats.element.api;

import java.util.Map;
import java.util.UUID;

import com.ats.element.AtsBaseElement;
import com.google.common.collect.ImmutableMap;

public class AtsApiElement extends AtsBaseElement {

	public AtsApiElement(String tag, Map<String, String> attributes) {
		this.id = UUID.randomUUID().toString();
		this.tag = tag;
		this.attributes = ImmutableMap.copyOf(attributes);
	}
}
