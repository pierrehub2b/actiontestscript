package com.ats.executor.channels;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionGotoUrl;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;

public class Channel {

	//private ChannelProcessData processData;

	private IDriverEngine engine;

	private String name;
	private boolean current = false;
	private boolean desktop = false;

	private ActionTestScript mainScript;

	private TestBound dimension;
	private TestBound subDimension;

	private int maxTry = 0;

	private String applicationVersion;

	private ProcessHandle process = null;
	private ArrayList<String> processWindows = new ArrayList<String>();

	private WindowsDesktopDriver windowsDesktopDriver;

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

		this.windowsDesktopDriver = new WindowsDesktopDriver(driverManager.getWinDesktopDriver().getDriverServerUrl());
		this.engine = driverManager.getDriverEngine(this, application.toLowerCase(), this.windowsDesktopDriver);

		this.desktop = engine.isDesktop();

		this.maxTry = driverManager.getMaxTry();

		this.refreshLocation();
	}

	public boolean isNetworkActivity() {

		return false;
	}

	public void refreshLocation(){
		
		int[] winRect = getWindowRect();
		
		TestBound[] dimensions = engine.getDimensions();
		TestBound mainDimension = dimensions[0];
		
		mainDimension.setX((double)winRect[0] + 7);
		mainDimension.setY((double)winRect[1]);
		
		setDimension(mainDimension);
		setSubDimension(dimensions[1]);
	}

	public void refreshMapElementLocation(){
		refreshLocation();
		windowsDesktopDriver.refreshElementMapLocation(this);
	}

	public void toFront(){
		windowsDesktopDriver.setWindowToFront(getProcessId());
		//switchToDefaultframe();
		showWindow(5);
	}

	public void clickWindow(){
		List<WebElement> childs = windowsDesktopDriver.getChildrenByPid(getProcessId());
		if(childs != null && childs.size() > 0){
			childs.get(0).click();
		}
	}

	public void hide(){
		showWindow(0);
	}

	private IDriverEngine getEngine(){
		return engine;
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
		return windowsDesktopDriver.getScreenshotByte(dim.getX(), dim.getY(), dim.getWidth(), dim.getHeight());
		//return engine.getScreenShot(dim); 
	}

	public void setApplicationData(String version, long pid, ArrayList<String> processWindows) {
		
		this.applicationVersion = version;
		Optional<ProcessHandle> procs = ProcessHandle.of(pid);
		if(procs.isPresent()) {
			this.process = procs.get();
			this.processWindows = processWindows;
		}

		moveWindowByHandle();
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
		return getEngine().getCssAttributes(element);
	}

	public CalculatedProperty[] getAttributes(FoundElement element){
		return getEngine().getAttributes(element);
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

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean value) {
		this.current = value;
		if(value){
			toFront();
		}else{
			hide();
		}
	}

	public boolean isDesktop() {
		return desktop;
	}

	public void setDesktop(boolean desktop) {
		this.desktop = desktop;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
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

		process.descendants().forEach(p -> p.destroy());
		process.destroy();
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

	public WebDriver getWebDriver() {
		return engine.getWebDriver();
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

	public void navigate(URL url, boolean newWindow) {
		engine.goToUrl(url, newWindow);
	}

	public void navigate(String type) {
		if(ActionGotoUrl.REFRESH.equals(type)) {
			engine.getWebDriver().navigate().refresh();
		}else if(ActionGotoUrl.NEXT.equals(type)) {
			engine.getWebDriver().navigate().forward();
		}else if(ActionGotoUrl.BACK.equals(type)) {
			engine.getWebDriver().navigate().back();
		}
	}

	public CalculatedProperty[] getAttributes(RemoteWebElement webElement) {
		return engine.getAttributes(webElement);
	}

	public CalculatedProperty[] getCssAttributes(RemoteWebElement webElement) {
		return engine.getCssAttributes(webElement);
	}

	public ArrayList<FoundElement> findWebElement(TestElement testObject, String tagName, String[] attributes, Predicate<Map<String, Object>> searchPredicate) {
		return engine.findWebElement(this, testObject, tagName, attributes, searchPredicate);
	}

	public ArrayList<FoundElement> findWindowsElement(WebElement parent, String tag, List<CalculatedProperty> attributes) {
		return windowsDesktopDriver.findElementByTag(getProcessId(), parent, tag, attributes, this);
	}

	//----------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------

	public void showWindow(int winCommand) {

		String windowHandle = processWindows.get(0);//TODO loop in list

		if(windowHandle != null){
			User32 user32 = User32.INSTANCE;
			HWND foreground = user32.GetForegroundWindow();

			user32.EnumWindows(new User32.WNDENUMPROC() { 
				@Override 
				public boolean callback(HWND hWnd, Pointer arg) { 

					if(hWnd != null && hWnd.toNative() != null){
						if (windowHandle.equals(hWnd.toNative().toString())) {
							if(winCommand != 0){
								//user32.SetWindowPos(hWnd, user32.GetForegroundWindow(), 0, 0, 0, 0, 0x0040 | 0x0001 | 0x0002);
								user32.SetWindowPos(hWnd, foreground, 0, 0, 0, 0, 0x0040 | 0x0001 | 0x0002);
								user32.SetForegroundWindow(hWnd);
								//user32.SetFocus(hWnd);
							}
							user32.ShowWindow(hWnd, winCommand);
							return false;
						}
					}
					return true; 
				} 
			}, null); 
		}
	}

	public void closeWindow() {

		String windowHandle = processWindows.get(0);

		if(windowHandle != null){
			User32 user32 = User32.INSTANCE;

			user32.EnumWindows(new User32.WNDENUMPROC() { 
				@Override 
				public boolean callback(HWND hWnd, Pointer arg) { 
					if(hWnd != null && hWnd.toNative() != null){
						if (windowHandle.equals(hWnd.toNative().toString())) {
							user32.PostMessage(hWnd, WinUser.WM_QUIT, null, null);
							return false;
						}
					}
					return true; 
				} 
			}, null); 
		}
	}

	private void moveWindowByHandle() {

		String handle = processWindows.get(0);

		if(handle != null){

			User32 user32 = User32.INSTANCE;

			user32.EnumWindows(new User32.WNDENUMPROC() { 
				@Override 
				public boolean callback(HWND hWnd, Pointer arg) { 
					if(hWnd != null){
						if (handle.equals(hWnd.toNative().toString())) {
							user32.MoveWindow(hWnd, dimension.getX().intValue(), dimension.getY().intValue(), dimension.getWidth().intValue(), dimension.getHeight().intValue(), true);
							return false;
						}
					}
					return true; 
				} 
			}, null); 
		}
	}
	
	private int[] getWindowRect() {
		
		String handle = processWindows.get(0);
		int[] result = {0, 0, 0, 0};

		if(handle != null){

			User32 user32 = User32.INSTANCE;

			user32.EnumWindows(new User32.WNDENUMPROC() { 
				@Override 
				public boolean callback(HWND hWnd, Pointer arg) { 
					if(hWnd != null){
						if (handle.equals(hWnd.toNative().toString())) {
							
							RECT rect = new RECT();
							user32.GetWindowRect(hWnd, rect);
							
							result[0] = rect.left;
							result[1] = rect.top;
							result[2] = rect.right;
							result[3] = rect.bottom;
							
							return false;
						}
					}
					return true;
				} 
			}, null); 
		}
		
		return result;
	}

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

	public void sendTextData(WebElement webElement, ArrayList<SendKeyData> textActionList) {
		engine.sendTextData(webElement, textActionList);
		actionTerminated();
	}

	public int getMaxTry() {
		return maxTry;
	}

	public void forceScrollElement(FoundElement foundElement) {
		engine.forceScrollElement(foundElement);
	}

}