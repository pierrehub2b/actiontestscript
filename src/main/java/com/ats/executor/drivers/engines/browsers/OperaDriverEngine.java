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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.opera.OperaOptions;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirectionData;

public class OperaDriverEngine extends WebDriverEngine {
	
	public OperaDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, ApplicationProperties props) {
		super(channel, DriverManager.OPERA_BROWSER, driverProcess, windowsDriver, props);
				
		initElementX = 20.0;
		
		browserArguments = new BrowserArgumentsParser(channel.getArguments(), props, DriverManager.OPERA_BROWSER, applicationPath);
		
		final OperaOptions options = new OperaOptions();

		if(browserArguments.getMoreOptions().length > 0) {
			for (String s: browserArguments.getMoreOptions()) {
				options.addArguments(s);
			}
		}else {
			options.setCapability("opera.log.level", "SEVERE");
			options.addArguments("test-type");
			options.addArguments("--disable-infobars");
			options.addArguments("--disable-notifications");
			options.addArguments("--no-default-browser-check");
			options.addArguments("--allow-file-access-from-files");
			options.addArguments("--allow-running-insecure-content");
		}
		
		if(browserArguments.isIncognito()) {
			options.addArguments("--incognito");
		}
					
		if(props.getDebugPort() > 0) {
			options.addArguments("--remote-debugging-port=" + props.getDebugPort());
		}

		if(browserArguments.getUserDataPath() != null) {
			options.addArguments("--user-data-dir=" + browserArguments.getUserDataPath());
		}

		if(lang != null) {
			options.addArguments("--lang=" + lang);
		}
		
		if(browserArguments.getBinaryPath() != null) {
			options.setBinary(browserArguments.getBinaryPath());
		}

		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

		final Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		options.setExperimentalOption("prefs", prefs);
		
		launchDriver(status, options);
	}

	@Override
	protected int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2,	Cartesian cart3) {
		return super.getCartesianOffset(value, direction, cart1, cart2, cart3) + value/2;
	}
}