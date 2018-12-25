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

package com.ats.executor.drivers.engines;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.element.AtsBaseElement;
import com.ats.element.AtsMobileElement;
import com.ats.element.FoundElement;
import com.ats.element.MobileTestElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MobileDriverEngine extends DriverEngineAbstract implements IDriverEngine{

	private final static String DRIVER = "driver";
	private final static String APP = "app";
	private final static String START = "start";
	private final static String STOP = "stop";
	private final static String SWITCH = "switch";
	private final static String CAPTURE = "capture";
	private final static String ELEMENT = "element";
	private final static String TAP = "tap";
	private final static String INPUT = "input";
	private final static String SWIPE = "swipe";

	private JsonParser parser = new JsonParser();
	private Gson gson = new Gson();

	private AtsMobileElement rootElement;
	private AtsMobileElement capturedElement;

	private MobileTestElement testElement;

	public MobileDriverEngine(Channel channel, ActionStatus status, String app, DesktopDriver desktopDriver, ApplicationProperties props) {

		super(channel, desktopDriver, app, props, 0, 60);

		if(applicationPath == null) {
			applicationPath = app;
		}

		int start = applicationPath.indexOf("://");
		if(start > -1) {
			applicationPath = applicationPath.substring(start + 3);
		}

		String[] appData = applicationPath.split("/");
		if(appData.length > 1) {

			String endPoint = appData[0];

			applicationPath = "http://" + endPoint;
			application = appData[1];

			JsonObject response = executeRequest(DRIVER, START);

			final String systemName = response.get("systemName").getAsString();
			final String os = response.get("os").getAsString();
			final String driverVersion = response.get("driverVersion").getAsString();

			final double channelWidth = response.get("channelWidth").getAsDouble();
			final double channelHeight = response.get("channelHeight").getAsDouble();

			final double channelX = response.get("channelX").getAsDouble();
			final double channelY = response.get("channelY").getAsDouble();

			final double deviceWidth = response.get("deviceWidth").getAsDouble();
			final double deviceHeight = response.get("deviceHeight").getAsDouble();

			final int screenCapturePort = response.get("screenCapturePort").getAsInt();

			channel.setDimensions(new TestBound(0D, 0D, deviceWidth, deviceHeight), new TestBound(channelX, channelY, channelWidth, channelHeight));

			response = executeRequest(APP, START, application);
			if(response != null) {
				if(response.get("status").getAsInt() == 0) {
					final String base64 = response.get("icon").getAsString();
					byte[] icon = new byte[0];
					if(base64.length() > 0) {
						try {
							icon = Base64.getDecoder().decode(base64);
						}catch(Exception e) {}
					}

					String[] endPointData = endPoint.split(":");

					channel.setApplicationData(os, systemName, driverVersion, -1, icon, endPointData[0] + ":" + screenCapturePort);

					refreshElementMapLocation(channel);
				}else {
					status.setCode(ActionStatus.CHANNEL_START_ERROR);
					status.setMessage(response.get("status").getAsString());
					status.setPassed(false);
				}
			}else {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("unable to connect to : " + this.application);
				status.setPassed(false);
			}
		}
	}

	@Override
	public void refreshElementMapLocation(Channel channel) {
		rootElement = gson.fromJson(executeRequest(CAPTURE), AtsMobileElement.class);
	}

	@Override
	public void close() {
		executeRequest(APP, STOP, application);
	}

	public void tearDown() {
		executeRequest(DRIVER, STOP);
	}

	@Override
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y) {
		capturedElement = gson.fromJson(executeRequest(CAPTURE), AtsMobileElement.class);
		
		ArrayList<AtsMobileElement> listElements = new ArrayList<AtsMobileElement>();
		
		loadList(capturedElement, listElements);
		
		final int mouseX = (int)(channel.getSubDimension().getX() + x);
		final int mouseY = (int)(channel.getSubDimension().getY() + y);

		AtsMobileElement element = capturedElement;
		
		for (int i=0; i<listElements.size(); i++) {
			AtsMobileElement child = listElements.get(i);
			if(child.getRect().contains(new Point(mouseX, mouseY)) && element.getRect().contains(child.getRect())){
				element = child;
			}
		}
	
		return element.getFoundElement();
	}
	
	private void loadList(AtsMobileElement element, ArrayList<AtsMobileElement> list) {
		for (int i=0; i<element.getChildren().length; i++) {
			AtsMobileElement child = element.getChildren()[i];
			
			list.add(child);
			loadList(child, list);
		}
	}

	@Override
	public void loadParents(FoundElement element) {
		final AtsMobileElement atsElement = getCapturedElementById(element.getId());
		if(atsElement != null) {
			FoundElement currentParent = null;
			AtsMobileElement parent = atsElement.getParent();
			if(parent != null) {

				element.setParent(parent.getFoundElement());
				currentParent = element.getParent();

				parent = parent.getParent();
				while (parent != null && !parent.isRoot()) {
					currentParent.setParent(parent.getFoundElement());
					currentParent = currentParent.getParent();

					parent = parent.getParent();
				}
			}
		}
	}

	@Override
	public CalculatedProperty[] getAttributes(FoundElement element) {
		final AtsMobileElement atsElement = getCapturedElementById(element.getId());
		if(atsElement != null) {
			return atsElement.getMobileAttributes();
		}
		return new CalculatedProperty[0];
	}

	private AtsMobileElement getCapturedElementById(String id) {
		return getElementById(capturedElement, id);
	}

	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		final AtsMobileElement atsElement = getElementById(element.getId());
		if(atsElement != null) {
			return atsElement.getAttribute(attributeName);
		}
		return null;
	}

	@Override
	public ArrayList<FoundElement> findElements(Channel channel, boolean sysComp, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> searchPredicate) {

		final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();

		if(testObject.getParent() == null) {
			refreshElementMapLocation(channel);
			loadElementsByTag(rootElement, tagName, list);
		}else {
			loadElementsByTag(getElementById(testObject.getParent().getWebElementId()), tagName, list);
		}

		return list.parallelStream().filter(searchPredicate).map(e -> new FoundElement(e)).collect(Collectors.toCollection(ArrayList::new));
	}

	private void loadElementsByTag(AtsMobileElement root, String tag, List<AtsMobileElement> list) {

		if(root.checkTag(tag)) {
			list.add(root);
		}

		for(AtsMobileElement child : root.getChildren()) {
			loadElementsByTag(child, tag, list);
		}
	}

	//-------------------------------------------------------------------------------------------------------------

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, boolean hold) {

		final Rectangle rect = element.getRectangle();
		final int mouseX = (int)(getOffsetX(rect, position));
		final int mouseY = (int)(getOffsetY(rect, position));

		if(hold) {
			testElement = new MobileTestElement(element.getElementId(), mouseX, mouseY);
		}else {
			executeRequest(ELEMENT, element.getElementId(), TAP, mouseX + "", mouseY + "");
		}
	}	

	@Override
	public void moveByOffset(int hDirection, int vDirection) {
		executeRequest(ELEMENT, testElement.getId(), SWIPE, testElement.getOffsetX() + "", testElement.getOffsetY() + "", hDirection + "", + vDirection + "");
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			executeRequest(ELEMENT, element.getFoundElement().getId(), INPUT, sequence.getSequenceDesktop());
		}
	}

	@Override
	public WebElement getRootElement() {

		refreshElementMapLocation(channel);

		RemoteWebElement elem = new RemoteWebElement();
		elem.setId(rootElement.getId());

		return elem;
	}
	
	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return new CalculatedProperty[0];
	}

	//----------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void api(ActionStatus status, ActionApi api) {}
	
	@Override
	public void switchWindow(int index) {}

	@Override
	public void closeWindow(ActionStatus status, int index) {}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		return null;
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {}

	@Override
	public void waitAfterAction() {}

	@Override
	public void scroll(FoundElement foundElement, int delta) {}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {}

	@Override
	public void clearText(ActionStatus status, FoundElement foundElement) {}

	@Override
	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {}

	@Override
	public void forceScrollElement(FoundElement value) {}

	@Override
	public void keyDown(Keys key) {}

	@Override
	public void keyUp(Keys key) {}

	@Override
	public void drop() {}

	@Override
	public void doubleClick() {}

	@Override
	public void rightClick() {}

	@Override
	public Alert switchToAlert() {
		return null;
	}

	@Override
	public void switchToDefaultContent() {}

	@Override
	public boolean setWindowToFront() {
		executeRequest(APP, SWITCH, application);
		return true;
	}

	@Override
	public void switchToFrameId(String id) {}

	@Override
	public void updateDimensions(Channel cnl) {}

	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------

	private AtsMobileElement getElementById(String id) {
		return getElementById(rootElement, id);
	}

	private AtsMobileElement getElementById(AtsMobileElement root, String id) {

		if(root.getId().equals(id)) {
			return root;
		}

		for(AtsMobileElement elem : root.getChildren()) {
			elem.setParent(root);
			AtsMobileElement found = getElementById(elem, id);
			if(found != null) {
				return found;
			}
		}
		return null;
	}

	protected JsonObject executeRequest(String ... parameters) {

		try {
			final URL url = new URL(applicationPath + "/" + String.join("/", parameters));
			final HttpURLConnection con = (HttpURLConnection) url.openConnection();

			final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

			if(in != null) {
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				} in .close();

				final String responseData = response.toString();
				final JsonElement jsonResponse = parser.parse(responseData);

				return jsonResponse.getAsJsonObject();
			}

		} catch (Exception e) {

		}

		return new JsonObject();
	}

	@Override
	public String getSource() {
		return "";
	}
}
