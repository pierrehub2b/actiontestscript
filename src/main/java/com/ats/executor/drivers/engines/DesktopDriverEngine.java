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
	
	private String osVersion;
	private String osName;

	public DesktopDriverEngine(Channel channel, String application, DesktopDriver desktopDriver, AtsManager ats) {

		super(channel, application, ats, DEFAULT_WAIT);
		
		this.driver = desktopDriver;

		int firstSpace = application.indexOf(" ");
		String applicationArguments = "";

		ArrayList<String> args = new ArrayList<String>();
		
		if(firstSpace > 0){
			applicationArguments = application.substring(firstSpace);
			application = application.substring(0, firstSpace);
			args.add(applicationArguments);
		}

		URI fileUri = null;
		File exeFile = null;

		if(applicationPath != null) {
			exeFile = new File(applicationPath);
			if(exeFile.exists() && exeFile.isFile()) {
				fileUri = exeFile.toURI();
			}
		}

		if(fileUri == null) {
			try {
				fileUri = new URI(application);
				exeFile = new File(fileUri);
			} catch (URISyntaxException | IllegalArgumentException e1) {}
		}

		if(exeFile == null) {//last chance to find exe file ....
			exeFile = new File(application);
		}

		if(exeFile != null && exeFile.exists() && exeFile.isFile()){

			applicationPath = exeFile.getAbsolutePath();
			args.add(0, applicationPath);

			Runtime runtime = Runtime.getRuntime();
			try{
				applicationProcess = runtime.exec(args.toArray(new String[args.size()]));
			} catch (IOException e) {
				channel.setStartError(e.getMessage());
				return;
			}

			String driverVersion = "N/A";
			String appVersion = "N/A";

			ArrayList<DesktopData> capabilities = desktopDriver.getVersion(applicationPath);
			for (DesktopData data : capabilities) {
				if("DriverVersion".equals(data.getName())) {
					driverVersion = data.getValue();
				}else if("ApplicationVersion".equals(data.getName())) {
					appVersion = data.getValue();
				}else if("BuildNumber".equals(data.getName())) {
					osVersion = data.getValue();
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
		actionWait();
	}
	
	@Override
	public DesktopDriver getDesktopDriver() {
		return (DesktopDriver)driver;
	}

	public void loadParents(FoundElement hoverElement){
		hoverElement.setParent(getDesktopDriver().getTestElementParent(hoverElement.getId(), channel));
	}

	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		return getDesktopDriver().getElementAttribute(element.getId(), attributeName);
	}

	public CalculatedProperty[] getAttributes(FoundElement element){
		return getAttributes(element.getId());
	}

	public CalculatedProperty[] getAttributes(String elementId){
		return getDesktopDriver().getElementAttributes(elementId);
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	public FoundElement getElementFromPoint(Double x, Double y){
		return getDesktopDriver().getElementFromPoint(x, y);
	}

	@Override
	public TestBound[] getDimensions() {

		List<DesktopWindow> wins = getDesktopDriver().getWindowsByPid(channel.getProcessId());
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
		getDesktopDriver().closeAllWindows(channel.getProcessId());
		if(applicationProcess != null) {
			applicationProcess.destroyForcibly();
		}
	}

	@Override
	public void switchWindow(int index) {
		getDesktopDriver().switchTo(channel, index);
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {
		getDesktopDriver().closeWindow(channel, index);
	}

	@Override
	public void scroll(FoundElement element, int delta) {
		getDesktopDriver().mouseWheel(delta);
	}

	@Override
	public void middleClick(ActionStatus status, TestElement element) {
		getDesktopDriver().mouseMiddleClick();
	}

	@Override
	public void doubleClick() {
		getDesktopDriver().doubleClick();
	}

	@Override
	public void rightClick() {
		getDesktopDriver().mouseRightClick();
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {

		Rectangle rect = foundElement.getRectangle();

		int offsetX = getOffsetX(rect, position) + foundElement.getScreenX().intValue();
		int offsetY = getOffsetY(rect, position) + foundElement.getScreenY().intValue();
		
		getDesktopDriver().mouseMove(offsetX, offsetY);
	}

	@Override
	public void mouseClick(boolean hold) {
		if(hold) {
			getDesktopDriver().mouseDown();
		}else {
			getDesktopDriver().mouseClick();
		}
	}
	
	@Override
	public void drop() {
		getDesktopDriver().mouseRelease();
	}

	@Override
	public void keyDown(Keys key) {
		getDesktopDriver().keyDown(key.getCodePoint());
	}

	@Override
	public void keyUp(Keys key) {
		getDesktopDriver().keyUp(key.getCodePoint());
	}

	@Override
	public void moveByOffset(int hDirection, int vDirection) {

		PointerInfo a = MouseInfo.getPointerInfo();
		java.awt.Point pt = a.getLocation();

		getDesktopDriver().mouseMove(pt.x + hDirection, pt.y + vDirection);
	}

	@Override
	protected void setPosition(Point pt) {
		getDesktopDriver().moveWindow(channel, pt);
	}

	@Override
	protected void setSize(Dimension size) {
		getDesktopDriver().resizeWindow(channel, size);
	}
		
	@Override
	public void sendTextData(ActionStatus status, FoundElement element, ArrayList<SendKeyData> textActionList,	boolean clear) {
		mouseMoveToElement(status, element, new MouseDirection());
		mouseClick(false);
		
		if(clear) {
			getDesktopDriver().clearText();
		}
		
		for(SendKeyData sequence : textActionList) {
			getDesktopDriver().sendKeys(sequence.getSequenceDesktop());
		}
	}
	
	@Override
	public void setWindowToFront() {
		//no window order management implemented for the moment
	}

	//--------------------------------------------------
	//do nothing with followings methods for the moment ....
	//--------------------------------------------------
	
	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		status.setPassed(true);
		return null;
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {} // open default browser ?

	@Override
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, String[] attributes,
			Predicate<Map<String, Object>> searchPredicate) {
		return new ArrayList<FoundElement>();
	}
	
	@Override
	public WebElement getRootElement() {
		return null;
	}

	@Override
	public void forceScrollElement(FoundElement value) {}
	
	@Override
	public Alert switchToAlert() {
		return null;
	}

	@Override
	public void switchToDefaultContent() {}

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
}