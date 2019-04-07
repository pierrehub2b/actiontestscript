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
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.AtsBaseElement;
import com.ats.element.AtsElement;
import com.ats.element.FoundElement;
import com.ats.element.SearchedElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.ScriptHeader;
import com.ats.tools.logger.IExecutionLogger;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

public class DesktopDriver extends RemoteWebDriver {

	private List<FoundElement> elementMapLocation;

	private String driverHost;
	private int driverPort;

	private DesktopDriverEngine engine;

	private String driverUrl;
	private RequestConfig requestConfig;
	private CloseableHttpClient httpClient;

	private String driverVersion;

	public DesktopDriver() {}

	public DesktopDriver(DriverManager driverManager) {

		this.driverHost = driverManager.getDesktopDriver().getDriverServerUrl().getHost();
		this.driverPort = driverManager.getDesktopDriver().getDriverServerUrl().getPort();
		this.driverUrl = "http://" + getDriverHost() + ":" + getDriverPort();

		requestConfig = RequestConfig.custom()
				.setConnectTimeout(10*000)
				.setConnectionRequestTimeout(10*000)
				.setSocketTimeout(20*000).build();

		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

		final ArrayList<DesktopData> osInfo = getOsInfo();
		for (DesktopData data : osInfo) {
			if("BuildNumber".equals(data.getName())) {
				driverManager.setWindowsBuildVersion(data.getValue());
			}else if("Name".equals(data.getName())) {
				driverManager.setOsName(data.getValue());
			}else if("Version".equals(data.getName())) {
				driverManager.setOsVersion(data.getValue());
			}else if("DriverVersion".equals(data.getName())) {
				driverVersion = data.getValue();
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
		Process (2);

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
		Attributes (3);

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
		Download(10);

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

	public void closeProcess(long processId) {
		sendRequestCommand(CommandType.Driver, DriverType.Process, processId);
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

		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Find, handle, SearchedElement.WILD_CHAR);

		if(resp != null && resp.elements != null) {
			return resp.elements.stream().map(e -> new FoundElement(e, channelDimension)).collect(Collectors.toCollection(ArrayList::new));
		}else {
			return new ArrayList<FoundElement>();
		}
	}

	public void refreshElementMapLocation(Channel channel) {
		new Thread(new LoadMapElement(channel, this)).start();
	}

	public void setElementMapLocation(List<FoundElement> list) {
		this.elementMapLocation = list;
	}

	public FoundElement getElementFromPoint(Double x, Double y) {
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
							|| hoverElement.getWidth() <= testElement.getWidth() 
							&& hoverElement.getHeight() <= testElement.getHeight()) continue;
					hoverElement = testElement;
				}
			}
		}
		return hoverElement;
	}

	public ArrayList<DesktopData> getOsInfo() {
		return sendRequestCommand(CommandType.Driver, DriverType.Capabilities).capabilities;
	}

	public ArrayList<DesktopData> getVersion(String appPath) {
		return sendRequestCommand(CommandType.Driver, DriverType.Application, appPath).capabilities;
	}

	public List<DesktopWindow> getWindowsByPid(Long pid) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.List, pid);
		if(resp.windows != null) {
			return resp.windows;
		}else {
			return new ArrayList<DesktopWindow>();
		}
	}

	public DesktopWindow getWindowByHandle(int handle) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Handle, handle);
		if(resp != null && resp.windows != null && resp.windows.size() > 0) {
			return resp.windows.get(0);
		}
		return null;
	}

	public DesktopWindow getWindowByTitle(String title) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Title, title);
		if(resp != null && resp.windows != null && resp.windows.size() > 0) {
			return resp.windows.get(0);
		}
		return null;
	}

	public void setChannelToFront(int handle, long pid) {
		sendRequestCommand(CommandType.Window, WindowType.ToFront, handle, pid);
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
		final DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Url, handle, url);
		if(resp != null) {
			status.setData(url);
			if(resp.errorCode < 0) {
				status.setPassed(false);
				status.setMessage(resp.errorMessage);
			}else {
				status.setPassed(true);
			}
		}
	}

	public FoundElement getTestElementParent(String elementId, Channel channel){

		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Parents, elementId);

		FoundElement result = null;

		if(resp.elements != null) {

			FoundElement current = null;

			for(Object obj : resp.elements) {
				FoundElement elem = new FoundElement((AtsElement)obj, channel.getDimension());
				if(current == null) {
					result = elem;
				}else {
					current.setParent(elem);
				}
				current = elem;
			}
		}
		return result;
	}

	public CalculatedProperty[] getElementAttributes(String elementId) {

		ArrayList<CalculatedProperty> listAttributes = new ArrayList<CalculatedProperty>();

		DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Attributes, elementId);
		if(resp.properties != null) {
			for(DesktopData data : resp.properties) {
				listAttributes.add(new CalculatedProperty(data.getName(), data.getValue()));
			}
		}

		return listAttributes.toArray(new CalculatedProperty[listAttributes.size()]);
	}

	public ArrayList<FoundElement> findElements(Channel channel, TestElement testElement, String tag, ArrayList<String> attributes, Predicate<AtsBaseElement> predicate) {

		DesktopResponse response = null;

		Object[] params = new Object[attributes.size() + 2];
		params[1] = tag;

		for (int counter = 0; counter < attributes.size(); counter++) { 		      
			params[counter+2] = attributes.get(counter); 		
		}   

		if(testElement.getParent() != null){
			params[0] = testElement.getParent().getWebElementId();
			response = sendRequestCommand(CommandType.Element, ElementType.Childs, params);
		}else{
			params[0] = channel.getHandle(this);
			response = sendRequestCommand(CommandType.Element, ElementType.Find, params);
		}

		if(response.elements != null) {
			return response.elements.parallelStream().filter(predicate).map(e -> new FoundElement(e, channel.getDimension())).collect(Collectors.toCollection(ArrayList::new));
		}else {
			return new ArrayList<FoundElement>();
		}
	}

	public String getElementAttribute(String elementId, String attribute) {
		final DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Attributes, elementId, attribute);
		if(resp.data != null) {
			return resp.data.getValue();
		}
		return null;
	}

	private static class LoadMapElement
	implements Runnable {
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
			
			//x = elem.getBoundX();
			//y = elem.getBoundY();
			
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
			hdirValue = hdir.getValue();
		}

		if(vdir != null) {
			vdirName = vdir.getName();
			vdirValue = vdir.getValue();
		}

		sendRequestCommand(CommandType.Record, RecordType.Position, hdirName, hdirValue, vdirName, vdirValue);

	}

	//---------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------

	public DesktopResponse sendRequestCommand(CommandType type, Enum<?> subType, Object... data) {

		final HttpPost request = new HttpPost(
				new StringBuilder(driverUrl)
				.append("/")
				.append(type)
				.append("/")
				.append(subType)
				.toString());

		StringJoiner joiner = new StringJoiner("\n");
		for (Object obj : data) {
			joiner.add(obj.toString());
		}

		request.setEntity(new StringEntity(joiner.toString(), ContentType.create("application/x-www-form-urlencoded", Consts.UTF_8)));

		try {

			final HttpResponse response = httpClient.execute(request);
			final AMF3Deserializer amf3 = new AMF3Deserializer(response.getEntity().getContent());
			final DesktopResponse desktopResponse = (DesktopResponse) amf3.readObject();

			amf3.close();

			return desktopResponse;

		} catch (IOException e) {
			return null;
		}
	}

	public void saveVisualReportFile(Path path, IExecutionLogger logger) {

		final CloseableHttpClient downloadClient = HttpClients.createDefault();

		final HttpGet request = new HttpGet(
				new StringBuilder(driverUrl)
				.append("/")
				.append(CommandType.Record)
				.append("/")
				.append(RecordType.Download)
				.toString());

		try {

			final HttpResponse response = downloadClient.execute(request);
			if(response.getStatusLine().getStatusCode() == 200) {
				BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toFile()));

				int inByte;
				while((inByte = bis.read()) != -1) bos.write(inByte);

				bis.close();
				bos.close();
				logger.sendInfo("Save ATSV file -> ", path.toString());
			}else {
				logger.sendError("Unable to save ATSV file -> ", response.getStatusLine().getReasonPhrase());
			}

		} catch (IOException e) {

		}
	}
}