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

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.ats.driver.ApplicationProperties;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.MouseDirection;

public class EdgeDriverEngine extends WebDriverEngine {

	private static final String JS_WAIT_READYSTATE = "var interval=setInterval(function(){if(window.document.readyState==='complete'){clearInterval(interval);done();}},200)};";

	public EdgeDriverEngine(
			Channel channel, 
			ActionStatus status, 
			DriverProcess driverProcess, 
			DesktopDriver windowsDriver, 
			ApplicationProperties props) {
		
		super(channel, DriverManager.EDGE_BROWSER, driverProcess, windowsDriver, props);

		this.searchElementScript = JS_WAIT_READYSTATE + JS_SEARCH_ELEMENT;

		EdgeOptions options = new EdgeOptions();
		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

		launchDriver(status, options);
	}

	@Override
	protected void setPosition(Point pt) {
		actionWait();
		super.setPosition(pt);
		actionWait();
	}

	@Override
	protected void setSize(Dimension dim) {
		actionWait();
		super.setSize(dim);
		actionWait();
	}

	@Override
	public void closeWindow(ActionStatus status) {
		actionWait();
		super.closeWindow(status);
		actionWait();
	}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
		middleClickSimulation(status, position, element);
	}

	@Override
	protected boolean switchToWindowHandle(String handle) {
		actionWait();
		if(super.switchToWindowHandle(handle)) {
			actionWait();
			return true;
		}
		return false;
	}

	@Override
	protected void switchToFrame(WebElement we) {
		actionWait();
		super.switchToFrame(we);
		actionWait();
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		boolean enterKey = false;

		for(SendKeyData sequence : textActionList) {
			element.getWebElement().sendKeys(sequence.getSequenceWeb(true));
			if(sequence.isEnterKey()) {
				enterKey = true;
			}
		}

		if(enterKey) {
			actionWait();
		}
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {
		super.goToUrl(status, url);
		waitAfterAction(status);
	}
}