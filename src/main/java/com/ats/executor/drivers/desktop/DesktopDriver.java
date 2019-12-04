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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.AtsBaseElement;
import com.ats.element.FoundElement;
import com.ats.element.SearchedElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
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

	private String screenResolution;

	private String driveLetter;

	private String diskTotalSize;

	private String diskFreeSpace;

	private String cpuArchitecture;

	private String cpuCores;

	private String cpuName;
	private String cpuSocket;

	private String cpuMaxClock;

	private String dotNetVersion;

	public DesktopDriver() {}

	public DesktopDriver(ActionStatus status, DriverManager driverManager) {

		final DriverProcess desktopDriverProcess = driverManager.getDesktopDriver(status);

		if(status.isPassed()) {

			this.driverHost = desktopDriverProcess.getDriverServerUrl().getHost();
			this.driverPort = desktopDriverProcess.getDriverServerUrl().getPort();
			this.driverUrl = "http://" + getDriverHost() + ":" + getDriverPort();

			this.client = new Builder().
					cache(null)
					.connectTimeout(60, TimeUnit.SECONDS)
					.writeTimeout(60, TimeUnit.SECONDS)
					.readTimeout(60, TimeUnit.SECONDS)
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
					}else if("ScreenResolution".equals(data.getName())) {
						this.screenResolution = data.getValue();
					}
					else if("DriveLetter".equals(data.getName())) {
						this.driveLetter = data.getValue();
					}
					else if("DiskTotalSize".equals(data.getName())) {
						this.diskTotalSize = data.getValue();
					}
					else if("DiskFreeSpace".equals(data.getName())) {
						this.diskFreeSpace = data.getValue();
					}
					else if("CpuSocket".equals(data.getName())) {
						this.cpuSocket = data.getValue();
					}
					else if("CpuName".equals(data.getName())) {
						this.cpuName = data.getValue();
					}
					else if("CpuArchitecture".equals(data.getName())) {
						this.cpuArchitecture = data.getValue();
					}
					else if("CpuMaxClockSpeed".equals(data.getName())) {
						this.cpuMaxClock = data.getValue();
					}
					else if("CpuCores".equals(data.getName())) {
						this.cpuCores = data.getValue();
					}
					else if("DotNetVersion".equals(data.getName())) {
						this.dotNetVersion = data.getValue();
					}
				}
				status.setPassed(true);
			}else {
				status.setPassed(false);
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("Unable to connect to desktop driver! Check DotNET and OS version ...");
			}
		}
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
		return screenResolution;
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

	public enum CommandType
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

	public enum DriverType
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

	public enum MouseType
	{
		Move (0),
		Click (1),
		RightClick (2),
		MiddleClick (3),
		DoubleClick (4),
		Down (5),
		Release (6),
		Wheel (7);

		private final int type;
		MouseType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	public enum KeyType
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

	public enum WindowType
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

	public enum ElementType
	{
		Childs (0),
		Parents (1),
		Find (2),
		Attributes (3),
		Select (4),
		FromPoint(5),
		Script(6),
		Root(7);

		private final int type;
		ElementType(int value){
			this.type = value;
		}

		@Override
		public String toString(){ return this.type + ""; }
	};

	public enum RecordType
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
		CreateMobile(12);

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

	public void closeWindows(long processId) {
		sendRequestCommand(CommandType.Driver, DriverType.CloseWindows, processId);
	}

	public void clearText() {
		sendRequestCommand(CommandType.Keyboard, KeyType.Clear);
	}

	public void sendKeys(String data) {
		sendRequestCommand(CommandType.Keyboard, KeyType.Enter, data);
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
		return sendRequestCommand(CommandType.Element, ElementType.Find, handle, SearchedElement.WILD_CHAR).getFoundElements(channelDimension);
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
		this.elementMapLocation = list;
	}
	
	public FoundElement getElementFromMousePoint(TestBound channelDimension) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.FromPoint);
		if(resp.elements != null && resp.elements.size() > 0) {
			return new FoundElement(resp.elements.get(0), channelDimension);
		}
		return null;
	}

	public FoundElement getElementFromPoint(Double x, Double y) {

		FoundElement hoverElement = null;
		if (elementMapLocation != null && elementMapLocation.size() > 1) {
			
			int size = elementMapLocation.size();
			hoverElement = elementMapLocation.get(0);
			
			for(int i = 1; i < size; i++) {
				FoundElement elem = elementMapLocation.get(i);
				if(elem != null && elem.isActive()){

					final Rectangle rect = elem.getRectangle();
					rect.translate(-1, -1);
					rect.setSize(rect.width+2, rect.height+2);
					
					if (!rect.contains(x, y) || hoverElement.getWidth() <= elem.getWidth() && hoverElement.getHeight() <= elem.getHeight()) {
						continue;
					}
					hoverElement = elem;
				}
			}
		}
		return hoverElement;
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
		refreshElementMap(channel);
		if (elementMapLocation != null && elementMapLocation.size() > 0) {
			return elementMapLocation.get(0);
		}
		return null;
	}

	public ArrayList<DesktopData> getVersion(String appPath) {
		return sendRequestCommand(CommandType.Driver, DriverType.Application, appPath).getData();
	}

	public List<DesktopWindow> getWindowsByPid(Long pid) {
		return sendRequestCommand(CommandType.Window, WindowType.List, pid).getWindows();
	}

	public DesktopWindow getWindowByHandle(int handle) {
		return sendRequestCommand(CommandType.Window, WindowType.Handle, handle).getWindow();
	}

	public DesktopWindow getWindowByTitle(String title) {
		return sendRequestCommand(CommandType.Window, WindowType.Title, title).getWindow();
	}

	public void setChannelToFront(int handle, long pid) {
		sendRequestCommand(CommandType.Window, WindowType.ToFront, handle, pid);
	}

	public void setWindowToFront(Long pid) {
		sendRequestCommand(CommandType.Window, WindowType.ToFront, pid);
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
			status.setPassed(false);
			status.setMessage(resp.errorMessage);
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

	public ArrayList<DesktopData> executeScript(ActionStatus status, String script, FoundElement element) {
		return sendRequestCommand(CommandType.Element, ElementType.Script, element.getId(), script).getData();
	}

	public ArrayList<FoundElement> findElements(Channel channel, TestElement testElement, String tag, ArrayList<String> attributes, Predicate<AtsBaseElement> predicate) {

		DesktopResponse response = null;
		attributes.add(0, tag);
		
		if(testElement.getParent() != null){
			attributes.add(0, testElement.getParent().getWebElementId());
			response = sendRequestCommand(CommandType.Element, ElementType.Childs, attributes.toArray());
		}else{
			attributes.add(0, channel.getHandle(this)+"");
			response = sendRequestCommand(CommandType.Element, ElementType.Find, attributes.toArray());
		}
		
		return response.getFoundElements(predicate, channel.getDimension());
	}
	
	public ArrayList<FoundElement> getChildren(TestBound dimension, String comboId, String tag){
		return sendRequestCommand(CommandType.Element, ElementType.Childs, new Object[] {comboId, tag}).getFoundElements(dimension);
	}
	
	public void selectItem(String elementId, String type, String value, boolean regexp) {
		sendRequestCommand(CommandType.Element, ElementType.Select, elementId, type, value, regexp);
	}

	public String getElementAttribute(String elementId, String attribute) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Attributes, elementId, attribute);
		return resp.getFirstAttribute();
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
			this.driver.setElementMapLocation(this.driver.getWebElementsListByHandle(this.channelDimension, this.handle));
		}
	}

	//---------------------------------------------------------------------------------------
	//visual actions
	//---------------------------------------------------------------------------------------

	public void stopVisualRecord() {
		sendRequestCommand(CommandType.Record, RecordType.Stop);
	}

	public byte[] getScreenshotByte(Double x, Double y, Double w, Double h){
		final DesktopResponse resp = sendRequestCommand(CommandType.Record, RecordType.Screenshot, x.intValue(), y.intValue(), w.intValue(), h.intValue());
		return resp.image;
	}

	public DesktopResponse startVisualRecord(Channel channel, ScriptHeader script, int quality, long started) {
		return sendRequestCommand(CommandType.Record, RecordType.Start, script.getId(), script.getQualifiedName(), script.getDescription(), script.getAuthor(), script.getJoinedGroups(), script.getPrerequisite(), quality, started);
	}

	public void createVisualAction(Channel channel, String actionType, int scriptLine, long timeline) {
		sendRequestCommand(CommandType.Record, RecordType.Create, actionType, scriptLine, timeline,
				channel.getName(), channel.getDimension().getX().intValue(), channel.getDimension().getY().intValue(), channel.getDimension().getWidth().intValue(), channel.getDimension().getHeight().intValue());
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

		Double x = 0.0;
		Double y = 0.0;
		Double w = 0.0;
		Double h = 0.0;

		final int numElements = element.getFoundElements().size();

		if(numElements > 0) {
			final FoundElement elem = element.getFoundElements().get(0);
			final TestBound bound = elem.getTestBound();

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
	
	public DesktopResponse sendRequestCommand(CommandType type, Enum<?> subType, Object... data) {

		final String url = new StringBuilder(driverUrl)
				.append("/")
				.append(type)
				.append("/")
				.append(subType)
				.toString();

		final Request request = new Request.Builder()
				.url(url)
				.addHeader("User-Agent", USER_AGENT)
				.post(RequestBody.create(MEDIA_UTF8, Stream.of(data).map(Object::toString).collect(Collectors.joining("\n"))))
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
				logger.sendError("Unable to save ATSV file -> ", resp.message());
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