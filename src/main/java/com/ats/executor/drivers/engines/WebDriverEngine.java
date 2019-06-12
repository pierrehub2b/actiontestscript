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

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.config.RequestConfig;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.driver.AtsManager;
import com.ats.element.AtsBaseElement;
import com.ats.element.AtsElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;
import com.ats.script.actions.ActionGotoUrl;
import com.ats.script.actions.ActionWindowState;
import com.ats.tools.ResourceContent;
import com.ats.tools.Utils;
import com.google.gson.Gson;

@SuppressWarnings("unchecked")
public class WebDriverEngine extends DriverEngine implements IDriverEngine {

	protected static final String WEB_ELEMENT_REF = "element-6066-11e4-a52e-4f735466cecf";

	//-----------------------------------------------------------------------------------------------------------------------------
	// Javascript static code
	//-----------------------------------------------------------------------------------------------------------------------------

	//protected static final String JS_WAIT_READYSTATE = "var result=window.document.readyState=='complete';";
	protected static final String JS_WAIT_BEFORE_SEARCH = "var interval=setInterval(function(){if(window.document.readyState==='complete'){clearInterval(interval);done();}},200);";

	//protected static final String JS_AUTO_SCROLL = "var e=arguments[0];e.scrollIntoView();var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	//protected static final String JS_AUTO_SCROLL_CALC = "var e=arguments[0];var r=e.getBoundingClientRect();var top=r.top + window.pageYOffset;window.scrollTo(0, top-(window.innerHeight / 2));r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	//protected static final String JS_AUTO_SCROLL_MOZ = "var e=arguments[0];e.scrollIntoView({behavior:'auto',block:'center',inline:'center'});var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";

	protected static final String JS_SCROLL_IF_NEEDED = "var e=arguments[0], result=[];var r=e.getBoundingClientRect();if(r.top < 0 || r.left < 0 || r.bottom > (window.innerHeight || document.documentElement.clientHeight) || r.right > (window.innerWidth || document.documentElement.clientWidth)) {e.scrollIntoView({behavior:'auto',block:'center',inline:'center'});r=e.getBoundingClientRect();result=[r.left+0.0001, r.top+0.0001];}";

	protected static final String JS_ELEMENT_SCROLL = "var e=arguments[0];var d=arguments[1];e.scrollTop += d;var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected static final String JS_WINDOW_SCROLL = "window.scrollBy(0,arguments[0]);var result=[0.0001, 0.0001]";
	protected static final String JS_ELEMENT_FROM_POINT = "var result=null;var e=document.elementFromPoint(arguments[0],arguments[1]);if(e){var r=e.getBoundingClientRect();result=[e, e.tagName, e.getAttribute('inputmode')=='numeric', e.getAttribute('type')=='password', r.x+0.0001, r.y+0.0001, r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001, 0.0001, 0.0001, {}];};";
	protected static final String JS_ELEMENT_FROM_RECT = "let x1=arguments[0],y1=arguments[1],w=arguments[2],h=arguments[3];let x2=x1+w,y2=y1+h;var e=document.elementFromPoint(x1+(w/2), y1+(h/2)),result=null;while(e != null){var r=e.getBoundingClientRect();if(x1 >= r.x && x2 <= r.x+r.width && y1 >= r.y && y2 <= r.y+r.height){result=[e,e.tagName,e.getAttribute('inputmode')=='numeric',e.getAttribute('type')=='password',r.x+0.0001,r.y+0.0001,r.width+0.0001,r.height+0.0001,r.left+0.0001,r.top+0.0001,0.0001,0.0001,{}];e=null;}else{e=e.parentElement;}};";
	protected static final String JS_ELEMENT_BOUNDING = "var rect=arguments[0].getBoundingClientRect();var result=[rect.left+0.0001, rect.top+0.0001];";
	protected static final String JS_MIDDLE_CLICK = "var evt=new MouseEvent('click', {bubbles: true,cancelable: true,view: window, button: 1}),result={};arguments[0].dispatchEvent(evt);";
	protected static final String JS_ELEMENT_CSS = "var result={};var o=getComputedStyle(arguments[0]);for(var i=0, len=o.length; i < len; i++){result[o[i]]=o.getPropertyValue(o[i]);};";

	protected static final String JS_SEARCH_ELEMENT = ResourceContent.getSearchElementsJavaScript();
	protected static final String JS_ELEMENT_AUTOSCROLL = ResourceContent.getScrollElementJavaScript();
	protected static final String JS_ELEMENT_ATTRIBUTES = ResourceContent.getElementAttributesJavaScript();
	protected static final String JS_ELEMENT_PARENTS = ResourceContent.getParentElementJavaScript();
	protected static final String JS_DOCUMENT_SIZE = ResourceContent.getDocumentSizeJavaScript();

	//-----------------------------------------------------------------------------------------------------------------------------

	protected Double initElementX = 0.0;
	protected Double initElementY = 0.0;

	private DriverProcess driverProcess;

	protected Actions actions;

	protected java.net.URI driverSession;
	protected RequestConfig requestConfig;

	protected String searchElementScript = JS_SEARCH_ELEMENT;
	protected String autoScrollElement = JS_SCROLL_IF_NEEDED;//JS_AUTO_SCROLL;

	public WebDriverEngine(
			Channel channel, 
			String browser, 
			DriverProcess driverProcess, 
			DesktopDriver desktopDriver,
			ApplicationProperties props,
			int defaultWait) {

		super(channel, desktopDriver, browser, props, defaultWait, 60);

		this.driverProcess = driverProcess;
	}

	protected DriverProcess getDriverProcess() {
		return driverProcess;
	}

	protected void launchDriver(ActionStatus status, MutableCapabilities cap) {

		final AtsManager ats = DriverManager.ATS;
		final int maxTrySearch = ats.getMaxTrySearch();
		final int maxTryProperty = ats.getMaxTryProperty();

		final int scriptTimeout = ats.getScriptTimeOut();
		final int pageLoadTimeout = ats.getPageloadTimeOut();
		final int watchdog = ats.getWatchDogTimeOut();		

		if(channel.isNeoload()) {
			channel.setNeoloadDesignApi(ats.getNeoloadDesignApi());
			cap.setCapability(CapabilityType.PROXY, ats.getNeoloadProxy());
		}else {
			channel.setNeoload(false);
			cap.setCapability(CapabilityType.PROXY, ats.getProxy());
		}

		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);
		cap.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE);

		int maxTry = 10;
		String errorMessage = null;

		while(maxTry > 0) {
			try{
				driver = new RemoteWebDriver(driverProcess.getDriverServerUrl(), cap);
				maxTry = 0;
			}catch(Exception ex){
				errorMessage = ex.getMessage();
				maxTry--;
			}
		}

		if(driver != null) {
			status.setPassed(true);

			actions = new Actions(driver);

			driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);

			try{
				driver.manage().window().setSize(channel.getDimension().getSize());
				driver.manage().window().setPosition(channel.getDimension().getPoint());
			}catch(Exception ex){
				System.err.println(ex.getMessage());
			}

			String applicationVersion = null;
			String driverVersion = null;

			Map<String, ?> infos = driver.getCapabilities().asMap();
			for (Map.Entry<String, ?> entry : infos.entrySet()){
				if("browserVersion".equals(entry.getKey()) || "version".equals(entry.getKey())){
					applicationVersion = entry.getValue().toString();
				}else if("chrome".equals(entry.getKey())) {
					Map<String, String> chromeData = (Map<String, String>) entry.getValue();
					driverVersion = chromeData.get("chromedriverVersion");
					if(driverVersion != null) {
						driverVersion = driverVersion.replaceFirst("\\(.*\\)", "").trim();
					}
				}else if("moz:geckodriverVersion".equals(entry.getKey())) {
					driverVersion = entry.getValue().toString();
				}
			}

			final String titleUid = UUID.randomUUID().toString();
			try {
				final File tempHtml = File.createTempFile("ats_", ".html");
				tempHtml.deleteOnExit();

				Files.write(tempHtml.toPath(), Utils.getAtsBrowserContent(titleUid, channel.getApplication(), applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait(), getPropertyWait(), maxTrySearch, maxTryProperty, scriptTimeout, pageLoadTimeout, watchdog, getDesktopDriver()));
				driver.get(tempHtml.toURI().toString());
			} catch (IOException e) {}

			maxTry = 10;
			while(maxTry > 0) {
				final DesktopWindow window = desktopDriver.getWindowByTitle(titleUid);
				if(window != null) {
					desktopDriver.setEngine(new DesktopDriverEngine(channel, window));
					channel.setApplicationData(
							"windows",
							applicationVersion,
							driverVersion,
							window.pid);
					maxTry = 0;
				}else {
					channel.sleep(300);
					maxTry--;
				}
			}

			requestConfig = RequestConfig.custom()
					.setConnectTimeout(5000)
					.setConnectionRequestTimeout(5000)
					.setSocketTimeout(10000).build();
			try {
				driverSession = new URI(driverProcess.getDriverServerUrl() + "/session/" + driver.getSessionId().toString());
			} catch (URISyntaxException e) {}

		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			status.setMessage(errorMessage);

			driverProcess.close();
		}
	}

	@Override
	public DesktopDriver getDesktopDriver() {
		return desktopDriver;
	}

	protected Channel getChannel() {
		return channel;
	}

	@Override
	public void waitAfterAction() {
		actionWait();
	}

	private String[] getWindowsHandle(int index) {
		Set<String> list = getDriverWindowsList();
		int maxTry = 15;
		while(index >= list.size() && maxTry > 0) {
			list = getDriverWindowsList();
			channel.sleep(500);
			maxTry--;
		}
		return list.toArray(new String[list.size()]);
	}

	private Set<String> getDriverWindowsList(){
		try {
			return driver.getWindowHandles();
		}catch (WebDriverException e) {
			return Collections.<String>emptySet();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public void scroll(int delta) {
		runJavaScript(JS_WINDOW_SCROLL, delta);
	}

	@Override
	public void scroll(FoundElement element, int delta) {
		ArrayList<Double> newPosition;
		if(delta == 0) {
			newPosition =  (ArrayList<Double>) runJavaScript(autoScrollElement, element.getValue());
		}else {
			newPosition =  (ArrayList<Double>) runJavaScript(JS_ELEMENT_SCROLL, element.getValue(), delta);
		}
		updatePosition(newPosition, element);
	}

	@Override
	public void scroll(FoundElement element) {
		final ArrayList<Double> newPosition = (ArrayList<Double>) runJavaScript(JS_SCROLL_IF_NEEDED, element.getValue());
		updatePosition(newPosition, element);
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	private void updatePosition(ArrayList<Double> position, FoundElement element) {
		if(position != null && position.size() > 1) {
			element.updatePosition(position.get(0), position.get(1), channel, 0.0, 0.0);
			channel.sleep(30);
		}
	}

	@Override
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y){

		if(syscomp) {
			return desktopDriver.getElementFromPoint(x, y);
		}else {

			switchToDefaultContent();

			x -= channel.getSubDimension().getX();
			y -= channel.getSubDimension().getY();

			return loadElement(new ArrayList<AtsElement>(), x, y, initElementX, initElementY);
		}
	}

	private FoundElement loadElement(ArrayList<AtsElement> iframes, Double x, Double y, Double offsetX, Double offsetY) {

		final ArrayList<Object> objectData = (ArrayList<Object>)runJavaScript(JS_ELEMENT_FROM_POINT, x - offsetX, y - offsetY);

		if(objectData != null){

			final AtsElement element = new AtsElement(objectData);

			if(element.isIframe()){

				iframes.add(0, element);

				FoundElement frm = new FoundElement(element);

				switchToFrame(frm.getValue());

				offsetX += element.getX();
				offsetY += element.getY();

				return loadElement(iframes, x, y, offsetX, offsetY);

			} else {
				return new FoundElement(element, iframes, channel, offsetX, offsetY);
			}

		} else {
			return null;
		}
	}

	@Override
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h){

		if(syscomp) {
			return desktopDriver.getElementFromRect(x, y, w, y);
		}else {

			switchToDefaultContent();

			x = x - channel.getSubDimension().getX() - channel.getDimension().getX();
			y = y - channel.getSubDimension().getY() - channel.getDimension().getY();

			return loadElement(new ArrayList<AtsElement>(), x, y, w, h, initElementX, initElementY);
		}
	}

	private FoundElement loadElement(ArrayList<AtsElement> iframes, Double x, Double y, Double w, Double h, Double offsetX, Double offsetY) {

		final ArrayList<Object> objectData = (ArrayList<Object>)runJavaScript(JS_ELEMENT_FROM_RECT, x - offsetX, y - offsetY, w, h);

		if(objectData != null){

			final AtsElement element = new AtsElement(objectData);

			if(element.isIframe()){

				iframes.add(0, element);

				FoundElement frm = new FoundElement(element);

				switchToFrame(frm.getValue());

				offsetX += element.getX();
				offsetY += element.getY();

				return loadElement(iframes, x, y, w, h, offsetX, offsetY);

			} else {
				return new FoundElement(element, iframes, channel, offsetX, offsetY);
			}

		} else {
			return null;
		}
	}

	@Override
	public void loadParents(FoundElement hoverElement){
		if(hoverElement.isDesktop()){
			hoverElement.setParent(desktopDriver.getTestElementParent(hoverElement.getId(), channel));
		}else{
			hoverElement.setParent(getTestElementParent(hoverElement));
		}
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public WebElement getRootElement() {
		int maxTry = 20;

		WebElement body = getBody();

		while(body == null && maxTry > 0) {
			maxTry--;
			body = getBody();
		}

		return body;
	}

	private WebElement getBody() {
		try {
			return driver.findElement(By.tagName("body"));
		}catch(NoSuchElementException ex) {
			channel.sleep(200);
			return null;
		}
	}

	private RemoteWebElement getWebElement(FoundElement element) {
		return element.getRemoteWebElement(driver);
	}

	@Override
	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry) {
		int tryLoop = maxTry;
		while (tryLoop > 0){
			String result = getAttribute(status, element, attributeName);
			if(result != null && doubleCheckAttribute(status, result, element, attributeName)) {
				return result;
			}
			tryLoop--;
		}
		return null;
	}

	private boolean doubleCheckAttribute(ActionStatus status, String verify, FoundElement element, String attributeName) {
		channel.sleep(getPropertyWait());

		final String current = getAttribute(status, element, attributeName);
		return current != null && current.equals(verify);
	}

	private String getAttribute(ActionStatus status, FoundElement element, String attributeName) {

		final RemoteWebElement elem = getWebElement(element);
		String result = elem.getAttribute(attributeName);

		if(result == null) {

			for (CalculatedProperty calc : getAttributes(element, false)) {
				if(attributeName.equals(calc.getName())) {
					return calc.getValue().getCalculated();
				}
			}
			result = getCssAttributeValueByName(element, attributeName);

			if(result == null) {
				final Object obj = executeJavaScript(status, attributeName);
				if(obj == null) {
					channel.sleep(100);
				}else {
					result = obj.toString();
				}
			}
		}
		return result;
	}

	private String getCssAttributeValueByName(FoundElement element, String name) {
		return foundAttributeValue(name, getCssAttributes(element));
	}

	private String foundAttributeValue(String name, CalculatedProperty[] properties) {
		final Stream<CalculatedProperty> stream = Arrays.stream(properties);
		final Optional<CalculatedProperty> calc = stream.parallel().filter(c -> c.getName().equals(name)).findFirst();
		if(calc.isPresent()) {
			return calc.get().getValue().getCalculated();
		}
		return null;
	}

	@Override
	public CalculatedProperty[] getAttributes(FoundElement element, boolean reload){
		if(element.isDesktop()){
			return desktopDriver.getElementAttributes(element.getId());
		}else {
			return getAttributes(getWebElement(element));
		}
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element){
		return getCssAttributes(getWebElement(element));
	}

	private CalculatedProperty[] getCssAttributes(RemoteWebElement element){
		return getAttributesList(element, JS_ELEMENT_CSS);
	}

	private CalculatedProperty[] getAttributes(RemoteWebElement element){
		return getAttributesList(element, JS_ELEMENT_ATTRIBUTES);
	}

	private CalculatedProperty[] getAttributesList(RemoteWebElement element, String script) {
		final Object result = runJavaScript(script, element);
		if(result != null && result instanceof Map){
			return ((Map<String, Object>)result).entrySet().stream().parallel().filter(e -> !(e.getValue() instanceof Map)).map(e -> new CalculatedProperty(e.getKey(), e.getValue().toString())).toArray(c -> new CalculatedProperty[c]);
		}
		return null;
	}

	public FoundElement getTestElementParent(FoundElement element){
		final ArrayList<ArrayList<Object>> listElements = (ArrayList<ArrayList<Object>>) runJavaScript(JS_ELEMENT_PARENTS, element.getValue());
		if(listElements != null){
			return new FoundElement(
					channel,
					element.getIframes(),
					listElements.stream().map(e -> new AtsElement(e)).collect(Collectors.toCollection(ArrayList::new)),
					initElementX,
					initElementY);
		}
		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public void updateDimensions() {
		final ArrayList<Double> response = (ArrayList<Double>) runJavaScript(JS_DOCUMENT_SIZE);
		if(response != null && response.size() == 8) {
			channel.getDimension().update(response.get(0), response.get(1), response.get(2), response.get(3));
			channel.getSubDimension().update(response.get(4), response.get(5), response.get(6), response.get(7));
		}
	}

	@Override
	public void close() {
		if(driver != null){
			Arrays.asList(getWindowsHandle(0)).stream().sorted(Collections.reverseOrder()).forEach(s -> closeWindowHandler(s));
			getDriverProcess().close();
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Mouse position by browser
	//-----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean withDesktop, int offsetX, int offsetY) {
		
		if(withDesktop) {
			desktopMoveToElement(foundElement, position,offsetX ,offsetY);
		}else {

			int maxTry = 10;
			while(maxTry > 0) {
				try {

					scrollAndMove(foundElement, position, offsetX, offsetY);

					status.setPassed(true);
					status.setMessage("");

					maxTry = 0;

				}catch(WebDriverException e) {

					channel.sleep(500);

					status.setPassed(false);
					status.setMessage(e.getMessage());

					maxTry--;
				}
			}
		}
	}

	private void scrollAndMove(FoundElement element, MouseDirection position, int offsetX, int offsetY) {
		scroll(element);

		final Rectangle rect = element.getRectangle();
		move(element, getOffsetX(rect, position) + offsetX, getOffsetY(rect, position) + offsetY);
	}

	protected void move(FoundElement element, int offsetX, int offsetY) {
		actions.moveToElement(element.getValue(), offsetX, offsetY).perform();
	}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {

		final Rectangle rect = element.getRectangle();
		try {
			click(element, getOffsetX(rect, position) + offsetX, getOffsetY(rect, position) + offsetY);
			status.setPassed(true);
		}catch(StaleElementReferenceException e1) {
			throw e1;
		}catch(ElementNotVisibleException e0) {	
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
		}catch (Exception e) {
			status.setPassed(false);
			status.setMessage(e.getMessage());
		}
	}

	protected void click(FoundElement element, int offsetX, int offsetY) {
		actions.moveToElement(element.getValue(), offsetX, offsetY).click().build().perform();
	}
	
	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position) {

		final Rectangle rect = element.getRectangle();

		try {

			final Actions act = actions.moveToElement(element.getValue(), getOffsetX(rect, position), getOffsetY(rect, position));
			act.clickAndHold().build().perform();
			status.setPassed(true);

		}catch(StaleElementReferenceException e1) {
			throw e1;
		}catch(ElementNotVisibleException e0) {	
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
		}catch (Exception e) {
			status.setPassed(false);
			status.setMessage(e.getMessage());
		}
	}

	@Override
	public void keyDown(Keys key) {
		actions.keyDown(key).perform();
	}

	@Override
	public void keyUp(Keys key) {
		actions.keyUp(key).perform();
	}

	@Override
	public void drop(MouseDirection md, boolean desktopDragDrop) {
		if(desktopDragDrop) {
			getDesktopDriver().mouseRelease();
		}else {
			actions.release().perform();
		}
	}

	@Override
	public void moveByOffset(int hDirection, int vDirection) {
		actions.moveByOffset(hDirection, vDirection).perform();
	}

	@Override
	public void doubleClick() {
		actions.doubleClick();
	}	

	@Override
	public void rightClick() {
		actions.contextClick().perform();
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Iframes management
	//-----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean switchToDefaultContent() {
		try {
			driver.switchTo().defaultContent();
			return true;
		}catch (WebDriverException e) {
			return false;
		}
	}

	@Override
	public void switchToFrameId(String id) {
		final RemoteWebElement rwe = new RemoteWebElement();
		rwe.setId(id);
		rwe.setParent(driver);
		switchToFrame(rwe);
	}

	protected void switchToFrame(WebElement we) {
		driver.switchTo().frame(we);
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Window management
	//-----------------------------------------------------------------------------------------------------------------------------------

	protected boolean switchToWindowHandle(String handle) {
		try {
			channel.sleep(300);
			driver.switchTo().window(handle);
			channel.sleep(300);
			return switchToDefaultContent();
		}catch(NoSuchWindowException ex) {
			return false;
		}	
	}

	protected void switchToWindowIndex(String[] wins, int index) {

		int maxTry = 10;

		channel.sleep(500);
		boolean switched = switchToWindowHandle(wins[index]);

		while(!switched && maxTry > 0) {
			channel.sleep(500);
			wins = getWindowsHandle(index);
			switched = switchToWindowHandle(wins[index]);
		}
		currentWindow = index;
	}

	@Override
	public boolean setWindowToFront() {
		final String[] wins = getWindowsHandle(0);
		if(wins.length> currentWindow) {
			driver.switchTo().window(wins[currentWindow]);
		}
		return true;
	}

	@Override
	public void switchWindow(ActionStatus status, int index) {
		if(index >= 0) {
			channel.sleep(500);
			final String[] wins = getWindowsHandle(index);
			if(wins.length > index) {
				switchToWindowIndex(wins, index);
				channel.cleanHandle();
			}else {
				status.setPassed(false);
				status.setCode(ActionStatus.WINDOW_NO_SWITCH);
				status.setMessage("Cannot switch to index '" + index + "', only " + wins.length + " windows found !");
			}
		}
	}

	@Override
	protected void setPosition(Point pt) {
		channel.sleep(500);
		driver.manage().window().setPosition(pt);
	}

	@Override
	protected void setSize(Dimension dim) {
		channel.sleep(500);
		driver.manage().window().setSize(dim);
	}

	private void closeWindowHandler(String windowHandle) {
		switchToWindowHandle(windowHandle);
		closeCurrentWindow();
	}

	protected void closeCurrentWindow() {
		driver.close();
		channel.sleep(200);
	}

	@Override
	public void closeWindow(ActionStatus status) {
		final String[] list = getWindowsHandle(0);
		if(list.length > 1) {
			if(currentWindow < list.length) {
				closeWindowHandler(list[currentWindow]);
				currentWindow = 0;
			}
			switchToWindowHandle(list[0]);
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	//
	//-----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object executeScript(ActionStatus status, String javaScript, Object... params) {
		final Object result = runJavaScript(status, "var result={};" + javaScript + ";", params);
		if(status.isPassed() && result != null) {
			status.setMessage(result.toString());
		}
		return result;
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String javaScript) {
		Object result = null;
		status.setPassed(true);
		try {
			result = driver.executeAsyncScript("return " + javaScript);
		}catch(StaleElementReferenceException ex) {
			throw ex;
		}catch(Exception ex) {
			status.setPassed(false);
			status.setCode(ActionStatus.JAVASCRIPT_ERROR);
			status.setMessage(ex.getMessage());
		}
		return result;
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String javaScript, TestElement element) {

		final Object result = runJavaScript(status, "var result=arguments[0]." + javaScript + ";", element.getWebElement());
		if(status.isPassed() && result != null) {
			status.setMessage(result.toString());
		}
		return result;
	}

	//TODO remove this default method and add actionstatus
	protected Object runJavaScript(String javaScript, Object ... params) {
		return runJavaScript(channel.newActionStatus(), javaScript, params);
	}

	protected Object runJavaScript(ActionStatus status, String javaScript, Object ... params) {

		status.setPassed(true);
		try {
			return driver.executeAsyncScript(javaScript + ";arguments[arguments.length-1](result);", params);
		}catch(StaleElementReferenceException ex) {
			throw ex;
		}catch(Exception ex) {
			status.setPassed(false);
			status.setCode(ActionStatus.JAVASCRIPT_ERROR);
			status.setMessage(ex.getMessage());
		}

		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public void goToUrl(ActionStatus status, String url) {

		if(ActionGotoUrl.REFRESH.equals(url)) {
			driver.navigate().refresh();
		}else if(ActionGotoUrl.NEXT.equals(url)) {
			driver.navigate().forward();
		}else if(ActionGotoUrl.BACK.equals(url)) {
			driver.navigate().back();
		}else {
			switchToDefaultContent();
			if(!url.startsWith("https://") && !url.startsWith("http://") && !url.startsWith("file://") ) {
				url = "http://" + url;
			}
			loadUrl(url);
		}

		status.setPassed(true);
		status.setData(url);
		status.setMessage(getCurrentUrl());

		actionWait();
	}

	protected void loadUrl(String url) {
		driver.get(url);
	}

	private WebElement iframe = null;
	private double offsetIframeX = 0.0;
	private double offsetIframeY = 0.0;

	@Override
	public ArrayList<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> predicate) {

		//if(tagName == null) {
		//	tagName = BODY;
		//}

		WebElement startElement = null;

		if(testObject.getParent() != null){
			if(testObject.getParent().isIframe()) {

				iframe = testObject.getParent().getWebElement();

				try {

					final Point pt = iframe.getLocation();

					offsetIframeX += pt.getX();
					offsetIframeY += pt.getY();

					switchToFrame(iframe);

				}catch(WebDriverException e) {
					return new ArrayList<FoundElement>();
				}

			}else {
				startElement = testObject.getParent().getWebElement();
			}
		}else {
			if(iframe != null) {
				iframe = null;
				offsetIframeX = 0.0;
				offsetIframeY = 0.0;
			}
			if(!switchToDefaultContent()) {
				return new ArrayList<FoundElement>();
			}
		}

		final ArrayList<ArrayList<Object>> response = (ArrayList<ArrayList<Object>>) runJavaScript(searchElementScript, startElement, tagName, attributes, attributes.size());
		if(response != null){
			final ArrayList<AtsElement> elements = response.parallelStream().map(e -> new AtsElement(e)).collect(Collectors.toCollection(ArrayList::new));
			return elements.parallelStream().filter(predicate).map(e -> new FoundElement(e, channel, initElementX + offsetIframeX, initElementY + offsetIframeY)).collect(Collectors.toCollection(ArrayList::new));
		}

		return new ArrayList<FoundElement>();
	}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {
		runJavaScript(status, JS_MIDDLE_CLICK, element.getWebElement());
	}

	protected void middleClickSimulation(ActionStatus status, MouseDirection position, TestElement element) {
		element.click(status, position, Keys.CONTROL);
	}

	@Override
	public Alert switchToAlert() {
		return driver.switchTo().alert();
	}

	private String getCurrentUrl() {
		try {
			return driver.getCurrentUrl();
		}catch(UnhandledAlertException e) {
			return "";
		}
	}

	@Override
	public void clearText(ActionStatus status, FoundElement element) {
		executeScript(status, "arguments[0].value='';", element.getValue());
		status.setMessage("");
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		status.setMessage(new Gson().toJson(
				executeScript(status, "result={size:window.getComputedStyle(arguments[0], null).getPropertyValue('font-size'), family:window.getComputedStyle(arguments[0], null).getPropertyValue('font-family'), weight:window.getComputedStyle(arguments[0], null).getPropertyValue('font-weight')};", element.getWebElement())));

		for(SendKeyData sequence : textActionList) {
			element.getWebElement().sendKeys(sequence.getSequenceWithDigit());
		}
	}

	@Override
	public void refreshElementMapLocation() {
		getDesktopDriver().refreshElementMapLocation(channel);
	}

	@Override
	public String getSource() {
		return driver.getPageSource();
	}

	@Override
	public void api(ActionStatus status, ActionApi api) {}

	@Override
	public void buttonClick(String id) {}

	@Override
	public void windowState(ActionStatus status, Channel channel, String state) {
		if(ActionWindowState.MAXIMIZE.equals(state)) {

			final List<Double> screenSize = (List<Double>) runJavaScript(status, "result=[screen.width+0.0001, screen.height+0.0001];");
			setPosition(new Point(0, 0));
			setSize(new Dimension(screenSize.get(0).intValue(), screenSize.get(1).intValue()));

		}else {
			getDesktopDriver().windowState(status, channel, state);
		}
	}
}