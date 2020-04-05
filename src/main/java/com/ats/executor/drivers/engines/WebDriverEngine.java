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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.Select;

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
import com.ats.executor.channels.ChannelManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;
import com.ats.script.actions.ActionChannelStart;
import com.ats.script.actions.ActionGotoUrl;
import com.ats.script.actions.ActionSelect;
import com.ats.script.actions.ActionWindowState;
import com.ats.tools.ResourceContent;
import com.ats.tools.Utils;

@SuppressWarnings("unchecked")
public class WebDriverEngine extends DriverEngine implements IDriverEngine {

	protected static final String WEB_ELEMENT_REF = "element-6066-11e4-a52e-4f735466cecf";

	private final static int DEFAULT_WAIT = 150;
	private final static int DEFAULT_PROPERTY_WAIT = 200;

	//-----------------------------------------------------------------------------------------------------------------------------
	// Javascript static code
	//-----------------------------------------------------------------------------------------------------------------------------

	//protected static final String JS_AUTO_SCROLL = "var e=arguments[0];e.scrollIntoView();var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	//protected static final String JS_AUTO_SCROLL_CALC = "var e=arguments[0];var r=e.getBoundingClientRect();var top=r.top + window.pageYOffset;window.scrollTo(0, top-(window.innerHeight / 2));r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	//protected static final String JS_AUTO_SCROLL_MOZ = "var e=arguments[0];e.scrollIntoView({behavior:'auto',block:'center',inline:'center'});var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";

	//protected String JS_SCROLL_IF_NEEDED = "var e=arguments[0], result=[], r=e.getBoundingClientRect(), top0=r.top, left0=r.left;if(r.top < 0 || r.left < 0 || r.bottom > (window.innerHeight || document.documentElement.clientHeight) || r.right > (window.innerWidth || document.documentElement.clientWidth)) {e.scrollIntoView({behavior:'instant',block:'center',inline:'nearest'});r=e.getBoundingClientRect();result=[r.left+0.0001, r.top+0.0001, left0+0.0001, top0+0.0001];}";
	protected String JS_SCROLL_IF_NEEDED = "var e=arguments[0], result=[], r=e.getBoundingClientRect(), top0=r.top, left0=r.left; e.scrollIntoView({behavior:'instant',block:'center',inline:'nearest'});r=e.getBoundingClientRect();if(r.left!=left0 || r.top!=top0) {result=[r.left+0.0001, r.top+0.0001];}";

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

	protected DriverProcess driverProcess;

	protected Actions actions;

	protected java.net.URI driverSession;

	protected String searchElementScript = JS_SEARCH_ELEMENT;

	public WebDriverEngine(
			Channel channel, 
			DriverProcess driverProcess, 
			DesktopDriver desktopDriver,
			ApplicationProperties props,
			int defaultWait,
			int defaultPropertyWait) {

		super(channel, desktopDriver, props, defaultWait, defaultPropertyWait);

		this.driverProcess = driverProcess;
	}

	public WebDriverEngine(
			Channel channel, 
			String browser, 
			DriverProcess driverProcess, 
			DesktopDriver desktopDriver,
			ApplicationProperties props) {

		this(channel, driverProcess, desktopDriver, props, DEFAULT_WAIT, DEFAULT_PROPERTY_WAIT);
	}

	public WebDriverEngine(Channel channel, DesktopDriver desktopDriver, String application, ApplicationProperties props, int defaultWait, int defaultCheck) {
		super(channel, desktopDriver, props, defaultWait, defaultCheck);
	}

	protected DriverProcess getDriverProcess() {
		return driverProcess;
	}

	public void setDriverProcess(DriverProcess driverProcess) {
		this.driverProcess = driverProcess;
	}

	protected void launchDriver(ActionStatus status, MutableCapabilities cap, String profilePath) {

		final AtsManager ats = ChannelManager.ATS;
		final int maxTrySearch = ats.getMaxTrySearch();
		final int maxTryProperty = ats.getMaxTryProperty();

		final int scriptTimeout = ats.getScriptTimeOut();
		final int pageLoadTimeout = ats.getPageloadTimeOut();
		final int watchdog = ats.getWatchDogTimeOut();		

		if(channel.getPerformance() == ActionChannelStart.PERF) {
			cap.setCapability(CapabilityType.PROXY, channel.startAtsProxy(ats));
		}else {
			if(channel.getPerformance() == ActionChannelStart.NEOLOAD) {
				channel.setNeoloadDesignApi(ats.getNeoloadDesignApi());
				cap.setCapability(CapabilityType.PROXY, ats.getNeoloadProxy().getValue());
			}else {
				cap.setCapability(CapabilityType.PROXY, ats.getProxy().getValue());
			}
		}

		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		cap.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
		cap.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE);

		try{
			driver = new RemoteWebDriver(driverProcess.getDriverServerUrl(), cap);
		}catch(Exception ex){
			status.setTechnicalError(ActionStatus.CHANNEL_START_ERROR, ex.getMessage());
			driverProcess.close(false);
			return;
		}

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

			Files.write(tempHtml.toPath(), Utils.getAtsBrowserContent(titleUid, channel.getApplication(), applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait(), getPropertyWait(), maxTrySearch, maxTryProperty, scriptTimeout, pageLoadTimeout, watchdog, getDesktopDriver(), profilePath));
			driver.get(tempHtml.toURI().toString());
		} catch (IOException e) {}

		final String osVersion = getDesktopDriver().getOsName() + " (" + getDesktopDriver().getOsVersion() +")";

		int maxTry = 10;
		while(maxTry > 0) {
			final DesktopWindow window = desktopDriver.getWindowByTitle(titleUid);
			if(window != null) {
				desktopDriver.setEngine(new DesktopDriverEngine(channel, window));
				channel.setApplicationData(
						osVersion,
						applicationVersion,
						driverVersion,
						window.getPid());
				maxTry = 0;
			}else {
				channel.sleep(300);
				maxTry--;
			}
		}

		try {
			driverSession = new URI(driverProcess.getDriverServerUrl() + "/session/" + driver.getSessionId().toString());
		} catch (URISyntaxException e) {}


	}

	@Override
	public DesktopDriver getDesktopDriver() {
		return desktopDriver;
	}

	protected Channel getChannel() {
		return channel;
	}

	@Override
	public void waitAfterAction(ActionStatus status) {
		actionWait();
	}

	protected String[] getWindowsHandle(int index, int tries) {
		Set<String> list = getDriverWindowsList();
		int maxTry = 1 + tries;
		while(index >= list.size() && maxTry > 0) {
			channel.sleep(1000);
			list = getDriverWindowsList();
			maxTry--;
		}
		return list.toArray(new String[list.size()]);
	}

	protected Set<String> getDriverWindowsList(){
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
		if(delta == 0) {
			scroll(element);
		}else {
			final ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(JS_ELEMENT_SCROLL, element.getValue(), delta);
			updatePosition(newPosition, element);
		}
	}

	@Override
	public void scroll(FoundElement element) {
		updatePosition((ArrayList<Double>) runJavaScript(JS_SCROLL_IF_NEEDED, element.getValue()), element);
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	private void updatePosition(ArrayList<Double> position, FoundElement element) {
		if(position != null && position.size() > 1) {
			element.updatePosition(position.get(0), position.get(1), channel, 0.0, 0.0);
			channel.sleep(500);
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
	public WebElement getRootElement(Channel cnl) {
		int maxTry = 20;

		WebElement body = getHtmlView();

		while(body == null && maxTry > 0) {
			maxTry--;
			body = getHtmlView();
		}

		return body;
	}

	@Override
	public String getTitle() {
		return driver.getTitle();
	}

	private WebElement getHtmlView() {
		return (WebElement)driver.executeScript("return window.document.getElementsByTagName(\"html\")[0];");
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
			channel.sendWarningLog("Property not found", tryLoop + "");
			channel.sleep(getPropertyWait());
			tryLoop--;
		}
		return null;
	}

	@Override
	public List<String[]> loadSelectOptions(TestElement element) {
		final ArrayList<String[]> result = new ArrayList<String[]>();
		final List<FoundElement> options = findSelectOptions(null, element);

		if(options != null && options.size() > 0) {
			options.stream().forEachOrdered(e -> result.add(new String[]{e.getValue().getAttribute("value"), e.getValue().getAttribute("text")}));
		}
		return result;
	}

	@Override
	public List<FoundElement> findSelectOptions(TestBound dimension, TestElement element) {
		switchToDefaultContent();
		return findElements(false, element, "option", new String[0], new String[0], Objects::nonNull, element.getWebElement(), false);
	}

	@Override
	public void selectOptionsItem(ActionStatus status, TestElement element, CalculatedProperty selectProperty) {

		final List<FoundElement> items = findSelectOptions(null, element);

		if(items != null && items.size() > 0) {

			element.click(status, new MouseDirection());

			if(ActionSelect.SELECT_INDEX.equals(selectProperty.getName())){

				final int index = Utils.string2Int(selectProperty.getValue().getCalculated());
				if(items.size() > index) {
					try {
						items.get(index).getValue().click();
					}catch (Exception e) {
						new Select(items.get(index).getValue()).selectByIndex(index);
					}
				}else {
					status.setError(ActionStatus.OBJECT_NOT_INTERACTABLE, "index not found, max length options : " + items.size());
				}

			}else{

				final String attribute = selectProperty.getName();
				final String searchedValue = selectProperty.getValue().getCalculated();
				Optional<FoundElement> foundOption = null;

				if(selectProperty.isRegexp()) {
					foundOption = items.stream().filter(e -> e.getValue().getAttribute(attribute).matches(searchedValue)).findFirst();
				}else {
					foundOption = items.stream().filter(e -> e.getValue().getAttribute(attribute).equals(searchedValue)).findFirst();
				}

				try {
					foundOption.get().getValue().click();
				}catch (NoSuchElementException e) {
					status.setError(ActionStatus.OBJECT_NOT_INTERACTABLE, "option not found : " + searchedValue);
				}
			}

			try {
				driver.executeScript("arguments[0].blur();", element.getWebElement());
			}catch(Exception e) {}
		}
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
				final Object obj = executeJavaScript(status, attributeName, true);
				if(obj != null) {
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
		return new CalculatedProperty[0];
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
	public void close(boolean keepRunning) {
		if(driver != null){
			Arrays.asList(getWindowsHandle(0, 0)).stream().sorted(Collections.reverseOrder()).forEach(s -> closeWindowHandler(s));
			driver.quit();
			channel.sleep(1000);
		}
		getDriverProcess().close(keepRunning);
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Mouse position by browser
	//-----------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2,	Cartesian cart3) {
		return super.getCartesianOffset(value, direction, cart1, cart2, cart3) - value/2;
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean withDesktop, int offsetX, int offsetY) {

		channel.waitBeforeMouseMoveToElement(this);

		if(withDesktop) {
			desktopMoveToElement(foundElement, position,offsetX ,offsetY);
		}else {
			int maxTry = 10;
			while(maxTry > 0) {
				status.setNoError();
				try {
					scrollAndMove(foundElement, position, offsetX, offsetY);
					maxTry = 0;
				}catch(StaleElementReferenceException e0) {
					throw e0;
				}catch(JavascriptException e1) {
					switchToDefaultContent();
					status.setException(ActionStatus.JAVASCRIPT_ERROR, e1);
					throw e1;
				}catch(MoveTargetOutOfBoundsException e2) {
					driver.executeScript("arguments[0].scrollIntoView();", foundElement.getValue());
					maxTry = 0;
				}catch(WebDriverException e3) {
					status.setException(ActionStatus.WEB_DRIVER_ERROR, e3);
					channel.sleep(500);
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
		try {
			actions.moveToElement(element.getValue(), offsetX, offsetY).perform();
		}catch (JavascriptException e) {
			if(!e.getMessage().contains("elementsFromPoint")){
				throw e;
			}
		}
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
			status.setError(ActionStatus.OBJECT_NOT_VISIBLE, "element is not visible");
		}catch(MoveTargetOutOfBoundsException e) {
			driver.executeScript("arguments[0].click();", element.getValue());
		}catch (Exception e) {
			status.setException(ActionStatus.OBJECT_NOT_INTERACTABLE, e);
		}
	}

	protected void click(FoundElement element, int offsetX, int offsetY) {
		actions.moveToElement(element.getValue(), offsetX, offsetY)
		.click()
		.build()
		.perform();
	}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {

		final Rectangle rect = element.getRectangle();

		try {

			actions.moveToElement(element.getValue(), getOffsetX(rect, position) + offsetX, getOffsetY(rect, position) + offsetY)
			.clickAndHold(element.getValue())
			.build()
			.perform();

			status.setPassed(true);

		}catch(StaleElementReferenceException e1) {
			throw e1;
		}catch(ElementNotVisibleException e0) {	
			status.setError(ActionStatus.OBJECT_NOT_VISIBLE, "element is not visible");
		}catch (Exception e) {
			status.setException(ActionStatus.OBJECT_NOT_INTERACTABLE, e);
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
		actions.doubleClick().perform();
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
			driver.switchTo().window(handle);
			channel.sleep(1000);
			return switchToDefaultContent();
		}catch(NoSuchWindowException ex) {
			return false;
		}	
	}

	protected void switchToWindowIndex(String[] wins, int index) {

		int maxTry = 10;
		boolean switched = switchToWindowHandle(wins[index]);

		while(!switched && maxTry > 0) {
			channel.sleep(1000);
			wins = getWindowsHandle(index, 0);
			switched = switchToWindowHandle(wins[index]);
		}

		if(switched) {
			currentWindow = index;
		}
	}

	@Override
	public void setWindowToFront() {
		final String[] wins = getWindowsHandle(0, 0);
		if(wins.length> currentWindow) {
			driver.switchTo().window(wins[currentWindow]);
		}
	}

	@Override
	public void switchWindow(ActionStatus status, int index, int tries) {
		channel.waitBeforeSwitchWindow(this);
		if(index >= 0) {
			final String[] wins = getWindowsHandle(index, tries);
			if(wins.length > index) {
				switchToWindowIndex(wins, index);
				channel.cleanHandle();
				getDesktopDriver().updateWindowHandle(channel);
			}else {
				status.setError(ActionStatus.WINDOW_NOT_FOUND, "cannot switch to window index '" + index + "', only " + wins.length + " window(s) found");
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

	protected void closeWindowHandler(String windowHandle) {
		switchToWindowHandle(windowHandle);
		closeCurrentWindow();
	}

	protected void closeCurrentWindow() {
		driver.close();
		channel.sleep(300);
	}

	@Override
	public void closeWindow(ActionStatus status) {
		final String[] list = getWindowsHandle(0, 0);
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
	public Object executeJavaScript(ActionStatus status, String javaScript, boolean returnValue) {
		try {
			if(returnValue) {
				final Object result = driver.executeAsyncScript("var callback=arguments[arguments.length-1];var result=" + javaScript + ";callback(result);");
				status.setMessage(result.toString());
				return result;
			}else {
				driver.executeScript(javaScript);
			}
			status.setPassed(true);
		}catch(StaleElementReferenceException e0) {
			throw e0;
		}catch(Exception e1) {
			status.setException(ActionStatus.JAVASCRIPT_ERROR, e1);
		}
		return null;
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String javaScript, TestElement element) {
		return executeJavaScript(status, javaScript, element.getWebElement());
	}

	public Object executeJavaScript(ActionStatus status, String javaScript, WebElement element) {
		final Object result = runJavaScript(status, "var e=arguments[0];var result=e." + javaScript.replaceAll("this", "e") + ";", element);
		if(status.isPassed() && result != null) {
			status.setMessage(result.toString());
		}
		return result;
	}

	//TODO remove this default method and add actionstatus
	public Object runJavaScript(String javaScript, Object ... params) {
		return runJavaScript(channel.newActionStatus(), javaScript, params);
	}

	public Object runJavaScriptResult(String javaScript) {
		try {
			return driver.executeAsyncScript("var result=" + javaScript + ";arguments[arguments.length-1](result);");
		}catch(Exception e) {
			return e.getMessage();
		}
	}

	protected Object runJavaScript(ActionStatus status, String javaScript, Object ... params) {
		status.setPassed(true);
		try {
			return driver.executeAsyncScript(javaScript + ";arguments[arguments.length-1](result);", params);
		}catch(StaleElementReferenceException e0) {
			status.setPassed(false);
			throw e0;
		}catch(Exception e1) {
			status.setException(ActionStatus.JAVASCRIPT_ERROR, e1);
		}
		return null;
	}

	public List<Double> getBoundingClientRect(RemoteWebElement element) {
		try {
			return (List<Double>) driver.executeAsyncScript("var callback=arguments[arguments.length-1], rect=arguments[0].getBoundingClientRect();var result=[rect.x+0.0001, rect.y+0.0001, rect.width+0.0001, rect.height+0.0001];callback(result);", element);
		}catch (Exception e) {
			return List.of(0D, 0D, 1D, 1D);
		}
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

		actionWait();
	}

	protected void loadUrl(String url) {
		driver.get(url);
	}

	private WebElement iframe = null;
	private double offsetIframeX = 0.0;
	private double offsetIframeY = 0.0;

	@Override
	public List<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, String[] attributes, String[] attributesValues, Predicate<AtsBaseElement> predicate, WebElement startElement, boolean waitAnimation) {

		if(testObject.getParent() != null){
			if(testObject.getParent().isIframe()) {

				iframe = testObject.getParent().getWebElement();

				try {

					final Point pt = iframe.getLocation();

					offsetIframeX += pt.getX();
					offsetIframeY += pt.getY();

					switchToFrame(iframe);

				}catch(WebDriverException e) {
					return Collections.<FoundElement>emptyList();
				}

			}else if(startElement == null) {
				startElement = testObject.getParent().getWebElement();
			}
		}else {
			if(iframe != null) {
				iframe = null;
				offsetIframeX = 0.0;
				offsetIframeY = 0.0;
			}

			if(!switchToDefaultContent()) {
				return Collections.<FoundElement>emptyList();
			}
		}

		channel.waitBeforeSearchElement(this);

		final List<List<Object>> response = (List<List<Object>>) runJavaScript(searchElementScript, startElement, tagName, attributes, attributes.length);
		if(response != null && response.size() > 0){
			final List<AtsElement> elements = response.parallelStream().filter(Objects::nonNull).map(e -> new AtsElement(e)).collect(Collectors.toCollection(ArrayList::new));
			return elements.parallelStream().filter(Objects::nonNull).filter(predicate).map(e -> new FoundElement(this, e, channel, initElementX + offsetIframeX, initElementY + offsetIframeY, waitAnimation)).collect(Collectors.toCollection(ArrayList::new));
		}

		return Collections.<FoundElement>emptyList();
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

	@Override
	public void clearText(ActionStatus status, TestElement te, MouseDirection md) {

		te.click(status, md);

		if(status.isPassed()) {
			final FoundElement element = te.getFoundElement();

			try {
				executeScript(status, "arguments[0].value='';", element.getValue());
				status.setMessage("");
				return;
			}catch (StaleElementReferenceException e) {}

			try {
				element.getValue().clear();
				status.setMessage("");
				return;
			}catch(Exception e) {}

		}

		status.setError(ActionStatus.ENTER_TEXT_FAIL, "clear text failed on this element");
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {

		final WebElement we = element.getWebElement();
		executeScript(status, "result={size:window.getComputedStyle(arguments[0], null).getPropertyValue('font-size'), family:window.getComputedStyle(arguments[0], null).getPropertyValue('font-family'), weight:window.getComputedStyle(arguments[0], null).getPropertyValue('font-weight')};", we);

		for(SendKeyData sequence : textActionList) {
			we.sendKeys(sequence.getSequenceWithDigit());
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