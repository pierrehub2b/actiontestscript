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

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopData;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;

public class DesktopDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	private final static int DEFAULT_WAIT = 100;

	private Process applicationProcess = null;
	private Robot robot;
	
	private String buildNumber;
	private String osName;

	public DesktopDriverEngine(Channel channel, String application, DesktopDriver desktopDriver, AtsManager ats, Robot robot) {

		super(channel, application);
		
		this.robot = robot;
		this.driver = desktopDriver;

		int firstSpace = application.indexOf(" ");
		String applicationArguments = "";

		if(firstSpace > 0){
			applicationArguments = application.substring(firstSpace);
			application = application.substring(0, firstSpace);
		}

		URI fileUri = null;
		File exeFile = null;

		ApplicationProperties properties = ats.getApplicationProperties(application);
		if(properties != null) {
			waitAfterAction = properties.getWait();
			exeFile = new File(properties.getPath());
			if(exeFile.exists()) {
				fileUri = exeFile.toURI();
			}
		}

		if(waitAfterAction == -1) {
			waitAfterAction = DEFAULT_WAIT;
		}

		if(fileUri == null) {
			try {
				fileUri = new URI(application);
				exeFile = new File(fileUri);
			} catch (URISyntaxException e) {}
		}

		if(exeFile == null) {//last chance
			exeFile = new File(application);
		}

		if(exeFile != null && exeFile.exists() && exeFile.isFile()){

			applicationPath = exeFile.getAbsolutePath();

			Runtime runtime = Runtime.getRuntime();
			try{
				applicationProcess = runtime.exec(applicationPath + applicationArguments);
			} catch (IOException e) {
				channel.setStartError(e.getMessage());
				return;
			}

			String driverVersion = "N/A";
			String appVersion = "N/A";

			ArrayList<DesktopData> capabilities = ((DesktopDriver)driver).getVersion(applicationPath);
			for (DesktopData data : capabilities) {
				if("DriverVersion".equals(data.getName())) {
					driverVersion = data.getValue();
				}else if("ApplicationVersion".equals(data.getName())) {
					appVersion = data.getValue();
				}else if("BuildNumber".equals(data.getName())) {
					buildNumber = data.getValue();
				}else if("Caption".equals(data.getName())) {
					osName = data.getValue();
				}
			}

			channel.setApplicationData(appVersion, driverVersion, applicationProcess.pid());

			int maxTry = 30;
			while(channel.getHandle() < 0 && maxTry > 0){
				channel.sleep(200);
				maxTry--;
			}

			desktopDriver.moveWindow(channel, channel.getDimension().getPoint());
			desktopDriver.resizeWindow(channel, channel.getDimension().getSize());
		}
	}

	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
	}

	public void loadParents(FoundElement hoverElement){
		hoverElement.setParent(((DesktopDriver)driver).getTestElementParent(hoverElement.getId(), channel));
	}

	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		return ((DesktopDriver)driver).getElementAttribute(element.getId(), attributeName);
	}

	public CalculatedProperty[] getAttributes(FoundElement element){
		return getAttributes(element.getId());
	}

	public CalculatedProperty[] getAttributes(String elementId){
		return ((DesktopDriver)driver).getElementAttributes(elementId);
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	public FoundElement getElementFromPoint(Double x, Double y){
		return ((DesktopDriver)driver).getElementFromPoint(x, y);
	}

	@Override
	public TestBound[] getDimensions() {

		List<DesktopWindow> wins = ((DesktopDriver)driver).getWindowsByPid(channel.getProcessId());
		if(wins != null && wins.size() > 0){
			DesktopWindow firstWin = wins.get(0);

			return new TestBound[]{new TestBound(
					firstWin.x,
					firstWin.y,
					firstWin.width,
					firstWin.height), 
					channel.getSubDimension()};

		}
		return new TestBound[]{channel.getDimension(), channel.getSubDimension()};
	}

	@Override
	public void close() {
		((DesktopDriver)driver).closeAllWindows(channel.getProcessId());
		if(applicationProcess != null) {
			applicationProcess.destroyForcibly();
		}
	}

	@Override
	public void switchWindow(int index) {
		((DesktopDriver)driver).switchTo(channel, index);
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {
		((DesktopDriver)driver).closeWindow(channel, index);
	}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		status.setPassed(true);
		return null;
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		// Do nothing
	}

	@Override
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, String[] attributes,
			Predicate<Map<String, Object>> searchPredicate) {

		ArrayList<FoundElement> result = new ArrayList<FoundElement>();

		return result;
	}

	@Override
	public void switchToDefaultframe() {
		//do nothing
	}

	@Override
	public void forceScrollElement(FoundElement value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scroll(FoundElement element, int delta) {
		robot.mouseWheel(delta);
	}

	@Override
	public void middleClick(WebElement element) {
		robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
	}

	@Override
	public void doubleClick() {
		mouseClick(false);
		robot.delay(50);
		mouseClick(false);
	}

	@Override
	public void rightClick() {
		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
		robot.delay(50);
		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	}

	@Override
	public WebElement getRootElement() {
		return null;
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {

		Rectangle rect = foundElement.getRectangle();

		int offsetX = getOffsetX(rect, position) + foundElement.getScreenX().intValue();
		int offsetY = getOffsetY(rect, position) + foundElement.getScreenY().intValue();
		
		((DesktopDriver)driver).mouseMove(offsetX, offsetY);
	}

	@Override
	public void mouseClick(boolean hold) {
		//((DesktopDriver)driver).mousePress(1);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		if(!hold) {
			//((DesktopDriver)driver).mouseRelease(1);
			robot.delay(50);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		}
	}

	@Override
	public void keyDown(Keys key) {
		robot.keyPress(key.getCodePoint());
	}

	@Override
	public void keyUp(Keys key) {
		robot.keyRelease(key.getCodePoint());
	}

	@Override
	public void drop() {
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	@Override
	public void moveByOffset(int hDirection, int vDirection) {

		PointerInfo a = MouseInfo.getPointerInfo();
		java.awt.Point pt = a.getLocation();

		robot.mouseMove(pt.x + hDirection, pt.y + vDirection);
	}

	@Override
	protected void setPosition(Point pt) {
		channel.sleep(200);
		((DesktopDriver)driver).moveWindow(channel, pt);
	}

	@Override
	protected void setSize(Dimension size) {
		channel.sleep(200);
		((DesktopDriver)driver).resizeWindow(channel, size);
	}

	@Override
	public Alert switchToAlert() {
		return null;
	}

	@Override
	public void switchToDefaultContent() {
	}

	@Override
	public void navigationRefresh() {}

	@Override
	public void navigationForward() {}

	@Override
	public void navigationBack() {}

	@Override
	public String getCurrentUrl() {
		return null;
	}

	@Override
	public void sendTextData(ActionStatus status, FoundElement element, ArrayList<SendKeyData> textActionList,	boolean clear) {
		mouseMoveToElement(status, element, new MouseDirection());
		mouseClick(false);
		
		if(clear) {
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_A);
			
			robot.keyRelease(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			
			robot.keyPress(KeyEvent.VK_BACK_SPACE);
			robot.keyRelease(KeyEvent.VK_BACK_SPACE);
		}
		
		for(SendKeyData sequence : textActionList) {
			((DesktopDriver)driver).sendKeys(sequence.getSequenceDesktop());
		}
	}
}
