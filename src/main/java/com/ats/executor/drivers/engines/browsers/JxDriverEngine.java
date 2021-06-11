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
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.DesktopDriverEngine;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.tools.Utils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JxDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 150;
	private final static int DEFAULT_PROPERTY_WAIT = 200;

	private final static String JX_WINDOW_SIZE = "var result=[window.screenX+7.0001, window.screenY+8.0001, window.innerWidth+0.0001, window.innerHeight+0.0001];";

	private ProcessHandle jxProcess;

	@SuppressWarnings("unchecked")
	public JxDriverEngine(DriverManager driverManager, Path driversPath, String driverName, Channel channel, ActionStatus status, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, desktopDriver, props.getUri(), props, DEFAULT_WAIT, DEFAULT_PROPERTY_WAIT);

		browserArguments = new BrowserArgumentsParser(channel.getArguments(), props, DriverManager.JX_BROWSER, applicationPath);
		
		final ProcessBuilder builder = new ProcessBuilder(getApplicationPath());
		
		try {
			builder.start();
			status.setPassed(true);
		} catch (IOException e1) {
			status.setException(ActionStatus.CHANNEL_START_ERROR, e1);
		}

		this.setDriverProcess(driverManager.getDriverProcess(status, DriverManager.JX_BROWSER, driverName, null, null));

		if(status.isPassed()) {
			try {
				DesiredCapabilities capabilities = new DesiredCapabilities();

				ChromeOptions options = new ChromeOptions();
				options.setExperimentalOption("debuggerAddress", "localhost:9222");
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);

				driver = new RemoteWebDriver(getDriverProcess().getDriverServerUrl(), capabilities);

			} catch (Exception e) {
				close(false);
				status.setError(ActionStatus.CHANNEL_START_ERROR, e.getMessage());
			}

			if(driver != null) {
				status.setPassed(true);

				actions = new Actions(driver);

				driver.manage().timeouts().setScriptTimeout(50, TimeUnit.SECONDS);
				driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

				String applicationVersion = "N/A";
				String driverVersion = "N/A";

				Map<String, ?> infos = driver.getCapabilities().asMap();
				for (Map.Entry<String, ?> entry : infos.entrySet()){
					if("browserVersion".equals(entry.getKey()) || "version".equals(entry.getKey())){
						applicationVersion = entry.getValue().toString();
					}else if("chrome".equals(entry.getKey())) {
						Map<String, String> chromeData = (Map<String, String>) entry.getValue();
						driverVersion = chromeData.get("chromedriverVersion");
						if(driverVersion != null) {
							driverVersion = driverVersion.replaceFirst("\\(.*\\)", "").trim();
						}
					}
				}

				final String titleUid = UUID.randomUUID().toString();
				String atsStartPageUri = null;
				try {
					final File tempHtml = File.createTempFile("ats_", ".html");
					tempHtml.deleteOnExit();

					Files.write(tempHtml.toPath(), Utils.getAtsBrowserContent(titleUid, channel.getApplication(), applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait(), getPropertyWait(), 20, 20, 20, 20, 20, getDesktopDriver(), null));
					atsStartPageUri = tempHtml.toURI().toString();
				} catch (IOException e) {}

				
				final DesktopWindow window = getJxBrowserWindow(atsStartPageUri, props.getTitle());
				if(window != null) {

					int maxTry = 10;
					while(maxTry > 0) {
						Optional<ProcessHandle> opt = ProcessHandle.allProcesses().filter(p -> p.pid() == window.getPid()).findFirst();
						if(opt.isPresent()) {
							maxTry = 0;
							jxProcess = opt.get();
							
							desktopDriver.setEngine(new DesktopDriverEngine(channel, window));
							channel.setApplicationData(
									"windows",
									applicationVersion,
									driverVersion,
									window.getPid(),
									window.getHandle());							
							
							setSize(channel.getDimension().getSize());
							setPosition(channel.getDimension().getPoint());
							
							return;
							
						}else {
							maxTry--;
						}
					}

					status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to find JxBrowser running process");
				}else {
					status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to find JxBrowser main window");
				}
			}
		}
	}

	private DesktopWindow getJxBrowserWindow(String atsStartPage, String title) {

		int maxTry = 10;
		while(maxTry > 0) {
			
			driver.get(atsStartPage);
			
			final DesktopWindow window = desktopDriver.getWindowByTitle("jx", title);
			if(window != null && window.getPid() > 0) {
				return window;
			}else {
				channel.sleep(300);
				maxTry--;
			}
		}
		return null;
	}

	@Override
	protected void setPosition(Point pt) {
		getDesktopDriver().moveWindow(channel, pt);
	}

	@Override
	protected void setSize(Dimension size) {
		getDesktopDriver().resizeWindow(channel, size);
	}

	@Override
	public void updateDimensions() {

		final DesktopWindow win = getDesktopDriver().getWindowByHandle(channel.getHandle(desktopDriver));
		final Double channelX = win.getX();
		final Double channelY = win.getY();

		channel.getDimension().update(channelX, channelY, win.getWidth(), win.getHeight());

		@SuppressWarnings("unchecked")
		final ArrayList<Double> response = (ArrayList<Double>) runJavaScript(JX_WINDOW_SIZE);
		channel.getSubDimension().update(response.get(0) - channelX, response.get(1) - channelY, response.get(2), response.get(3));
	}

	@Override
	public void close(boolean keepRunning) {

		try {

			jxProcess.destroyForcibly();
			driver.close();
			getDriverProcess().close(keepRunning);

		}catch(Exception e) {}
	}
}
