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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirectionData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FirefoxDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 200;

	private final String WEB_ELEMENT_REF = "element-6066-11e4-a52e-4f735466cecf";
	private java.net.URI driverSessionUri;
	private RequestConfig requestConfig;

	public FirefoxDriverEngine(Channel channel, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.FIREFOX_BROWSER, driverProcess, windowsDriver, ats, DEFAULT_WAIT);

		initElementY = 4.0;

		DesiredCapabilities cap = new DesiredCapabilities();
		
		FirefoxOptions options = new FirefoxOptions();
		options.setCapability(FirefoxDriver.MARIONETTE, true);
		options.setCapability("acceptSslCerts ","true");
		options.setCapability("acceptInsecureCerts ","true");
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);

		if(applicationPath != null) {
			options.setBinary(applicationPath);
		}

		cap.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);

		launchDriver(cap);

		requestConfig = RequestConfig.custom()
				.setConnectTimeout(20 * 1000)
				.setConnectionRequestTimeout(20 * 1000)
				.setSocketTimeout(20 * 1000).build();
		try {
			driverSessionUri = new URI(driverProcess.getDriverServerUrl() + "/session/" + driver.getSessionId().toString() + "/actions");
		} catch (URISyntaxException e) {}

	}

	@Override
	protected void switchToWindowHandle(String handle) {
		channel.sleep(150);
		super.switchToWindowHandle(handle);
		channel.sleep(150);
	}

	@Override
	protected void switchToFrame(WebElement we) {
		channel.sleep(150);
		super.switchToFrame(we);
		channel.sleep(150);
	}

	@Override
	public TestBound[] getDimensions() {
		TestBound[] dimension = super.getDimensions();
		dimension[1].setY(dimension[1].getY() + 2);
		return dimension;
	}

	@Override
	protected int getDirectionValue(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2) {
		int offset = super.getDirectionValue(value, direction, cart1, cart2);
		offset -= value/2;
		return offset;
	}

	@Override
	protected int getNoDirectionValue(int value) {
		return 0;
	}

	@Override
	public void middleClick(ActionStatus status, TestElement element) {
		middleClickSimulation(status, element);
	}

	@Override
	public void sendTextData(ActionStatus status, FoundElement element, ArrayList<SendKeyData> textActionList, boolean clear) {
		if(clear) {
			clearText(status, element.getValue());
		}
		for(SendKeyData sequence : textActionList) {
			element.getValue().sendKeys(sequence.getSequenceFirefox());
		}
	}

	@Override
	protected void move(WebElement element, int offsetX, int offsetY) {

		JsonObject postData = getElementAction((RemoteWebElement)element,offsetX, offsetY);
		StringEntity postDataEntity = null;
		try {
			postDataEntity = new StringEntity(postData.toString());
		} catch (UnsupportedEncodingException e) {
			return;
		}

		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost request = new HttpPost(driverSessionUri);
		request.addHeader("content-type", "application/json");

		try {

			request.setEntity(postDataEntity);
			httpClient.execute(request);

		} catch (SocketTimeoutException e) {

			closeDriver();
			throw new WebDriverException("Geckodriver hangup issue after mouse move action (try to raise up 'actionWait' value in ATS properties for firefox)");

		} catch (IOException e) {

		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {}
		}
	}

	private JsonObject getElementAction(RemoteWebElement elem, int offsetX, int offsetY) {

		String elemId = elem.getId();

		JsonObject origin = new JsonObject();
		origin.addProperty("ELEMENT", elemId);
		origin.addProperty(WEB_ELEMENT_REF, elemId);

		JsonObject action = new JsonObject();
		action.addProperty("duration", 200);
		action.addProperty("x", offsetX);
		action.addProperty("y", offsetY);
		action.addProperty("type", "pointerMove");
		action.add("origin", origin);

		JsonArray actionList = new JsonArray();
		actionList.add(action);

		JsonObject parameters = new JsonObject();
		parameters.addProperty("pointerType", "mouse");

		JsonObject actions = new JsonObject();
		actions.addProperty("id", "default mouse");
		actions.addProperty("type", "pointer");
		actions.add("parameters", parameters);

		actions.add("actions", actionList);

		JsonArray chainedAction = new JsonArray();
		chainedAction.add(actions);

		JsonObject postData = new JsonObject();
		postData.add("actions", chainedAction);

		return postData;
	}
}
