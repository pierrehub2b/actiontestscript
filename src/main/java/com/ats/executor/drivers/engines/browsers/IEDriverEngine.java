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

import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.MouseDirection;

public class IEDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 150;

	public IEDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {
		super(channel, "ie", driverProcess, windowsDriver, ats, DEFAULT_WAIT);

		InternetExplorerOptions ieOptions = new InternetExplorerOptions();
		ieOptions.introduceFlakinessByIgnoringSecurityDomains();
		ieOptions.enableNativeEvents();
		ieOptions.enablePersistentHovering();
		
		ieOptions.setCapability(CapabilityType.PROXY, ats.getProxy().getProxy());

		launchDriver(status, ieOptions);
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {
		
		Rectangle rect = foundElement.getRectangle();

		getDesktopDriver().mouseMove(
				getOffsetX(rect, position) + foundElement.getScreenX().intValue(),
				getOffsetY(rect, position) + foundElement.getScreenY().intValue() - 9);
	}

	@Override
	public void mouseClick(FoundElement element, boolean hold) {
		if(hold) {
			getDesktopDriver().mouseDown();
		}else {
			super.mouseClick(element, false);
		}
	}
	
	@Override
	public void drop() {
		getDesktopDriver().mouseRelease();
	}
}