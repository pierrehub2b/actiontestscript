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

package com.ats.executor.drivers;

import java.awt.Rectangle;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;
import org.openqa.selenium.winium.WiniumDriverService;
import org.openqa.selenium.winium.WiniumOptions;

import com.ats.element.FoundElement;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedProperty;
import com.google.common.collect.ImmutableMap;

public class WindowsDesktopDriver extends WiniumDriver {

	private List<FoundElement> elementMapLocation;

	public WindowsDesktopDriver(URL remoteAddress) {
		super(remoteAddress, (WiniumOptions)WindowsDesktopDriver.getNoApplicationOptions());
	}

	public WindowsDesktopDriver(WiniumOptions options) {
		super(options);
	}

	public WindowsDesktopDriver(WiniumDriverService service, WiniumOptions options) {
		super(service, options);
	}

	public WindowsDesktopDriver(URL remoteAddress, WiniumOptions options) {
		super(remoteAddress, options);
	}

	private static DesktopOptions getNoApplicationOptions() {
		DesktopOptions options = new DesktopOptions();
		options.setApplicationPath("NO_APPLICATION");
		return options;
	}

	public long getProcessDataByWindowTitle(String windowTitle, ArrayList<String> windows) {

		long pid = -1L;

		List<WebElement> childs = this.findElementsByXPath("./child::*");
		for (WebElement we : childs) {
			String attribute = we.getAttribute("Name");

			if (attribute != null && attribute.contains(windowTitle)) { 
				try {
					pid = Long.parseLong(we.getAttribute("ProcessId"));
				}catch (NumberFormatException ex) {}

				windows.add(getWindowHandle(we.getAttribute("NativeWindowHandle")));
			}
		}

		return pid;
	}

	public String getWindowHandle(String handle) {
		try {
			int handleId = Integer.parseInt(handle);
			return "native@0x" + Integer.toHexString(handleId);
		}catch(NumberFormatException ex) {
			return null;
		}
	}

	public void refreshElementMapLocation(Channel channel) {
		Thread t = new Thread(new LoadMapElement(channel, channel.getProcessId(), this));
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

	public FoundElement getTestElementParent(WebElement element, Channel channel){

		ArrayList<WebElement> parentsList = new ArrayList<WebElement>();
		try{
			WebElement parent = element.findElement(By.xpath(".."));
			while(parent != null) {
				parentsList.add(parent);
				parent = parent.findElement(By.xpath(".."));
			}
		}catch(NoSuchElementException ex){}

		if(parentsList != null && parentsList.size() > 0) {
			return new FoundElement(parentsList, channel.getDimension().getX(), channel.getDimension().getY());
		}

		return null;
	}

	public List<WebElement> getChildrenByPid(Long pid) {
		return findElements(By.xpath("./child::*[@ProcessId='" + pid + "']"));
	}

	public List<WebElement> getDescendantByPid(Long pid) {
		return findElements(By.xpath("./descendant::*[@ProcessId='" + pid + "']"));
	}

	private List<WebElement> getDescendant(WebElement element) {
		return element.findElements(By.xpath("./descendant::*"));
	}

	private List<WebElement> getDescendantByTag(WebElement element, String tag, List<CalculatedProperty> attributes) {

		tag = tag.replace(TestElement.DESKTOP_PREFIX, "");
		String xpath = "contains(@ControlType, \"ControlType." + tag + "\")";
		for (CalculatedProperty calc : attributes){
			if(!calc.isRegexp()){
				xpath += " and @" + calc.getName() + "=\"" + calc.getValue().getCalculated() + "\"";
			}
		}

		return element.findElements(By.xpath("./descendant::*[" + xpath + "]"));
	}

	public List<FoundElement> getWebElementsListByPid(Long pid, Double channelX, Double channelY) {

		List<WebElement> rootChildren = getChildrenByPid(pid);
		ArrayList<FoundElement> listElements = new ArrayList<FoundElement>();

		for (WebElement childElement : rootChildren){
			addWindowsElement(listElements, childElement, channelX, channelY);
			getDescendant(childElement).parallelStream().forEach(e -> addWindowsElement(listElements, e, channelX, channelY));
		}

		return listElements;
	}

	public ArrayList<FoundElement> findElementByTag(Long pid, WebElement parent, String tag, List<CalculatedProperty> attributes, Channel channel) {

		//channel.refreshLocation();
		TestBound channelDimension = channel.getDimension();

		ArrayList<FoundElement> foundElements = new ArrayList<FoundElement>();

		Predicate<WebElement> fullPredicate = Objects::nonNull;
		for(CalculatedProperty calc : attributes){
			if(calc.isRegexp()){
				fullPredicate = fullPredicate.and(e -> e.getAttribute(calc.getName()).matches(calc.getValue().getCalculated()));
			}else{
				fullPredicate = fullPredicate.and(e -> e.getAttribute(calc.getName()).equals(calc.getValue().getCalculated()));
			}
		}

		if(parent != null){
			List<WebElement> temp = getDescendantByTag(parent, tag, attributes);
			temp.parallelStream().filter(fullPredicate).forEach(e -> foundElements.add(new FoundElement((RemoteWebElement)e, channelDimension.getX(), channelDimension.getY())));
		}else{
			List<WebElement> temp = new ArrayList<WebElement>();
			List<WebElement> children = getChildrenByPid(pid);
			children.parallelStream().forEach(e -> temp.addAll(getDescendantByTag(e, tag, attributes)));

			temp.parallelStream().filter(fullPredicate).forEach(e -> foundElements.add(new FoundElement((RemoteWebElement)e, channelDimension.getX(), channelDimension.getY())));
		}

		return foundElements;
	}

	private void addWindowsElement(List<FoundElement> listChildren, WebElement element, Double channelX, Double channelY){
		listChildren.add(new FoundElement((RemoteWebElement)element, channelX, channelY));
	}

	private static class LoadMapElement
	implements Runnable {
		final Double channelX;
		final Double channelY;
		final Long pid;
		final WindowsDesktopDriver driver;

		public LoadMapElement(Channel channel, Long pid, WindowsDesktopDriver driver) {
			this.channelX = channel.getDimension().getX() - 10;
			this.channelY = channel.getDimension().getY() - 10;
			this.pid = pid;
			this.driver = driver;
		}

		@Override
		public void run() {
			this.driver.setElementMapLocation(this.driver.getWebElementsListByPid(this.pid, this.channelX, this.channelY));
		}
	}

	private String getbase64ImageData(Double x, Double y, Double w, Double h){
		Map<String, String> script = ImmutableMap.of("script", "screenRect:" + x.intValue() + "," + y.intValue() + "," + w.intValue() + "," + h.intValue());
		Response resp = execute(DriverCommand.EXECUTE_SCRIPT, script);
		return (String)resp.getValue();
	}

	public File getScreenshotFile(Double x, Double y, Double w, Double h){
		OutputType<File> outputType = OutputType.FILE;
		return  outputType.convertFromBase64Png(getbase64ImageData(x, y, w ,h));
	}

	public byte[] getScreenshotByte(Double x, Double y, Double w, Double h){
		OutputType<byte[]> outputType = OutputType.BYTES;
		return  outputType.convertFromBase64Png(getbase64ImageData(x, y, w ,h));
	}

	public void setWindowToFront(Long processId) {
		switchTo().window("pid:" + processId);
	}
}