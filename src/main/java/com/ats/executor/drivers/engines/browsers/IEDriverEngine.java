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

import java.util.ArrayList;
import java.util.Set;

import org.openqa.selenium.ie.InternetExplorerOptions;

import com.ats.driver.ApplicationProperties;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.ats.tools.Utils;

public class IEDriverEngine extends WebDriverEngine {

	protected static final String IE_MIDDLE_CLICK = "var evt=document.createEvent(\"MouseEvents\"),result={};evt.initMouseEvent(\"click\",true,true,window,1,0,0,0,0,false,false,false,false,1,null);arguments[0].dispatchEvent(evt);";

	public IEDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, ApplicationProperties props) {
		super(channel, "ie", driverProcess, windowsDriver, props);

		InternetExplorerOptions ieOptions = new InternetExplorerOptions();
		ieOptions.introduceFlakinessByIgnoringSecurityDomains();
		ieOptions.enablePersistentHovering();

		launchDriver(status, ieOptions);
		
		if(!"11".equals(channel.getApplicationVersion())) {
			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			status.setMessage("Cannot start channel with IE" + channel.getApplicationVersion() + " ! (Only IE11 is supported by ATS)");
		}
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
		desktopMoveToElement(foundElement, position, (int)(foundElement.getWidth()/2), (int)(foundElement.getHeight()/2) - 8);
	}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		getDesktopDriver().mouseDown();
	}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
		runJavaScript(status, IE_MIDDLE_CLICK, element.getWebElement());
	}
	
	@Override
	public void doubleClick() {
		getDesktopDriver().doubleClick();
	}
	
	@Override
	public void rightClick() {
		getDesktopDriver().mouseRightClick();
	}

	@Override
	public void drop(MouseDirection md, boolean desktopDriver) {
		getDesktopDriver().mouseRelease();
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		if(element.isNumeric()) {
			for(SendKeyData sequence : textActionList) {
				element.executeScript(status, "value='" + sequence.getData() + "'", true);
			}
		}else {
			for(SendKeyData sequence : textActionList) {
				element.getWebElement().sendKeys(sequence.getSequenceWithDigit());
			}
		}
	}

	@Override
	public void switchWindow(ActionStatus status, int index) {
		DesktopResponse resp = getDesktopDriver().setIEWindowToFront(index);
		
		//TODO clean this mess
		
		if(resp.data != null && resp.data.size() > 0) {
			
			final int winIndex = Utils.string2Int(resp.data.get(0).getValue());
			final Set<String> wins = driver.getWindowHandles();
			
			switchToWindowIndex(wins.toArray(new String[wins.size()]), index);
			
			
			//final List<String> handles = new ArrayList<>(driver.getWindowHandles());
			//driver.switchTo().window(handles.get(winIndex));
			
			//channel.cleanHandle();
			channel.setWinHandle(-1);
			//updateDimensions();
			
		}
		

	}
}