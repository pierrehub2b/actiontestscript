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

import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
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
import com.ats.tools.ResourceContent;
import com.ats.tools.StartHtmlPage;
import com.ats.tools.logger.MessageCode;

public class WebDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	protected DesktopDriver desktopDriver;

	protected Double initElementX = 0.0;
	protected Double initElementY = 0.0;

	private Proxy proxy;

	private int scriptTimeout;
	private int loadPageTimeOut;
	private int maxTryInteractable;

	private DriverProcess driverProcess;

	private Actions actions;

	private String firstWindow;
	protected int waitBeforeSwitch = 100;

	public WebDriverEngine(
			Channel channel, 
			String browser, 
			DriverProcess driverProcess, 
			DesktopDriver desktopDriver,
			AtsManager ats,
			int defaultWait) {

		super(channel, browser, ats, defaultWait);

		this.driverProcess = driverProcess;
		this.desktopDriver = desktopDriver;
		this.proxy = ats.getProxy();
		this.loadPageTimeOut = ats.getPageloadTimeOut();
		this.scriptTimeout = ats.getScriptTimeOut();
		this.maxTryInteractable = ats.getMaxTryInteractable();
	}

	protected DriverProcess getDriverProcess() {
		return driverProcess;
	}

	@SuppressWarnings("unchecked")
	protected void launchDriver(MutableCapabilities cap) {

		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.PROXY, proxy);
		cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
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

			Files.write(tempHtml.toPath(), StartHtmlPage.getAtsBrowserContent(titleUid, application, applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait()));
			driver.get(tempHtml.toURI().toString());
		} catch (IOException e) {}

		channel.setApplicationData(
				applicationVersion,
				driverVersion,
				desktopDriver.getProcessDataByWindowTitle(titleUid));

		firstWindow = driver.getWindowHandle();
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
	
	protected void waitReadyState() {
		
		int maxTry = 40;
		String readyState = "return window.document.readyState=='complete';";
		Boolean ready = (Boolean) driver.executeScript(readyState);
		
		while(!ready && maxTry > 0) {
			channel.sleep(200);
			ready = (Boolean) driver.executeScript(readyState);
			maxTry--;
		}
		
		/*int maxWait = 50;
		ArrayList<Boolean> iframesStatus = (ArrayList<Boolean>) runJavaScript(ResourceContent.getReadyStatesJavaScript());
		while(maxWait > 0 && !iframesStatus.stream().parallel().allMatch(e -> true)){
			channel.sleep(200);
			maxWait--;

			iframesStatus = (ArrayList<Boolean>) runJavaScript(ResourceContent.getReadyStatesJavaScript());
		}*/
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public void scroll(FoundElement element, int delta) {
		if(element != null && element.getTag() != null) {
			String code = null;
			if(delta == 0) {
				code = ResourceContent.getScrollElementJavaScript();
			}else {
				code = "var e=arguments[0];var d=arguments[1];e.scrollTop += d;var r=e.getBoundingClientRect();var result=[r.left+0.00001, r.top+0.00001]";
			}

			ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(code, element.getValue(), delta);

			if(newPosition.size() > 1) {
				element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
			}

		}else {
			runJavaScript("window.scrollBy(0,arguments[0]);var result=[0.00001, 0.00001]", delta);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forceScrollElement(FoundElement element) {
		String code = "var e=arguments[0];e.scrollIntoView();var r=e.getBoundingClientRect();var result=[r.left+0.00001, r.top+0.00001]";
		ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(code, element.getValue());

		if(newPosition.size() > 1) {
			element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
		}
	}

	public FoundElement getElementFromPoint(Double x, Double y){

		if(x < channel.getSubDimension().getX() || y < channel.getSubDimension().getY()) {

			return desktopDriver.getElementFromPoint(x, y);

		}else {

			//switchToDefaultContent();

			x -= channel.getSubDimension().getX();
			y -= channel.getSubDimension().getY();

			return loadElement(x, y, initElementX, initElementY);
		}
	}

	private FoundElement loadElement(Double x, Double y, Double offsetX, Double offsetY) {

		@SuppressWarnings("unchecked")
		Map<String, Object> objectData = (Map<String, Object>) runJavaScript(ResourceContent.getHoverElementJavaScript(), x - offsetX, y - offsetY);
		if(objectData != null){

			if(FoundElement.IFRAME.equals(objectData.get("tag"))){

				FoundElement frm = new FoundElement(objectData);

				switchToFrame(frm.getValue());

				offsetX += (Double)objectData.get("x");
				offsetY += (Double)objectData.get("y");

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

			if(!doubleCheckAttribute(result, element, attributeName)) {
				channel.sleep(200);
				result = null;
			}
		}

		return result;
	}

	private boolean doubleCheckAttribute(String verify, FoundElement element, String attributeName) {
		String current = getAttribute(element, attributeName);
		return current != null && current.equals(verify);
	}

	private String getAttribute(FoundElement element, String attributeName) {

		RemoteWebElement elem = getWebElement(element);
		String result = elem.getAttribute(attributeName);

		if(result == null) {

			for (CalculatedProperty calc : getAttributes(element)) {
				if(attributeName.equals(calc.getName())) {
					result = calc.getValue().getCalculated();
				}
			}

			if(result == null) {
				result = getCssAttributeValueByName(element, attributeName);
			}

			if(result == null) {
				channel.sleep(200);
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
		return getAttributesList(element, ResourceContent.getElementCssJavaScript());
	}

	private CalculatedProperty[] getAttributes(RemoteWebElement element){
		return getAttributesList(element, ResourceContent.getElementAttributesJavaScript());
	}

	private CalculatedProperty[] getAttributesList(RemoteWebElement element, String script) {
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<String>> result = (ArrayList<ArrayList<String>>) runJavaScript(script, element);
		if(result != null){
			return result.stream().parallel().map(p -> new CalculatedProperty(p.get(0), p.get(1))).toArray(s -> new CalculatedProperty[s]);
		}
		return null;
	}

	public FoundElement getTestElementParent(FoundElement element){

		@SuppressWarnings("unchecked")
		ArrayList<Map<String, Object>> listElements = (ArrayList<Map<String, Object>>) runJavaScript(ResourceContent.getParentElementJavaScript(), element.getValue());

		if(listElements != null && listElements.size() > 0){
			return new FoundElement(listElements, channel, initElementX, initElementY);
		}

		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public TestBound[] getDimensions() {

		switchWindow(currentWindow);

		Map<String, ArrayList<Double>> response = (Map<String, ArrayList<Double>>) runJavaScript(ResourceContent.getDocumentSizeJavaScript());

		TestBound testDimension = new TestBound();
		TestBound testSubDimension = new TestBound();

		if(response != null) {
			testDimension.update(response.get("main"));
			testSubDimension.update(response.get("sub"));
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

			if(list.size() > 0) {
				for (String handle : list) {
					closeWindowHandler(handle);
				}
			}
		}

		try {
			driver.quit();
		}catch(UnhandledAlertException e) {
			try {
				driver.switchTo().alert().accept();
			}catch(WebDriverException e0) {}
		}catch(Exception e) {}

		getDriverProcess().close();
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Mouse position by browser
	//-----------------------------------------------------------------------------------------------------------------------------------

	private boolean waitElementInteractable (FoundElement element) {

		RemoteWebElement rwe = (RemoteWebElement)element.getValue();
		runJavaScript(ResourceContent.getVisibilityJavaScript(), rwe);

		int maxTry = maxTryInteractable;
		boolean interactable = rwe.isEnabled();

		while(!interactable && maxTry > 0) {

			channel.sendLog(MessageCode.OBJECT_INTERACTABLE, "wait element interactable", maxTry);
			channel.sleep(300);

			interactable = rwe.isEnabled();

			maxTry--;
		}

		return interactable;
	}

	// here I must found a way to be sure that the element is enabled AND visible, I have found thar is nothing working on both chrome, firefox, opera and edge browsers
	/*protected boolean isInteractable(RemoteWebElement rwe) {
		return (Boolean) runJavaScript(ResourceContent.getVisibilityJavaScript(), rwe) && rwe.isEnabled();
	}*/

	@SuppressWarnings("unchecked")
	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {

		if(waitElementInteractable(foundElement)) {

			Rectangle rect = foundElement.getRectangle();

			int offsetX = getOffsetX(rect, position);
			int offsetY = getOffsetY(rect, position);

			move(foundElement.getValue(), offsetX, offsetY);

			ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript("var rect=arguments[0].getBoundingClientRect();var result=[rect.left+0.00001, rect.top+0.00001];", foundElement.getValue());
			if(newPosition.size() > 1) {
				foundElement.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
			}

		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_INTERACTABLE);
			status.setMessage("Element is not interactable");
		}
	}

	protected void move(WebElement element, int offsetX, int offsetY) {
		actions.moveToElement(element, offsetX, offsetY).perform();
	}

	@Override
	public void mouseClick(boolean hold) {
		if(hold) {
			actions.clickAndHold();
		}else {
			actions.click();
		}
		actions.perform();
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
		channel.sleep(waitBeforeSwitch);
		try {
			driver.switchTo().defaultContent();
		}catch (NoSuchWindowException e) {
			switchWindow(0);
		}
	}

	private void switchToFrame(WebElement we) {
		channel.sleep(waitBeforeSwitch);
		driver.switchTo().frame(we);
	}

	@Override
	public void switchToIframe(String iframe) {
		RemoteWebElement rwe = new RemoteWebElement();
		rwe.setId(iframe);
		rwe.setParent(driver);
		switchToFrame(rwe);
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Window management
	//-----------------------------------------------------------------------------------------------------------------------------------

	protected void switchToWindowHandle(String handle) {
		channel.sleep(waitBeforeSwitch);
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

	private int closeWindowHandler(String windowHandle) {

		switchToWindowHandle(windowHandle);

		if(firstWindow.equals(windowHandle)) {
			try {
				driver.close();
			}catch(WebDriverException e) {}
		}else {
			driver.executeScript("window.close();");
		}

		channel.sleep(200);

		ArrayList<String> list = new ArrayList<String>();
		try {
			list.addAll(driver.getWindowHandles());
		}catch(WebDriverException e) {}

		int windowCount = list.size();

		if(windowCount > 0) {

			if(list.contains(windowHandle)) {

				windowCount--;

				channel.sleep(300);
				try {
					driver.switchTo().alert().accept();
				}catch(WebDriverException e0) {}
				channel.sleep(200);

				list = new ArrayList<String>();
				try {
					list.addAll(driver.getWindowHandles());
				}catch(WebDriverException e) {}

			}

			if(windowCount > 0) {
				try {
					switchToWindowHandle(list.get(0));
				}catch(UnhandledAlertException e) {
					try {
						driver.switchTo().alert().accept();
					}catch(WebDriverException e0) {}
				}
			}
		}

		channel.sleep(100);

		return windowCount;
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {

		ArrayList<String> list = new ArrayList<String>();
		try {
			list.addAll(driver.getWindowHandles());
		}catch(WebDriverException e) {}

		if(index < list.size()) {

			int windowCount = closeWindowHandler(list.get(index));

			status.setOccurences(windowCount);
			if(windowCount == 0) {
				channel.lastWindowClosed(status);
			}

			index--;
		}

		if(index < 0) {
			index = 0;
		}

		currentWindow = index;
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	//
	//-----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object executeScript(ActionStatus status, String javaScript, Object... params) {

		Object result = null;

		try {

			result = runJavaScript("result = " + javaScript, params);

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
		return driver.executeAsyncScript(javaScript + ";var callbackResult=arguments[arguments.length-1];callbackResult(result);", params);
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		switchToDefaultContent();
		if(newWindow) {
			driver.executeScript("window.open('" + url.toString() + "', '_blank', 'height=" + channel.getSubDimension().getHeight() + ",width=" + channel.getSubDimension().getWidth() + "');");
		}else {
			driver.executeScript("var result=window.location.assign('" + url.toString() + "')");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, String[] attributes,
			Predicate<Map<String, Object>> predicate) {

		ArrayList<FoundElement> webElementList = new ArrayList<FoundElement>();
		WebElement startElement = null;

		if(testObject.getParent() != null){
			if(testObject.getParent().isIframe()) {
				try {
					switchToFrame(testObject.getParent().getWebElement());
				}catch(StaleElementReferenceException e) {
					switchToCurrentWindow();

					testObject.getParent().searchAgain();
					return webElementList;
				}
			}else {
				startElement = testObject.getParent().getWebElement();
			}
		}else {
			switchToCurrentWindow();
			waitReadyState();
		}
		
		ArrayList<Map<String, Object>> response = (ArrayList<Map<String, Object>>)runJavaScript(ResourceContent.getSearchElementsJavaScript(), startElement, tagName, attributes);
		if(response != null){
			response.parallelStream().filter(predicate).forEachOrdered(e -> addWebElement(webElementList, (Map<String, Object>) e.get("ats-elt")));
		}

		return webElementList;
	}

	private void addWebElement(ArrayList<FoundElement> webElementList, Map<String, Object> elements){
		if(elements != null){
			webElementList.add(new FoundElement(elements, channel, initElementX, initElementY));
		}
	}

	@Override
	public void middleClick(ActionStatus status, TestElement element) {
		runJavaScript("var evt=new MouseEvent('click', {bubbles: true,cancelable: true,view: window, button: 1}),result={};arguments[0].dispatchEvent(evt);", element.getWebElement());
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
		return driver.getCurrentUrl();
	}

	protected void clearText(ActionStatus status, WebElement element) {
		executeScript(status, "arguments[0].value=''", element);
	}

	@Override
	public void sendTextData(ActionStatus status, FoundElement element, ArrayList<SendKeyData> textActionList, boolean clear) {
		if(clear) {
			clearText(status, element.getValue());
		}
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