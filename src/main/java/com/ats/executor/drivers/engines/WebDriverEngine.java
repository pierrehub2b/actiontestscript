package com.ats.executor.drivers.engines;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
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
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.tools.ResourceContent;
import com.ats.tools.StartHtmlPage;
import com.ats.tools.logger.MessageCode;

public class WebDriverEngine extends DriverEngineAbstract implements IDriverEngine {

   private static final String resultAsync = ";var callbackResult=arguments[arguments.length-1];callbackResult(result);";

	protected WindowsDesktopDriver windowsDriver;

	protected Double initElementX = 0.0;
	protected Double initElementY = 0.0;

	private Proxy proxy;

	private int scriptTimeout;
	private int loadPageTimeOut;
	private int maxTryInteractable;

	private DriverProcess driverProcess;

	private String firstWindow;

	public WebDriverEngine(
			Channel channel, 
			String browser, 
			DriverProcess driverProcess, 
			WindowsDesktopDriver windowsDriver,
			AtsManager ats) {

		super(channel, browser);

		this.driverProcess = driverProcess;
		this.windowsDriver = windowsDriver;
		this.proxy = ats.getProxy();
		this.loadPageTimeOut = ats.getPageloadTimeOut();
		this.scriptTimeout = ats.getScriptTimeOut();
		this.maxTryInteractable = ats.getMaxTryInteractable();
	}

	@Override
	public boolean isDesktop() {
		return false;
	}

	protected DriverProcess getDriverProcess() {
		return driverProcess;
	}

	protected void launchDriver(MutableCapabilities cap, boolean isEdge) {

		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.PROXY, proxy);
		cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
		cap.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);

		driver = new RemoteWebDriver(driverProcess.getDriverServerUrl(), cap);

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
			
        try {
               File tempHtml = File.createTempFile("ats_", ".html");
               tempHtml.deleteOnExit();
               
               Files.write(tempHtml.toPath(), StartHtmlPage.getAtsBrowserContent(applicationVersion, driverVersion, channel.getDimension()));
               driver.get(tempHtml.toURI().toString());
        } catch (IOException e) {}

        ArrayList<String> windows = new ArrayList<String>();
        channel.setApplicationData(
        		applicationVersion,
        		driverVersion,
        		windowsDriver.getProcessDataByWindowTitle(StartHtmlPage.getAtsBrowserTitle(), windows),
        		windows);
				
		firstWindow = driver.getWindowHandle();
	}

	protected void closeDriver() {
		driverProcess.close();
	}

	protected Channel getChannel() {
		return channel;
	}

	@Override
	public void waitAfterAction() {
		int maxWait = 50;
		while(maxWait > 0 && !((ArrayList<Boolean>) runJavaScript(ResourceContent.getReadyStatesJavaScript())).stream().parallel().allMatch(e -> true)){
			channel.sleep(200);
			maxWait--;
		}
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

	@Override
	public void forceScrollElement(FoundElement element) {
		String code = "var e=arguments[0];e.scrollIntoView();var r=e.getBoundingClientRect();var result=[r.left+0.00001, r.top+0.00001]";
		ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript(code, element.getValue());

		if(newPosition.size() > 1) {
			element.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
		}
	}

	private void addWebElement(ArrayList<FoundElement> webElementList, Map<String, Object> elements){
		if(elements != null){
			webElementList.add(new FoundElement(elements, channel, initElementX, initElementY));
		}
	}

	public FoundElement getElementFromPoint(Double x, Double y){

		if(x < channel.getSubDimension().getX() || y < channel.getSubDimension().getY()) {

			return windowsDriver.getElementFromPoint(x, y);
			
		}else {
			
			switchToDefaultframe();

			x -= channel.getSubDimension().getX();
			y -= channel.getSubDimension().getY();

			return loadElement(x, y, initElementX, initElementY);
		}
	}

	private FoundElement loadElement(Double x, Double y, Double offsetX, Double offsetY) {
		return loadElement(x, y, offsetX, offsetY, new ArrayList<String>());
	}

	private FoundElement loadElement(Double x, Double y, Double offsetX, Double offsetY, ArrayList<String> iframes) {

		@SuppressWarnings("unchecked")
		Map<String, Object> objectData = (Map<String, Object>) runJavaScript(ResourceContent.getHoverElementJavaScript(), x - offsetX, y - offsetY);
		if(objectData != null){

			if(FoundElement.IFRAME.equals(objectData.get("tag"))){

				FoundElement frm = new FoundElement(objectData);

				iframes.add(frm.getId());

				switchToFrame(frm.getValue());

				offsetX += (Double)objectData.get("x");
				offsetY += (Double)objectData.get("y");

				return loadElement(x, y, offsetX, offsetY, iframes);

			} else {
				return new FoundElement(objectData, channel, offsetX, offsetY, iframes);
			}

		} else {
			return null;
		}
	}

	public void loadParents(FoundElement hoverElement){
		if(hoverElement.isDesktop()){
			hoverElement.setParent(windowsDriver.getTestElementParent(hoverElement.getValue(), channel));
		}else{
			hoverElement.setParent(getTestElementParent(hoverElement));
		}
	}

	@Override
	public WebElement getRootElement() {
		return driver.findElementByXPath("//body");
	}

	private RemoteWebElement getWebElement(FoundElement te) {

		switchToDefaultframe();

		ArrayList<String>iframesData = te.getIframes();

		if(iframesData != null) {
			for (String rweId : iframesData) {
				RemoteWebElement rwe = new RemoteWebElement();
				rwe.setId(rweId);
				rwe.setParent(driver);
				switchToFrame(rwe);
			}
		}

		return te.getRemoteWebElement(driver);
	}

	public CalculatedProperty[] getAttributes(FoundElement te){
		if(te.isDesktop()){
			return getWindowsAttributes(te.getRemoteWebElement(windowsDriver));
		}else {
			return getAttributes(getWebElement(te));
		}
	}

	public CalculatedProperty[] getCssAttributes(FoundElement te){
		return getCssAttributes(getWebElement(te));
	}

	public CalculatedProperty[] getCssAttributes(RemoteWebElement element){
		return getAttributesList(element, ResourceContent.getElementCssJavaScript());
	}

	public CalculatedProperty[] getAttributes(RemoteWebElement element){
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
			return new FoundElement(listElements, channel, initElementX, initElementY, element.getIframes());
		}

		return null;
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public TestBound[] getDimensions() {

		switchToDefaultframe();

		Map<String, ArrayList<Double>> response = (Map<String, ArrayList<Double>>) runJavaScript(ResourceContent.getDocumentSizeJavaScript());
		int maxTry = 10;

		while (response == null && maxTry > 0) {
			response = (Map<String, ArrayList<Double>>) runJavaScript(ResourceContent.getDocumentSizeJavaScript());
			maxTry--;
		}

		TestBound testDimension = null;
		TestBound testSubDimension = null;

		if(response != null) {
			ArrayList<Double> dimension = response.get("main");
			ArrayList<Double> subDimension = response.get("sub");

			testDimension = new TestBound(
					dimension.get(0),
					dimension.get(1),
					dimension.get(2),
					dimension.get(3));

			testSubDimension = new TestBound(
					subDimension.get(0),
					subDimension.get(1),
					subDimension.get(2),
					subDimension.get(3));


		}else {
			testDimension = new TestBound(0.0, 0.0, 500.0, 500.0);
			testSubDimension = new TestBound(0.0, 0.0, 500.0, 500.0);
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

		int maxTry = maxTryInteractable;
		boolean interactable = isInteractable(rwe);

		while(!interactable && maxTry > 0) {
			interactable = isInteractable(rwe);

			channel.sendLog(MessageCode.OBJECT_INTERACTABLE, "wait element interactable", maxTry);
			channel.sleep(200);
			maxTry--;
		}

		return interactable;
	}

	private boolean isInteractable(RemoteWebElement rwe) {
		if((Boolean) runJavaScript(ResourceContent.getVisibilityJavaScript(), rwe)) {
			if(rwe.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {
		if(waitElementInteractable(foundElement)) {

			Rectangle rect = foundElement.getRectangle();

			int offsetX = getOffsetX(rect, position);
			int offsetY = getOffsetY(rect, position);

			move(foundElement.getValue(), offsetX, offsetY);

			ArrayList<Double> newPosition =  (ArrayList<Double>) runJavaScript("var rect=arguments[0].getBoundingClientRect();var result=[rect.left+0.00001, rect.top+0.00001]", foundElement.getValue());

			if(newPosition.size() > 1) {
				foundElement.updatePosition(newPosition.get(0), newPosition.get(1), channel, 0.0, 0.0);
			}

		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
			status.setMessage("element not visible");
		}
	}

	protected void move(WebElement element, int offsetX, int offsetY) {
		Actions act = new Actions(driver);
		act.moveToElement(element, offsetX, offsetY).perform();
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Window management
	//-----------------------------------------------------------------------------------------------------------------------------------

	protected void switchToLastWindow() {
		int windowsNum = driver.getWindowHandles().size();
		switchWindow(windowsNum - 1);
	}	

	@Override
	public void switchWindow(int index) {
		ArrayList<String> list = new ArrayList<String>(driver.getWindowHandles());
		if(index < list.size()) {
			channel.sleep(300);
			driver.switchTo().window(list.get(index));
			driver.switchTo().defaultContent();
		}
	}

	@Override
	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {

		if(width != null || height != null){
			int newWidth = channel.getDimension().getWidth().intValue();
			if(width != null) {
				newWidth = width.getValue();
			}

			int newHeight = channel.getDimension().getHeight().intValue();
			if(height != null) {
				newHeight = height.getValue();
			}

			driver.manage().window().setSize(new Dimension(newWidth, newHeight));
		}

		if(x != null || y != null){
			int newX = channel.getDimension().getX().intValue();
			if(x != null) {
				newX = x.getValue();
			}

			int newY = channel.getDimension().getY().intValue();
			if(y != null) {
				newY = y.getValue();
			}

			driver.manage().window().setPosition(new Point(newX, newY));
		}
	}

	private int closeWindowHandler(String windowHandle) {

		driver.switchTo().window(windowHandle);
		driver.switchTo().defaultContent();

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
					driver.switchTo().window(list.get(0));
					driver.switchTo().defaultContent();
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
		}
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
		Object result = driver.executeAsyncScript(javaScript + resultAsync, params);
		return result;
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		driver.switchTo().defaultContent();
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
					switchToDefaultframe();
					testObject.getParent().searchAgain();
					return webElementList;
				}
			}else {
				startElement = testObject.getParent().getWebElement();
			}
		}else {
			switchToDefaultframe();
		}

		ArrayList<Map<String, Object>> response = (ArrayList<Map<String, Object>>)runJavaScript(ResourceContent.getSearchElementsJavaScript(), startElement, tagName, attributes);

		if(response != null){
			response.parallelStream().filter(predicate).forEachOrdered(e -> addWebElement(webElementList, (Map<String, Object>) e.get("atsElem")));
		}

		return webElementList;
	}

	@Override
	public void switchToDefaultframe() {
		try {
			driver.switchTo().defaultContent();
		}catch (NoSuchWindowException e) {
			switchWindow(0);
			driver.switchTo().defaultContent();
		}
	}

	private void switchToFrame(WebElement we) {
		driver.switchTo().frame(we);
	}

	@Override
	public void middleClick(WebElement element) {
		runJavaScript("var e=arguments[0];var evt=new MouseEvent(\"click\", {bubbles: true,cancelable: true,view: window, button: 1});e.dispatchEvent(evt);var result={}", element);
	}
}