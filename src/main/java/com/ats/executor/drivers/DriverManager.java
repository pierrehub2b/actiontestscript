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

package com.ats.executor.drivers;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.ats.driver.ApplicationProperties;
import com.ats.driver.AtsManager;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.ApiDriverEngine;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.executor.drivers.engines.browsers.ChromeDriverEngine;
import com.ats.executor.drivers.engines.browsers.ChromiumDriverEngine;
import com.ats.executor.drivers.engines.browsers.EdgeDriverEngine;
import com.ats.executor.drivers.engines.browsers.FirefoxDriverEngine;
import com.ats.executor.drivers.engines.browsers.IEDriverEngine;
import com.ats.executor.drivers.engines.browsers.OperaDriverEngine;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;

public class DriverManager {

	public static final String CHROME_BROWSER = "chrome";
	public static final String CHROMIUM_BROWSER = "chromium";
	public static final String FIREFOX_BROWSER = "firefox";
	public static final String IE_BROWSER = "ie";
	public static final String EDGE_BROWSER = "edge";
	public static final String OPERA_BROWSER = "opera";
	public static final String SAFARI_BROWSER = "safari";

	public static final String DESKTOP_DRIVER_FILE_NAME = "windowsdriver.exe";
	public static final String CHROME_DRIVER_FILE_NAME = "chromedriver.exe";
	public static final String IE_DRIVER_FILE_NAME = "IEDriverServer.exe";
	public static final String EDGE_DRIVER_FILE_NAME = "MicrosoftWebDriver";
	public static final String OPERA_DRIVER_FILE_NAME = "operadriver.exe";
	public static final String FIREFOX_DRIVER_FILE_NAME = "geckodriver.exe";

	public static final String DESKTOP_EXPLORER = "explorer";
	public static final String DESKTOP = "desktop";
	public static final String MOBILE = "mobile";
	public static final String HTTP = "http";
	public static final String HTTPS = "https";

	private ArrayList<DriverProcess> driversProcess = new ArrayList<DriverProcess>();

	public static AtsManager ATS = new AtsManager();

	private MobileDriverEngine mobileDriverEngine;

	private String windowsBuildVersion;
	private String osName;
	private String osVersion;

	public String getDriverFolderPath() {
		return ATS.getDriversFolderPath().toFile().getAbsolutePath();
	}

	public void setWindowsBuildVersion(String value) {
		windowsBuildVersion = value;
	}

	public void setOsName(String value) {
		osName = value;
	}

	public void setOsVersion(String value) {
		osVersion = value;
	}

	//--------------------------------------------------------------------------------------------------------------

	public static void killAllDrivers() {
		Predicate<ProcessHandle> fullPredicate = p -> p.info().command().isPresent();
		Predicate<ProcessHandle> desktop  =  p -> p.info().command().get().contains(DESKTOP_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> chrome  = p -> p.info().command().get().contains(CHROME_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> opera  =  p -> p.info().command().get().contains(OPERA_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> edge  =  p -> p.info().command().get().contains(EDGE_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> firefox  =  p -> p.info().command().get().contains(FIREFOX_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> ie  =  p -> p.info().command().get().contains(IE_DRIVER_FILE_NAME);

		ProcessHandle
		.allProcesses()
		.parallel()
		.filter(fullPredicate)
		.filter(chrome.or(edge).or(firefox).or(opera).or(ie).or(desktop))
		.forEach(p2 -> {
			p2.children().parallel().forEach(p3 -> p3.destroy());
			p2.destroy();
		});
	}

	//--------------------------------------------------------------------------------------------------------------

	public DriverProcess getDesktopDriver() {
		return getDriverProcess(DESKTOP, DESKTOP_DRIVER_FILE_NAME);
	}

	public void processTerminated(DriverProcess dp) {
		driversProcess.remove(dp);
	}

	public IDriverEngine getDriverEngine(Channel channel, ActionStatus status, DesktopDriver desktopDriver) {

		final String application = channel.getApplication();
		final ApplicationProperties props = ATS.getApplicationProperties(application);

		final String appName = props.getName().toLowerCase();

		if(CHROME_BROWSER.equals(appName)) {
			return new ChromeDriverEngine(channel, status, getDriverProcess(CHROME_BROWSER, CHROME_DRIVER_FILE_NAME), desktopDriver, props);	
		}else if(EDGE_BROWSER.equals(appName)) {
			return new EdgeDriverEngine(channel, status, getDriverProcess(EDGE_BROWSER, EDGE_DRIVER_FILE_NAME + "-" + windowsBuildVersion + ".exe"), desktopDriver, props);
		}else if(OPERA_BROWSER.equals(appName)) {
			return new OperaDriverEngine(channel, status, getDriverProcess(OPERA_BROWSER, OPERA_DRIVER_FILE_NAME), desktopDriver, props);
		}else if(FIREFOX_BROWSER.equals(appName)) {
			return new FirefoxDriverEngine(channel, status, getDriverProcess(FIREFOX_BROWSER, FIREFOX_DRIVER_FILE_NAME), desktopDriver, props);
		}else if(IE_BROWSER.equals(appName)) {
			return new IEDriverEngine(channel, status, getDriverProcess(IE_BROWSER, IE_DRIVER_FILE_NAME), desktopDriver, props);
		}else if(CHROMIUM_BROWSER.equals(appName)) {
			if(props.getDriver() != null && props.getUri() != null) {
				final DriverProcess chromiumDriver = getDriverProcess(props.getName(), props.getDriver() + ".exe");
				if(chromiumDriver.isStarted()) {
					return new ChromiumDriverEngine(channel, status, props.getName(), chromiumDriver, desktopDriver, props);
				}else {
					status.setPassed(false);
					status.setCode(ActionStatus.CHANNEL_START_ERROR);
					status.setMessage("Cannot start Chromium driver : " + chromiumDriver.getError());
					return null;
				}
			}else {
				status.setPassed(false);
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("Missing Chromium properties ('path' and 'driver') in .atsProperties file !");
				return null;
			}
		}else if(props.isMobile() || application.startsWith(MOBILE + "://")){
			mobileDriverEngine = new MobileDriverEngine(channel, status, application, desktopDriver, props);
			return mobileDriverEngine;			
		}else if(props.isApi() || application.startsWith(HTTP + "://") || application.startsWith(HTTPS + "://")) {	
			return new ApiDriverEngine(channel, status, application, desktopDriver, props);
		}

		return new DesktopDriverEngine(channel, status, application, desktopDriver, props);
	}

	private DriverProcess getDriverProcess(String name, String driverName) {
		for (DriverProcess proc : driversProcess) {
			if(proc.getName().equals(name)) {
				return proc;
			}
		}

		final DriverProcess proc = new DriverProcess(name, this, ATS.getDriversFolderPath(), driverName, null);
		if(proc.isStarted()) {
			driversProcess.add(proc);
		}
		return proc;
	}

	public void tearDown(){
		
		while(driversProcess.size() > 0) {
			driversProcess.remove(0).close();
		}

		if(mobileDriverEngine != null){
			mobileDriverEngine.tearDown();
			mobileDriverEngine = null;
		}
	}
}