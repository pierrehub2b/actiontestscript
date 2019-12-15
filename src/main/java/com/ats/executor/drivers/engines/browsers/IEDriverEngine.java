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
import java.util.Iterator;
import java.util.Set;

import org.openqa.selenium.ie.InternetExplorerOptions;
import org.testng.collections.Sets;

import com.ats.driver.ApplicationProperties;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.MouseDirection;

public class IEDriverEngine extends WebDriverEngine {
	
	private final Set<String> windows = Sets.newLinkedHashSet();

	public IEDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, ApplicationProperties props) {
		super(channel, DriverManager.IE_BROWSER, driverProcess, windowsDriver, props);

		JS_SCROLL_IF_NEEDED = "var e=arguments[0], bo=arguments[1], result=[];var r=e.getBoundingClientRect();if(r.top < 0 || r.left < 0 || r.bottom > (window.innerHeight || document.documentElement.clientHeight) || r.right > (window.innerWidth || document.documentElement.clientWidth)) {e.scrollIntoView(false);r=e.getBoundingClientRect();result=[r.left+0.0001, r.top+0.0001];}";

		final InternetExplorerOptions ieOptions = new InternetExplorerOptions();
		ieOptions.introduceFlakinessByIgnoringSecurityDomains();
		ieOptions.enablePersistentHovering();

		launchDriver(status, ieOptions, null);

		if(status.isPassed() && !"11".equals(channel.getApplicationVersion())) {
			status.setError(ActionStatus.CHANNEL_START_ERROR, "cannot start channel with IE" + channel.getApplicationVersion() + " (Only IE11 is supported by ATS)");
		}
		
		executeToFront();
	}

	@Override
	public void toFront() {
		channel.setWindowToFront();
		executeToFront();
	}
	
	@Override
	public void setWindowToFront() {
		super.setWindowToFront();
		executeToFront();
	}
	
	private void executeToFront() {
		try {
			driver.executeAsyncScript("var callback=arguments[arguments.length-1];var result=setTimeout(function(){window.focus();},1000);callback(result);");
		}catch (Exception e) {}
	}

	@Override
	public void scroll(FoundElement element) {
		try {
			super.scroll(element);
		}catch(Exception e) {}
	}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		desktopMoveToElement(element, position, (int)(element.getWidth()/2), (int)(element.getHeight()/2) - 8);
		getDesktopDriver().mouseDown();
	}

	@Override
	public void drop(MouseDirection md, boolean desktopDriver) {
		getDesktopDriver().mouseRelease();
	}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
		getDesktopDriver().mouseMiddleClick();
	}

	@Override
	public void doubleClick() {
		getDesktopDriver().doubleClick();
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		if(element.isNumeric()) {
			for(SendKeyData sequence : textActionList) {
				element.executeScript(status, "value='" + sequence.getData() + "'", true);
			}
		}else {
			try {
				for(SendKeyData sequence : textActionList) {
					element.getWebElement().sendKeys(sequence.getSequenceWithDigit());
				}
			}catch(Exception e) {
				status.setError(ActionStatus.ENTER_TEXT_FAIL, e.getMessage());
			}
		}
	}	

	@Override
	public void switchWindow(ActionStatus status, int index) {
		super.switchWindow(status, index);
		if(status.isPassed()) {
			executeJavaScript(status, "setTimeout(function(){window.focus();},1000);", true);
			toFront();
			channel.sleep(2000);
		}
	}

	@Override
	protected Set<String> getDriverWindowsList() {
		final Set<String> driverList = super.getDriverWindowsList();
		driverList.parallelStream().forEach(s -> windows.add(s));

		for (Iterator<String> iterator = windows.iterator(); iterator.hasNext();) {
		    if(!driverList.contains(iterator.next())) {
		    	iterator.remove();
		    }
		}
		return windows;
	}
}