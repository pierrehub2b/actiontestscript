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

import java.awt.Rectangle;
import java.util.ArrayList;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopWindow;

public class FoundElement{

	public final static String HTML = "html";
	public final static String DESKTOP = "desktop";
	public final static String MOBILE = "mobile";
	
	private String id;

	private Double x = 0.0;
	private Double y = 0.0;

	private Double screenX = 0.0;
	private Double screenY = 0.0;

	private Double width = 0.0;
	private Double height = 0.0;

	private String tag;

	private String type = HTML;

	private RemoteWebElement value;
	private FoundElement parent;
	private boolean visible = true;
	private boolean numeric = false;
	private boolean clickable = true;

	//------------------------------------------------------------------------------------------------------------------------------
	// contructors
	//------------------------------------------------------------------------------------------------------------------------------
	
	public FoundElement() {}

	public FoundElement(Channel channel) {
		this.setRemoteWebElement((RemoteWebElement) channel.getRootElement());
		this.width = channel.getDimension().getWidth();
		this.height = channel.getDimension().getHeight();
	}

	public FoundElement(AtsElement element) {
		this.setRemoteWebElement(element.getElement());
		this.tag = element.getTag();
		this.width = element.getWidth();
		this.height = element.getHeight();
		this.numeric = element.isNumeric();
	}
	
	public FoundElement(DesktopWindow win) {
		this.id = win.getId();
		this.tag = win.getTag();
		this.width = win.getWidth();
		this.height = win.getHeight();
	}

	public FoundElement(AtsElement element, Channel channel, Double offsetX, Double offsetY) {

		this(element);

		final Double elemX = element.getX();
		final Double elemY = element.getY();

		this.updatePosition(elemX, elemY, channel, offsetX, offsetY);
		this.screenX = element.getScreenX() + elemX;
		this.screenY = element.getScreenY() + elemY;
	}

	public FoundElement(AtsElement element, TestBound channelDimension) {

		this.type = DESKTOP;
		this.visible = element.isVisible();
		this.id = element.getId();

		this.tag = element.getTag();
		this.width = element.getWidth();
		this.height = element.getHeight();

		this.screenX = element.getX();
		this.screenY = element.getY();

		this.x = this.screenX - channelDimension.getX();
		this.y = this.screenY - channelDimension.getY();
	}
	
	public FoundElement(AtsMobileElement element) {

		this.type = MOBILE;
		this.visible = true;
		this.id = element.getId();

		this.tag = element.getTag();
		this.width = element.getWidth();
		this.height = element.getHeight();
		
		this.clickable = element.isClickable();

		this.screenX = element.getX();
		this.screenY = element.getY();

		this.x = this.screenX;
		this.y = this.screenY;
	}

	public FoundElement(Channel channel, ArrayList<AtsElement> elements, Double initElementX, Double initElementY) {
		this(elements.remove(0), channel, initElementX, initElementY);
		if(elements.size() > 0) {
			setParent(new FoundElement(channel, elements, initElementX, initElementY));
		}
	}
	
	public boolean isDesktop() {
		return DESKTOP.equals(type);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------
	// end of contructors
	//------------------------------------------------------------------------------------------------------------------------------

	public void dispose() {
		if(parent != null) {
			parent.dispose();
			parent = null;
		}
		value = null;
	}

	public void updatePosition(Double x, Double y, Channel channel, Double offsetX, Double offsetY) {
		this.setX(x + channel.getSubDimension().getX() + offsetX);
		this.setY(y + channel.getSubDimension().getY() + offsetY);
	}

	private void setRemoteWebElement(RemoteWebElement rwe){
		this.value = rwe;
		this.id = rwe.getId();
	}

	public boolean isIframe(){
		return AtsElement.checkIframe(this.tag);
	}

	public WebElement getValue(){
		return value;
	}

	public String getElementId() {
		return id;
	}

	public void activate(RemoteWebDriver remoteWebDriver) {
		value = new RemoteWebElement();
		value.setParent(remoteWebDriver);
		value.setId(id);
	}

	public Rectangle getRectangle(){
		return new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
	}

	public Double getScreenX() {
		return screenX;
	}

	public Double getScreenY() {
		return screenY;
	}
	
	public boolean isNumeric() {
		return numeric;
	}
	
	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean value){
		this.clickable = value;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean value){
		this.visible = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public FoundElement getParent() {
		return parent;
	}

	public void setParent(FoundElement parent) {
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Remote element
	//----------------------------------------------------------------------------------------------------------------------

	public RemoteWebElement getRemoteWebElement(RemoteWebDriver driver) {
		final RemoteWebElement element = new RemoteWebElement();
		
		element.setParent(driver);
		element.setId(id);
		
		return element;
	}

	public TestBound getTestBound() {
		return new TestBound(x, y, width, height);
	}
}