package com.ats.element.api;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AtsXmlElement extends AtsApiElement {

	private Element element;

	public AtsXmlElement(Node element, String tag, Map<String, String> attributes) {
		super(tag, attributes);
		try {
			this.element = (Element)element;
		}catch (ClassCastException e) {}
	}
		
	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}
}