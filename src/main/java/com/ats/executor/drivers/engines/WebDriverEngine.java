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
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.AtsManager;
import com.ats.element.AtsBaseElement;
import com.ats.element.AtsElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionGotoUrl;
import com.ats.tools.ResourceContent;
import com.ats.tools.StartHtmlPage;

@SuppressWarnings("unchecked")
public class WebDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	protected static final String WEB_ELEMENT_REF = "element-6066-11e4-a52e-4f735466cecf";

	//-----------------------------------------------------------------------------------------------------------------------------
	// Javascript static code
	//-----------------------------------------------------------------------------------------------------------------------------

	protected static final String JS_WAIT_READYSTATE = "var result=window.document.readyState=='complete';";
	protected static final String JS_WAIT_BEFORE_SEARCH = "var interval=setInterval(function(){if(window.document.readyState==='complete'){clearInterval(interval);done();}},200);";

	protected static final String JS_AUTO_SCROLL = "var e=arguments[0];e.scrollIntoView();var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected static final String JS_AUTO_SCROLL_CALC = "var e=arguments[0];var r=e.getBoundingClientRect();var top=r.top + window.pageYOffset;window.scrollTo(0, top-(window.innerHeight / 2));r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected static final String JS_AUTO_SCROLL_MOZ = "var e=arguments[0];e.scrollIntoView({behavior:'auto',block:'center',inline:'center'});var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";

	protected static final String JS_ELEMENT_SCROLL = "var e=arguments[0];var d=arguments[1];e.scrollTop += d;var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected static final String JS_WINDOW_SCROLL = "window.scrollBy(0,arguments[0]);var result=[0.0001, 0.0001]";
	protected static final String JS_ELEMENT_DATA = "var result=null;var e=document.elementFromPoint(arguments[0],arguments[1]);if(e){var r=e.getBoundingClientRect();result=[e, e.tagName, e.getAttribute('inputmode')=='numeric', r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001, 0.0001, 0.0001];};";
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
	protected String autoScrollElement = JS_AUTO_SCROLL;

	public WebDriverEngine(
			Channel channel, 
			String browser, 
			DriverProcess driverProcess, 
			DesktopDriver desktopDriver,
			AtsManager ats,
			int defaultWait) {

		super(channel, desktopDriver, browser, ats.getBrowserProperties(browser), defaultWait, 60);

		this.driverProcess = driverProcess;
	}

	protected DriverProcess getDriverProcess() {
		return driverProcess;
	}

	protected void launchDriver(ActionStatus status, MutableCapabilities cap) {

		final int maxTrySearch = DriverManager.ATS.getMaxTrySearch();
		final int maxTryProperty = DriverManager.ATS.getMaxTryProperty();

		final int scriptTimeout = DriverManager.ATS.getScriptTimeOut();
		final int pageLoadTimeout = DriverManager.ATS.getPageloadTimeOut();
		final int watchdog = DriverManager.ATS.getWatchDogTimeOut();		

		cap.setCapability(CapabilityType.PROXY, DriverManager.ATS.getProxy().getValue());
		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);

		int maxTry = 20;
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
		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			status.setMessage(errorMessage);

			driverProcess.close();
			return;
		}

		actions = new Actions(driver);

		driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);

		try{
			driver.manage().window().setSize(channel.getDimension().getSize());
			driver.manage().window().setPosition(channel.getDimension().getPoint());
		}catch(Exception ex){
			System.err.println(ex.getMessage());
		}

		String applicationVersion = "N/A";
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
			}
		}

		final String titleUid = UUID.randomUUID().toString();
		try {
			final File tempHtml = File.createTempFile("ats_", ".html");
			tempHtml.deleteOnExit();

			Files.write(tempHtml.toPath(), StartHtmlPage.getAtsBrowserContent(titleUid, application, applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait(), getPropertyWait(), maxTrySearch, maxTryProperty, scriptTimeout, pageLoadTimeout, watchdog));
			driver.get(tempHtml.toURI().toString());
		} catch (IOException e) {}


		maxTry = 10;
		while(maxTry > 0) {
			final DesktopWindow window = desktopDriver.getWindowByTitle(titleUid);
			if(window != null) {

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
				.setConnectTimeout(3500)
				.setConnectionRequestTimeout(3500)
				.setSocketTimeout(3500).build();
		try {
			driverSession = new URI(driverProcess.getDriverServerUrl() + "/session/" + driver.getSessionId().toString());
		} catch (URISyntaxException e) {}
	}

	@Override
	public DesktopDriver getDesktopDriver() {
		return desktopDriver;
	}

	protected void closeDriver() {
		driverProcess.close();
	}

	protected Channel getChannel() {
		return channel;
	}

	@Override
	public void waitAfterAction() {
		actionWait();
	}

	private Set<String> getWindowsHandle() {
		try {
			driver.switchTo().defaultContent();
		}catch(WebDriverException ex) {}
		return driver.getWindowHandles();
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public void scroll(FoundElement element, int delta) {
		if(element != null && element.getTag() != null) {
			String code = null;
			if(delta == 0) {
				code = JS_ELEMENT_AUTOSCROLL;
			}else {
				code = JS_ELEMENT_SCROLL;
			}

			final ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(code, element.getValue(), delta);

			if(newPosition.size() > 1) {
				element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
			}

		}else {
			runJavaScript(JS_WINDOW_SCROLL, delta);
		}
	}

	@Override
	public void forceScrollElement(FoundElement element) {
		final ArrayList<Double> newPosition = (ArrayList<Double>) runJavaScript(autoScrollElement, element.getValue());
		if(newPosition != null && newPosition.size() > 1) {
			element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
		}
	}

	@Override
	public FoundElement getElementFromPoint(Double x, Double y){

		if(x < channel.getSubDimension().getX() || y < channel.getSubDimension().getY()) {

			return desktopDriver.getElementFromPoint(x, y);

		}else {

			switchToDefaultContent();

			x -= channel.getSubDimension().getX();
			y -= channel.getSubDimension().getY();

			return loadElement(x, y, initElementX, initElementY);
		}
	}

	private FoundElement loadElement(Double x, Double y, Double offsetX, Double offsetY) {

		final ArrayList<Object> objectData = (ArrayList<Object>)runJavaScript(JS_ELEMENT_DATA, x - offsetX, y - offsetY);

		if(objectData != null){

			final AtsElement element = new AtsElement(objectData);

			if(element.isIframe()){

				FoundElement frm = new FoundElement(element);

				switchToFrame(frm.getValue());

				offsetX += element.getX();
				offsetY += element.getY();

				return loadElement(x, y, offsetX, offsetY);

			} else {
				return new FoundElement(element, channel, offsetX, offsetY);
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
			return driver.findElementByXPath("//body");
		}catch(NoSuchElementException ex) {
			channel.sleep(200);
			return null;
		}
	}

	private RemoteWebElement getWebElement(FoundElement element) {
		return element.getRemoteWebElement(driver);
	}

	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		int tryLoop = maxTry;
		while (tryLoop > 0){
			String result = getAttribute(element, attributeName);
			if(result != null && doubleCheckAttribute(result, element, attributeName)) {
				return result;
			}
			tryLoop--;
		}
		return null;
	}

	private boolean doubleCheckAttribute(String verify, FoundElement element, String attributeName) {
		channel.sleep(getPropertyWait());

		final String current = getAttribute(element, attributeName);
		return current != null && current.equals(verify);
	}

	private String getAttribute(FoundElement element, String attributeName) {

		final RemoteWebElement elem = getWebElement(element);
		String result = elem.getAttribute(attributeName);

		if(result == null) {

			for (CalculatedProperty calc : getAttributes(element)) {
				if(attributeName.equals(calc.getName())) {
					return calc.getValue().getCalculated();
				}
			}
			result = getCssAttributeValueByName(element, attributeName);

			if(result == null) {
				channel.sleep(100);
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
	public CalculatedProperty[] getAttributes(FoundElement element){
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
		final Map<String, String> result = (Map<String, String>) runJavaScript(script, element);
		if(result != null){
			return result.entrySet().stream().parallel().map(e -> new CalculatedProperty(e.getKey(), e.getValue())).toArray(c -> new CalculatedProperty[c]);
		}
		return null;
	}

	public FoundElement getTestElementParent(FoundElement element){
		final ArrayList<ArrayList<Object>> listElements = (ArrayList<ArrayList<Object>>) runJavaScript(JS_ELEMENT_PARENTS, element.getValue());
		if(listElements != null){
			return new FoundElement(
					channel, 
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
	public void updateDimensions(Channel channel) {

		switchWindow(currentWindow);

		TestBound dimension = new TestBound();
		TestBound subDimension = new TestBound();

		final ArrayList<Double> response = (ArrayList<Double>) runJavaScript(JS_DOCUMENT_SIZE);

		if(response != null) {
			dimension.update(response.get(0), response.get(1), response.get(2), response.get(3));
			subDimension.update(response.get(4), response.get(5), response.get(6), response.get(7));
		}

		channel.setDimensions(dimension, subDimension);
	}

	@Override
	public void close() {
		if(driver != null){
			final ArrayList<String> list = new ArrayList<String>();

			try {
				list.addAll(getWindowsHandle());
			}catch(WebDriverException e) {}

			while(list.size() > 0) {
				final String winHandler = list.remove(list.size()-1);
				if(list.size() == 0) {
					closeLastWindow(new ActionStatus(channel));
				}else {
					closeWindowHandler(winHandler);
				}
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Mouse position by browser
	//-----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {

		final Rectangle rect = foundElement.getRectangle();
		move(foundElement, getOffsetX(rect, position), getOffsetY(rect, position));

		final ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(status, JS_ELEMENT_BOUNDING, foundElement.getValue());
		if(newPosition.size() > 1) {
			foundElement.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
		}
	}

	protected void move(FoundElement element, int offsetX, int offsetY) {
		actions.moveToElement(element.getValue(), offsetX, offsetY).perform();
	}

	@Override
	public void mouseClick(FoundElement element, MouseDirection position, boolean hold) {
		
		final Rectangle rect = element.getRectangle();
		final int xOffset = getOffsetX(rect, position);
		final int yOffset = getOffsetY(rect, position);
			
		Actions act = actions.moveToElement(element.getValue(), xOffset, yOffset);
		if(hold) {
			act = act.clickAndHold();
			//actions.clickAndHold(element.getValue()).perform();
		}else {
			act = act.click();
			//actions.click(element.getValue()).perform();
		}
		act.build().perform();
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
	public void drop() {
		actions.release().perform();
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
	public void switchToDefaultContent() {
		try {
			driver.switchTo().defaultContent();
		}catch (NoSuchWindowException e) {
			//switchWindow(0);
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

	protected void switchToWindowHandle(String handle) {
		driver.switchTo().window(handle);
		switchToDefaultContent();
	}

	protected void switchToLastWindow() {
		int windowsNum = getWindowsHandle().size();
		switchWindow(windowsNum - 1);
	}		

	protected void switchToFirstWindow(String[] list) {
		switchToWindowHandle(list[0]);
		currentWindow = 0;
	}

	@Override
	public void switchWindow(int index) {

		if(currentWindow != index) {

			Set<String> list = getWindowsHandle();

			int maxTry = 20;
			while(index >= list.size() && maxTry > 0) {
				list = getWindowsHandle();
				channel.sleep(500);
				maxTry--;
			}

			String[] wins = list.toArray(new String[list.size()]);

			if(maxTry == 0) {
				switchToFirstWindow(wins);
			}else {
				try {
					switchToWindowHandle(wins[index]);
					currentWindow = index;
				}catch(NoSuchWindowException ex) {
					switchToFirstWindow(wins);
				}	
			}
		}else {
			switchToDefaultContent();
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
		driver.close();
		channel.sleep(200);
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {

		final ArrayList<String> list = new ArrayList<String>();
		try {
			list.addAll(getWindowsHandle());
		}catch(WebDriverException e) {}

		if(list.size() == 1) {

			closeLastWindow(status);

		}else {

			if(index < list.size()) {
				closeWindowHandler(list.get(index));
				index--;
			}

			if(index < 0) {
				index = 0;
			}

			currentWindow = index;
			switchToWindowHandle(list.get(index));
		}
	}

	private void closeLastWindow(ActionStatus status) {

		final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		final HttpDelete request = new HttpDelete(driverSession + "/window");
		request.addHeader("content-type", "application/json");

		try {
			httpClient.execute(request);
		} catch (SocketTimeoutException e) {

		} catch (IOException e) {

		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {}
		}

		closeDriver();
		channel.lastWindowClosed(status);
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

	//TODO remove this default method and add actionstatus
	protected Object runJavaScript(String javaScript, Object ... params) {
		return runJavaScript(new ActionStatus(channel), javaScript, params);
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
			driver.get(url);
		}

		status.setPassed(true);
		status.setData(url);
		status.setMessage(getCurrentUrl());

		actionWait();
	}

	private WebElement iframe = null;
	private double offsetIframeX = 0.0;
	private double offsetIframeY = 0.0;

	@Override
	public ArrayList<FoundElement> findElements(Channel channel, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> predicate) {

		if(tagName == null) {
			tagName = "BODY";
		}

		WebElement startElement = null;

		if(testObject.getParent() != null){
			if(testObject.getParent().isIframe()) {

				iframe = testObject.getParent().getWebElement();

				try {

					final Point pt = iframe.getLocation();

					offsetIframeX += pt.getX();
					offsetIframeY += pt.getY();

					switchToFrame(iframe);

				}catch(StaleElementReferenceException e) {
					return new ArrayList<FoundElement>();
				}

			}else {
				startElement = testObject.getParent().getWebElement();
			}

		}else if(iframe != null){

			iframe = null;
			offsetIframeX = 0.0;
			offsetIframeY = 0.0;

			switchToDefaultContent();
		}

		final ArrayList<ArrayList<Object>> response = (ArrayList<ArrayList<Object>>) runJavaScript(searchElementScript, startElement, tagName, attributes, attributes.size());
		if(response != null){

			final ArrayList<AtsElement> elements = response.parallelStream().map(e -> new AtsElement(e)).collect(Collectors.toCollection(ArrayList::new));

			final double elmX = initElementX + offsetIframeX;
			final double elmY = initElementY + offsetIframeY;

			return elements.parallelStream().filter(predicate).map(e -> new FoundElement(e, channel, elmX, elmY)).collect(Collectors.toCollection(ArrayList::new));
		}else {
			return new ArrayList<FoundElement>();
		}
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
	}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			element.getWebElement().sendKeys(sequence.getSequenceWithDigit());
		}
	}

	@Override
	public void setWindowToFront() {
		final List<String> listWins = new ArrayList<>(getWindowsHandle());
		if(listWins.size() > currentWindow) {
			driver.switchTo().window(listWins.get(currentWindow));
		}
	}

	@Override
	public void refreshElementMapLocation(Channel channel) {
		getDesktopDriver().refreshElementMapLocation(channel);
	}
}