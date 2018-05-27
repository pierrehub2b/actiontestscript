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

import java.net.URL;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.ats.driver.ApplicationProperties;
import com.ats.driver.AtsManager;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class EdgeDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 200;

	public EdgeDriverEngine(Channel channel, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.EDGE_BROWSER, driverProcess, windowsDriver, ats);

		EdgeOptions options = new EdgeOptions();
		options.setPageLoadStrategy("normal");
		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

		ApplicationProperties props = ats.getBrowserProperties(DriverManager.EDGE_BROWSER);
		if(props != null) {
			waitAfterAction = props.getWait();
		}
		
		if(waitAfterAction == -1) {
			waitAfterAction = DEFAULT_WAIT;
		}

		launchDriver(options);
	}		

	@Override
	protected void setPosition(Point pt) {
		channel.sleep(500);
		super.setPosition(pt);
	}

	@Override
	protected void setSize(Dimension dim) {
		channel.sleep(500);
		super.setSize(dim);
	}

	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {
		channel.sleep(500);
		super.closeWindow(status, index);
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		super.goToUrl(url, newWindow);
		waitAfterAction();

		/*if(newWindow) {
			channel.sleep(100);

			ArrayList<CalculatedProperty> attributes = new ArrayList<CalculatedProperty>(1);
			attributes.add(new CalculatedProperty("ClassName", "LandmarkTarget"));
			ArrayList<FoundElement> listElements = channel.findWindowsElement(null, "Group", attributes);

			if(listElements.size() > 0) {
				FoundElement parent = listElements.get(0);
				if(parent.isVisible()) {

					attributes = new ArrayList<CalculatedProperty>(1);
					attributes.add(new CalculatedProperty("ClassName", "NotificationBar"));
					listElements = channel.findWindowsElement(parent.getValue(), "ToolBar", attributes);

					if(listElements.size() > 0) {
						parent = listElements.get(0);
						if(parent.isVisible()) {

							attributes = new ArrayList<CalculatedProperty>(1);
							attributes.add(new CalculatedProperty("ClassName", "Button"));

							listElements = channel.findWindowsElement(parent.getValue(), "Button", attributes);

							if(listElements.size() > 1) {
								FoundElement button = listElements.get(1);
								if(button.isVisible()) {

									TestBound bound = button.getTestBound();
									Actions action = new Actions(desktopDriver);
									action.moveToElement(button.getValue(), bound.getWidth().intValue()/2, 40).perform();
									action.click().perform();
								}
							}
						}
					}
				}
			}
			switchToLastWindow();
		}*/
	}
}