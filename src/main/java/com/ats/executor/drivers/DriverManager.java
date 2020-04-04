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
import com.ats.executor.drivers.engines.browsers.JxDriverEngine;
import com.ats.executor.drivers.engines.browsers.MsEdgeDriverEngine;
import com.ats.executor.drivers.engines.browsers.OperaDriverEngine;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.executor.drivers.engines.desktop.ExplorerDriverEngine;

public class DriverManager {

	public static final String CHROME_BROWSER = "chrome";
	public static final String CHROMIUM_BROWSER = "chromium";
	public static final String JX_BROWSER = "jx";
	public static final String FIREFOX_BROWSER = "firefox";
	public static final String IE_BROWSER = "ie";
	public static final String EDGE_BROWSER = "edge";
	public static final String MSEDGE_BROWSER = "msedge";
	public static final String OPERA_BROWSER = "opera";
	public static final String SAFARI_BROWSER = "safari";

	public static final String DESKTOP_DRIVER_FILE_NAME = "windowsdriver";
	public static final String CHROME_DRIVER_FILE_NAME = "chromedriver";
	public static final String CHROMIUM_DRIVER_FILE_NAME = "chromiumdriver";
	public static final String IE_DRIVER_FILE_NAME = "IEDriverServer";
	public static final String EDGE_DRIVER_FILE_NAME = "MicrosoftWebDriver";
	public static final String MSEDGE_DRIVER_FILE_NAME = "msedgedriver";
	public static final String OPERA_DRIVER_FILE_NAME = "operadriver";
	public static final String FIREFOX_DRIVER_FILE_NAME = "geckodriver";

	public static final String DESKTOP_EXPLORER = "explorer";
	public static final String DESKTOP = "desktop";
	public static final String MOBILE = "mobile";
	public static final String HTTP = "http";
	public static final String HTTPS = "https";

	private ArrayList<DriverProcess> driversProcess = new ArrayList<DriverProcess>();

	public static AtsManager ATS = new AtsManager();

	private DriverProcess desktopDriver;
	private MobileDriverEngine mobileDriverEngine;
		
	public String getDriverFolderPath() {
		return ATS.getDriversFolderPath().toFile().getAbsolutePath();
	}

	//--------------------------------------------------------------------------------------------------------------

	public static void killAllDrivers() {
		/*Predicate<ProcessHandle> fullPredicate = p -> p.info().command().isPresent();
		Predicate<ProcessHandle> desktop  =  p -> p.info().command().get().contains(DESKTOP_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> chrome  = p -> p.info().command().get().contains(CHROME_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> opera  =  p -> p.info().command().get().contains(OPERA_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> edge  =  p -> p.info().command().get().contains(EDGE_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> firefox  =  p -> p.info().command().get().contains(FIREFOX_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> ie  =  p -> p.info().command().get().contains(IE_DRIVER_FILE_NAME);

		try {
			ProcessHandle
			.allProcesses()
			.parallel()
			.filter(fullPredicate)
			.filter(chrome.or(edge).or(firefox).or(opera).or(ie).or(desktop))
			.forEach(p2 -> {
				p2.children().parallel().forEach(p3 -> p3.destroy());
				p2.destroy();
			});
		}catch (Exception e) {}*/
	}

	//--------------------------------------------------------------------------------------------------------------

	public DriverProcess getDesktopDriver(ActionStatus status) {
		if(desktopDriver == null) {
			desktopDriver = getDriverProcess(status, DESKTOP, null, DESKTOP_DRIVER_FILE_NAME);
		}
		return desktopDriver;
	}

	public void processTerminated(DriverProcess dp) {
		driversProcess.remove(dp);
	}

	public IDriverEngine getDriverEngine(Channel channel, ActionStatus status, DesktopDriver desktopDriver) {

		final String application = channel.getApplication();
		final ApplicationProperties props = ATS.getApplicationProperties(application);

		final String appName = props.getName().toLowerCase();
		final String driverName = props.getDriver();

		channel.setAtsManager(ATS);
		
		DriverProcess driverProcess = null;

		if(CHROME_BROWSER.equals(appName)) {
			driverProcess = getDriverProcess(status, appName, driverName, CHROME_DRIVER_FILE_NAME);
			if(status.isPassed()) {
				return new ChromeDriverEngine(
						channel, 
						status, 
						driverProcess, 
						desktopDriver, 
						props);	
			}else {
				return null;
			}
		}else if(EDGE_BROWSER.equals(appName)) {
			driverProcess = getDriverProcess(status, appName, driverName, EDGE_DRIVER_FILE_NAME + "-" + desktopDriver.getOsBuildVersion());
			if(status.isPassed()) {
				return new EdgeDriverEngine(
						channel, 
						status, 
						driverProcess, 
						desktopDriver, 
						props);	
			}else {
				return null;
			}
		}else if(MSEDGE_BROWSER.equals(appName)) {
			driverProcess = getDriverProcess(status, appName, driverName, MSEDGE_DRIVER_FILE_NAME);
			if(status.isPassed()) {
				return new MsEdgeDriverEngine(
						channel, 
						status,
						MSEDGE_BROWSER,
						driverProcess, 
						desktopDriver, 
						props);	
			}else {
				return null;
			}
		}else if(OPERA_BROWSER.equals(appName)) {
			driverProcess = getDriverProcess(status, appName, driverName, OPERA_DRIVER_FILE_NAME);
			if(status.isPassed()) {
				return new OperaDriverEngine(
						channel, 
						status, 
						driverProcess, 
						desktopDriver, 
						props);	
			}else {
				return null;
			}
		}else if(FIREFOX_BROWSER.equals(appName)) {
			driverProcess = getDriverProcess(status, appName, driverName, FIREFOX_DRIVER_FILE_NAME);
			if(status.isPassed()) {
				return new FirefoxDriverEngine(
						channel, 
						status, 
						driverProcess, 
						desktopDriver, 
						props);	
			}else {
				return null;
			}
		}else if(IE_BROWSER.equals(appName)) {
			driverProcess = getDriverProcess(status, appName, driverName, IE_DRIVER_FILE_NAME);
			if(status.isPassed()) {
				return new IEDriverEngine(
						channel, 
						status, 
						driverProcess, 
						desktopDriver, 
						props);	
			}else {
				return null;
			}
		}else if(CHROMIUM_BROWSER.equals(appName)) {
			if(driverName != null && props.getUri() != null) {
				driverProcess = getDriverProcess(status, appName, driverName, null);
				if(status.isPassed()) {
					return new ChromiumDriverEngine(
							channel, 
							status, 
							props.getName(), 
							driverProcess, 
							desktopDriver, 
							props);
				}else {
					return null;
				}
			}else {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "missing Chromium properties ('path' and 'driver') in .atsProperties file");
				return null;
			}
		}else if(JX_BROWSER.equals(appName)) {
			if(driverName != null && props.getUri() != null) {

				final JxDriverEngine jxEngine = new JxDriverEngine(this, appName, ATS.getDriversFolderPath(), driverName, channel, status, desktopDriver, props);
				if(status.isPassed()) {
					return jxEngine;
				}

				jxEngine.close(false);
				return null;

			}else {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "missing JxBrowser properties ('path' and 'driver') in .atsProperties file");
				return null;
			}

		}else if(props.isMobile() || application.startsWith(MOBILE + "://")){
			mobileDriverEngine = new MobileDriverEngine(channel, status, application, desktopDriver, props);
			return mobileDriverEngine;			
		}else if(props.isApi() || application.startsWith(HTTP + "://") || application.startsWith(HTTPS + "://")) {	
			return new ApiDriverEngine(channel, status, application, desktopDriver, props);
		}else if(DESKTOP_EXPLORER.equals(appName)) {
			return new ExplorerDriverEngine(channel, status, desktopDriver, props);
		}

		return new DesktopDriverEngine(channel, status, application, desktopDriver, props);
	}

	private String getDriverName(String driverName, String defaultName) {
		if(driverName == null) {
			driverName = defaultName;
		}
		return driverName + ".exe";
	}

	public DriverProcess getDriverProcess(ActionStatus status, String name, String driverName, String defaultDriverName) {

		driverName = getDriverName(driverName, defaultDriverName);

		for (DriverProcess proc : driversProcess) {
			if(proc.getName().equals(name)) {
				return proc;
			}
		}

		final DriverProcess proc = new DriverProcess(status, name, this, ATS.getDriversFolderPath(), driverName, null);
		if(status.isPassed()) {
			driversProcess.add(proc);
			return proc;
		}

		return null;
	}

	public void tearDown(){

		//while(driversProcess.size() > 0) {
		//	driversProcess.remove(0).close();
		//}
		
		if(desktopDriver != null) {
			desktopDriver.close(false);
			desktopDriver = null;
		}

		if(mobileDriverEngine != null){
			mobileDriverEngine.tearDown();
			mobileDriverEngine = null;
		}
	}
}