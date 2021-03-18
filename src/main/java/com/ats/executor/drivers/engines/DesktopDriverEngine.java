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
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.element.AtsBaseElement;
import com.ats.element.DesktopRootElement;
import com.ats.element.DialogBox;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.desktop.DesktopAlert;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;

public class DesktopDriverEngine extends DriverEngine implements IDriverEngine {

	private final static String DESKTOP_TYPE = "desktop";
	private final static int DEFAULT_WAIT = 100;

	protected DesktopWindow window;
	private int windowIndex = -1;

	public DesktopDriverEngine(Channel channel, DesktopWindow window) {
		super(channel);
		this.window = window;
	}

	public DesktopDriverEngine(Channel channel, DesktopDriver desktopDriver, ApplicationProperties props, int defaultWait) {
		super(channel, desktopDriver, props, DEFAULT_WAIT, 0);
		desktopDriver.setEngine(this);
	}

	public DesktopDriverEngine(Channel channel, ActionStatus status, String application, DesktopDriver desktopDriver, ApplicationProperties props) {

		this(channel, desktopDriver, props, DEFAULT_WAIT);

		if(application.startsWith(DESKTOP_TYPE)) {

			channel.setApplicationData(desktopDriver.getOsName() + " (" + desktopDriver.getOsVersion() +")", "", desktopDriver.getDriverVersion(), 0L);
			final FoundElement desktop = desktopDriver.getRootElement(-1);
			channel.setDimensions(desktop.getTestScreenBound(), desktop.getTestScreenBound());
		
		}else {
						
			final ArrayList<String> args = new ArrayList<String>(Arrays.asList(application));
			channel.getArguments().forEach(c -> args.add(c.getCalculated()));
						
			final DesktopResponse resp = getDesktopDriver().startApplication(args);
			if(resp.errorCode == 0) {
				window = resp.getWindow();
				if(window != null) {
					channel.setApplicationData(desktopDriver.getOsName() + " (" + desktopDriver.getOsVersion() +")", window.getAppName(), window.getAppVersion() + " (" + window.getAppBuildVersion() + ")", desktopDriver.getDriverVersion(), window.getPid(), window.getHandle(), window.getAppIcon());
					windowIndex = 0;
					applicationPath = window.getAppPath();
										
					desktopDriver.moveWindow(channel, channel.getDimension().getPoint());
					desktopDriver.resizeWindow(channel, channel.getDimension().getSize());
				}else {
					status.setError(ActionStatus.CHANNEL_START_ERROR, "no window found for this application");
				}
			}else {
				status.setError(ActionStatus.CHANNEL_START_ERROR, resp.errorMessage);
			}
		}
	}

	public void setWindow(DesktopWindow window) {
		this.window = window;
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

	@Override
	public void setSysProperty(String propertyName, String propertyValue) {

	}

	public CalculatedProperty[] getAttributes(String elementId){
		return getDesktopDriver().getElementAttributes(elementId);
	}

	@Override
	public List<String[]> loadSelectOptions(TestElement element) {
		final ArrayList<String[]> result = new ArrayList<String[]>();
		final List<FoundElement> options = findSelectOptions(channel.getDimension(), element);

		if(options != null && options.size() > 0) {
			options.stream().forEachOrdered(e -> result.add(e.getItemAttribute()));
		}
		return result;
	}

	@Override
	public List<FoundElement> findSelectOptions(TestBound dimension, TestElement element) {
		return getDesktopDriver().getListItems(dimension, element.getFoundElement().getId());
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
	public List<FoundElement> findElements(boolean sysComp, TestElement testElement, String tag, String[] attributes, String[] attributesValues, Predicate<AtsBaseElement> predicate, WebElement startElement) {
		if(sysComp) {
			if(SYSCOMP.equals(tag.toUpperCase())) {
				return new ArrayList<FoundElement>(List.of(new FoundElement(window)));
			}else {
				return getDesktopDriver().findElements(channel, testElement, tag, attributesValues, predicate);
			}
		}else {
			return getDesktopDriver().findElements(channel, testElement, tag, attributesValues, predicate);
		}
	}

	@Override
	public void updateDimensions() {
		final DesktopWindow win = getDesktopDriver().getWindowByHandle(channel.getHandle(desktopDriver));
		if(win != null && win.getWidth() > 0 && win.getHeight() > 0){
			channel.setDimensions(new TestBound(
					win.getX(),
					win.getY(),
					win.getWidth(),
					win.getHeight()), channel.getSubDimension());
		}
	}

	@Override
	public void close(boolean keepRunning) {
		if(getDesktopDriver() != null) {
			getDesktopDriver().closeWindows(channel.getProcessId(), channel.getHandle());
			getDesktopDriver().closeDriver();
		}
	}

	@Override
	public void switchWindow(ActionStatus status, int index, int tries) {

		DesktopResponse resp = getDesktopDriver().switchTo(channel.getProcessId(), index, channel.getHandle());
		int maxTry = 1 + tries;
		while(resp.errorCode == ActionStatus.WINDOW_NOT_FOUND && maxTry > 0) {
			channel.sleep(1000);
			resp = getDesktopDriver().switchTo(channel.getProcessId(), index, channel.getHandle());
			maxTry--;
		}

		if(resp.errorCode == ActionStatus.WINDOW_NOT_FOUND) {
			status.setError(ActionStatus.WINDOW_NOT_FOUND, "cannot switch to window index '" + index + "'");
		}else {
			channel.updateWinHandle(getDesktopDriver(), index);
			windowIndex = index;
			status.setPassed(true);
		}
	}

	@Override
	public void setWindowToFront() {
		channel.toFront();
		getDesktopDriver().switchTo(channel.getProcessId(), windowIndex, channel.getHandle());
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
		getDesktopDriver().drag();
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
	public void clearText(ActionStatus status, TestElement te, MouseDirection md) {

		final FoundElement element = te.getFoundElement();

		mouseMoveToElement(status, element, md, false, 0, 0);
		mouseClick(status, element, null, 0, 0);

		getDesktopDriver().clearText(te.getWebElementId());
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			if(sequence.getDownKey() != null) {
				getDesktopDriver().keyDown(sequence.getDownKey().getCodePoint());
				getDesktopDriver().sendKeys(sequence.getSequenceDesktop(), element.getWebElementId());
				getDesktopDriver().keyUp(sequence.getDownKey().getCodePoint());
			} else {
				getDesktopDriver().sendKeys(sequence.getSequenceDesktop(), element.getWebElementId());
			}
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
	public void goToUrl(ActionStatus status, String url) {
		getDesktopDriver().gotoUrl(status, window.getHandle(), url);
	}

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
	public DialogBox switchToAlert() {
		return new DesktopAlert(this, channel.getDimension());
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
	public void buttonClick(ActionStatus status, String id) {}

	@Override
	public void tap(int count, FoundElement element) {}

	@Override
	public void press(int duration, ArrayList<String> paths, FoundElement element) {}

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

	public List<FoundElement> getDialogBox() {
		return getDesktopDriver().getDialogBox(channel.getDimension());
	}

	@Override
	public int getNumWindows() {
		return getDesktopDriver().getWindowsByPid(channel.getProcessId()).size();
	}

	@Override
	public String getUrl() {
		return applicationPath;
	}

	@Override
	public Rectangle getBoundRect(TestElement testElement) {
		return null;
	}
}