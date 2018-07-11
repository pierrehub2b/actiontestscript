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

import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ats.driver.AtsManager;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class IEDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 150;

	public IEDriverEngine(Channel channel, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {
		super(channel, "ie", driverProcess, windowsDriver, ats, DEFAULT_WAIT);
		
		DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
		capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);  
		capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, false);
		capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
		capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		capabilities.setCapability(InternetExplorerDriver.ENABLE_ELEMENT_CACHE_CLEANUP, true);
		capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, true);
		capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "accept");
		capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
		//capabilities.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);

		launchDriver(capabilities);
	}
}