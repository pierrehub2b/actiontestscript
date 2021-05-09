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

import com.ats.driver.ApplicationProperties;
import com.ats.element.SearchedElement;
import com.ats.element.TestElementSystem;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.browsers.capabilities.MsEdgeOptions;
import com.ats.generator.variables.CalculatedProperty;

public class MsEdgeDriverEngine extends ChromiumBasedDriverEngine {

	public MsEdgeDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, status, DriverManager.MSEDGE_BROWSER, driverProcess, desktopDriver, props);

		browserArguments = new BrowserArgumentsParser(channel.getArguments(), props, DriverManager.MSEDGE_BROWSER, applicationPath);
		launchDriver(status, new MsEdgeOptions(browserArguments));
	}

	@Override
	public void started(ActionStatus status) {
		if(!isHeadless() && channel.getDesktopDriver() != null) {
			final TestElementSystem closInfobarButton = new TestElementSystem(channel, 1, p -> p == 1, new SearchedElement(new SearchedElement(new SearchedElement(0, "syscomp", new CalculatedProperty[] {}), 0, "Group", new CalculatedProperty[] {new CalculatedProperty("ClassName", "InfoBarContainerView")}), 0, "Button", new CalculatedProperty[] {}));
			if(closInfobarButton.getCount() == 1) {
				closInfobarButton.executeScript(status, "Invoke()", false);
			}
		}
	}
}