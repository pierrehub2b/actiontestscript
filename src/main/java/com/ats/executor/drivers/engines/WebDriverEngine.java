package com.ats.executor.drivers.engines;

import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
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
import com.ats.executor.scripting.ResourceContent;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;

public class WebDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	private static final String resultAsync = ";var callbackResult=arguments[arguments.length-1];callbackResult(result);";

	protected WindowsDesktopDriver windowsDriver;

	private String searchElementsJavaScript;
	private String documentSizeJavaScript;
	private String getHoverElementJavaScript;
	private String getElementAttributesJavaScript;
	private String getParentElementJavaScript;
	private String getElementCssJavaScript;
	private String checkElementIsVisibleJavaScript;
	private String scrollElementJavaScript;

	protected Double initElementX = 0.0;
	protected Double initElementY = 0.0;

	private Proxy proxy;
	
	private int scriptTimeout;
	private int loadPageTimeOut;
	
	private DriverProcess driverProcess;

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

		scrollElementJavaScript = ResourceContent.getScript("scrollElement");
		searchElementsJavaScript = ResourceContent.getScript("searchElements");
		documentSizeJavaScript = ResourceContent.getScript("documentSize");
		getHoverElementJavaScript = ResourceContent.getScript("getHoverElement");
		getElementAttributesJavaScript = ResourceContent.getScript("getElementAttributes");
		getParentElementJavaScript = ResourceContent.getScript("getParentElement");
		getElementCssJavaScript = ResourceContent.getScript("getElementCss");
		checkElementIsVisibleJavaScript = ResourceContent.getScript("checkElementIsVisible");
	}
	
	@Override
	public boolean isDesktop() {
		return false;
	}

	protected void launchDriver(DesiredCapabilities cap, boolean isEdge) {

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

		Map<String, ?> infos = (this.driver).getCapabilities().asMap();

		for (Map.Entry<String, ?> entry : infos.entrySet()){
			if("browserVersion".equals(entry.getKey()) || "version".equals(entry.getKey())){
				channel.setApplicationVersion(entry.getValue().toString());
			}
		}

		if(isEdge){
			channel.setProcessData(windowsDriver.getProcessDataByWindowTitle("Microsoft Edge"));
		}else{
			String uuidHandle = UUID.randomUUID().toString();
			Object response = runJavaScript("top.document.title=arguments[0];result=document.title", uuidHandle);
			channel.setProcessData(windowsDriver.getProcessDataByWindowTitle(response.toString()));
		}

		driver.get("about:blank");
	}
	
	protected void closeDriver() {
		driverProcess.close();
	}

	@Override
	public void waitAfterAction() {
		int maxWait = 120;
		while(maxWait > 0 && !((Boolean) driver.executeScript("return document.readyState=='complete';"))){
			channel.sleep(500);
			maxWait--;
		}
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public void scroll(FoundElement element, int delta) {
		if(element != null) {
			String code = null;
			if(delta == 0) {
				code = scrollElementJavaScript;
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

	public boolean waitElementIsVisible(WebElement element) {

		int tryLoop = 20;
		while(tryLoop > 0 && !(Boolean) runJavaScript(checkElementIsVisibleJavaScript, element)){
			tryLoop--;
			channel.sleep(200);
		}

		if(tryLoop > 0) {
			tryLoop = 20;
			while(tryLoop > 0 && !element.isDisplayed() && !element.isEnabled()){
				tryLoop--;
				channel.sleep(200);
			}
			return tryLoop > 0;
		}

		return false;
	}

	private void addWebElement(ArrayList<FoundElement> webElementList, Map<String, Object> elements){
		if(elements != null){
			webElementList.add(new FoundElement(elements, channel, initElementX, initElementY));
		}
	}

	public FoundElement getElementFromPoint(Double x, Double y){

		FoundElement element = null;//windowsDriver.getElementFromPoint(x, y);

		/*if(element != null){
			if(element.getWidth() >= channel.getSubDimension().getWidth() + 1 || element.getHeight() >= channel.getSubDimension().getHeight() + 1){
				element = null;
			}
		}*/

		//if(element == null){

		switchToDefaultframe();
		//driver.switchTo().defaultContent();

		x -= channel.getSubDimension().getX();
		y -= channel.getSubDimension().getY();

		element = loadElement(x, y, initElementX, initElementY);
		//}

		return element;
	}

	private FoundElement loadElement(Double x, Double y, Double offsetX, Double offsetY) {
		return loadElement(x, y, offsetX, offsetY, new ArrayList<String>());
	}

	private FoundElement loadElement(Double x, Double y, Double offsetX, Double offsetY, ArrayList<String> iframes) {

		@SuppressWarnings("unchecked")
		Map<String, Object> objectData = (Map<String, Object>) runJavaScript(getHoverElementJavaScript, x - offsetX, y - offsetY);
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
		return driver.findElementByXPath("/*");
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
		return getAttributesList(element, getElementCssJavaScript);
	}

	public CalculatedProperty[] getAttributes(RemoteWebElement element){
		return getAttributesList(element, getElementAttributesJavaScript);
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
		ArrayList<Map<String, Object>> listElements = (ArrayList<Map<String, Object>>) runJavaScript(getParentElementJavaScript, element.getValue());

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

		Map<String, ArrayList<Double>> response = (Map<String, ArrayList<Double>>) runJavaScript(documentSizeJavaScript);

		ArrayList<Double> dimension = response.get("main");
		ArrayList<Double> subDimension = response.get("sub");

		TestBound testDimension = new TestBound(
				dimension.get(0),
				dimension.get(1),
				dimension.get(2),
				dimension.get(3));

		TestBound testSubDimension = new TestBound(
				subDimension.get(0),
				subDimension.get(1),
				subDimension.get(2),
				subDimension.get(3));

		return new TestBound[]{testDimension, testSubDimension};
	}

	@Override
	public void close() {
		if(driver != null){

			try {
				driver.quit();
			}catch (Exception ex) {}

		}
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	// Mouse position by browser name
	//-----------------------------------------------------------------------------------------------------------------------------------

	private int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2) {
		if(direction != null) {
			return getDirectionValue(value, direction, cart1, cart2);
		}else {
			return getNoDirectionValue(value);
		}
	}
	
	protected int getOffsetX(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.width, position.getHorizontalPos(), Cartesian.LEFT, Cartesian.RIGHT);
	}
	
	protected int getOffsetY(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.height, position.getVerticalPos(), Cartesian.TOP, Cartesian.BOTTOM);
	}
			
	protected int getDirectionValue(int value, MouseDirectionData direction,Cartesian cart1, Cartesian cart2) {
		if(cart1.equals(direction.getName())) {
			return direction.getValue();
		}else if(cart2.equals(direction.getName())) {
			return value - direction.getValue();
		}
		return 0;
	}
	
	protected int getNoDirectionValue(int value) {
		return value / 2;
	}
		
	public void mouseMoveToElement(WebElement element, Rectangle elemRect, MouseDirection position) {
		
		int offsetX = getOffsetX(elemRect, position);
		int offsetY = getOffsetY(elemRect, position);

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
	public int switchWindow(int index) {
		int windowsNum = driver.getWindowHandles().size();
		if(windowsNum > 1 && windowsNum >= index) {
			Object[] wins = driver.getWindowHandles().toArray();
			driver.switchTo().window(wins[index].toString());
		}
		return windowsNum;
	}

	@Override
	public void resizeWindow(int width, int height) {
		Dimension dim = new Dimension(width, height);
		driver.manage().window().setSize(dim);
	}

	@Override
	public int closeWindow(int index) {
		int windowsNum = driver.getWindowHandles().size();
		if(windowsNum > 1 && index > 0 && windowsNum >= index) {
			Object[] wins = driver.getWindowHandles().toArray();
			driver.switchTo().window(wins[index].toString());
			driver.close();

			driver.switchTo().window(wins[0].toString());
		}
		return windowsNum;
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
					return webElementList;
				}
			}else {
				startElement = testObject.getParent().getWebElement();
			}
		}

		ArrayList<Map<String, Object>> response = (ArrayList<Map<String, Object>>)runJavaScript(searchElementsJavaScript, startElement, tagName, attributes);

		if(response != null){
			response.parallelStream().filter(predicate).forEachOrdered(e -> addWebElement(webElementList, (Map<String, Object>) e.get("atsElem")));
		}

		return webElementList;
	}

	@Override
	public void switchToDefaultframe() {
		driver.switchTo().defaultContent();
	}

	private void switchToFrame(WebElement we) {
		driver.switchTo().frame(we);
	}

	@Override
	public void middleClick(WebElement element) {
		runJavaScript("var e=arguments[0];var evt=new MouseEvent(\"click\", {bubbles: true,cancelable: true,view: window, button: 1});e.dispatchEvent(evt);var result={}", element);
	}
}