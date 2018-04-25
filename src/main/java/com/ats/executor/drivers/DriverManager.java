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

import java.util.function.Predicate;

import com.ats.driver.AtsManager;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.executor.drivers.engines.WindowsDriverEngine;
import com.ats.executor.drivers.engines.browsers.ChromeDriverEngine;
import com.ats.executor.drivers.engines.browsers.EdgeDriverEngine;
import com.ats.executor.drivers.engines.browsers.FirefoxDriverEngine;
import com.ats.executor.drivers.engines.browsers.IEDriverEngine;
import com.ats.executor.drivers.engines.browsers.OperaDriverEngine;
import com.ats.tools.Utils;

public class DriverManager {

	public static final String CHROME_BROWSER = "chrome";
	public static final String FIREFOX_BROWSER = "firefox";
	public static final String EDGE_BROWSER = "edge";
	public static final String OPERA_BROWSER = "opera";
	public static final String SAFARI_BROWSER = "safari";
	
	public static final String WINDOWS_DESKTOP_FILE_NAME = "Windows.Desktop.Driver.exe";
	public static final String CHROME_DRIVER_FILE_NAME = "chromedriver.exe";
	public static final String MICROSOFT_WEBDRIVER_FILE_NAME = "MicrosoftWebDriver";
	public static final String OPERA_WEBDRIVER_FILE_NAME = "operadriver.exe";
	public static final String FIREFOX_DRIVER_FILE_NAME = "geckodriver.exe";

	private DriverProcess winDesktopDriver;
	private DriverProcess chromeDriver;
	private DriverProcess edgeDriver;
	private DriverProcess operaDriver;
	private DriverProcess firefoxDriver;
	private DriverProcess ieDriver;
	
	private AtsManager ats;

	public DriverManager() {
		this.ats = new AtsManager();
	}
	
	public String getDriverFolderPath() {
		return ats.getDriversFolderPath().toFile().getAbsolutePath();
	}
	
	public TestBound getApplicationBound() {
		return ats.getApplicationBound();
	}
	
	public int getMaxTry() {
		return ats.getMaxTrySearch();
	}
	
	//--------------------------------------------------------------------------------------------------------------
	
	public static void killAllDrivers() {
		Predicate<ProcessHandle> fullPredicate = p -> p.info().command().isPresent();
		Predicate<ProcessHandle> chrome  = p -> p.info().command().get().contains(CHROME_DRIVER_FILE_NAME);
		Predicate<ProcessHandle> opera  =  p -> p.info().command().get().contains(OPERA_WEBDRIVER_FILE_NAME);
		Predicate<ProcessHandle> desktop  =  p -> p.info().command().get().contains(WINDOWS_DESKTOP_FILE_NAME);
		Predicate<ProcessHandle> edge  =  p -> p.info().command().get().contains(MICROSOFT_WEBDRIVER_FILE_NAME);
		Predicate<ProcessHandle> firefox  =  p -> p.info().command().get().contains(FIREFOX_DRIVER_FILE_NAME);
		
		ProcessHandle
		.allProcesses()
		.parallel()
		.filter(fullPredicate)
		.filter(chrome.or(edge).or(firefox).or(opera).or(desktop))
		.forEach(p2 -> {
			p2.children().parallel().forEach(p3 -> p3.destroy());
			p2.destroy();
		});
	}
	
	//--------------------------------------------------------------------------------------------------------------
	
	public DriverProcess getWinDesktopDriver() {
		if(winDesktopDriver == null){
			winDesktopDriver = new DriverProcess(this, ats.getDriversFolderPath(), WINDOWS_DESKTOP_FILE_NAME, null);
		}
		return winDesktopDriver;
	}
	
	public void processTerminated(DriverProcess dp) {
		if(dp.equals(operaDriver)) {
			operaDriver = null;
		}else if(dp.equals(firefoxDriver)) {
			firefoxDriver = null;
		}else if(dp.equals(edgeDriver)) {
			edgeDriver = null;
		}else if(dp.equals(chromeDriver)) {
			chromeDriver = null;
		}
	}
	
	public IDriverEngine getDriverEngine(Channel channel, String application, WindowsDesktopDriver desktopDriver) {
		switch(application.toLowerCase()) {
		case CHROME_BROWSER :
			return new ChromeDriverEngine(channel, getChromeDriver(), desktopDriver, ats);
		case EDGE_BROWSER :
			return new EdgeDriverEngine(channel, getEdgeDriver(), desktopDriver, ats);
		case OPERA_BROWSER :
			return new OperaDriverEngine(channel, getOperaDriver(), desktopDriver, ats);
		case FIREFOX_BROWSER :
			return new FirefoxDriverEngine(channel, getFirefoxDriver(), desktopDriver, ats);
		case "ie" :
			return new IEDriverEngine(channel, getIEDriver(), desktopDriver, ats);
		default :
			return new WindowsDriverEngine(channel, application, desktopDriver, ats);
		}
	}
	
	public DriverProcess getFirefoxDriver() {
		if(firefoxDriver == null){
			firefoxDriver = new DriverProcess(this, ats.getDriversFolderPath(), FIREFOX_DRIVER_FILE_NAME, null);
		}
		return firefoxDriver;
	}

	public DriverProcess getChromeDriver() {
		if(chromeDriver == null){
			chromeDriver = new DriverProcess(this, ats.getDriversFolderPath(), CHROME_DRIVER_FILE_NAME, null);
		}
		return chromeDriver;
	}

	public DriverProcess getIEDriver() {
		if(ieDriver == null){
			ieDriver = new DriverProcess(this, ats.getDriversFolderPath(), "IEDriverServer.exe", null);
		}
		return ieDriver;
	}
	
	
	public DriverProcess getEdgeDriver() {
		if(edgeDriver == null){
			edgeDriver = new DriverProcess(this, ats.getDriversFolderPath(), MICROSOFT_WEBDRIVER_FILE_NAME + "-" + Utils.getWindowsBuildVersion() + ".exe", null);
		}
		return edgeDriver;
	}

	public DriverProcess getOperaDriver() {
		if(operaDriver == null){
			operaDriver = new DriverProcess(this, ats.getDriversFolderPath(), OPERA_WEBDRIVER_FILE_NAME, null);
		}
		return operaDriver;
	}

	public void tearDown(){
		
		if(winDesktopDriver != null){
			winDesktopDriver.close();
		}

		if(chromeDriver != null){
			chromeDriver.close();
		}
		
		if(edgeDriver != null){
			edgeDriver.close();
		}
		
		if(operaDriver != null){
			operaDriver.close();
		}
		
		if(firefoxDriver != null){
			firefoxDriver.close();
		}
	}
}