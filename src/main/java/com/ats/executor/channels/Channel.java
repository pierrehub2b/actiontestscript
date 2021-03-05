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

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;

import com.ats.driver.AtsManager;
import com.ats.element.DialogBox;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.ScriptStatus;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.DesktopDriverEngine;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.ATS;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.ReportSummary;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionApi;
import com.ats.script.actions.ActionChannelStart;
import com.ats.script.actions.ActionExecute;
import com.ats.script.actions.neoload.ActionNeoload;
import com.ats.script.actions.neoload.ActionNeoloadStop;
import com.ats.script.actions.performance.octoperf.ActionOctoperfVirtualUser;
import com.ats.tools.ResourceContent;
import com.ats.tools.logger.ExecutionLogger;
import com.ats.tools.performance.proxy.AtsNoProxy;
import com.ats.tools.performance.proxy.AtsProxy;
import com.ats.tools.performance.proxy.IAtsProxy;
import com.google.gson.JsonArray;

public class Channel {

	private IDriverEngine engine;

	private ActionChannelStart actionStart;
	private boolean current = false;

	private ActionTestScript mainScript;

	private int scrollUnit = AtsManager.getScrollUnit();
	private TestBound dimension = AtsManager.getInstance().getApplicationBound();
	private TestBound subDimension = new TestBound();

	private String driverVersion = "";

	private SystemValues systemValues = new SystemValues();

	private byte[] icon;
	private String screenServer;
	private ArrayList<String> operations = new ArrayList<String>();

	private int winHandle = -1;
	private long processId = 0;	

	private String neoloadDesignApi;

	private ArrayList<String> systemProperties = new ArrayList<>();
	public void addSystemProperties(JsonArray info) {
		if(info != null) {
			for (int i = 0; i < info.size(); i++) {
				systemProperties.add(info.get(i).getAsString());
			}
		}
	}

	private ArrayList<String> systemButtons = new ArrayList<>();
	public void addSystemButtons(JsonArray info) {
		if(info != null) {
			for (int i = 0; i < info.size(); i++) {
				systemButtons.add(info.get(i).getAsString());
			}
		}
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Constructor
	//----------------------------------------------------------------------------------------------------------------------

	public Channel() {}

	public Channel(
			ActionStatus status,
			ActionTestScript script,
			DriverManager driverManager, 
			ActionChannelStart action) {

		final DesktopDriver desktopDriver = new DesktopDriver(status, driverManager);

		if(status.isPassed()) {

			status.setChannel(this);
			
			systemValues.setOsName(desktopDriver.getOsName());
			systemValues.setApplicationName(action.getApplication().getCalculated());
			systemValues.setCountry(desktopDriver.getCountryCode());
			systemValues.setMachineName(desktopDriver.getMachineName());
			systemValues.setOsVersion(desktopDriver.getOsVersion());
			systemValues.setOsBuild(desktopDriver.getOsBuildVersion());
			
			this.mainScript = script;
			this.current = true;
			this.actionStart = action;
			this.engine = driverManager.getDriverEngine(this, status, desktopDriver);

			if(status.isPassed()) {
				
				systemValues.setApplicationPath(engine.getApplicationPath());
				
				refreshLocation();
				engine.started(status);
				
			}else {
				status.setChannel(null);
			}
		}
	}

	public void waitBeforeMouseMoveToElement(WebDriverEngine webDriverEngine) {
		AtsManager.getInstance().getWaitGuiReady().waitBeforeMouseMoveToElement(this, webDriverEngine);
	}

	public void waitBeforeSwitchWindow(WebDriverEngine webDriverEngine) {
		AtsManager.getInstance().getWaitGuiReady().waitBeforeSwitchWindow(this, webDriverEngine);
	}

	public void waitBeforeSearchElement(WebDriverEngine webDriverEngine) {
		AtsManager.getInstance().getWaitGuiReady().waitBeforeSearchElement(this, webDriverEngine);
	}

	public void waitBeforeEnterText(WebDriverEngine webDriverEngine) {
		AtsManager.getInstance().getWaitGuiReady().waitBeforeEnterText(this, webDriverEngine);
	}

	public void waitBeforeGotoUrl(WebDriverEngine webDriverEngine) {
		AtsManager.getInstance().getWaitGuiReady().waitBeforeGotoUrl(this, webDriverEngine);
	}

	public Class<ActionTestScript> loadTestScriptClass(String name){
		return AtsManager.getInstance().loadTestScriptClass(name);
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public String getTopScriptPackage() {
		final String topScriptName = mainScript.getTopScript().getTestName();
		final int lastDot = topScriptName.lastIndexOf(".");

		if(lastDot > 0) {
			return topScriptName.substring(0, lastDot);
		}else {
			return topScriptName;
		}
	}

	public ActionStatus newActionStatus() {
		return new ActionStatus(this, "", 0);
	}

	public ActionStatus newActionStatus(String testName, int testLine) {
		return new ActionStatus(this, testName, testLine);
	}

	public DesktopDriver getDesktopDriver() {
		return engine.getDesktopDriver();
	}

	public void cleanHandle() {
		winHandle = -1;
		setWindowToFront();
	}

	public void setWinHandle(int hdl) {
		winHandle = hdl;
	}

	public void updateWinHandle(DesktopDriver drv, int index) {
		winHandle = getHandle(drv, index);
		getDesktopDriver().updateWindowHandle(this);
	}

	public int getHandle(DesktopDriver drv) {
		if(winHandle < 0) {
			winHandle = getHandle(drv, 0);
		}
		return winHandle;
	}
	
	public int getHandle() {
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
		getDesktopDriver().setChannelToFront(getHandle(getDesktopDriver()), processId);
	}

	public void setWindowToFront(){
		engine.setWindowToFront();
	}

	public void rootKeys(ActionStatus status, String keys){
		getDesktopDriver().rootKeys(getHandle(getDesktopDriver()), keys);
		actionTerminated(status);
	}

	public String getSource(){
		return engine.getSource();
	}

	public void checkStatus(ActionExecute actionExecute, String testName, int testLine) {
		//do nothing in non empty channel
	}

	//---------------------------------------------------------------------------
	// Screen shot management
	//---------------------------------------------------------------------------

	public byte[] getScreenShot(TestBound dim) {
		dim.setX(dim.getX() + dimension.getX());
		dim.setY(dim.getY() + dimension.getY());

		return getScreenShotEngine(dim);
	}

	public byte[] getScreenShot(){
		return getScreenShotEngine(dimension);
	}

	private byte[] getScreenShotEngine(TestBound dim) {
		mainScript.sleep(50);
		return engine.getScreenshot(dim.getX(), dim.getY(), dim.getWidth(), dim.getHeight());
	}

	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------

	public String getAuthenticationValue() {
		return actionStart.getAuthenticationValue();
	}

	public ArrayList<CalculatedValue> getArguments() {
		return actionStart.getArguments();
	}

	public void setNeoloadDesignApi(String value) {
		this.neoloadDesignApi = value;
	}

	//--------------------------------------------------------------------------------------------------
	// Mobile channel init
	//--------------------------------------------------------------------------------------------------

	public void setApplicationData(String os, String system, String version, String dVersion, byte[] icon, String udp, String appName, String userName, String machineName, String osBuild, String country) {
		setApplicationData(os + ":" + system, version, dVersion, -1, icon, udp);
		systemValues.setApplicationName(appName);
		systemValues.setUserName(userName);
		systemValues.setMachineName(machineName);
		systemValues.setOsVersion(system);
		systemValues.setOsBuild(osBuild);
		systemValues.setCountry(country);
	}

	//--------------------------------------------------------------------------------------------------
	// Api webservices init
	//--------------------------------------------------------------------------------------------------

	public void setApplicationData(String os) {
		icon = ResourceContent.getAtsByteLogo();
		driverVersion = ATS.VERSION;
		dimension = new TestBound(0D, 0D, 1D, 1D);
		systemValues.setOsName(os);
	}

	public void setApplicationData(String os, String type, ArrayList<String> op) {
		operations = op;
		systemValues.setOsName(os);
		systemValues.setApplicationName(type);
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public void setApplicationData(String os, String version, String dv, long pid, byte[] ic, String screen) {
		driverVersion = dv;
		processId = pid;
		icon = ic;
		screenServer = screen;

		systemValues.setOsName(os);
		systemValues.setApplicationVersion(version);
	}

	public void setApplicationData(String os, String serviceType) {
		systemValues.setOsName(os);
		systemValues.setApplicationName(serviceType);
	}

	public void setApplicationData(String os, int handle) {
		driverVersion = "";
		winHandle = handle;
		systemValues.setOsName(os);
	}

	public void setApplicationData(String os, String version, String dVersion, long pid) {
		setApplicationData(os, version, dVersion, pid, new byte[0], "");
	}
	
	public void setApplicationData(String os, String name, String version, String dVersion, long pid, int handle) {
		setApplicationData(os, version, dVersion, pid, new byte[0], "");
		systemValues.setApplicationName(name);
		winHandle = handle;
	}

	public void setApplicationData(String os, String name, String version, String dVersion, long pid) {
		setApplicationData(os, version, dVersion, pid, new byte[0], "");
		systemValues.setApplicationName(name);
	}

	public void setApplicationData(String os, String version, String dVersion, long pid, long handle) {
		setApplicationData(os, version, dVersion, pid, new byte[0], "");
		winHandle = (int) handle;
	}

	public String getSystemValue(String name) {
		if(name != null) {
			return systemValues.get(name);
		}
		return "";
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

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

	public List<String[]> findSelectOptions(TestElement element){
		return engine.loadSelectOptions(element);
	}

	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry){
		return engine.getAttribute(status, element, attributeName, maxTry + AtsManager.getInstance().getMaxTryProperty());
	}

	public void setSysProperty(String attributeName, String attributeValue) {
		engine.setSysProperty(attributeName, attributeValue);
	}
	
	//----------------------------------------------------------------------------------------------------------------------

	public String getOs() {
		return systemValues.getOsName();
	}
	
	public String getApplication() {
		return systemValues.getApplicationName();
	}
	
	public String getApplicationVersion() {
		return systemValues.getApplicationVersion();
	}

	//----------------------------------------------------------------------------------------------------------------------
	// logs
	//----------------------------------------------------------------------------------------------------------------------

	public void sendLog(int code, String message, Object value) {
		mainScript.sendLog(code, message, value);
	}

	public void sendWarningLog(String message, String value) {
		mainScript.sendWarningLog(message, value);
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public ArrayList<String> getSystemProperties() {
		return systemProperties; 
	}
	public void setSystemProperties(ArrayList<String> props) { 
		systemProperties = props; 
	}

	public ArrayList<String> getSystemButtons() { 
		return systemButtons; 
	}
	public void setSystemButtons(ArrayList<String> buttons) { 
		systemButtons = buttons;
	}

	public ArrayList<String> getOperations() {
		return operations;
	}

	public void setOperations(ArrayList<String> operations) {
		this.operations = operations;
	}

	public SystemValues getSystemValues(){
		return systemValues;
	}
	public void setSystemValues(SystemValues value) {} //read only
	
	public String getDriverVersion() {
		return driverVersion;
	}
	public void setDriverVersion(String url) {} // read only	

	public boolean isDesktop() {
		return engine instanceof DesktopDriverEngine;
	}
	public void setDesktop(boolean value) {} // read only

	public boolean isMobile() {
		return engine instanceof MobileDriverEngine;
	}
	public void setMobile(boolean value) {} // read only

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

	public int getPerformance() {
		return actionStart.getPerformance();
	}
	public void setPerformance(int value) {
		actionStart.setPerformance(value);
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean value) {
		this.current = value;
		if(value){
			setWindowToFront();
		}
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

	public String getBoundDimension() {
		return dimension.getX().intValue() + "," + dimension.getY().intValue() + "," + dimension.getWidth().intValue() + "," + dimension.getHeight().intValue();
	}

	public Long getProcessId() {
		return processId;
	}
	public void setProcessId(Long value) {} //read only

	public TestBound getSubDimension(){
		return subDimension;
	}

	public void setSubDimension(TestBound dimension){
		this.subDimension = dimension;
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void close(){
		close(newActionStatus(), false);
	}

	public void close(ActionStatus status, boolean keepRunning){

		if(stopNeoloadRecord != null) {
			neoloadAction(stopNeoloadRecord, "", 0);
		}

		closeAtsProxy();

		engine.close(keepRunning);
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

	public void switchWindow(ActionStatus status, int index, int tries){
		engine.switchWindow(status, index, tries);
		if(status.isPassed()) {
			engine.updateDimensions();
			updateWinHandle(getDesktopDriver(), index);
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

	public DialogBox switchToAlert() {
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

	public void buttonClick(ActionStatus status, String buttonType) {
		engine.buttonClick(status, buttonType);
	}

	//----------------------------------------------------------------------------------------------------------
	// Performance
	//----------------------------------------------------------------------------------------------------------

	private ActionNeoloadStop stopNeoloadRecord = null;

	public void neoloadAction(ActionNeoload action, String testName, int testLine) {
		action.setStatus(newActionStatus(testName, testLine));
		if(getPerformance() == ActionChannelStart.NEOLOAD) {
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

	private IAtsProxy atsProxy = new AtsNoProxy();

	public Proxy startAtsProxy(AtsManager ats) {
		atsProxy = new AtsProxy(getName(), getApplication(), ats.getBlackListServers(), ats.getTrafficIdle(), ats.getOctoperf());
		return atsProxy.startProxy();
	}

	public void startHarServer(ActionStatus status, List<String> whiteList, int trafficIddle, int latency, long sendBandWidth, long receiveBandWidth) {
		atsProxy.startRecord(status, whiteList, trafficIddle, latency, sendBandWidth, receiveBandWidth);
	}

	public void pauseHarRecord() {
		atsProxy.pauseRecord();
	}

	public void resumeHarRecord() {
		atsProxy.resumeRecord();
	}

	public void startHarAction(Action action, String testLine) {
		atsProxy.startAction(action, testLine);
	}

	public void endHarAction() {
		atsProxy.endAction();
	}

	public void sendToOctoperfServer(ActionOctoperfVirtualUser action) {
		atsProxy.sendToOctoperfServer(this, action);
	}

	public void closeAtsProxy() {
		atsProxy.terminate(getName());
		atsProxy = null;
	}

	//----------------------------------------------------------------------------------------------------------
	// Visual reporting
	//----------------------------------------------------------------------------------------------------------

	public DesktopResponse startVisualRecord(ScriptHeader script, int quality, long started) {
		return getDesktopDriver().startVisualRecord(this, script, quality, started);
	}

	public void stopVisualRecord(ScriptStatus status, ReportSummary summary) {
		getDesktopDriver().saveSummary(status, summary);
		getDesktopDriver().stopVisualRecord();
	}

	public void saveVisualReportFile(Path path, String fileName, ExecutionLogger logger) {
		getDesktopDriver().saveVisualReportFile(path.resolve(fileName), logger);
	}

	public void createVisualAction(boolean stop, String actionName, int scriptLine, String scriptName, long timeline, boolean sync) {
		this.engine.createVisualAction(this, stop, actionName, scriptLine, scriptName, timeline, sync);
	}

	public void updateVisualAction(boolean isRef) {
		this.engine.updateScreenshot(this.dimension, isRef);
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