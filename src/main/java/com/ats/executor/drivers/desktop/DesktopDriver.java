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

package com.ats.executor.drivers.desktop;

import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.AtsBaseElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ScriptStatus;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.engines.DesktopDriverEngine;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.recorder.ReportSummary;
import com.ats.script.ScriptHeader;
import com.ats.tools.logger.ExecutionLogger;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DesktopDriver extends RemoteWebDriver {

	private static final int TIME_OUT = 60;

	private List<FoundElement> elementMapLocation;

	private String driverHost;
	private int driverPort;

	private DesktopDriverEngine engine;

	private String driverUrl;

	private OkHttpClient client;

	private String driverVersion;

	private String osName;
	private String osVersion;
	private String osBuildVersion;

	private String countryCode;
	private String machineName;
	private String screenWidth;
	private String screenHeight;
	private String driveLetter;

	private String diskTotalSize;
	private String diskFreeSpace;

	private String cpuArchitecture;
	private String cpuCores;
	private String cpuName;
	private String cpuSocket;
	private String cpuMaxClock;

	private String dotNetVersion;

	/*private final static Comparator<FoundElement> compareElementBySize = Comparator
			.comparing(FoundElement::getWidth)
			.thenComparing(FoundElement::getHeight);*/

	public DesktopDriver() {}

	public DesktopDriver(ActionStatus status, DriverManager driverManager) {

		final DriverProcess desktopDriverProcess = driverManager.getDesktopDriver(status);

		if(status.isPassed()) {

			this.driverHost = desktopDriverProcess.getDriverServerUrl().getHost();
			this.driverPort = desktopDriverProcess.getDriverServerUrl().getPort();
			this.driverUrl = "http://" + getDriverHost() + ":" + getDriverPort();

			this.client = new Builder().
					cache(null)
					.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
					.writeTimeout(TIME_OUT, TimeUnit.SECONDS)
					.readTimeout(TIME_OUT, TimeUnit.SECONDS)
					.build();

			int maxTry = 10;
			DesktopResponse resp = sendRequestCommand(CommandType.Driver, DriverType.Capabilities);
			while (maxTry > 0 && (resp == null || resp.errorCode == -999)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}

				maxTry--;
				resp = sendRequestCommand(CommandType.Driver, DriverType.Capabilities);
			}

			if(resp.errorCode != -999) {
				for (DesktopData data : resp.data) {
					if("BuildNumber".equals(data.getName())) {
						this.osBuildVersion = data.getValue();
					}else if("Name".equals(data.getName())) {
						this.osName = data.getValue();
					}else if("Version".equals(data.getName())) {
						this.osVersion = data.getValue();
					}else if("DriverVersion".equals(data.getName())) {
						this.driverVersion = data.getValue();
					}else if("CountryCode".equals(data.getName())) {
						this.countryCode = data.getValue();
					}else if("MachineName".equals(data.getName())) {
						this.machineName = data.getValue();
					}else if("ScreenWidth".equals(data.getName())) {
						this.screenWidth = data.getValue();
					}else if("ScreenHeight".equals(data.getName())) {
						this.screenHeight= data.getValue();
					}else if("DriveLetter".equals(data.getName())) {
						this.driveLetter = data.getValue();
					}else if("DiskTotalSize".equals(data.getName())) {
						this.diskTotalSize = data.getValue();
					}else if("DiskFreeSpace".equals(data.getName())) {
						this.diskFreeSpace = data.getValue();
					}else if("CpuSocket".equals(data.getName())) {
						this.cpuSocket = data.getValue();
					}else if("CpuName".equals(data.getName())) {
						this.cpuName = data.getValue();
					}else if("CpuArchitecture".equals(data.getName())) {
						this.cpuArchitecture = data.getValue();
					}else if("CpuMaxClockSpeed".equals(data.getName())) {
						this.cpuMaxClock = data.getValue();
					}else if("CpuCores".equals(data.getName())) {
						this.cpuCores = data.getValue();
					}else if("DotNetVersion".equals(data.getName())) {
						this.dotNetVersion = data.getValue();
					}
				}
				status.setPassed(true);
			}else {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to connect to desktop driver, check DotNET and OS version ...");
			}
		}
	}

	public Double getScreenWidth() {
		try {
			return Double.parseDouble(screenWidth);
		}catch(NumberFormatException e) {}
		return 0D;
	}

	public Double getScreenHeight() {
		try {
			return Double.parseDouble(screenHeight);
		}catch(NumberFormatException e) {}
		return 0D;
	}

	public TestBound getScreenBound() {
		return new TestBound(0D, 0D, getScreenWidth(), getScreenHeight());
	}

	public void setEngine(DesktopDriverEngine engine) {
		this.engine = engine;
		engine.setDriver(this);
	}

	public DesktopDriverEngine getEngine() {
		return engine;
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	public String getOsBuildVersion() {
		return osBuildVersion;
	}

	public String getOsName() {
		return osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getMachineName() {
		return machineName;
	}

	public String getScreenResolution() {
		return screenWidth + " x " + screenHeight;
	}

	public String getDriveLetter() {
		return driveLetter;
	}

	public String getDiskTotalSize() {
		return diskTotalSize;
	}

	public String getDiskFreeSpace() {
		return diskFreeSpace;
	}

	public String getCpuArchitecture() {
		return cpuArchitecture;
	}

	public String getCpuCores() {
		return cpuCores;
	}

	public String getCpuName() {
		return cpuName;
	}

	public String getCpuSocket() {
		return cpuSocket;
	}

	public String getCpuMaxClock() {
		return cpuMaxClock;
	}	

	public String getDotNetVersion() {
		return dotNetVersion;
	}

	//------------------------------------------------------------------------------------------------------------
	// Enum types
	//------------------------------------------------------------------------------------------------------------

	private enum CommandType
	{
		Driver (0),
		Record (1),
		Window (2),
		Element (3),
		Keyboard (4),
		Mouse (5);

		private final int type;
		CommandType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	private enum DriverType
	{
		Capabilities (0),
		Application (1),
		CloseWindows (2),
		Close (3);

		private final int type;
		DriverType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	private enum MouseType
	{
		Move (0),
		Click (1),
		RightClick (2),
		MiddleClick (3),
		DoubleClick (4),
		Down (5),
		Release (6),
		Wheel (7),
		Drag (8);

		private final int type;
		MouseType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	private enum KeyType
	{
		Clear (0),
		Enter (1),
		Down (2),
		Release (3);

		private final int type;
		KeyType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	private enum WindowType
	{
		Title (0),
		Handle(1),
		List (2),
		Move (3),
		Resize (4),
		ToFront (5),
		Switch (6),
		Close (7),
		Url (8),
		Keys(9),
		State(10);

		private final int type;
		WindowType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	private enum ElementType
	{
		Childs (0),
		Parents (1),
		Find (2),
		Attributes (3),
		Select (4),
		FromPoint(5),
		Script(6),
		Root(7),
		LoadTree(8),
		ListItems(9), 
		DialogBox(10),
		SetValue(11),
		Focus(12);

		private final int type;
		ElementType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	private enum RecordType
	{
		Stop (0),
		Screenshot (1),
		Start (2),
		Create (3),
		Image (4),
		Value (5),
		Data(6),
		Status(7),
		Element(8),
		Position(9),
		Download(10),
		ImageMobile(11),
		CreateMobile(12),
		ScreenshotMobile(13),
		Summary(14);

		private final int type;
		RecordType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	//------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------

	public String getDriverHost() {
		return driverHost;
	}

	public int getDriverPort() {
		return driverPort;
	}

	public void closeDriver() {
		sendRequestCommand(CommandType.Driver, DriverType.Close);
	}

	public void closeWindows(long processId, int handle) {
		sendRequestCommand(CommandType.Driver, DriverType.CloseWindows, processId, handle);
	}

	public void clearText() {
		sendRequestCommand(CommandType.Keyboard, KeyType.Clear);
	}

	public void clearText(String elemId) {
		sendRequestCommand(CommandType.Keyboard, KeyType.Clear, elemId);
	}

	public void sendKeys(String data, String elemId) {
		sendRequestCommand(CommandType.Keyboard, KeyType.Enter, data, elemId);
	}

	public void mouseMove(int x, int y) {
		sendRequestCommand(CommandType.Mouse, MouseType.Move, x, y);
	}

	public void mouseClick() {
		sendRequestCommand(CommandType.Mouse, MouseType.Click);
	}

	public void mouseMiddleClick() {
		sendRequestCommand(CommandType.Mouse, MouseType.MiddleClick);
	}

	public void mouseRightClick() {
		sendRequestCommand(CommandType.Mouse, MouseType.RightClick);
	}

	public void mouseClick(int key) {
		sendRequestCommand(CommandType.Mouse, MouseType.Click, key);
	}

	public void mouseDown() {
		sendRequestCommand(CommandType.Mouse, MouseType.Down);
	}

	public void drag() {
		sendRequestCommand(CommandType.Mouse, MouseType.Drag);
	}

	public void mouseRelease() {
		sendRequestCommand(CommandType.Mouse, MouseType.Release);
	}

	public void mouseWheel(int delta) {
		sendRequestCommand(CommandType.Mouse, MouseType.Wheel, delta);
	}

	public void doubleClick() {
		sendRequestCommand(CommandType.Mouse, MouseType.DoubleClick);
	}

	public void keyDown(int codePoint) {
		sendRequestCommand(CommandType.Keyboard, KeyType.Down, codePoint);
	}

	public void keyUp(int codePoint) {
		sendRequestCommand(CommandType.Keyboard, KeyType.Release, codePoint);
	}

	//---------------------------------------------------------------------------------------------------------------------------
	// get elements
	//---------------------------------------------------------------------------------------------------------------------------

	public List<FoundElement> getWebElementsListByHandle(TestBound channelDimension, int handle) {
		return sendRequestCommand(CommandType.Element, ElementType.LoadTree, handle).getFoundElements(channelDimension);
	}

	public void defineRoot(TestBound channelDimension, String id) {
		setElementMapLocation(sendRequestCommand(CommandType.Element, ElementType.Root, id).getFoundElements(channelDimension));
	}

	public void refreshElementMapLocation(Channel channel) {
		new Thread(new LoadMapElement(channel, this)).start();
	}

	public void refreshElementMap(Channel channel) {
		setElementMapLocation(getWebElementsListByHandle(channel.getDimension(), channel.getHandle(this)));
	}

	public void setElementMapLocation(List<FoundElement> list) {
		final FoundElement parent = new FoundElement();
		list.parallelStream().forEach(e -> recalculateSize(parent, e));
		this.elementMapLocation = list;
	}
	
	private void recalculateSize(FoundElement parent, FoundElement elem) {
		if(elem.isVisible() && elem.getWidth() > 0 && elem.getHeight() > 0) {
			parent.updateSize(elem.getBoundX() + elem.getWidth(), elem.getBoundY() + elem.getHeight());
		}
		elem.getChildren().parallelStream().forEach(e -> recalculateSize(elem, e));
	}

	public FoundElement getElementFromPoint(final Double x, final Double y) {

		final double xPos = x;
		final double yPos = y - 10;

		if (elementMapLocation != null && elementMapLocation.size() > 0) {
			final Optional<FoundElement> fe = elementMapLocation.stream().filter(e -> e.isActive() && e.getRectangle().contains(xPos, yPos)).findFirst();
			if(fe.isPresent()) {
				return getHoverChild(fe.get(), xPos, yPos);
			}
		}
		return null;
	}

	private FoundElement getHoverChild(final FoundElement elem, final double xPos, final double yPos) {
		if(elem.getChildren() != null && elem.getChildren().size() > 0) {
			final Optional<FoundElement> child = elem.getChildren().stream().parallel().filter(e -> e.isActive() && e.getRectangle().contains(xPos, yPos)).parallel().sorted((a,b)->-1).findFirst();//.sorted(compareElementBySize)
			if(child.isPresent()) {
				return getHoverChild(child.get(), xPos, yPos);
			}
		}
		return elem;
	}

	public FoundElement getElementFromRect(Double x, Double y, Double w, Double h) {
		FoundElement hoverElement = null;
		if (elementMapLocation != null) {
			for (FoundElement testElement : elementMapLocation) {
				if(testElement != null && testElement.isVisible()){
					if (hoverElement == null) {
						hoverElement = testElement;
						continue;
					}

					final Rectangle rect = testElement.getRectangle();

					if (!rect.contains(x, y)
							|| rect.getWidth() > w || rect.getHeight() > h
							|| hoverElement.getWidth() <= testElement.getWidth() 
							&& hoverElement.getHeight() <= testElement.getHeight()) continue;
					hoverElement = testElement;
				}
			}
		}
		return hoverElement;
	}

	public FoundElement getRootElement(Channel channel) {
		return getRootElement(channel.getHandle(this));
	}

	public FoundElement getRootElement(int handle) {
		return new FoundElement(sendRequestCommand(CommandType.Window, WindowType.Handle, handle).getWindow());
	}
	
	public DesktopResponse startApplication(boolean attach, ArrayList<String> args) {
		return sendRequestCommand(CommandType.Driver, DriverType.Application, attach, String.join("\n", args));
	}

	public List<DesktopWindow> getWindowsByPid(Long pid) {
		return sendRequestCommand(CommandType.Window, WindowType.List, pid).getWindows();
	}

	public void updateWindowHandle(Channel channel) {
		int handle = channel.getHandle(this);
		if(handle > 0) {
			getEngine().setWindow(sendRequestCommand(CommandType.Window, WindowType.Handle, handle).getWindow());
		}
	}

	public DesktopWindow getWindowByHandle(int handle) {
		return sendRequestCommand(CommandType.Window, WindowType.Handle, handle).getWindow();
	}

	public DesktopWindow getWindowByTitle(String title, String name) {
		return sendRequestCommand(CommandType.Window, WindowType.Title, title, name).getWindow();
	}

	public void setChannelToFront(int handle, long pid) {
		sendRequestCommand(CommandType.Window, WindowType.ToFront, handle, pid);
	}

	public void setWindowToFront(Long pid, int handle) {
		sendRequestCommand(CommandType.Window, WindowType.ToFront, pid, handle);
	}

	public void rootKeys(int handle, String keys) {
		sendRequestCommand(CommandType.Window, WindowType.Keys, handle, keys);
	}

	public void moveWindow(Channel channel, Point point) {
		sendRequestCommand(CommandType.Window, WindowType.Move, channel.getHandle(this), point.x, point.y);
	}

	public void resizeWindow(Channel channel, Dimension size) {
		sendRequestCommand(CommandType.Window, WindowType.Resize, channel.getHandle(this), size.width, size.height);
	}

	public void switchTo(Channel channel, int index) {
		sendRequestCommand(CommandType.Window, WindowType.Switch, channel.getHandle(this, index));
	}

	public DesktopResponse switchTo(Long processId, int index, int handle) {
		return sendRequestCommand(CommandType.Window, WindowType.Switch, processId, index, handle);
	}

	public void closeWindow(Channel channel) {
		closeWindow(channel.getHandle(this));
	}

	public void closeWindow(int handle) {
		sendRequestCommand(CommandType.Window, WindowType.Close, handle);
	}

	public void windowState(ActionStatus status, Channel channel, String state) {
		sendRequestCommand(CommandType.Window, WindowType.State, channel.getHandle(this), state);
	}

	public void gotoUrl(ActionStatus status, int handle, String url) {

		status.setData(url);

		final DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Url, handle, url);
		if(resp.errorCode < 0) {
			status.setError(resp.errorCode, resp.errorMessage);
		}else {
			status.setPassed(true);
		}
	}

	public FoundElement getTestElementParent(String elementId, Channel channel){
		return sendRequestCommand(CommandType.Element, ElementType.Parents, elementId).getParentsElement(channel.getDimension());
	}

	public CalculatedProperty[] getElementAttributes(String elementId) {
		return sendRequestCommand(CommandType.Element, ElementType.Attributes, elementId).getAttributes();
	}

	public List<DesktopData> executeScript(ActionStatus status, String script, FoundElement element) {
		return sendRequestCommand(CommandType.Element, ElementType.Script, element.getId(), script).getData();
	}
	
	public void elementFocus(FoundElement element) {
		sendRequestCommand(CommandType.Element, ElementType.Focus, element.getId());
	}

	public List<FoundElement> findElements(Channel channel, TestElement testElement, String tag, String[] attributes, Predicate<AtsBaseElement> predicate) {

		DesktopResponse response = null;

		final String[] firstData = new String[2];
		firstData[1] = tag;

		final Object[] data = Stream.concat(Stream.of(firstData), Stream.of(attributes)).toArray(String[]::new);

		if(testElement.getParent() != null){
			data[0] = testElement.getParent().getWebElementId();
			response = sendRequestCommand(CommandType.Element, ElementType.Childs, data);
		}else{
			data[0] = channel.getHandle(this) + "";
			response = sendRequestCommand(CommandType.Element, ElementType.Find, data);
		}

		return response.getFoundElements(predicate, channel.getDimension());
	}

	public List<FoundElement> getListItems(TestBound dimension, String comboId){
		return sendRequestCommand(CommandType.Element, ElementType.ListItems, new Object[] {comboId}).getFoundElements(dimension);
	}

	public List<FoundElement> getChildren(TestBound dimension, String comboId, String tag){
		return sendRequestCommand(CommandType.Element, ElementType.Childs, new Object[] {comboId, tag}).getFoundElements(dimension);
	}

	public void selectItem(String elementId, String type, String value, boolean regexp) {
		sendRequestCommand(CommandType.Element, ElementType.Select, elementId, type, value, regexp);
	}

	public String getElementAttribute(String elementId, String attribute) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Attributes, elementId, attribute);
		return resp.getFirstAttribute();
	}

	public List<FoundElement> getDialogBox(TestBound dimension) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.DialogBox);
		return resp.getFoundElements(dimension);
	}

	private static class LoadMapElement	implements Runnable {
		final TestBound channelDimension;
		final int handle;
		final DesktopDriver driver;

		public LoadMapElement(Channel channel, DesktopDriver driver) {
			this.channelDimension = channel.getDimension();
			this.handle = channel.getHandle(driver);
			this.driver = driver;
		}

		@Override
		public void run() {
			driver.setElementMapLocation(driver.getWebElementsListByHandle(channelDimension, handle));
		}
	}

	//---------------------------------------------------------------------------------------
	//visual actions
	//---------------------------------------------------------------------------------------

	public void saveSummary(ScriptStatus status, ReportSummary summary) {
		sendRequestCommand(CommandType.Record, RecordType.Summary, summary.toData(status));
	}

	public void stopVisualRecord() {
		sendRequestCommand(CommandType.Record, RecordType.Stop);
	}

	public byte[] getScreenshotByte(Double x, Double y, Double w, Double h){
		final DesktopResponse resp = sendRequestCommand(CommandType.Record, RecordType.Screenshot, x.intValue(), y.intValue(), w.intValue(), h.intValue());
		return resp.image;
	}

	public void createMobileRecord(boolean stop, String actionType, int scriptLine, String scriptName, long timeline, String channelName, TestBound subDimension, String screenshotPath, boolean sync){
		sendRequestCommand(
				CommandType.Record, 
				RecordType.CreateMobile, 
				actionType, 
				scriptLine, 
				scriptName,
				timeline,
				channelName, 
				subDimension.getX().intValue(), 
				subDimension.getY().intValue(), 
				subDimension.getWidth().intValue(), 
				subDimension.getHeight().intValue(), 
				screenshotPath, 
				sync, 
				stop);
	}

	public void createVisualAction(Channel channel, boolean stop, String actionType, int scriptLine, String scriptName, long timeline, boolean sync) {
		sendRequestCommand(
				CommandType.Record, 
				RecordType.Create, 
				actionType, 
				scriptLine, 
				scriptName,
				timeline,
				channel.getName(), 
				channel.getDimension().getX().intValue(), 
				channel.getDimension().getY().intValue(), 
				channel.getDimension().getWidth().intValue(), 
				channel.getDimension().getHeight().intValue(), 
				sync,
				stop);
	}

	public byte[] getMobileScreenshotByte(String url){
		final DesktopResponse resp = sendRequestCommand(CommandType.Record, RecordType.ScreenshotMobile, url);
		return resp.image;
	}

	public void updateMobileScreenshot(TestBound bound, boolean isRef,String url){
		sendRequestCommand(CommandType.Record, RecordType.ImageMobile, 0, 0, bound.getWidth().intValue(), bound.getHeight().intValue(),	isRef, url); 
	}

	public DesktopResponse startVisualRecord(Channel channel, ScriptHeader script, int quality, long started) {
		return sendRequestCommand(CommandType.Record, RecordType.Start, script.getId(), script.getQualifiedName(), script.getDescription(), script.getAuthor(), script.getJoinedGroups(), script.getPrerequisite(), quality, started);
	}

	public void updateVisualImage(TestBound dimension, boolean isRef) {
		sendRequestCommand(CommandType.Record, RecordType.Image, dimension.getX().intValue(), dimension.getY().intValue(), dimension.getWidth().intValue(), dimension.getHeight().intValue(), isRef);
	}

	public void updateVisualValue(String value) {
		sendRequestCommand(CommandType.Record, RecordType.Value, value);
	}

	public void updateVisualData(String value, String data) {
		sendRequestCommand(CommandType.Record, RecordType.Data, value, data);
	}

	public void updateVisualStatus(int error, long duration) {
		sendRequestCommand(CommandType.Record, RecordType.Status, error, duration);
	}

	public void updateVisualElement(TestElement element) {

		Double x = 0D;
		Double y = 0D;
		Double w = 0D;
		Double h = 0D;

		final int numElements = element.getFoundElements().size();

		if(numElements > 0) {
			int i = element.getIndex() == 0 ? 0 : element.getIndex() - 1;
			final TestBound bound = element
					.getFoundElements()
					.get(i)
					.getTestBound();

			x = bound.getX();
			y = bound.getY();

			w = bound.getWidth();
			h = bound.getHeight();

			if(element.isSysComp()) {
				x += 8;
				y += 8;
			}
		}

		String savedCriterias = element.getCriterias();
		if(savedCriterias.length() > 100) {
			savedCriterias = savedCriterias.substring(0, 100);
		}

		sendRequestCommand(CommandType.Record, RecordType.Element, 
				x.intValue(), y.intValue(), w.intValue(), h.intValue(), 
				element.getTotalSearchDuration(), numElements, savedCriterias, element.getSearchedTag());
	}

	public void updateVisualPosition(String type, MouseDirectionData hdir, MouseDirectionData vdir) {

		String hdirName = "";
		int hdirValue = 0;

		String vdirName = "";
		int vdirValue = 0;

		if(hdir != null) {
			hdirName = hdir.getName();
			hdirValue = hdir.getIntValue();
		}

		if(vdir != null) {
			vdirName = vdir.getName();
			vdirValue = vdir.getIntValue();
		}

		sendRequestCommand(CommandType.Record, RecordType.Position, hdirName, hdirValue, vdirName, vdirValue);
	}

	//---------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------

	private final static String USER_AGENT = "AtsDesktopDriver";
	private static final MediaType MEDIA_UTF8 = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

	private DesktopResponse sendRequestCommand(CommandType type, Enum<?> subType, Object... data) {
		return sendRequestCommand(type, subType, Stream.of(data).map(Object::toString).collect(Collectors.joining("\n")));
	}
	
	private DesktopResponse sendRequestCommand(CommandType type, Enum<?> subType, String data) {

		final String url = new StringBuilder(driverUrl)
				.append("/")
				.append(type)
				.append("/")
				.append(subType)
				.toString();

		final Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent", USER_AGENT)
				.post(RequestBody.create(MEDIA_UTF8, data))
				.build();

		try {

			final Response response = client.newCall(request).execute();
			final AMF3Deserializer amf3 = new AMF3Deserializer(response.body().byteStream());
			final DesktopResponse desktopResponse = (DesktopResponse) amf3.readObject();

			amf3.close();
			response.close();

			return desktopResponse;

		} catch (IOException e) {
			return new DesktopResponse(e.getMessage());
		}
	}

	public void saveVisualReportFile(Path path, ExecutionLogger logger) {

		final String url = new StringBuilder(driverUrl)
				.append("/")
				.append(CommandType.Record)
				.append("/")
				.append(RecordType.Download)
				.toString();

		final Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent", USER_AGENT)
				.get()
				.build();

		try {

			final Response resp = client.newCall(request).execute();
			if(resp.code() == 200) {

				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toFile()));
				BufferedInputStream bis = new BufferedInputStream(resp.body().byteStream());

				int inByte;
				while((inByte = bis.read()) != -1) bos.write(inByte);

				bis.close();
				bos.close();

				logger.sendInfo("Save ATSV file", path.toString());

			}else {
				logger.sendError("Unable to save ATSV file", resp.message());
			}

			resp.close();

		} catch (IOException e) {

		}
	}

	public String getSource() {
		final Gson gson = new Gson();
		return gson.toJson(elementMapLocation);
	}

}