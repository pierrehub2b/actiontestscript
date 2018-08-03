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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.ActionChannelStart;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

public class DesktopDriver extends RemoteWebDriver {

	public static final String DESKTOP_REQUEST_SEPARATOR = "\n";

	private List<FoundElement> elementMapLocation;

	private String driverHost;
	private int driverPort;
	
	private DesktopDriverEngine engine;

	public DesktopDriver(DriverManager driverManager) {
		this.driverHost = driverManager.getDesktopDriver().getDriverServerUrl().getHost();
		this.driverPort = driverManager.getDesktopDriver().getDriverServerUrl().getPort();
	}
	
	public void setEngine(DesktopDriverEngine engine) {
		this.engine = engine;
		engine.setDriver(this);
	}
	
	public DesktopDriverEngine getEngine() {
		return engine;
	}

	//------------------------------------------------------------------------------------------------------------
	// Enum types
	//------------------------------------------------------------------------------------------------------------

	public enum CommandType
	{
		Version (0),
		Record (1),
		Window (2),
		Element (3),
		Keyboard (4),
		Mouse (5);

		private final int type;
		CommandType(int value){
			this.type = value;
		}

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
		CloseAll (8),
		Url (9);

		private final int type;
		WindowType(int value){
			this.type = value;
		}

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
		Position(9);

		private final int type;
		RecordType(int value){
			this.type = value;
		}

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

	public List<FoundElement> getWebElementsListByHandle(Double channelX, Double channelY, int handle) {

		ArrayList<FoundElement> listElements = new ArrayList<FoundElement>();

		DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Find, handle, "*");

		if(resp != null && resp.elements != null) {
			resp.elements.forEach(e -> listElements.add(new FoundElement(e, channelX, channelY)));
		}

		return listElements;
	}

	public void refreshElementMapLocation(Channel channel) {
		Thread t = new Thread(new LoadMapElement(channel, this));
		t.start();
	}

	public void setElementMapLocation(List<FoundElement> data) {
		this.elementMapLocation = data;
	}

	public FoundElement getElementFromPoint(Double x, Double y) {
		FoundElement hoverElement = null;
		if (this.elementMapLocation != null) {
			for (FoundElement testElement : this.elementMapLocation) {
				if(testElement != null && testElement.isVisible()){
					if (hoverElement == null) {
						hoverElement = testElement;
						continue;
					}

					Rectangle rect = testElement.getRectangle();

					if (!rect.contains(x, y) 
							|| hoverElement.getWidth() <= testElement.getWidth() 
							&& hoverElement.getHeight() <= testElement.getHeight()) continue;
					hoverElement = testElement;
				}
			}
		}
		return hoverElement;
	}

	public ArrayList<DesktopData> getVersion(String appPath) {
		DesktopResponse resp = sendRequestCommand(CommandType.Version, appPath);
		return resp.capabilities;
	}

	public List<DesktopWindow> getWindowsByPid(Long pid) {

		List<DesktopWindow> windows = new ArrayList<DesktopWindow>();

		DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.List, pid);

		if(resp.windows != null) {
			resp.windows.forEach(e -> windows.add(e));
		}

		return windows;
	}
	
	public DesktopWindow getWindowByHandle(int handle) {
		DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Handle, handle);
		if(resp != null && resp.windows != null && resp.windows.size() > 0) {
			return resp.windows.get(0);
		}
		return null;
	}
	
	public DesktopWindow getWindowByTitle(String title) {
		DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Title, title);
		if(resp != null && resp.windows != null && resp.windows.size() > 0) {
			return resp.windows.get(0);
		}
		return null;
	}

	public void setChannelToFront(int handle) {
		sendRequestCommand(CommandType.Window, WindowType.ToFront, handle);
	}

	public void moveWindow(Channel channel, Point point) {
		sendRequestCommand(CommandType.Window, WindowType.Move, channel.getHandle(), point.x, point.y);
	}

	public void resizeWindow(Channel channel, Dimension size) {
		sendRequestCommand(CommandType.Window, WindowType.Resize, channel.getHandle(), size.width, size.height);
	}

	public void closeAllWindows(Long pid) {
		sendRequestCommand(CommandType.Window, WindowType.CloseAll, pid);
	}

	public void switchTo(Channel channel, int index) {
		sendRequestCommand(CommandType.Window, WindowType.Switch, channel.getHandle());
	}

	public void closeWindow(Channel channel, int index) {
		sendRequestCommand(CommandType.Window, WindowType.Close, channel.getHandle());
	}
	
	public void closeWindow(int handle) {
		sendRequestCommand(CommandType.Window, WindowType.Close, handle);
	}
	
	public void gotoUrl(ActionStatus status, int handle, String url) {
		DesktopResponse resp = sendRequestCommand(CommandType.Window, WindowType.Url, handle, url);
		if(resp != null) {
			status.setCode(resp.errorCode);
			status.setData(url);
			status.setMessage(resp.errorMessage);
		}
	}

	public FoundElement getTestElementParent(String elementId, Channel channel){

		DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Parents, elementId);

		FoundElement result = null;

		if(resp.elements != null) {

			FoundElement current = null;

			for(Object obj : resp.elements) {
				FoundElement elem = new FoundElement((DesktopElement)obj, channel.getDimension().getX(), channel.getDimension().getY());
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

	public ArrayList<FoundElement> findElements(Channel channel, TestElement testElement, String tag, ArrayList<String> attributes, Predicate<Object> predicate) {

		//channel.refreshLocation();
		TestBound channelDimension = channel.getDimension();
		ArrayList<FoundElement> foundElements = new ArrayList<FoundElement>();
		
		DesktopResponse response = null;
		
		if(testElement.getParent() != null){
			if(attributes.isEmpty()) {
				response = sendRequestCommand(CommandType.Element, ElementType.Childs, testElement.getParent().getWebElementId(), tag);
			}else {
				response = sendRequestCommand(CommandType.Element, ElementType.Childs, testElement.getParent().getWebElementId(), tag, String.join(DESKTOP_REQUEST_SEPARATOR, attributes));
			}
		}else{
			if(attributes.isEmpty()) {
				response = sendRequestCommand(CommandType.Element, ElementType.Find, channel.getHandle(), tag);
			}else {
				response = sendRequestCommand(CommandType.Element, ElementType.Find, channel.getHandle(), tag, String.join(DESKTOP_REQUEST_SEPARATOR, attributes));
			}
		}

		if(response.elements != null) {
			response.elements.parallelStream().filter(predicate).forEach(e -> foundElements.add(new FoundElement(e, channelDimension.getX(), channelDimension.getY())));
		}		

		return foundElements;
	}

	public String getElementAttribute(String elementId, String attribute) {
		DesktopResponse resp = sendRequestCommand(CommandType.Element, ElementType.Attributes, elementId, attribute);
		if(resp.data != null) {
			return resp.data.getValue();
		}
		return null;
	}

	private static class LoadMapElement
	implements Runnable {
		final Double channelX;
		final Double channelY;
		final int handle;
		final DesktopDriver driver;

		public LoadMapElement(Channel channel, DesktopDriver driver) {
			this.channelX = channel.getDimension().getX() - 10;
			this.channelY = channel.getDimension().getY() - 10;
			this.handle = channel.getHandle();
			this.driver = driver;
		}

		@Override
		public void run() {
			this.driver.setElementMapLocation(this.driver.getWebElementsListByHandle(this.channelX, this.channelY, this.handle));
		}
	}

	//---------------------------------------------------------------------------------------
	//visual actions
	//---------------------------------------------------------------------------------------

	public void stopVisualRecord() {
		sendRequestCommand(CommandType.Record, RecordType.Stop);
	}

	public byte[] getScreenshotByte(Double x, Double y, Double w, Double h){
		DesktopResponse resp = sendRequestCommand(CommandType.Record, RecordType.Screenshot, x.intValue(), y.intValue(), w.intValue(), h.intValue());
		return resp.image;
	}

	public void startVisualRecord(Channel channel, 
			String absolutePath, ScriptHeader script, int quality) {
		
		sendRequestCommand(CommandType.Record, RecordType.Start, absolutePath, script.getId(), script.getQualifiedName(), script.getDescription(), script.getAuthor(), script.getJoinedGroups(), script.getPrerequisite(), quality);
		
		createVisualAction(channel, ActionChannelStart.class.getName(), 0);
	}

	public void createVisualAction(Channel channel, String actionType, int scriptLine) {
		sendRequestCommand(CommandType.Record, RecordType.Create, actionType, scriptLine, 
				channel.getName(), channel.getDimension().getX().intValue(), channel.getDimension().getY().intValue(), channel.getDimension().getWidth().intValue(), channel.getDimension().getHeight().intValue());
	}

	public void updateVisualImage(TestBound dimension) {
		sendRequestCommand(CommandType.Record, RecordType.Image, dimension.getX().intValue(), dimension.getY().intValue(), dimension.getWidth().intValue(), dimension.getHeight().intValue());
	}

	public void updateVisualValue(String value) {
		sendRequestCommand(CommandType.Record, RecordType.Value, value);
	}

	public void updateVisualData(String value, String data) {
		sendRequestCommand(CommandType.Record, RecordType.Data, value, data);
	}

	public void updateVisualStatus(int error) {
		sendRequestCommand(CommandType.Record, RecordType.Status, error);
	}

	public void updateVisualElement(TestElement element) {
		
		Double x = 0.0;
		Double y = 0.0;
		Double w = 0.0;
		Double h = 0.0;
		
		int numElements = element.getFoundElements().size();
		
		if(numElements > 0) {
			TestBound bound = element.getFoundElements().get(0).getTestBound();
			
			x = bound.getX();
			y = bound.getY();
			w = bound.getWidth();
			h = bound.getHeight();
		}

		sendRequestCommand(CommandType.Record, RecordType.Element, 
				x.intValue(), y.intValue(), w.intValue(), h.intValue(), 
				element.getTotalSearchDuration(), numElements, element.getCriterias(), element.getElementTag());
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

	public DesktopResponse sendRequestCommand(Object... request) {

		DesktopResponse response = null;
		String data = StringUtils.join(request, DESKTOP_REQUEST_SEPARATOR);
		int maxTry = 30;

		while((response = sendRequest(data)) == null && maxTry > 0) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
			maxTry--;
		}
		return response;
	}

	private DesktopResponse sendRequest(String data) {

		DesktopResponse response = null;
		try {
			Socket socket = new Socket(getDriverHost(), getDriverPort());

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			writer.print(data);
			writer.flush();

			AMF3Deserializer amf3 = new AMF3Deserializer(socket.getInputStream());
			response = (DesktopResponse) amf3.readObject();

			amf3.close();
			writer.close();
			socket.close();

		} catch (Exception e) {}

		return response;
	}
}