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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.tools.Utils;

public class ChromeDriverEngine extends WebDriverEngine {

	public ChromeDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, ApplicationProperties props) {

		super(channel, DriverManager.CHROME_BROWSER, driverProcess, windowsDriver, props);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--no-default-browser-check");
		options.addArguments("--test-type");
		options.addArguments("--allow-file-access-from-files");
		options.addArguments("--allow-running-insecure-content");
		options.addArguments("--allow-file-access-from-files");
		options.addArguments("--allow-cross-origin-auth-prompt");
		options.addArguments("--allow-file-access");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-web-security");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--use-fake-ui-for-media-stream");
		options.addArguments("--use-fake-device-for-media-stream");
		
		File profileFolder = null;
		if(props.getUserDataDir() != null) {
			profileFolder = new File(props.getUserDataDir());
			if(!profileFolder.exists()) {
				profileFolder = null;
			}
		}
		
		if(profileFolder == null) {
			profileFolder = Utils.createDriverFolder(DriverManager.CHROME_BROWSER);
			
			Map<String, Object> prefs = new HashMap<String, Object>();
	        prefs.put("credentials_enable_service", false);
	        prefs.put("profile.password_manager_enabled", false);
	        options.setExperimentalOption("prefs", prefs);
		}
		options.addArguments("--user-data-dir=" + profileFolder.getAbsolutePath());
        
		if(lang != null) {
			options.addArguments("--lang=" + lang);
		}

		if(applicationPath != null) {
			options.setBinary(applicationPath);
		}
		
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);

		launchDriver(status, options);
	}
}