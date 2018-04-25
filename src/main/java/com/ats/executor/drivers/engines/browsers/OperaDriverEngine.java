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

import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ats.driver.AtsManager;
import com.ats.driver.BrowserProperties;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class OperaDriverEngine extends WebDriverEngine {

	private int waitAfterAction = 150;
	
	public OperaDriverEngine(Channel channel, DriverProcess driverProcess, WindowsDesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.OPERA_BROWSER, driverProcess, windowsDriver, ats);
				
		OperaOptions options = new OperaOptions();
		options.setCapability("opera.log.level", "SEVERE");
				
		BrowserProperties props = ats.getBrowserProperties(DriverManager.OPERA_BROWSER);
		if(props != null) {
			waitAfterAction = props.getWait();
			applicationPath = props.getPath();
			if(applicationPath != null) {
				options.setBinary(applicationPath);
			}
		}
				
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(OperaOptions.CAPABILITY, options);
		
		launchDriver(cap);
	}
	
	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
		super.waitAfterAction();
	}
}