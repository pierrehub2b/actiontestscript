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

package com.ats.executor.channels;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.DesktopDriverEngine;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionGotoUrl;

public class Channel {

	private IDriverEngine engine;

	private String name;
	private boolean current = false;

	private ActionTestScript mainScript;

	private TestBound dimension;
	private TestBound subDimension;

	private String startError;

	private int maxTry = 0;

	private String applicationVersion;
	private String driverVersion;

	private ProcessHandle process = null;
	private DesktopDriver desktopDriver;

	//----------------------------------------------------------------------------------------------------------------------
	// Constructor
	//----------------------------------------------------------------------------------------------------------------------

	public Channel(
			ActionTestScript script,
			DriverManager driverManager, 
			String name, 
			String application) {

		this.mainScript = script;
		this.name = name;
		this.dimension = driverManager.getApplicationBound();
		this.current = true;

		this.desktopDriver = new DesktopDriver(driverManager);
		this.engine = driverManager.getDriverEngine(this, application, this.desktopDriver);

		//this.desktop = engine.isDesktop();

		this.maxTry = driverManager.getMaxTry();

		this.refreshLocation();
	}
	
	public int getHandle() {
		List<DesktopWindow> processWindows = desktopDriver.getWindowsByPid(getProcessId());
		if(processWindows != null && processWindows.size() > 0) {
			return processWindows.get(0).handle;
		}
		return -1;
	}

	public void refreshLocation(){

		Double[] winRect = desktopDriver.getWindowSize(getProcessId());

		TestBound[] dimensions = engine.getDimensions();
		TestBound mainDimension = dimensions[0];

		mainDimension.setX(winRect[0] + 7);
		mainDimension.setY(winRect[1]);

		setDimension(mainDimension);
		setSubDimension(dimensions[1]);
	}

	public void refreshMapElementLocation(){
		refreshLocation();
		desktopDriver.refreshElementMapLocation(this);
	}

	public void toFront(){
		desktopDriver.setWindowToFront(getProcessId());
	}

	public byte[] getScreenShot(){
		return screenShot(dimension);
	}

	public byte[] getScreenShot(TestBound dim) {
		dim.setX(dim.getX()+dimension.getX());
		dim.setY(dim.getY()+dimension.getY());

		return screenShot(dim);
	}

	private byte[] screenShot(TestBound dim) {
		mainScript.sleep(50);
		return desktopDriver.getScreenshotByte(dim.getX(), dim.getY(), dim.getWidth(), dim.getHeight());
		//return engine.getScreenShot(dim); 
	}

	public void setApplicationData(String version, String dVersion, long pid) {
		this.applicationVersion = version;
		this.driverVersion = dVersion;
		Optional<ProcessHandle> procs = ProcessHandle.of(pid);
		if(procs.isPresent()) {
			this.process = procs.get();
		}
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Elements
	//----------------------------------------------------------------------------------------------------------------------

	public FoundElement getElementFromPoint(Double x, Double y){
		return engine.getElementFromPoint(x, y);
	}

	public void loadParents(FoundElement hoverElement) {
		if(hoverElement != null) {
			engine.loadParents(hoverElement);
		}
	}

	public CalculatedProperty[] getCssAttributes(FoundElement element){
		return engine.getCssAttributes(element);
	}

	public CalculatedProperty[] getAttributes(FoundElement element){
		return engine.getAttributes(element);
	}
	
	public String getAttribute(FoundElement element, String attributeName, int maxTry){
		return engine.getAttribute(element, attributeName, maxTry);
	}

	//----------------------------------------------------------------------------------------------------------------------
	// logs
	//----------------------------------------------------------------------------------------------------------------------

	public void sendLog(int code, String message, Object value) {
		mainScript.sendLog(code, message, value);
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplication() {
		return engine.getApplication();
	}
	public void setApplication(String url) {} // read only	

	public String getApplicationPath() {
		return engine.getApplicationPath();
	}
	public void setApplicationPath(String url) {} // read only	

	public String getDriverVersion() {
		return driverVersion;
	}
	public void setDriverVersion(String url) {} // read only	

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean value) {
		this.current = value;
		if(value){
			toFront();
		}
	}

	public boolean isDesktop() {
		return engine instanceof DesktopDriverEngine;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public void setStartError(String error) {
		this.startError = error;
	}

	public String getStartError() {
		return startError;
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public TestBound getDimension() {
		return dimension;
	}

	public void setDimension(TestBound dimension) {
		this.dimension = dimension;
	}

	public void setProcessId(Long value) {
	}

	public Long getProcessId() {
		if(process != null) {
			return process.pid();
		}else {
			return -1L;
		}
	}

	public TestBound getSubDimension(){
		return subDimension;
	}

	public void setSubDimension(TestBound dimension){
		this.subDimension = dimension;
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void close(){
		engine.close();
		if(process != null) {
			process.descendants().forEach(p -> p.destroy());
			process.destroy();
		}
	}

	public void lastWindowClosed(ActionStatus status) {
		mainScript.closeChannel(status, name);
	}

	//----------------------------------------------------------------------------------------------------------
	// Browser's secific parameters
	//----------------------------------------------------------------------------------------------------------

	public void sleep(int ms){
		mainScript.sleep(ms);
	}

	public void actionTerminated(){
		engine.waitAfterAction();
	}

	//----------------------------------------------------------------------------------------------------------
	// driver actions
	//----------------------------------------------------------------------------------------------------------

	public WebElement getRootElement() {
		return engine.getRootElement();
	}

	public void switchToDefaultframe() {
		engine.switchToDefaultframe();
	}

	public void switchWindow(int index){
		engine.switchWindow(index);
	}

	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {
		engine.setWindowBound(x, y, width, height);
	}

	public void closeWindow(ActionStatus status, int index){
		engine.closeWindow(status, index);
	}

	public Object executeScript(ActionStatus status, String script, Object ... params){
		return engine.executeScript(status, script, params);
	}	
	
	public Alert switchToAlert() {
		return engine.switchToAlert();
	}
	
	public void switchToDefaultContent() {
		engine.switchToDefaultContent();
	}

	public void navigate(URL url, boolean newWindow) {
		engine.goToUrl(url, newWindow);
	}

	public void navigate(String type) {
		if(ActionGotoUrl.REFRESH.equals(type)) {
			engine.navigationRefresh();
		}else if(ActionGotoUrl.NEXT.equals(type)) {
			engine.navigationForward();
		}else if(ActionGotoUrl.BACK.equals(type)) {
			engine.navigationBack();
		}
	}

	public ArrayList<FoundElement> findWebElement(TestElement testObject, String tagName, String[] attributes, Predicate<Map<String, Object>> searchPredicate) {
		return engine.findWebElement(this, testObject, tagName, attributes, searchPredicate);
	}

	public ArrayList<FoundElement> findDesktopElement(String parentId, String tag, List<CalculatedProperty> attributes) {
		return desktopDriver.findElementByTag(parentId, tag, attributes, this);
	}

	//----------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------

	public void scroll(FoundElement foundElement, int delta) {
		engine.scroll(foundElement, delta);
	}

	public void middleClick(WebElement element) {
		engine.middleClick(element);
	}

	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {
		engine.mouseMoveToElement(status, foundElement, position);
		actionTerminated();
	}
	
	public void mouseClick(boolean hold) {
		engine.mouseClick(hold);
		actionTerminated();
	}

	public void sendTextData(ActionStatus status, FoundElement foundElement, ArrayList<SendKeyData> textActionList, boolean clear) {
		engine.sendTextData(status, foundElement, textActionList, clear);
		actionTerminated();
	}

	public int getMaxTry() {
		return maxTry;
	}

	public void forceScrollElement(FoundElement foundElement) {
		engine.forceScrollElement(foundElement);
	}

	public void keyDown(Keys key) {
		engine.keyDown(key);
	}

	public void keyUp(Keys key) {
		engine.keyUp(key);
	}

	public void drop() {
		engine.drop();
	}

	public void moveByOffset(int hDirection, int vDirection) {
		engine.moveByOffset(hDirection, vDirection);
	}

	public void doubleClick() {
		engine.doubleClick();
	}

	public void rightClick() {
		engine.rightClick();
	}

	public String getCurrentUrl() {
		return engine.getCurrentUrl();
	}
}