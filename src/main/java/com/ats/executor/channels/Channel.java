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
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.ActionApi;
import com.ats.script.actions.ActionChannelStart;
import com.ats.script.actions.neoload.ActionNeoload;
import com.ats.script.actions.neoload.ActionNeoloadStop;
import com.ats.tools.ResourceContent;
import com.ats.tools.logger.IExecutionLogger;

public class Channel {

	private IDriverEngine engine;

	private ActionChannelStart actionStart;
	private String application;
	private boolean current = false;

	private ActionTestScript mainScript;

	private int scrollUnit = DriverManager.ATS.getScrollUnit();
	private TestBound dimension = DriverManager.ATS.getApplicationBound();
	private TestBound subDimension = new TestBound();

	private String applicationVersion = "";
	private String driverVersion = "";
	private String os = "";

	private byte[] icon;
	private String screenServer;
	private ArrayList<String> operations = new ArrayList<String>();

	private int winHandle = -1;
	private long processId;

	private String neoloadDesignApi;

	//----------------------------------------------------------------------------------------------------------------------
	// Constructor
	//----------------------------------------------------------------------------------------------------------------------

	public Channel(
			ActionStatus status,
			ActionTestScript script,
			DriverManager driverManager, 
			ActionChannelStart action) {

		final DesktopDriver desktopDriver = new DesktopDriver(status, driverManager);

		if(status.isPassed()) {

			status.setChannel(this);
			
			this.mainScript = script;
			this.current = true;
			this.actionStart = action;
			this.application = action.getApplication().getCalculated();
			this.engine = driverManager.getDriverEngine(this, status, desktopDriver);

			if(status.isPassed()) {
				this.refreshLocation();
			}else {
				status.setChannel(null);
			}
		}
	}
	
	public String getTopScriptPackage() {
		final String topScriptName = mainScript.getTopScript().getTestName();
		return topScriptName.substring(0, topScriptName.lastIndexOf("."));
	}

	public ActionStatus newActionStatus() {
		return new ActionStatus(this);
	}

	public DesktopDriver getDesktopDriver() {
		return engine.getDesktopDriver();
	}

	public void cleanHandle() {
		winHandle = -1;
		toFront();
	}
	
	public void setWinHandle(int hdl) {
		winHandle = hdl;
	}
	
	public void updateWinHandle(DesktopDriver drv, int index) {
		winHandle = getHandle(drv, index);
	}

	public int getHandle(DesktopDriver drv) {
		if(winHandle < 0) {
			winHandle = getHandle(drv, 0);
		}
		return winHandle;
	}

	public int getHandle(DesktopDriver drv, int index) {
		List<DesktopWindow> processWindows = drv.getWindowsByPid(getProcessId());
		if(processWindows != null && processWindows.size() > index) {
			return processWindows.get(index).getHandle();
		}
		return -1;
	}

	public void refreshLocation(){
		engine.updateDimensions();
	}

	public void setDimensions(TestBound dim1, TestBound dim2) {
		setDimension(dim1);
		setSubDimension(dim2);
	}
	
	public double getOffsetY() {
		return dimension.getHeight() - subDimension.getHeight();
	}

	public void refreshMapElementLocation(){
		refreshLocation();
		engine.refreshElementMapLocation();
	}
	
	public void defineRoot(String id) {
		getDesktopDriver().defineRoot(dimension, id);
	}

	public void toFront(){
		if(engine.setWindowToFront()) {
			getDesktopDriver().setChannelToFront(getHandle(getDesktopDriver()), processId);
		}
	}

	public void rootKeys(ActionStatus status, String keys){
		getDesktopDriver().rootKeys(getHandle(getDesktopDriver()), keys);
		actionTerminated(status);
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
	
	public void setApplicationData(String os, String version, String dVersion, long pid, long handle) {
		setApplicationData(os, version, dVersion, pid, new byte[0], "");
		this.winHandle = (int) handle;
	}

	public String getAuthenticationValue() {
		return actionStart.getAuthenticationValue();
	}

	public void setNeoloadDesignApi(String value) {
		this.neoloadDesignApi = value;
	}

	//--------------------------------------------------------------------------------------------------
	// Api webservices init
	//--------------------------------------------------------------------------------------------------

	public void setApplicationData(String os) {
		this.os = os;
		this.icon = ResourceContent.getAtsByteLogo();
		this.driverVersion = AtsManager.getVersion();
		this.dimension = new TestBound(0D, 0D, 1D, 1D);
	}

	public void setApplicationData(String os, String type, ArrayList<String> operations) {
		this.os = os;
		this.application = type;
		this.operations = operations;
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public void setApplicationData(String os, String version, String dVersion, long pid, byte[] icon, String screenServer) {
		this.os = os;
		this.applicationVersion = version;
		this.driverVersion = dVersion;
		this.processId = pid;
		this.icon = icon;
		this.screenServer = screenServer;
	}

	public void setApplicationData(String os, String serviceType) {
		this.os = os;
		this.application = serviceType;
	}

	public void setApplicationData(String os, int handle) {
		this.os = os;
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
	
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h){
		return engine.getElementFromRect(syscomp, x, y, w, h);
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
		return engine.getAttributes(element, false);
	}
	
	public ArrayList<String[]> findSelectOptions(TestElement element){
				
		final ArrayList<String[]> result = new ArrayList<String[]>();
		final ArrayList<FoundElement> options = engine.findSelectOptions(element);

		if(options != null && options.size() > 0) {
			options.stream().forEachOrdered(e -> result.add(new String[]{e.getValue().getAttribute("value"), e.getValue().getAttribute("text")}));
		}
				
		return result;
	}

	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry){
		return engine.getAttribute(status, element, attributeName, maxTry + DriverManager.ATS.getMaxTryProperty());
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
		return application;
	}

	public void setApplication(String value) {
		this.application = value;
	}

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
		return actionStart.getName();
	}
	public void setName(String name) {} // read only

	public String getAuthentication() {
		if(actionStart.getAuthentication() != null && actionStart.getAuthenticationValue() != null && actionStart.getAuthentication().length() > 0 && actionStart.getAuthenticationValue().length() > 0) {
			return actionStart.getAuthentication();
		}
		return "";
	}
	public void setAuthentication(String value) {} // read only

	public boolean isNeoload() {
		return actionStart.isNeoload();
	}
	public void setNeoload(boolean neoload) {} // read only

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

		if(stopNeoloadRecord != null) {
			neoloadAction(stopNeoloadRecord);
		}

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

	public void actionTerminated(ActionStatus status){
		engine.waitAfterAction(status);
	}

	//----------------------------------------------------------------------------------------------------------
	// driver actions
	//----------------------------------------------------------------------------------------------------------

	public WebElement getRootElement() {
		return engine.getRootElement(this);
	}

	public void switchWindow(ActionStatus status, int index){
		engine.switchWindow(status, index);
		if(status.isPassed()) {
			engine.updateDimensions();
		}
	}

	public String setWindowBound(BoundData x, BoundData y, BoundData w, BoundData h) {
		String bounds = engine.setWindowBound(x, y, w, h);
		engine.updateDimensions();
		return bounds;
	}

	public void closeWindow(ActionStatus status){
		engine.closeWindow(status);
	}

	public void windowState(ActionStatus status, String state){
		engine.windowState(status, this, state);
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

	public void scroll(int delta) {
		engine.scroll(delta*scrollUnit);
	}
	
	public void scroll(FoundElement foundElement, int delta) {
		engine.scroll(foundElement, delta*scrollUnit);
	}

	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {
		engine.mouseMoveToElement(status, foundElement, position, false, 0, 0);
		actionTerminated(status);
	}

	//----------------------------------------------------------------------------------------------------------
	// Neoload
	//----------------------------------------------------------------------------------------------------------

	private ActionNeoloadStop stopNeoloadRecord = null;

	public void neoloadAction(ActionNeoload action) {
		action.execute(this);
		if(isNeoload()) {
			if(neoloadDesignApi != null) {
				action.executeRequest(this, neoloadDesignApi);
			}else {
				action.getStatus().setPassed(false);
				action.getStatus().setMessage("Neoload design API is not defined in .atsProperties !");
			}
		}else {
			action.getStatus().setPassed(true);
		}
	}

	public void setStopNeoloadRecord(ActionNeoloadStop value) {
		this.stopNeoloadRecord = value;
	}

	//----------------------------------------------------------------------------------------------------------
	// Visual reporting
	//----------------------------------------------------------------------------------------------------------

	public DesktopResponse startVisualRecord(ScriptHeader script, int quality, long started) {
		return getDesktopDriver().startVisualRecord(this, script, quality, started);
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

	public FoundElement getElementFromMousePoint() {
		return getDesktopDriver().getElementFromMousePoint(dimension);
	}
}