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

package com.ats.executor.drivers.engines.browsers;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.ats.driver.ApplicationProperties;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.MouseDirection;

public class IEDriverEngine extends WebDriverEngine {

	protected static final String IE_MIDDLE_CLICK = "var evt=document.createEvent(\"MouseEvents\"),result={};evt.initMouseEvent(\"click\",true,true,window,1,0,0,0,0,false,false,false,false,1,null);arguments[0].dispatchEvent(evt);";

	private final static int DEFAULT_WAIT = 150;

	public IEDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, ApplicationProperties props) {
		super(channel, "ie", driverProcess, windowsDriver, props, DEFAULT_WAIT);

		InternetExplorerOptions ieOptions = new InternetExplorerOptions();
		ieOptions.introduceFlakinessByIgnoringSecurityDomains();
		ieOptions.enablePersistentHovering();

		launchDriver(status, ieOptions);
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop) {
		desktopMoveToElement(foundElement, position,0 ,-9);
	}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position) {
		getDesktopDriver().mouseDown();
	}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position) {

		final Rectangle rect = element.getRectangle();

		try {
			actions.moveToElement(element.getValue(), getOffsetX(rect, position), getOffsetY(rect, position)).click().build().perform();
			status.setPassed(true);
		}catch(StaleElementReferenceException e1) {
			throw e1;
		}catch(ElementNotVisibleException e0) {	
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
		}catch (Exception e) {
			status.setPassed(false);
			status.setMessage(e.getMessage());
		}
	}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
		runJavaScript(status, IE_MIDDLE_CLICK, element.getWebElement());
	}

	@Override
	public void drop(MouseDirection md, boolean desktopDriver) {
		getDesktopDriver().mouseRelease();
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		if(element.isNumeric()) {
			for(SendKeyData sequence : textActionList) {
				element.executeScript(status, "value='" + sequence.getData() + "'");
			}
		}else {
			for(SendKeyData sequence : textActionList) {
				element.getWebElement().sendKeys(sequence.getSequenceWithDigit());
			}
		}
	}

	@Override
	public boolean setWindowToFront() {
		int index = currentWindow;
		currentWindow = -1;

		switchWindow(index);
		refreshElementMapLocation(channel);
		return false;
	}

	@Override
	public void switchWindow(int index) {
		if(currentWindow != index) {
			List<DesktopWindow> wins = getDesktopDriver().getWindowsByPid(channel.getProcessId());
			if(wins.size() > index) {
				getDesktopDriver().setChannelToFront(wins.get(index).handle);
			}
			super.switchWindow(index);
		}
	}
}