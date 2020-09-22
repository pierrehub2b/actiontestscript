/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.ats.element;

import com.ats.generator.variables.CalculatedProperty;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AtsMobileElement extends AtsBaseElement {
	
	private static final String INNER_TEXT = "innerText";
	private static final String TEXT = "text";
	private static final String ROOT = "root";

	private boolean clickable = false;

	private AtsMobileElement parent;
	private AtsMobileElement[] children;
	private String positionInDom;

	private Rectangle rect;
	public Rectangle getRect() {
		if (rect == null) {
			rect = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
		}
		return rect;
	}
	
	public AtsMobileElement(String id, String tag, Double width, Double height, Double x, Double y, Boolean clickable, Map<String, String> attributes) {
		this.id = id;
		this.tag = tag;
		this.width = width;
		this.height = height;
		this.clickable = clickable;
		this.x = x;
		this.y = y;
		this.attributes = attributes;
	}
	
	public void setPositionInDom(String value) {
		this.positionInDom = value;
	}
	
	public String getPositionInDom() {
		return this.positionInDom;
	}
	
	
	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	private String getText() {
		final String result = attributes.get(TEXT);
		if(result == null) {
			return "";
		}
		return result;
	}

	private String getInnerText() {
		String result = getText();
		if(children != null) {
			for(int i=0; i<children.length; i++) {
				result += " " + children[i].getInnerText();
			}
		}
		return result.trim();
	}

	public CalculatedProperty[] getMobileAttributes() {
		final List<CalculatedProperty> properties = attributes.entrySet().stream().parallel().map(e -> new CalculatedProperty(e.getKey(), e.getValue())).collect(Collectors.toCollection(ArrayList::new));
		properties.add(new CalculatedProperty(INNER_TEXT, getInnerText()));

		return properties.toArray(new CalculatedProperty[properties.size()]);
	}
	
	@Override
	public String getAttribute(String key) {
		if(INNER_TEXT.equals(key)) {
			return getInnerText();
		}
		return super.getAttribute(key);
	}

	public boolean isRoot() {
		return ROOT.equals(tag);
	}

	public boolean checkTag(String value) {
		return SearchedElement.WILD_CHAR.equals(value) || tag.toLowerCase().equals(value.toLowerCase());
	}

	public FoundElement getFoundElement() {
		return new FoundElement(this);
	}

	public AtsMobileElement getParent() {
		return parent;
	}

	public void setParent(AtsMobileElement parent) {
		this.parent = parent;
	}

	public AtsMobileElement[] getChildren() {
		if(this.children == null) {
			return new AtsMobileElement[0];
		}
		return children;
	}

	public void setChildren(AtsMobileElement[] children) {
		this.children = children;
	}
	
	public void addChildren(AtsMobileElement element) {
		ArrayList<AtsMobileElement> tmpChild = new ArrayList<AtsMobileElement>();
		if(this.children != null) {
			tmpChild = new ArrayList<AtsMobileElement>(Arrays.asList(this.children));
		}
		tmpChild.add(element);
		this.children = tmpChild.toArray(new AtsMobileElement[tmpChild.size()]);
	}
}