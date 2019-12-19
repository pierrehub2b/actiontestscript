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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.element.AtsBaseElement;
import com.ats.element.DesktopRootElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopData;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.DriverEngine;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;

public class DesktopDriverEngine extends DriverEngine implements IDriverEngine {

	private final static String PROCESS_PROTOCOL = "process://";
	private final static int DEFAULT_WAIT = 100;

	private DesktopWindow mainWindow;

	public DesktopDriverEngine(Channel channel, DesktopWindow window) {
		super(channel);
		this.mainWindow = window;
	}

	public DesktopDriverEngine(Channel channel, String application, DesktopDriver desktopDriver, ApplicationProperties props, int defaultWait) {
		super(channel, desktopDriver, application, props, DEFAULT_WAIT, 0);
		desktopDriver.setEngine(this);
	}

	public DesktopDriverEngine(Channel channel, ActionStatus status, String application, DesktopDriver desktopDriver, ApplicationProperties props) {

		this(channel, application, desktopDriver, props, DEFAULT_WAIT);

		long processId = -1;

		if(application.startsWith(PROCESS_PROTOCOL)) {

			final Pattern procPattern = Pattern.compile(application.substring(PROCESS_PROTOCOL.length()));

			processId = getProcessId(procPattern);
			int maxTry = 20;

			while (processId < 0 && maxTry > 0) {
				processId = getProcessId(procPattern);
				channel.sleep(200);
				maxTry--;
			}

			if(processId < 0) {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to attach process with command like -> " + procPattern.pattern());
				return;
			}

		}else {

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

				final ArrayList<String> args = new ArrayList<String>();
				args.add(applicationPath);
				channel.getArguments().forEach(c -> args.add(c.getCalculated()));

				Runtime rt = Runtime.getRuntime();
				
				try{

					Process proc = rt.exec(args.toArray(new String[args.size()]));
					StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");            
					StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

					errorGobbler.start();
					outputGobbler.start();

					processId = proc.pid();
					status.setNoError();

				} catch (Exception e) {
					status.setError(ActionStatus.CHANNEL_START_ERROR, e.getMessage());
					return;
				}

			}else {
				status.setError(ActionStatus.CHANNEL_START_ERROR, "app file path not found -> " + application);
				return;
			}
		}

		String appVersion = "N/A";
		String appBuildVersion = "N/A";
		String appName = "";

		final ArrayList<DesktopData> appInfo = desktopDriver.getVersion(applicationPath);
		for (DesktopData data : appInfo) {
			if("ApplicationBuildVersion".equals(data.getName())) {
				appBuildVersion = data.getValue();
			}else if("ApplicationVersion".equals(data.getName())) {
				appVersion = data.getValue();
			}else if("ApplicationName".equals(data.getName())) {
				appName = data.getValue();
			}
		}

		channel.setApplicationData(desktopDriver.getOsName() + " (" + desktopDriver.getOsVersion() +")", appName, appVersion + " (" + appBuildVersion + ")", desktopDriver.getDriverVersion(), processId);

		int maxTry = 30;
		while(maxTry > 0){
			if(channel.getHandle(desktopDriver) > 0) {
				maxTry = 0;
			}else {
				channel.sleep(200);
				maxTry--;
			}
		}

		desktopDriver.moveWindow(channel, channel.getDimension().getPoint());
		desktopDriver.resizeWindow(channel, channel.getDimension().getSize());
	}

	private long getProcessId(Pattern procPattern) {
		try {
			ProcessHandle proc = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent() && procPattern.matcher(p.info().command().get()).matches()).findFirst().get();
			applicationPath = proc.info().command().get();
			return proc.pid();
		}catch(Exception e) {
			return -1;
		}
	}

	@Override
	public void toFront() {
		channel.setWindowToFront();
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public void waitAfterAction(ActionStatus status) {
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
	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry) {
		return getDesktopDriver().getElementAttribute(element.getId(), attributeName);
	}

	@Override
	public CalculatedProperty[] getAttributes(FoundElement element, boolean reload){
		return getAttributes(element.getId());
	}

	public CalculatedProperty[] getAttributes(String elementId){
		return getDesktopDriver().getElementAttributes(elementId);
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return null;
	}

	@Override
	public ArrayList<FoundElement> findSelectOptions(TestBound dimension, TestElement element) {
		return getDesktopDriver().getChildren(dimension, element.getFoundElement().getId(), "ListItem");
	}

	@Override
	public void selectOptionsItem(ActionStatus status, TestElement element, CalculatedProperty selectProperty) {
		getDesktopDriver().selectItem(element.getFoundElement().getId(), selectProperty.getName(), selectProperty.getValue().getCalculated(), selectProperty.isRegexp());
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y){
		return getDesktopDriver().getElementFromPoint(x, y);
	}

	@Override
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h){
		return getDesktopDriver().getElementFromRect(x, y, w, h);
	}

	@Override
	public ArrayList<FoundElement> findElements(boolean sysComp, TestElement testElement, String tag, ArrayList<String> attributes, ArrayList<String> attributesValues, Predicate<AtsBaseElement> predicate, WebElement startElement) {
		if(sysComp) {
			if(SYSCOMP.equals(tag.toUpperCase())) {
				ArrayList<FoundElement> win = new ArrayList<FoundElement>();
				win.add(new FoundElement(mainWindow));
				return win;
			}else {
				return getDesktopDriver().findElements(channel, testElement, tag, attributesValues, predicate);
			}
		}else {
			return getDesktopDriver().findElements(channel, testElement, tag, attributesValues, predicate);
		}
	}

	@Override
	public void updateDimensions() {
		DesktopWindow win = getDesktopDriver().getWindowByHandle(channel.getHandle(desktopDriver));
		if(win != null && win.getWidth() != 9898 && win.getHeight() != 9898){
			channel.setDimensions(new TestBound(
					win.getX(),
					win.getY(),
					win.getWidth(),
					win.getHeight()), channel.getSubDimension());
		}
	}

	@Override
	public void close() {
		getDesktopDriver().closeWindows(channel.getProcessId());
		getDesktopDriver().closeDriver();
	}

	@Override
	public void switchWindow(ActionStatus status, int index) {
		getDesktopDriver().switchTo(channel, index);
		channel.updateWinHandle(getDesktopDriver(), index);
		status.setPassed(true);
	}

	@Override
	public void setWindowToFront() {
		//no window order management implemented for the moment
	}

	@Override
	public void closeWindow(ActionStatus status) {
		getDesktopDriver().closeWindow(channel);
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
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
		final Rectangle rect = foundElement.getRectangle();
		getDesktopDriver().mouseMove(
				getOffsetX(rect, position) + foundElement.getScreenX().intValue() - foundElement.getCenterWidth(), 
				getOffsetY(rect, position) + foundElement.getScreenY().intValue() - foundElement.getCenterHeight());
	}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		getDesktopDriver().mouseClick();
	}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection md, int offsetX, int offsetY) {
		getDesktopDriver().mouseDown();
		md.updateForDrag();
		mouseMoveToElement(status, element, md, true, 0, 0);
	}

	@Override
	public void drop(MouseDirection md, boolean desktopDriver) {
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
		mouseMoveToElement(status, element, new MouseDirection(), false, 0, 0);
		mouseClick(status, element, null, 0, 0);
		getDesktopDriver().clearText();
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			getDesktopDriver().sendKeys(sequence.getSequenceDesktop());
		}
	}

	@Override
	public void refreshElementMapLocation() {
		getDesktopDriver().refreshElementMapLocation(channel);
	}

	//--------------------------------------------------
	//do nothing with followings methods for the moment ....
	//--------------------------------------------------

	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		status.setPassed(true);
		return null;
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {} // open default browser ?

	@Override
	public WebElement getRootElement(Channel cnl) {
		return new DesktopRootElement(getDesktopDriver().getRootElement(cnl));
	}

	@Override
	public void scroll(FoundElement element) {}

	@Override
	public void scroll(int delta) {
		getDesktopDriver().mouseWheel(delta);
	}

	@Override
	public void scroll(FoundElement element, int delta) {
		getDesktopDriver().mouseWheel(delta);
	}

	@Override
	public Alert switchToAlert() {
		return null;
	}

	@Override
	public boolean switchToDefaultContent() {return true;}

	@Override
	public void switchToFrameId(String id) {}

	@Override
	public String getSource() {
		getDesktopDriver().refreshElementMap(channel);
		return getDesktopDriver().getSource();
	}

	@Override
	public void api(ActionStatus status, ActionApi api) {}

	@Override
	public void buttonClick(String id) {}

	@Override
	public void windowState(ActionStatus status, Channel channel, String state) {
		getDesktopDriver().windowState(status, channel, state);
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, TestElement element) {
		return getDesktopDriver().executeScript(status, script, element.getFoundElement());
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, boolean returnValue) {
		status.setPassed(true);
		return null;
	}

	class StreamGobbler extends Thread
	{
		InputStream is;
		String type;

		StreamGobbler(InputStream is, String type)
		{
			this.is = is;
			this.type = type;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
					System.out.println(type + ">" + line);    
			} catch (IOException ioe)
			{
				ioe.printStackTrace();  
			}
		}
	}
}