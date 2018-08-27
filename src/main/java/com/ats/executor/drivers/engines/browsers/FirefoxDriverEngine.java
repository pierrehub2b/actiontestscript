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

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
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

	private final static int DEFAULT_WAIT = 150;

	public FirefoxDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.FIREFOX_BROWSER, driverProcess, windowsDriver, ats, DEFAULT_WAIT);

		this.autoScrollElement = JS_AUTO_SCROLL_MOZ;

		FirefoxOptions options = new FirefoxOptions();
		options.setCapability("marionnette ","true");
		options.setCapability("acceptSslCerts ","true");
		options.setCapability("acceptInsecureCerts ","true");
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

		if(applicationPath != null) {
			options.setBinary(applicationPath);
		}

		launchDriver(status, options);
	}

	@Override
	protected void switchToWindowHandle(String handle) {
		super.switchToWindowHandle(handle);
		actionWait();
	}

	@Override
	protected void switchToFrame(WebElement we) {
		super.switchToFrame(we);
		actionWait();
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
	protected void move(FoundElement element, int offsetX, int offsetY) {

		forceScrollElement(element);

		JsonArray actionList = new JsonArray();
		actionList.add(getMoveAction(((RemoteWebElement)element.getValue()).getId(), offsetX, offsetY));

		executeAction(getElementAction(actionList));
	}

	/*@Override
	protected void click() {
		JsonArray actionList = new JsonArray();
		actionList.add(getMouseClickAction("pointerDown"));
		actionList.add(getMouseClickAction("pointerUp"));

		executeAction(getElementAction(actionList));
	}

	private JsonObject getMouseClickAction(String type) {
		JsonObject action = new JsonObject();
		action.addProperty("type", type);
		action.addProperty("button", 0);
		return action;
	}*/

	private JsonObject getMoveAction(String elemId, int offsetX, int offsetY) {
		JsonObject action = new JsonObject();
		action.addProperty("duration", 200);
		action.addProperty("x", offsetX);
		action.addProperty("y", offsetY);
		action.addProperty("type", "pointerMove");
		action.add("origin", getElementOrigin(elemId));
		return action;
	}

	private JsonObject getElementOrigin(String elemId) {
		JsonObject origin = new JsonObject();
		origin.addProperty("ELEMENT", elemId);
		origin.addProperty(WEB_ELEMENT_REF, elemId);
		return origin;
	}

	private JsonObject getElementAction(JsonArray actionList) {

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

	private void executeAction(JsonObject action) {

		StringEntity postDataEntity = null;
		try {
			postDataEntity = new StringEntity(action.toString());
		} catch (UnsupportedEncodingException e) {
			return;
		}

		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost request = new HttpPost(driverSession + "/actions");
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
}
