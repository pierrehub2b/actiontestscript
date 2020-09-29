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
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ats.driver.ApplicationProperties;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirefoxDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 250;
	private final static int DEFAULT_PROPERTY_WAIT = 250;

	private OkHttpClient client;

	private static final String BINARY = "firefox_binary";

	public FirefoxDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver windowsDriver, ApplicationProperties props) {
		super(channel, driverProcess, windowsDriver, props, DEFAULT_WAIT, DEFAULT_PROPERTY_WAIT);

		final DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability("acceptSslCerts ", true);
		capabilities.setCapability("acceptInsecureCerts ", true);
		capabilities.setCapability("security.fileuri.strict_origin_policy", false);
		capabilities.setCapability("app.update.disabledForTesting", true);
		//options.setCapability("marionnette ", true);
		//options.setCapability("nativeEvents", false);

		if(applicationPath != null) {
			capabilities.setCapability(BINARY, applicationPath);
		}

		if(props.getUserDataDir() != null) {
			final FirefoxProfile profile = new FirefoxProfile(new File(props.getUserDataDir()));
			capabilities.setCapability(FirefoxDriver.PROFILE, profile);
		}
		
		if(props.getOptions() != null) {
			//TODO
		}

		final Builder builder = new Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.writeTimeout(20, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.cache(null);

		client = builder.build();

		launchDriver(status, capabilities, props.getUserDataDir());
	}

	@Override
	public void close(boolean keepRunning) {
		if(!keepRunning) {
			getDesktopDriver().closeWindows(channel.getProcessId());
		}
		getDriverProcess().close(keepRunning);
		/*if(driver != null){
			driver.quit();

		}*/

		//super.close();
	}

	@Override
	protected boolean switchToWindowHandle(String handle) {
		if(super.switchToWindowHandle(handle)) {
			actionWait();
			return true;
		}
		return false;
	}

	@Override
	protected void switchToFrame(WebElement we) {
		super.switchToFrame(we);
		actionWait();
	}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
		middleClickSimulation(status, position, element);
	}

	@Override
	protected void move(FoundElement element, int offsetX, int offsetY) {

		//-----------------------------------------------------------------------------------------------------
		// I don't know why, but we have to do that to make click action reliable with Firefox and geckodriver
		//-----------------------------------------------------------------------------------------------------

		element.getValue().getTagName();
		element.getValue().getRect();

		//--------------------------------------------------------------------------------------------

		final JsonArray actionList = new JsonArray();

		actionList.add(getMoveAction(getElementOrigin(element.getId()), offsetX, offsetY));
		executeRequestActions(getElementAction(actionList));
	}

	@Override
	protected void click(FoundElement element, int offsetX, int offsetY) {

		move(element, offsetX, offsetY);
		channel.sleep(30);

		final JsonObject origin = getElementOrigin(element.getId());

		final JsonArray actionList = new JsonArray();
		actionList.add(getMouseClickAction(origin, "pointerDown"));
		actionList.add(getMouseClickAction(origin, "pointerUp"));

		executeRequestActions(getElementAction(actionList));
	}

	@Override
	public void doubleClick() {
		final JsonArray actionList = new JsonArray();
		actionList.add(getMouseClickAction("pointerDown"));
		actionList.add(getMouseClickAction("pointerUp"));
		actionList.add(getMouseClickAction("pointerDown"));
		actionList.add(getMouseClickAction("pointerUp"));	
		executeRequestActions(getElementAction(actionList));
	}

	@Override
	protected void loadUrl(String url) {
		final JsonObject parameters = new JsonObject();
		parameters.addProperty("url", url);

		executeRequest(parameters, "url");
	}

	private JsonObject getMouseClickAction(JsonObject origin, String type) {
		final JsonObject action = new JsonObject();
		action.addProperty("duration", 20);
		action.addProperty("type", type);
		action.addProperty("button", 0);
		action.add("origin", origin);
		return action;
	}

	private JsonObject getMouseClickAction(String type) {
		final JsonObject action = new JsonObject();
		action.addProperty("duration", 20);
		action.addProperty("type", type);
		action.addProperty("button", 0);
		return action;
	}

	private JsonObject getMoveAction(JsonObject origin, int offsetX, int offsetY) {
		final JsonObject action = new JsonObject();
		action.addProperty("duration", 150);
		action.addProperty("x", offsetX);
		action.addProperty("y", offsetY);
		action.addProperty("type", "pointerMove");
		action.add("origin", origin);
		return action;
	}

	private JsonObject getElementOrigin(String elemId) {
		final JsonObject origin = new JsonObject();
		origin.addProperty("ELEMENT", elemId);
		origin.addProperty(WEB_ELEMENT_REF, elemId);
		return origin;
	}

	private JsonObject getElementAction(JsonArray actionList) {

		final JsonObject parameters = new JsonObject();
		parameters.addProperty("pointerType", "mouse");

		final JsonObject actions = new JsonObject();

		actions.addProperty("id", "default mouse");
		actions.addProperty("type", "pointer");

		actions.add("parameters", parameters);
		actions.add("actions", actionList);

		final JsonArray chainedAction = new JsonArray();
		chainedAction.add(actions);

		final JsonObject postData = new JsonObject();
		postData.add("actions", chainedAction);

		return postData;
	}

	private void executeRequestActions(JsonObject action) {
		executeRequest(action, "actions");
	}

	private void executeRequest(JsonObject action, String type) {

		final Request request = new Request.Builder()
				.url(driverSession + "/" + type)
				.addHeader("Content-Type", "application/json")
				.post(RequestBody.create(null, action.toString()))
				.build();

		try {
			final Response response = client.newCall(request).execute();
			response.close();
		} catch (IOException e) {
			throw new WebDriverException("Geckodriver hangup issue after mouse move action (try to raise up 'actionWait' value in ATS properties for firefox)");
		}
	}
}
