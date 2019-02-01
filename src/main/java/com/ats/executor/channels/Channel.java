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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.ActionApi;
import com.ats.tools.ResourceContent;
import com.ats.tools.logger.IExecutionLogger;

public class Channel {

	private IDriverEngine engine;

	private String name;
	private boolean current = false;

	private ActionTestScript mainScript;

	private int scrollUnit = DriverManager.ATS.getScrollUnit();
	private TestBound dimension = DriverManager.ATS.getApplicationBound();
	private TestBound subDimension;

	private String applicationVersion;
	private String driverVersion;
	private String os;
	
	private byte[] icon;
	private String screenServer;
	private ArrayList<String> operations = new ArrayList<String>();

	private int winHandle = -1;
	private long processId;

	//----------------------------------------------------------------------------------------------------------------------
	// Constructor
	//----------------------------------------------------------------------------------------------------------------------

	public Channel(
			ActionStatus status,
			ActionTestScript script,
			DriverManager driverManager, 
			String name, 
			String application) {

		status.setChannel(this);

		this.mainScript = script;

		this.name = name;
		this.current = true;

		this.engine = driverManager.getDriverEngine(this, status, application, new DesktopDriver(driverManager));

		if(status.isPassed()) {
			this.refreshLocation();
		}else {
			this.mainScript.sendLog(ActionStatus.CHANNEL_START_ERROR, status.getMessage());
		}
	}
	
	public ActionStatus newActionStatus() {
		return new ActionStatus(this);
	}

	public DesktopDriver getDesktopDriver() {
		return engine.getDesktopDriver();
	}

	public int getHandle(DesktopDriver drv) {
		if(winHandle > 0) {
			return winHandle;
		}else {
			return getHandle(drv, 0);
		}
	}
	
	public int getHandle(DesktopDriver drv, int index) {
		List<DesktopWindow> processWindows = drv.getWindowsByPid(getProcessId());
		if(processWindows != null && processWindows.size() > index) {
			return processWindows.get(index).handle;
		}
		return -1;
	}

	public void refreshLocation(){
		engine.updateDimensions(this);
	}

	public void setDimensions(TestBound dim1, TestBound dim2) {
		setDimension(dim1);
		setSubDimension(dim2);
	}

	public void refreshMapElementLocation(){
		refreshLocation();
		engine.refreshElementMapLocation(this);
	}

	public void toFront(){
		if(engine.setWindowToFront()) {
			getDesktopDriver().setChannelToFront(getHandle(engine.getDesktopDriver()));
		}
	}

	public byte[] getScreenShot(){
		return screenShot(dimension);
	}
	
	public String getSource(){
		return engine.getSource();
	}

	public byte[] getScreenShot(TestBound dim) {
		dim.setX(dim.getX()+dimension.getX());
		dim.setY(dim.getY()+dimension.getY());

		return screenShot(dim);
	}

	private byte[] screenShot(TestBound dim) {
		mainScript.sleep(50);
		return getDesktopDriver().getScreenshotByte(dim.getX(), dim.getY(), dim.getWidth(), dim.getHeight());
	}
	
	public void setApplicationData(String os, String version, String dVersion, long pid) {
		setApplicationData(os, version, dVersion, pid, new byte[0], "");
	}
	
	public void setApplicationData(String os) {
		this.os = os;
		this.icon = ResourceContent.getAtsByteLogo();
		this.driverVersion = AtsManager.getVersion();
	}
	
	public void setApplicationData(String os, ArrayList<String> operations) {
		this.setApplicationData(os);
		this.operations = operations;
	}
	
	public void setApplicationData(String os, String version, String dVersion, long pid, byte[] icon, String screenServer) {
		this.os = os;
		this.applicationVersion = version;
		this.driverVersion = dVersion;
		this.processId = pid;
		this.icon = icon;
		this.screenServer = screenServer;
	}

	public void setApplicationData(int handle) {
		this.applicationVersion = "";
		this.driverVersion = "";
		this.winHandle = handle;
	}

	public void switchToFrame(String id) {
		engine.switchToFrameId(id);
	}
	
	public void clearData() {
		icon = null;
		operations.clear();
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Elements
	//----------------------------------------------------------------------------------------------------------------------

	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y){
		return engine.getElementFromPoint(syscomp, x, y);
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
		return engine.getAttribute(element, attributeName, maxTry + DriverManager.ATS.getMaxTryProperty());
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

	public ArrayList<String> getOperations() {
		return operations;
	}

	public void setOperations(ArrayList<String> operations) {
		this.operations = operations;
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

	public boolean isDesktop() {
		return engine instanceof DesktopDriverEngine;
	}
	
	public void setDesktop(boolean value) {} // read only

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean value) {
		this.current = value;
		if(value){
			toFront();
		}
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}
	
	public byte[] getIcon() {
		return icon;
	}
	
	public void setIcon(byte[] value) {
		this.icon = value;
	}
	
	public String getScreenServer() {
		return screenServer;
	}
	
	public void setScreenServer(String value) {
		this.screenServer = value;
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
		return processId;
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
		close(newActionStatus());
	}
	
	public void close(ActionStatus status){
		engine.close();
		mainScript.getChannelManager().channelClosed(status, this);
	}

	//----------------------------------------------------------------------------------------------------------
	// Browser's secific parameters
	//----------------------------------------------------------------------------------------------------------

	public void progressiveWait(int value) {
		sleep(200 + value*50);
	}
	
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

	public void navigate(ActionStatus status, String url) {
		engine.goToUrl(status, url);
	}
	
	public void api(ActionStatus status, ActionApi api) {
		engine.api(status, api);
	}

	public IDriverEngine getDesktopDriverEngine() {
		return getDesktopDriver().getEngine();
	}

	public IDriverEngine getDriverEngine() {
		return engine;
	}

	//----------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------

	public void scroll(FoundElement foundElement, int delta) {
		engine.scroll(foundElement, delta*scrollUnit);
	}

	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {
		engine.mouseMoveToElement(status, foundElement, position);
		actionTerminated();
	}

	//----------------------------------------------------------------------------------------------------------
	// Visual reporting
	//----------------------------------------------------------------------------------------------------------

	public void startVisualRecord(ScriptHeader script, int quality, long started) {
		getDesktopDriver().startVisualRecord(this, script, quality, started);
	}

	public void stopVisualRecord() {
		getDesktopDriver().stopVisualRecord();
	}
	
	public void saveVisualReportFile(Path path, String fileName, IExecutionLogger logger) {
		getDesktopDriver().saveVisualReportFile(path.resolve(fileName), logger);
	}

	public void createVisualAction(String actionName, int scriptLine, long timeline) {
		getDesktopDriver().createVisualAction(this, actionName, scriptLine, timeline);
	}

	public void updateVisualAction(boolean isRef) {
		getDesktopDriver().updateVisualImage(dimension, isRef);
	}

	public void updateVisualAction(String value) {
		getDesktopDriver().updateVisualValue(value);
	}

	public void updateVisualAction(String value, String data) {
		getDesktopDriver().updateVisualData(value, data);
	}

	public void updateVisualAction(String type, MouseDirectionData hdir, MouseDirectionData vdir) {
		getDesktopDriver().updateVisualPosition(type, hdir, vdir);
	}

	public void updateVisualAction(TestElement element) {
		getDesktopDriver().updateVisualElement(element);
	}

	public void updateVisualAction(int error, long duration) {
		getDesktopDriver().updateVisualStatus(error, duration);
	}
	
	public void updateVisualAction(int error, long duration, String value) {
		getDesktopDriver().updateVisualStatus(error, duration);
		getDesktopDriver().updateVisualValue(value);
	}
	
	public void updateVisualAction(int error, long duration, String value, String data) {
		getDesktopDriver().updateVisualStatus(error, duration);
		getDesktopDriver().updateVisualData(value, data);
	}
}