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
import java.util.List;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ats.driver.AtsManager;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class ChromeDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 130;

	public ChromeDriverEngine(Channel channel, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {

		super(channel, DriverManager.CHROME_BROWSER, driverProcess, windowsDriver, ats, DEFAULT_WAIT);

		List<String> args = new ArrayList<String>();

		args.add("--disable-infobars");
		args.add("--disable-notifications");
		args.add("--no-default-browser-check");
		args.add("--disable-web-security");
		args.add("--allow-running-insecure-content");
		args.add("test-type");

		ChromeOptions options = new ChromeOptions();
		options.addArguments(args);

		if(applicationPath != null) {
			options.setBinary(applicationPath);
		}

		DesiredCapabilities caps = DesiredCapabilities.chrome(); 
		caps.setCapability(ChromeOptions.CAPABILITY, options); 

		launchDriver(caps);
	}
}
