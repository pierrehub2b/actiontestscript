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

import com.ats.driver.ApplicationProperties;
import com.ats.element.*;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.mobiles.AndroidRootElement;
import com.ats.executor.drivers.engines.mobiles.IosRootElement;
import com.ats.executor.drivers.engines.mobiles.RootElement;
import com.ats.generator.ATS;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.graphic.ImageTemplateMatchingSimple;
import com.ats.script.actions.ActionApi;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MobileDriverEngine extends DriverEngine implements IDriverEngine {

	private final static String DRIVER = "driver";
	private final static String APP = "app";
	private final static String START = "start";
	private final static String STOP = "stop";
	private final static String SWITCH = "switch";
	private final static String CAPTURE = "capture";
	private final static String INPUT = "input";
	public final static String ELEMENT = "element";
	public final static String TAP = "tap";
	public final static String PRESS = "press";
	public final static String SWIPE = "swipe";
	public final static String SCRIPTING = "scripting";
	public final static String SET_PROP = "set-property";
	public final static String GET_PROP = "sysprop-get";
	public final static String SYS_BUTTON = "sysbutton";

	private final static String SCREENSHOT_METHOD = "/screenshot";
	
	private JsonObject source;
	private MobileTestElement testElement;
	private OkHttpClient client;

	protected RootElement rootElement;
	protected RootElement cachedElement;
	protected long cachedElementTime = 0L;
	
	private String userAgent;
	private String token;
	private String endPoint;

	public MobileDriverEngine(Channel channel, ActionStatus status, String app, DesktopDriver desktopDriver, ApplicationProperties props, String token) {

		super(channel, desktopDriver, props, 0, 60);

		if(applicationPath == null) {
			applicationPath = app;
		}

		final int start = applicationPath.indexOf("://");
		if(start > -1) {
			applicationPath = applicationPath.substring(start + 3);
		}

		final String[] appData = applicationPath.split("/");
		if(appData.length > 1) {

			endPoint = appData[0];
			final String application = appData[1];

			this.applicationPath = "http://" + endPoint;
			channel.setApplication(application);

			this.client = new Builder().cache(null).connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(40, TimeUnit.SECONDS).build();

			this.userAgent = "AtsMobileDriver/" + ATS.VERSION + "," + System.getProperty("user.name") + ",";
			this.token = token;

			JsonObject response = executeRequest(DRIVER, START);
			if (response == null) {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to connect to : mobile://" + endPoint);
				return;
			}

			// handle driver start error
			if (response.get("status").getAsInt() != 0) {
				status.setError(ActionStatus.CHANNEL_START_ERROR, response.get("message").getAsString());
				return;
			}

			this.token = response.get("token").getAsString();
			
			final String systemName = response.get("systemName").getAsString();
			final String os = response.get("os").getAsString();

			if (os.equals("ios")) {
				rootElement = new IosRootElement(this);
				cachedElement = new IosRootElement(this);
			} else {
				rootElement = new AndroidRootElement(this);
				cachedElement = new AndroidRootElement(this);
			}
			
			final JsonArray properties = response.getAsJsonArray("systemProperties");
			channel.setSystemProperties(properties);
			
			final String driverVersion = response.get("driverVersion").getAsString();

			final double deviceWidth = response.get("deviceWidth").getAsDouble();
			final double deviceHeight = response.get("deviceHeight").getAsDouble();
			final double channelWidth = response.get("channelWidth").getAsDouble();
			final double channelHeight = response.get("channelHeight").getAsDouble();

			final int screenCapturePort = response.get("screenCapturePort").getAsInt();

			final JsonElement udpEndPoint = response.get("udpEndPoint");

			channel.setDimensions(new TestBound(0D, 0D, deviceWidth, deviceHeight), new TestBound(0D, 0D, channelWidth, channelHeight));

			response = executeRequest(APP, START, application);
			if (response == null) {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to connect to : " + application);
				return;
			}

			// handle app start error
			if (response.get("status").getAsInt() != 0) {
				status.setError(ActionStatus.CHANNEL_START_ERROR, response.get("message").getAsString());
				return;
			}

			final String base64 = response.get("icon").getAsString();
			byte[] icon = new byte[0];
			if (base64.length() > 0) {
				try {
					icon = Base64.getDecoder().decode(base64);
				}catch(Exception ignored) {}
			}

			final String[] endPointData = endPoint.split(":");
			final String version = response.get("version").getAsString();

			if(udpEndPoint != null) {
				channel.setApplicationData(os + ":" + systemName, version, driverVersion, -1, icon, udpEndPoint.getAsString() + ":" + screenCapturePort);
			} else {
				channel.setApplicationData(os + ":" + systemName, version, driverVersion, -1, icon, endPointData[0] + ":" + screenCapturePort);
			}
			
			refreshElementMapLocation();
		}
	}

	@Override
	public WebElement getRootElement(Channel cnl) {
		refreshElementMapLocation();
		return new MobileRootElement(rootElement.getValue());
	}

	@Override
	public TestElement getTestElementRoot() {
		refreshElementMapLocation();
		return new TestElement(new FoundElement(rootElement.getValue()), channel);
	}

	@Override
	public void refreshElementMapLocation() {
		source = executeRequest(CAPTURE);
		rootElement.refresh(source);
	}

	protected void loadCapturedElement() {
		long current = System.currentTimeMillis();
		if(cachedElement == null || current - 2500 > cachedElementTime) {
			if (cachedElement != null) {
				cachedElement.refresh(executeRequest(CAPTURE));
			}
			cachedElementTime = System.currentTimeMillis();
		}
	}

	@Override
	public void close(boolean keepRunning) {
		executeRequest(APP, STOP, channel.getApplication());
		this.channel = null;
	}

	public void tearDown() {
		JsonObject jsonObject = executeRequest(DRIVER, STOP);
		if (jsonObject != null) {
			this.token = null;
		}
	}

	@Override
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y) {

		loadCapturedElement();

		ArrayList<AtsMobileElement> listElements = new ArrayList<AtsMobileElement>();

		loadList(cachedElement.getValue(), listElements, "A");

		final int mouseX = (int)(channel.getSubDimension().getX() + x);
		final int mouseY = (int)(channel.getSubDimension().getY() + y);

		AtsMobileElement element = cachedElement.getValue();

		listElements.sort(Comparator.comparing(AtsMobileElement::getPositionInDom));
		
		for (AtsMobileElement child : listElements) {
			int coordinateX = child.getX().intValue();
			int coordinateY = child.getY().intValue();
			int coordinateW = child.getWidth().intValue();
			int coordinateH = child.getHeight().intValue();
			
			if (child.getRect().contains(new Point(mouseX, mouseY)) &&
					(
							element.getRect().contains(new Point(coordinateX, coordinateY)) ||
									element.getRect().contains(new Point(coordinateX + coordinateW, coordinateY)) ||
									element.getRect().contains(new Point(coordinateX + coordinateW, coordinateY + coordinateH)) ||
									element.getRect().contains(new Point(coordinateX, coordinateY + coordinateH))
					)
			) {
				element = child;
			}
		}

		return element.getFoundElement();
	}

	@Override
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h) {

		loadCapturedElement();

		ArrayList<AtsMobileElement> listElements = new ArrayList<AtsMobileElement>();

		loadList(cachedElement.getValue(), listElements, "A");

		final int mouseX = (int)(channel.getSubDimension().getX() + x);
		final int mouseY = (int)(channel.getSubDimension().getY() + y);

		AtsMobileElement element = cachedElement.getValue();

		listElements.sort(Comparator.comparing(AtsMobileElement::getPositionInDom));
		
		for (AtsMobileElement child : listElements) {
			if (child.getRect().contains(new Point(mouseX, mouseY)) && child.getRect().getWidth() <= w && child.getRect().getHeight() <= h && element.getRect().contains(child.getRect())) {
				element = child;
			}
		}

		return element.getFoundElement();
	}
	
	public String getToken() {
		return token;
	}

	public String getEndPoint() {
		return endPoint;
	}

	private void loadList(AtsMobileElement element, ArrayList<AtsMobileElement> list, String order) {
		final AtsMobileElement[] children = element.getChildren();
		if(children != null) {
			for (int i=0; i<children.length; i++) {
				if(element.getChildren() != null) {
					final AtsMobileElement child = element.getChildren()[i];
					String newOrder = order  + getCharForNumber(i+1);
					child.setPositionInDom(newOrder);
					list.add(child);
					loadList(child, list, newOrder);
				}
			}
		}
	}

	private String getCharForNumber(int i) {
		return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : "Z";
	}

	@Override
	public void loadParents(FoundElement element) {
		final AtsMobileElement atsElement = getCapturedElementById(element.getId(), false);
		if(atsElement != null) {
			FoundElement currentParent;
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
		if (reload) {
			return getElementById(rootElement.getValue(), id);
		} else if(cachedElement != null) {
			return getElementById(cachedElement.getValue(), id);
		} else {
			return null;
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
	public List<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, String[] attributes, String[] attributesValues, Predicate<AtsBaseElement> searchPredicate, WebElement startElement, boolean waitAnimation) {

		final List<AtsMobileElement> list = new ArrayList<AtsMobileElement>();

		if(testObject.getParent() == null) {
			refreshElementMapLocation();
			loadElementsByTag(rootElement.getValue(), tagName, list);
		}else {
			loadElementsByTag(getElementById(testObject.getParent().getWebElementId()), tagName, list);
		}

		return list.parallelStream().filter(searchPredicate).map(FoundElement::new).collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public List<FoundElement> findElements(TestElement parent, ImageTemplateMatchingSimple template) {
		final byte[] screenshot = getDesktopDriver().getMobileScreenshotByte(getScreenshotPath());
		return template.findOccurrences(screenshot).parallelStream().map(r -> new FoundElement(channel, parent, r)).collect(Collectors.toCollection(ArrayList::new));
	}

	private void loadElementsByTag(AtsMobileElement root, String tag, List<AtsMobileElement> list)
	{
		if(root == null) return;
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
		executeRequest(SYS_BUTTON, type);
	}
	
	@Override
	public void buttonClick(ArrayList<String> ids) {
		executeRequest(SYS_BUTTON, ids.toArray(new String[0]));
	}
	
	@Override
	public void tap(int count, FoundElement element) {
		rootElement.tap(element, count);
	}
	
	@Override
	public void press(int duration, ArrayList<String> paths, FoundElement element) {
		rootElement.press(element, paths, duration);
	}
	
	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		cachedElementTime = 0L;
		rootElement.tap(status, element, position);
	}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		testElement = rootElement.getCurrentElement(element, position);
	}

	@Override
	public void moveByOffset(int hDirection, int vDirection) {
		rootElement.swipe(testElement, hDirection, vDirection);
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			executeRequest(ELEMENT, element.getFoundElement().getId(), INPUT, sequence.getSequenceMobile());
		}
	}

	@Override
	public List<String[]> loadSelectOptions(TestElement element) {
		return Collections.emptyList();
	}

	@Override
	public List<FoundElement> findSelectOptions(TestBound dimension, TestElement element) {
		return Collections.emptyList();
	}

	@Override
	public void selectOptionsItem(ActionStatus status, TestElement element, CalculatedProperty selectProperty) {
	}

	@Override
	public void clearText(ActionStatus status, TestElement te, MouseDirection md) {
		mouseClick(status, te.getFoundElement(), md, 0, 0);
		executeRequest(ELEMENT, te.getFoundElement().getId(), INPUT, SendKeyData.EMPTY_DATA);
	}

	@Override
	public void updateScreenshot(TestBound dimension, boolean isRef) {
		getDesktopDriver().updateMobileScreenshot(channel.getSubDimension(), isRef, getScreenshotPath());
	}

	@Override
	public byte[] getScreenshot(Double x, Double y, Double width, Double height) {

		final byte[] screen = getDesktopDriver().getMobileScreenshotByte(getScreenshotPath());

		if(screen != null) {
			ImageIO.setUseCache(false);

			final InputStream in = new ByteArrayInputStream(screen);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			final BufferedImage subImage = new BufferedImage(width.intValue(), height.intValue(), BufferedImage.TYPE_INT_RGB);
			final Graphics g = subImage.getGraphics();

			try {
				//final BufferedImage subImage = ImageIO.read(in).getSubimage(x.intValue(), y.intValue(), width.intValue(), height.intValue());
				g.drawImage(ImageIO.read(in), 0, 0, width.intValue(), height.intValue(), x.intValue(), y.intValue(), x.intValue() + width.intValue(), y.intValue() + height.intValue(), null);
				g.dispose();

				ImageIO.write(subImage, "png", baos);
				baos.flush();

				final byte[] result = baos.toByteArray();
				baos.close();

				return result;

			} catch (IOException ignored) {}
		}

		return new byte[1];
	}

	@Override
	public void createVisualAction(Channel channel, String actionType, int scriptLine, long timeline, boolean sync) {
		getDesktopDriver().createMobileRecord(actionType, scriptLine, timeline,	channel.getName(), channel.getSubDimension(), getScreenshotPath(), sync);
	}

	private String getScreenshotPath() {
		return getApplicationPath() + SCREENSHOT_METHOD;
	}

	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void api(ActionStatus status, ActionApi api) {}

	@Override
	public void switchWindow(ActionStatus status, int index, int tries) {}

	@Override
	public void closeWindow(ActionStatus status) {}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		return null;
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {}

	@Override
	public void waitAfterAction(ActionStatus status) {}

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
	public DialogBox switchToAlert() {
		return null;
	}

	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public boolean switchToDefaultContent() {return true;}

	@Override
	public void setWindowToFront() {
		executeRequest(APP, SWITCH, channel.getApplication());
	}

	@Override
	public void switchToFrameId(String id) {}

	@Override
	public void updateDimensions() {}

	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------

	private AtsMobileElement getElementById(String id) {
		return getElementById(rootElement.getValue(), id);
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

	public JsonObject executeRequest(String type, String ... data) {

		final String url = applicationPath + "/" + type;

		final Request.Builder requestBuilder = new Request.Builder();
		requestBuilder.url(url);

		if (token != null) {
			requestBuilder.addHeader("Token", token);
		}

		final Request request = requestBuilder
				.addHeader("User-Agent", userAgent)
				.addHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF8")
				.post(RequestBody.create(null, Stream.of(data).map(Object::toString).collect(Collectors.joining("\n"))))
				.build();

		try {
			final Response response = client.newCall(request).execute();
			final String responseData = CharStreams.toString(new InputStreamReader(response.body().byteStream(), Charsets.UTF_8));
			response.close();
			return JsonParser.parseString(responseData).getAsJsonObject();
		} catch (JsonSyntaxException | IOException e) {
			return null;
		}
	}

	@Override
	public String getSource() {
		refreshElementMapLocation();
		return source.toString();
	}

	@Override
	public void windowState(ActionStatus status, Channel channel, String state) { }
	
	@Override
	public void setSysProperty(String propertyName, String propertyValue) {
		executeRequest(SET_PROP, propertyName, propertyValue);
	}
	
	@Override
	public String getSysProperty(ActionStatus status, String propertyName, FoundElement element) {
		JsonObject result = executeRequest(GET_PROP, propertyName);
		
		int code = result.get("status").getAsInt();
		String message = result.get("message").getAsString();
		
		return code != 0 ? null : message;
	}
	
	@Override
	public Object executeJavaScript(ActionStatus status, String script, TestElement element) {
		final JsonObject result = (JsonObject)rootElement.scripting(script, element.getFoundElement());
		return handleResult(status, result);
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, boolean returnValue) {
		final JsonObject result = (JsonObject)rootElement.scripting(script);
		return handleResult(status, result);
	}
	
	private Object handleResult(ActionStatus status, JsonObject result) {
		int code = result.get("status").getAsInt();
		String message = result.get("message").getAsString();
		
		if (code == 0) {
			status.setNoError(message);
		} else {
			status.setError(ActionStatus.JAVASCRIPT_ERROR, message);
		}
		
		return result;
	}
	
	@Override
	protected void setPosition(org.openqa.selenium.Point pt) { }

	@Override
	protected void setSize(Dimension dim) { }
	
	@Override
	public int getNumWindows() {
		return 1;
	}
}
