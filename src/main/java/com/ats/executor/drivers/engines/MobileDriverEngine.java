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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.ats.graphic.TemplateMatchingSimple;
import com.ats.script.actions.ActionApi;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MobileDriverEngine extends DriverEngine implements IDriverEngine{

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
	private final static String BUTTON = "button";

	private JsonParser parser = new JsonParser();
	private Gson gson = new Gson();

	private AtsMobileElement rootElement;
	private AtsMobileElement capturedElement;

	private MobileTestElement testElement;
	
	private OkHttpClient client;

	public MobileDriverEngine(Channel channel, ActionStatus status, String app, DesktopDriver desktopDriver, ApplicationProperties props) {

		super(channel, desktopDriver, app, props, 0, 60);

		if(applicationPath == null) {
			applicationPath = app;
		}

		final int start = applicationPath.indexOf("://");
		if(start > -1) {
			applicationPath = applicationPath.substring(start + 3);
		}

		final String[] appData = applicationPath.split("/");
		if(appData.length > 1) {

			final String endPoint = appData[0];
			final String application = appData[1];

			this.applicationPath = "http://" + endPoint;
			channel.setApplication(application);
									
			this.client = new Builder().cache(null).connectTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS).readTimeout(40, TimeUnit.SECONDS).build();

			JsonObject response = executeRequest(DRIVER, START);

			if(response == null) {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("unable to connect to : " + applicationPath);
				status.setPassed(false);
			}else {

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

						final String[] endPointData = endPoint.split(":");
						final String version = response.get("version").getAsString();
						
						channel.setApplicationData(os + ":" + systemName, version, driverVersion, -1, icon, endPointData[0] + ":" + screenCapturePort);

						refreshElementMapLocation();
					}else {
						status.setCode(ActionStatus.CHANNEL_START_ERROR);
						status.setMessage(response.get("status").getAsString());
						status.setPassed(false);
					}
				}else {
					status.setCode(ActionStatus.CHANNEL_START_ERROR);
					status.setMessage("unable to connect to : " + application);
					status.setPassed(false);
				}
			}
		}
	}

	@Override
	public void refreshElementMapLocation() {
		rootElement = gson.fromJson(executeRequest(CAPTURE), AtsMobileElement.class);
	}

	@Override
	public void close() {
		executeRequest(APP, STOP, channel.getApplication());
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
	
	@Override
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h) {
		capturedElement = gson.fromJson(executeRequest(CAPTURE), AtsMobileElement.class);

		ArrayList<AtsMobileElement> listElements = new ArrayList<AtsMobileElement>();

		loadList(capturedElement, listElements);

		final int mouseX = (int)(channel.getSubDimension().getX() + x);
		final int mouseY = (int)(channel.getSubDimension().getY() + y);

		AtsMobileElement element = capturedElement;

		for (int i=0; i<listElements.size(); i++) {
			AtsMobileElement child = listElements.get(i);
			if(child.getRect().contains(new Point(mouseX, mouseY)) && child.getRect().getWidth() <= w && child.getRect().getHeight() <= h  && element.getRect().contains(child.getRect())){
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
		final AtsMobileElement atsElement = getCapturedElementById(element.getId(), false);
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
	public CalculatedProperty[] getAttributes(FoundElement element, boolean reload) {
		final AtsMobileElement atsElement = getCapturedElementById(element.getId(), reload);
		if(atsElement != null) {
			return atsElement.getMobileAttributes();
		}
		return new CalculatedProperty[0];
	}

	private AtsMobileElement getCapturedElementById(String id, boolean reload) {
		if(!reload && capturedElement != null) {
			return getElementById(capturedElement, id);
		}else {
			return getElementById(rootElement, id);
		}
	}

	@Override
	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry) {
		final AtsMobileElement atsElement = getElementById(element.getId());
		if(atsElement != null) {
			return atsElement.getAttribute(attributeName);
		}
		return null;
	}

	@Override
	public ArrayList<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> searchPredicate) {

		final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();

		if(testObject.getParent() == null) {
			refreshElementMapLocation();
			loadElementsByTag(rootElement, tagName, list);
		}else {
			loadElementsByTag(getElementById(testObject.getParent().getWebElementId()), tagName, list);
		}

		return list.parallelStream().filter(searchPredicate).map(e -> new FoundElement(e)).collect(Collectors.toCollection(ArrayList::new));
	}
	
	@Override
	public ArrayList<FoundElement> findElements(TestElement parent, TemplateMatchingSimple template) {

		return null;
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
	public void buttonClick(String type) {
		executeRequest(BUTTON, type);
	}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		final Rectangle rect = element.getRectangle();
		executeRequest(ELEMENT, element.getId(), TAP, (int)(getOffsetX(rect, position)) + "", (int)(getOffsetY(rect, position)) + "");
	}	

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		final Rectangle rect = element.getRectangle();
		testElement = new MobileTestElement(element.getId(), (int)(getOffsetX(rect, position)), (int)(getOffsetY(rect, position)));
	}

	@Override
	public void moveByOffset(int hDirection, int vDirection) {
		executeRequest(ELEMENT, testElement.getId(), SWIPE, testElement.getOffsetX() + "", testElement.getOffsetY() + "", hDirection + "", + vDirection + "");
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			executeRequest(ELEMENT, element.getFoundElement().getId(), INPUT, sequence.getMobileSequence());
		}
	}

	@Override
	public WebElement getRootElement() {

		refreshElementMapLocation();

		RemoteWebElement elem = new RemoteWebElement();
		elem.setId(rootElement.getId());

		return elem;
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return new CalculatedProperty[0];
	}

	@Override
	public void clearText(ActionStatus status, FoundElement element) {
		executeRequest(ELEMENT, element.getId(), INPUT, SendKeyData.EMPTY_DATA);
	}

	//----------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void api(ActionStatus status, ActionApi api) {}

	@Override
	public void switchWindow(ActionStatus status, int index) {}

	@Override
	public void closeWindow(ActionStatus status) {}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		return null;
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {}

	@Override
	public void waitAfterAction() {}

	@Override
	public void scroll(FoundElement element) {}
	
	@Override
	public void scroll(int value) {}
	
	@Override
	public void scroll(FoundElement element, int delta) {}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {}

	@Override
	public String setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {return "";}

	@Override
	public void keyDown(Keys key) {}

	@Override
	public void keyUp(Keys key) {}

	@Override
	public void drop(MouseDirection md, boolean desktopDriver) {}

	@Override
	public void doubleClick() {}

	@Override
	public void rightClick() {}

	@Override
	public Alert switchToAlert() {
		return null;
	}

	@Override
	public boolean switchToDefaultContent() {return true;}

	@Override
	public boolean setWindowToFront() {
		executeRequest(APP, SWITCH, channel.getApplication());
		return true;
	}

	@Override
	public void switchToFrameId(String id) {}

	@Override
	public void updateDimensions() {}

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

	protected JsonObject executeRequest(String type, String ... data) {
		
		final String url = new StringBuilder(applicationPath)
				.append("/")
				.append(type)
				.toString();

		final Request request = new Request.Builder()
				.url(url)
				.addHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF8")
				.post(RequestBody.
						create(null, 
								Stream.of(data).
								map(Object::toString).
								collect(Collectors.joining("\n"))))
				.build();
		
		try {
			final Response response = client.newCall(request).execute();
			final JsonElement jsonResponse = parser.parse(
					CharStreams.toString(
							new InputStreamReader(
									response
									.body()
									.byteStream(), 
									Charsets.UTF_8)));
			response.close();
			return jsonResponse.getAsJsonObject();
			
		} catch (JsonSyntaxException | IOException e) {
			return null;
		}
	}

	@Override
	public String getSource() {
		return "";
	}

	@Override
	public void windowState(ActionStatus status, Channel channel, String state) {
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, TestElement element) {
		return null;
	}
	@Override
	public Object executeJavaScript(ActionStatus status, String script) {
		return null;
	}
}
