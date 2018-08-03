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
import java.util.function.Predicate;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class EdgeDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 150;

	public EdgeDriverEngine(Channel channel, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.EDGE_BROWSER, driverProcess, windowsDriver, ats, DEFAULT_WAIT);
		
		this.searchElementScript = JS_WAIT_BEFORE_SEARCH + JS_SEARCH_ELEMENT;
		this.autoScrollElement = JS_AUTO_SCROLL_CALC;
		
		EdgeOptions options = new EdgeOptions();
		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
		options.setPageLoadStrategy("normal");

		launchDriver(options);
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
	public void closeWindow(ActionStatus status, int index) {
		actionWait();
		super.closeWindow(status, index);
		actionWait();
	}
	
	@Override
	public void middleClick(ActionStatus status, TestElement element) {
		middleClickSimulation(status, element);
	}
	
	@Override
	protected void move(FoundElement element, int offsetX, int offsetY) {
		forceScrollElement(element);
		super.move(element, offsetX, offsetY);
	}

	@Override
	protected void switchToWindowHandle(String handle) {
		actionWait();
		super.switchToWindowHandle(handle);
		actionWait();
	}

	@Override
	protected void switchToFrame(WebElement we) {
		actionWait();
		super.switchToFrame(we);
		actionWait();
	}

	@Override
	public void sendTextData(ActionStatus status, FoundElement element, ArrayList<SendKeyData> textActionList) {
		boolean enterKey = false;

		for(SendKeyData sequence : textActionList) {
			element.getValue().sendKeys(sequence.getSequence());
			if(sequence.isEnterKey()) {
				enterKey = true;
			}
		}
		
		if(enterKey) {
			actionWait();
		}
	}
	
	@Override
	public ArrayList<FoundElement> findElements(Channel channel, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<Object> predicate) {
		int maxTry = 40;
		while(!((Boolean)runJavaScript(JS_WAIT_READYSTATE)) && maxTry > 0) {
			channel.sleep(200);
			maxTry--;
		}
		return super.findElements(channel, testObject, tagName, attributes, predicate);
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {
		super.goToUrl(status, url);
		waitAfterAction();
	}
}