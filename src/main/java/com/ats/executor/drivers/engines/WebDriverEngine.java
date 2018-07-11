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
import java.net.URL;
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
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.tools.StartHtmlPage;

@SuppressWarnings("unchecked")
public class WebDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	protected DesktopDriver desktopDriver;

	protected Double initElementX = 0.0;
	protected Double initElementY = 0.0;

	private Proxy proxy;

	private int scriptTimeout;
	private int loadPageTimeOut;

	private DriverProcess driverProcess;

	private Actions actions;

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

		super(channel, browser, ats.getBrowserProperties(browser), defaultWait);

		this.driverProcess = driverProcess;
		this.desktopDriver = desktopDriver;
		this.proxy = ats.getProxy();
		this.loadPageTimeOut = ats.getPageloadTimeOut();
		this.scriptTimeout = ats.getScriptTimeOut();
	}

	protected DriverProcess getDriverProcess() {
		return driverProcess;
	}

	protected void launchDriver(MutableCapabilities cap) {

		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.PROXY, proxy);
		cap.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);

		int maxTry = 20;
		String errorMessage = null;
		while(driver == null && maxTry > 0) {
			try{
				driver = new RemoteWebDriver(driverProcess.getDriverServerUrl(), cap);
			}catch(Exception ex){
				errorMessage = ex.getMessage();
				driver = null;
			}
			maxTry--;
		}

		if(maxTry == 0) {
			channel.setStartError(errorMessage);
			return;
		}

		actions = new Actions(driver);

		driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(loadPageTimeOut, TimeUnit.SECONDS);

		try{
			driver.manage().window().setSize(channel.getDimension().getSize());
			driver.manage().window().setPosition(channel.getDimension().getPoint());
		}catch(Exception ex){
			System.err.println(ex.getMessage());
		}

		String applicationVersion = "N/A";
		String driverVersion = null;

		Map<String, ?> infos = (this.driver).getCapabilities().asMap();
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

		String titleUid = UUID.randomUUID().toString();
		try {
			File tempHtml = File.createTempFile("ats_", ".html");
			tempHtml.deleteOnExit();

			Files.write(tempHtml.toPath(), StartHtmlPage.getAtsBrowserContent(titleUid, application, applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait(), channel.getMaxTry()));
			driver.get(tempHtml.toURI().toString());
		} catch (IOException e) {}

		channel.setApplicationData(
				applicationVersion,
				driverVersion,
				desktopDriver.getProcessDataByWindowTitle(titleUid));

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

			ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(code, element.getValue(), delta);

			if(newPosition.size() > 1) {
				element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
			}

		}else {
			runJavaScript(JS_WINDOW_SCROLL, delta);
		}
	}

	@Override
	public void forceScrollElement(FoundElement element) {
		ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(autoScrollElement, element.getValue());
		if(newPosition.size() > 1) {
			element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
		}
	}

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

		ArrayList<Object> objectData = (ArrayList<Object>)runJavaScript(JS_ELEMENT_DATA, x - offsetX, y - offsetY);

		if(objectData != null){

			if(FoundElement.IFRAME.equals(objectData.get(1))){

				FoundElement frm = new FoundElement(objectData);

				switchToFrame(frm.getValue());

				offsetX += (Double)objectData.get(4);
				offsetY += (Double)objectData.get(5);

				return loadElement(x, y, offsetX, offsetY);

			} else {
				return new FoundElement(objectData, channel, offsetX, offsetY);
			}

		} else {
			return null;
		}
	}

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

		String result = null;
		int tryLoop = maxTry;

		while (result == null && tryLoop > 0){
			tryLoop--;
			result = getAttribute(element, attributeName);

			if(result != null && !doubleCheckAttribute(result, element, attributeName)) {
				result = null;
			}
		}

		return result;
	}

	private boolean doubleCheckAttribute(String verify, FoundElement element, String attributeName) {
		channel.sleep(50);
		String current = getAttribute(element, attributeName);
		return current != null && current.equals(verify);
	}

	private String getAttribute(FoundElement element, String attributeName) {

		RemoteWebElement elem = getWebElement(element);
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
		Stream<CalculatedProperty> stream = Arrays.stream(properties);
		Optional<CalculatedProperty> calc = stream.parallel().filter(c -> c.getName().equals(name)).findFirst();
		if(calc.isPresent()) {
			return calc.get().getValue().getCalculated();
		}
		return null;
	}

	public CalculatedProperty[] getAttributes(FoundElement element){
		if(element.isDesktop()){
			return desktopDriver.getElementAttributes(element.getId());
		}else {
			return getAttributes(getWebElement(element));
		}
	}

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
		ArrayList<ArrayList<String>> result = (ArrayList<ArrayList<String>>) runJavaScript(script, element);
		if(result != null){
			return result.stream().parallel().map(p -> new CalculatedProperty(p.get(0), p.get(1))).toArray(s -> new CalculatedProperty[s]);
		}
		return null;
	}

	public FoundElement getTestElementParent(FoundElement element){

		ArrayList<ArrayList<Object>> listElements = (ArrayList<ArrayList<Object>>) runJavaScript(JS_ELEMENT_PARENTS, element.getValue());

		if(listElements != null && listElements.size() > 0){
			return new FoundElement(channel, listElements, initElementX, initElementY);
		}

		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@Override
	public TestBound[] getDimensions() {

		switchWindow(currentWindow);

		ArrayList<ArrayList<Double>> response = (ArrayList<ArrayList<Double>>) runJavaScript(JS_DOCUMENT_SIZE);

		TestBound testDimension = new TestBound();
		TestBound testSubDimension = new TestBound();

		if(response != null) {
			testDimension.update(response.get(0));
			testSubDimension.update(response.get(1));
		}

		return new TestBound[]{testDimension, testSubDimension};
	}

	@Override
	public void close() {
		if(driver != null){
			ArrayList<String> list = new ArrayList<String>();

			try {
				list.addAll(driver.getWindowHandles());
			}catch(WebDriverException e) {}

			while(list.size() > 0) {
				String winHandler = list.remove(list.size()-1);
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

	// here I must found a way to be sure that the element is enabled AND visible, I have found thar is nothing working on both chrome, firefox, opera and edge browsers
	/*protected boolean isInteractable(RemoteWebElement rwe) {
		return (Boolean) runJavaScript(ResourceContent.getVisibilityJavaScript(), rwe) && rwe.isEnabled();
	}*/

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {

		Rectangle rect = foundElement.getRectangle();

		int offsetX = getOffsetX(rect, position);
		int offsetY = getOffsetY(rect, position);

		move(foundElement, offsetX, offsetY);

		ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(JS_ELEMENT_BOUNDING, foundElement.getValue());
		if(newPosition.size() > 1) {
			foundElement.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
		}
	}

	protected void move(FoundElement element, int offsetX, int offsetY) {
		actions.moveToElement(element.getValue(), offsetX, offsetY).perform();
	}

	@Override
	public void mouseClick(FoundElement element, boolean hold) {
		if(hold) {
			actions.clickAndHold(element.getValue()).perform();
		}else {
			actions.click(element.getValue()).perform();
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
		RemoteWebElement rwe = new RemoteWebElement();
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
		int windowsNum = driver.getWindowHandles().size();
		switchWindow(windowsNum - 1);
	}		

	protected void switchToFirstWindow(String[] list) {
		switchToWindowHandle(list[0]);
		currentWindow = 0;
	}

	@Override
	public void switchWindow(int index) {

		if(currentWindow != index) {

			Set<String> list = driver.getWindowHandles();

			int maxTry = 20;
			while(index >= list.size() && maxTry > 0) {
				list = driver.getWindowHandles();
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

		ArrayList<String> list = new ArrayList<String>();
		try {
			list.addAll(driver.getWindowHandles());
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

		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpDelete request = new HttpDelete(driverSession + "/window");
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

		Object result = null;

		try {

			result = runJavaScript("var result=null;" + javaScript + ";", params);

			status.setPassed(true);
			if(result != null) {
				status.setMessage(result.toString());
			}

		} catch(WebDriverException e){
			if(e instanceof StaleElementReferenceException) {
				throw e;
			}else {
				status.setPassed(false);
				status.setCode(ActionStatus.JAVASCRIPT_ERROR);
				status.setMessage(e.getMessage().replace("javascript error:", ""));
			}
		}

		return result;
	}

	protected Object runJavaScript(String javaScript, Object ... params) {
		try {
			return driver.executeAsyncScript(javaScript + ";arguments[arguments.length-1](result);", params);
		}catch(Exception ex) {
			return null;
		}
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		switchToDefaultContent();
		driver.get(url.toString());
		actionWait();
	}

	private WebElement iframe = null;
	private double offsetIframeX = 0.0;
	private double offsetIframeY = 0.0;

	@Override
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, ArrayList<String> attributes,
			Predicate<Map<String, Object>> predicate) {

		if(tagName == null) {
			tagName = "BODY";
		}

		ArrayList<FoundElement> webElementList = new ArrayList<FoundElement>();
		WebElement startElement = null;

		if(testObject.getParent() != null){
			if(testObject.getParent().isIframe()) {

				iframe = testObject.getParent().getWebElement();
				Point pt = iframe.getLocation();
				
				offsetIframeX += pt.getX();
				offsetIframeY += pt.getY();

				try {
					switchToFrame(iframe);
				}catch(StaleElementReferenceException e) {

					testObject.getParent().searchAgain();
					return webElementList;
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
		
		ArrayList<Map<String, Object>> response = (ArrayList<Map<String, Object>>) runJavaScript(searchElementScript, startElement, tagName, attributes, attributes.size());
		if(response != null){
			
			final double elmX = initElementX + offsetIframeX;
			final double elmY = initElementY + offsetIframeY;
			
			response.parallelStream().filter(predicate).forEachOrdered(e -> webElementList.add(new FoundElement((ArrayList<Object>)e.get("ats-elt"), channel, elmX, elmY)));
		}

		return webElementList;
	}

	@Override
	public void middleClick(ActionStatus status, TestElement element) {
		runJavaScript(JS_MIDDLE_CLICK, element.getWebElement());
	}

	protected void middleClickSimulation(ActionStatus status, TestElement element) {
		element.click(status, Keys.CONTROL);
	}

	@Override
	public Alert switchToAlert() {
		return driver.switchTo().alert();
	}

	@Override
	public void navigationRefresh() {
		driver.navigate().refresh();
	}

	@Override
	public void navigationForward() {
		driver.navigate().forward();
	}

	@Override
	public void navigationBack() {
		driver.navigate().back();
	}

	@Override
	public String getCurrentUrl() {
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
	public void sendTextData(ActionStatus status, FoundElement element, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			element.getValue().sendKeys(sequence.getSequence());
		}
	}

	@Override
	public void setWindowToFront() {
		List<String> listWins = new ArrayList<>(driver.getWindowHandles());
		if(listWins.size() > currentWindow) {
			driver.switchTo().window(listWins.get(currentWindow));
		}
	}
}