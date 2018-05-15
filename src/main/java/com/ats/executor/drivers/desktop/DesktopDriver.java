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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.FoundElement;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.generator.variables.CalculatedProperty;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

public class DesktopDriver extends RemoteWebDriver {

	private static final String DESKTOP_REQUEST_SEPARATOR = "|";

	private List<FoundElement> elementMapLocation;

	private String driverHost;
	private int driverPort;

	public DesktopDriver(DriverManager driverManager) {
		this.driverHost = driverManager.getDesktopDriver().getDriverServerUrl().getHost();
		this.driverPort = driverManager.getDesktopDriver().getDriverServerUrl().getPort();
	}

	public enum CommandType
	{
		Version (0),
		Window (1),
		Windows (2),
		Move (3),
		Resize (4),
		ToFront (5),
		CloseAll (6),
		Elements (7),
		Childs (8),
		Attribute (9),
		Parents (10),
		Switch (11),
		Close (12),
		ScreenShot (13),
		SendKeys (14),
		MouseMove (15);

		private final int type;
		CommandType(int value){
			this.type = value;
		}

		public String toString(){ return this.type + ""; }
	};

	public String getDriverHost() {
		return driverHost;
	}

	public int getDriverPort() {
		return driverPort;
	}

	public void sendKeys(String data) {
		sendRequestCommand(CommandType.SendKeys, data);
	}
	
	public void mouseMove(int x, int y) {
		sendRequestCommand(CommandType.MouseMove, x, y);
	}

	//---------------------------------------------------------------------------------------------------------------------------
	// get elements
	//---------------------------------------------------------------------------------------------------------------------------

	public List<FoundElement> getWebElementsListByPid(int pid, Double channelX, Double channelY) {

		ArrayList<FoundElement> listElements = new ArrayList<FoundElement>();

		DesktopResponse resp = sendRequestCommand(CommandType.Elements, pid);

		if(resp.elements != null) {
			resp.elements.forEach(e -> listElements.add(new FoundElement(e, channelX, channelY)));
		}

		return listElements;
	}

	public long getProcessDataByWindowTitle(String windowTitle) {

		long pid = -1L;
		int maxTry = 50;

		while(pid == -1L && maxTry > 0) {
			DesktopResponse resp = sendRequestCommand(CommandType.Window, windowTitle);
			if(resp.windows != null && resp.windows.size() > 0) {
				pid = resp.windows.get(0).pid;
			}else {
				maxTry--;
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {}
			}
		}

		return pid;
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

					if (!rect.contains(x, y) || hoverElement.getWidth() <= testElement.getWidth() && hoverElement.getHeight() <= testElement.getHeight()) continue;
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

		DesktopResponse resp = sendRequestCommand(CommandType.Windows, pid);

		if(resp.windows != null) {
			resp.windows.forEach(e -> windows.add((DesktopWindow)e));
		}

		return windows;
	}

	public void setWindowToFront(Long processId) {
		sendRequestCommand(CommandType.ToFront, processId);
	}

	public void moveWindow(Channel channel, Point point) {
		sendRequestCommand(CommandType.Move, channel.getHandle(), point.x, point.y);
	}

	public void resizeWindow(Channel channel, Dimension size) {
		sendRequestCommand(CommandType.Resize, channel.getHandle(), size.width, size.height);
	}

	public void closeAllWindows(Long pid) {
		sendRequestCommand(CommandType.CloseAll, pid);
	}

	public void switchTo(Channel channel, int index) {
		sendRequestCommand(CommandType.Switch, channel.getHandle());
	}

	public void closeWindow(Channel channel, int index) {
		sendRequestCommand(CommandType.Close, channel.getHandle());
	}

	public Double[] getWindowSize(Long pid) {
		DesktopResponse resp = sendRequestCommand(CommandType.Windows, pid);
		if(resp.windows != null && resp.windows.size() > 0) {
			DesktopWindow win = resp.windows.get(0);
			return new Double[] {win.x, win.y, win.width, win.height};
		}
		return new Double[] {0.0, 0.0, 0.0, 0.0};
	}

	public FoundElement getTestElementParent(String elementId, Channel channel){

		DesktopResponse resp = sendRequestCommand(CommandType.Parents, elementId);

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

		DesktopResponse resp = sendRequestCommand(CommandType.Attribute, elementId);
		if(resp.properties != null) {
			for(DesktopData data : resp.properties) {
				listAttributes.add(new CalculatedProperty(data.getName(), data.getValue()));
			}
		}

		return listAttributes.toArray(new CalculatedProperty[listAttributes.size()]);
	}

	public ArrayList<FoundElement> findElementByTag(String parentId, String tag, List<CalculatedProperty> attributes, Channel channel) {

		//channel.refreshLocation();
		TestBound channelDimension = channel.getDimension();

		ArrayList<FoundElement> foundElements = new ArrayList<FoundElement>();

		Predicate<DesktopElement> fullPredicate = Objects::nonNull;
		for(CalculatedProperty calc : attributes){
			if(calc.isRegexp()){
				fullPredicate = fullPredicate.and(e -> getElementAttribute(e.id, calc.getName()).matches(calc.getValue().getCalculated()));
			}else{
				fullPredicate = fullPredicate.and(e -> getElementAttribute(e.id, calc.getName()).equals(calc.getValue().getCalculated()));
			}
		}

		DesktopResponse response = null;
		if(parentId != null){
			response = sendRequestCommand(CommandType.Childs, parentId, tag);
		}else{
			response = sendRequestCommand(CommandType.Elements, channel.getProcessId(), tag);
		}

		if(response.elements != null) {
			response.elements.parallelStream().filter(fullPredicate).forEach(e -> foundElements.add(new FoundElement(e, channelDimension.getX(), channelDimension.getY())));
		}		

		return foundElements;
	}

	public String getElementAttribute(String elementId, String attribute) {
		DesktopResponse resp = sendRequestCommand(CommandType.Attribute, elementId, attribute);
		return resp.data.getValue();
	}

	private static class LoadMapElement
	implements Runnable {
		final Double channelX;
		final Double channelY;
		final int pid;
		final DesktopDriver driver;

		public LoadMapElement(Channel channel, DesktopDriver driver) {
			this.channelX = channel.getDimension().getX() - 10;
			this.channelY = channel.getDimension().getY() - 10;
			this.pid = channel.getProcessId().intValue();
			this.driver = driver;
		}

		@Override
		public void run() {
			this.driver.setElementMapLocation(this.driver.getWebElementsListByPid(this.pid, this.channelX, this.channelY));
		}
	}

	public byte[] getScreenshotByte(Double x, Double y, Double w, Double h){
		DesktopResponse resp = sendRequestCommand(CommandType.ScreenShot, x.intValue(), y.intValue(), w.intValue(), h.intValue());
		return resp.image;
	}

	private DesktopResponse sendRequestCommand(Object... request) {

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

			PrintWriter writer = new PrintWriter(socket.getOutputStream());
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