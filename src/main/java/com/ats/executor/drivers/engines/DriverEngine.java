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

package com.ats.executor.drivers.engines;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.driver.ApplicationProperties;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.graphic.TemplateMatchingSimple;

public abstract class DriverEngine {
	
	protected static String BODY = "BODY";
	protected static String SYSCOMP = "SYSCOMP";
	
	protected Channel channel;
	
	protected RemoteWebDriver driver;
	protected DesktopDriver desktopDriver;
		
	protected String applicationPath;
	protected String lang;
	
	protected int currentWindow = 0;
	
	private int actionWait = -1;
	private int propertyWait = -1;

	public DriverEngine(Channel channel) {
		this.channel = channel;
	}
	
	public DriverEngine(Channel channel, DesktopDriver desktopDriver, ApplicationProperties props) {
		this(channel);
		this.desktopDriver = desktopDriver;
	}
	
	public DriverEngine(Channel channel, DesktopDriver desktopDriver, ApplicationProperties props, int defaultWait, int defaultCheck){
		
		this(channel);
		this.desktopDriver = desktopDriver;
		
		actionWait = props.getWait();
		propertyWait = props.getCheck();
		applicationPath = props.getUri();
		lang = props.getLang();
		
		if(actionWait == -1) {
			actionWait = defaultWait;
		}
		
		if(propertyWait == -1) {
			propertyWait = defaultCheck;
		}
	}
	
	public void updateScreenshot(TestBound dimension, boolean isRef) {
		getDesktopDriver().updateVisualImage(dimension, isRef);
	}
	
	public void createVisualAction(Channel channel, String actionType, int scriptLine, long timeline, boolean sync) {
		getDesktopDriver().createVisualAction(channel, actionType, scriptLine, timeline, sync);
	}

	public TestElement getTestElementRoot() {
		return new TestElement(channel);
	}
		
	public DesktopDriver getDesktopDriver() {
		return desktopDriver;
	}
	
	public int getCurrentWindow() {
		return currentWindow;
	}
	
	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}
	
	public String getApplicationPath() {
		return applicationPath;
	}
	
	public void actionWait() {
		channel.sleep(actionWait);
	}
	
	public int getActionWait() {
		return actionWait;
	}
	
	public int getPropertyWait() {
		return propertyWait;
	}

	protected int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2, Cartesian cart3) {
		if(direction != null) {
			if(cart1.equals(direction.getName())) {				// <-- left or top
				return direction.getIntValue() + 1;				//
			}else if(cart3.equals(direction.getName())) {		// <-- right or bottom
				return value - direction.getIntValue() - 2;		//
			}													//
			return direction.getIntValue() + (value/2); 			// <-- middle or center
		}
		return value/2;
	}

	public int getOffsetX(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.width, position.getHorizontalPos(), Cartesian.LEFT, Cartesian.CENTER, Cartesian.RIGHT);
	}

	public int getOffsetY(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.height, position.getVerticalPos(), Cartesian.TOP, Cartesian.MIDDLE, Cartesian.BOTTOM);
	}
		
	public String setWindowBound(BoundData x, BoundData y, BoundData w, BoundData h) {

		int newX = 0;
		int newY = 0;
		int newWidth = 0;
		int newHeight = 0;
		
		if(w != null || h != null){
			
			if(w != null) {
				newWidth = w.getValue();
			}else {
				newWidth = channel.getDimension().getWidth().intValue();
			}
			
			if(h != null) {
				newHeight = h.getValue();
			}else {
				newHeight = channel.getDimension().getHeight().intValue();
			}

			setSize(new Dimension(newWidth, newHeight));
		}

		if(x != null || y != null){

			if(x != null) {
				newX = x.getValue();
			}else {
				newX = channel.getDimension().getX().intValue();
			}

			if(y != null) {
				newY = y.getValue();
			}else {
				newY = channel.getDimension().getY().intValue();
			}

			setPosition(new Point(newX, newY));
		}
		
		return newX + "," + newY + "," + newWidth + "," + newHeight;
	}
	
	protected void desktopMoveToElement(FoundElement foundElement, MouseDirection position, int offsetX, int offsetY) {
		final Rectangle rect = foundElement.getRectangle();
		getDesktopDriver().mouseMove(
				getOffsetX(rect, position) + foundElement.getScreenX().intValue() + offsetX,
				getOffsetY(rect, position) + foundElement.getScreenY().intValue() + offsetY);
	}

	abstract protected void setPosition(Point pt);
	abstract protected void setSize(Dimension dim);
	public void toFront() {};
	
	public List<FoundElement> findElements(TestElement parent, TemplateMatchingSimple template) {

		TestBound outterBound = null;
		if(parent != null) {
			outterBound = parent.getFoundElement().getTestScreenBound();
		}else {
			outterBound = channel.getDimension();
		}
		
		channel.setWindowToFront();
		
		byte[] screenshot = new byte[0];
		screenshot = getDesktopDriver().getScreenshotByte(outterBound.getX(), outterBound.getY(), outterBound.getWidth(), outterBound.getHeight());

		return template.findOccurrences(screenshot).parallelStream().map(r -> new FoundElement(channel, parent, r)).collect(Collectors.toCollection(ArrayList::new));
	}
	
	public byte[] getScreenshot(Double x, Double y, Double width, Double height) {
		return getDesktopDriver().getScreenshotByte(x, y, width, height);
	}
	
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return new CalculatedProperty[0];
	}
}