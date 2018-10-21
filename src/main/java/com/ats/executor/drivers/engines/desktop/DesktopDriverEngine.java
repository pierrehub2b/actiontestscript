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

package com.ats.executor.drivers.engines.desktop;

import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.driver.AtsManager;
import com.ats.element.AtsBaseElement;
import com.ats.element.AtsElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopData;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.DriverEngineAbstract;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;

public class DesktopDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	private final static int DEFAULT_WAIT = 100;

	private Process applicationProcess = null;
	
	private String osVersion;
	private String osName;

	public DesktopDriverEngine(Channel channel, String application, DesktopDriver desktopDriver, ApplicationProperties applicationProperties, int defaultWait) {
		super(channel, desktopDriver, application, applicationProperties, DEFAULT_WAIT, 0);
		desktopDriver.setEngine(this);
	}
	
	public DesktopDriverEngine(Channel channel, ActionStatus status, String application, DesktopDriver desktopDriver, AtsManager ats) {

		this(channel, application, desktopDriver, ats.getApplicationProperties(application), DEFAULT_WAIT);

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

			final Runtime runtime = Runtime.getRuntime();
			try{
				
				applicationProcess = runtime.exec(args.toArray(new String[args.size()]));
				status.setPassed(true);
				
			} catch (IOException e) {
				
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage(e.getMessage());
				status.setPassed(false);
				
				return;
			}

			String driverVersion = "N/A";
			String appVersion = "N/A";

			final ArrayList<DesktopData> capabilities = desktopDriver.getVersion(applicationPath);
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

			Icon icon = FileSystemView.getFileSystemView().getSystemIcon(exeFile);
			
			channel.setApplicationData("windows", appVersion, driverVersion, applicationProcess.pid());

			int maxTry = 30;
			while(maxTry > 0){
				if(channel.getHandle() > 0) {
					maxTry = 0;
				}else {
					channel.sleep(200);
					maxTry--;
				}
			}

			desktopDriver.moveWindow(channel, channel.getDimension().getPoint());
			desktopDriver.resizeWindow(channel, channel.getDimension().getSize());
		}
	}
	
	public String getOsVersion() {
		return osVersion;
	}

	public String getOsName() {
		return osName;
	}
	
	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public void waitAfterAction() {
		actionWait();
	}
	
	@Override
	public DesktopDriver getDesktopDriver() {
		return (DesktopDriver)driver;
	}

	@Override
	public void loadParents(FoundElement hoverElement){
		hoverElement.setParent(getDesktopDriver().getTestElementParent(hoverElement.getId(), channel));
	}

	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		return getDesktopDriver().getElementAttribute(element.getId(), attributeName);
	}

	@Override
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

	@Override
	public FoundElement getElementFromPoint(Double x, Double y){
		return getDesktopDriver().getElementFromPoint(x, y);
	}
	
	@Override
	public ArrayList<FoundElement> findElements(Channel channel, TestElement testElement, String tag, ArrayList<String> attributes, Predicate<AtsBaseElement> predicate) {
		return getDesktopDriver().findElements(channel, testElement, tag, attributes, predicate);
	}

	@Override
	public void updateDimensions(Channel cnl) {

		DesktopWindow win = getDesktopDriver().getWindowByHandle(channel.getHandle());
		if(win != null){
			cnl.setDimensions(new TestBound(
					win.getX(),
					win.getY(),
					win.getWidth(),
					win.getHeight()), channel.getSubDimension());
		}
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
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
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
		final Rectangle rect = foundElement.getRectangle();
		getDesktopDriver().mouseMove(
				getOffsetX(rect, position) + foundElement.getScreenX().intValue(), 
				getOffsetY(rect, position) + foundElement.getScreenY().intValue());
	}

	@Override
	public void mouseClick(FoundElement element, MouseDirection position, boolean hold) {
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
		final java.awt.Point pt = MouseInfo.getPointerInfo().getLocation();
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
	public void clearText(ActionStatus status, FoundElement element) {
		mouseMoveToElement(status, element, new MouseDirection());
		mouseClick(element, null, false);
		getDesktopDriver().clearText();
	}
	
	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			getDesktopDriver().sendKeys(sequence.getSequenceDesktop());
		}
	}
	
	@Override
	public void refreshElementMapLocation(Channel channel) {
		getDesktopDriver().refreshElementMapLocation(channel);
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
	public void goToUrl(ActionStatus status, String url) {} // open default browser ?
	
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
	public void switchToFrameId(String id) {}

}