package com.ats.executor.drivers.engines;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.safari.SafariOptions;

import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.scripting.ResourceContent;
import com.ats.generator.variables.CalculatedProperty;

public class WebBrowserDriverSearchEngine extends DriverSearchEngineAbstract implements DriverSearchEngineImpl {

	private static final String resultAsync = ";var callbackResult=arguments[arguments.length-1];callbackResult(result);";

	protected WindowsDesktopDriver windowsDriver;

	private String searchElementsJavaScript;
	private String documentSizeJavaScript;
	private String getHoverElementJavaScript;
	private String getElementAttributesJavaScript;
	private String getParentElementJavaScript;
	private String getElementCssJavaScript;
	private String checkElementIsVisibleJavaScript;
	private String scrollElementIfNeeded;

	private Double initElementX = 0.0;
	private Double initElementY = 0.0;

	private int waitAfterAction = 50;

	public WebBrowserDriverSearchEngine(
			Channel channel, 
			String browser, 
			URL driverServerUrl, 
			WindowsDesktopDriver windowsDriver,
			boolean proxySystem) {

		super(channel, browser);

		this.windowsDriver = windowsDriver;

		scrollElementIfNeeded = ResourceContent.getScript("scrollElementIfNeeded");
		searchElementsJavaScript = ResourceContent.getScript("searchElements");
		documentSizeJavaScript = ResourceContent.getScript("documentSize");
		getHoverElementJavaScript = ResourceContent.getScript("getHoverElement");
		getElementAttributesJavaScript = ResourceContent.getScript("getElementAttributes");
		getParentElementJavaScript = ResourceContent.getScript("getParentElement");
		getElementCssJavaScript = ResourceContent.getScript("getElementCss");
		checkElementIsVisibleJavaScript = ResourceContent.getScript("checkElementIsVisible");

		Proxy proxy = new Proxy();
		if(proxySystem){
			proxy.setProxyType(ProxyType.SYSTEM);
		}else{
			proxy.setProxyType(ProxyType.DIRECT);
		}

		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		cap.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
		cap.setCapability(CapabilityType.PROXY, proxy);
		cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);

		if (isEdge()){

			waitAfterAction = 50;

			EdgeOptions options = new EdgeOptions();
			options.setPageLoadStrategy("eager");

			cap.setCapability(EdgeOptions.CAPABILITY, options);

		}else if (isChrome()){

			waitAfterAction = 50;

			List<String> args = new ArrayList<String>();

			//args.add("--disable-extensions");
			//args.add("--no-sandbox");
			args.add("--disable-infobars");
			args.add("--no-default-browser-check");
			//args.add("--bwsi");
			//args.add("--test-type");
			//args.add("--disable-bundled-ppapi-flash");
			//args.add("--disable-plugins-discovery");
			//args.add("--browser-test");

			//args.add("--headless");
			args.add("--window-position=" + channel.getDimension().getX() + "," + channel.getDimension().getY());
			args.add("--window-size=" + channel.getDimension().getWidth() + "," + channel.getDimension().getHeight());

			ChromeOptions options = new ChromeOptions();
			options.addArguments(args);

			//HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
			//options.setExperimentalOption("prefs", chromePrefs);
			//chromePrefs.put("plugins.plugins_disabled", new String[] {"Adobe Flash Player", "Chrome PDF Viewer", "Widevine Content Decryption Module", "Native Client"});
			//chromePrefs.put("profile.default_content_settings.popups", 0);
			//chromePrefs.put("credentials_enable_service", false);
			//chromePrefs.put("profile.password_manager_enabled", false);

			cap.setCapability(ChromeOptions.CAPABILITY, options);

		}else if (isSafari()){

			SafariOptions options = new SafariOptions();
			cap.setCapability(SafariOptions.CAPABILITY, options);

		}else if (isFirefox()){


			cap = DesiredCapabilities.firefox();
			cap.setCapability(FirefoxDriver.MARIONETTE, true);

			this.initElementY = 5.0;

		}else if (isOpera()){

			//OperaOptions options = new OperaOptions();
			//cap.setCapability(OperaOptions.CAPABILITY, options);
			//cap.setCapability("opera.guess_binary_path", true);

			cap = DesiredCapabilities.opera();
			cap.setCapability("opera.guess_binary_path", true);
			cap.setCapability("opera.log.level", "CONFIG");
		}

		this.driver = new RemoteWebDriver(driverServerUrl, cap);
		this.driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
		this.driver.manage().timeouts().pageLoadTimeout(2, TimeUnit.MINUTES);

		try{
			this.driver.manage().window().setSize(channel.getDimension().getSize());
			this.driver.manage().window().setPosition(channel.getDimension().getPoint());
		}catch(Exception ex){
			System.err.println(ex.getMessage());
		}

		Map<String, ?> infos = (this.driver).getCapabilities().asMap();

		for (Map.Entry<String, ?> entry : infos.entrySet())
		{
			if("browserVersion".equals(entry.getKey()) || "version".equals(entry.getKey())){
				channel.setApplicationVersion(entry.getValue().toString());
			}
		}

		if(isEdge()){
			channel.setProcessData(windowsDriver.getProcessDataByWindowTitle("Microsoft Edge"));
		}else{
			String uuidHandle = UUID.randomUUID().toString();
			Object response = runJavaScript("top.document.title=arguments[0];result=document.title", uuidHandle);
			channel.setProcessData(windowsDriver.getProcessDataByWindowTitle(response.toString()));
		}

		this.driver.get("about:blank");
	}

	private boolean isChrome(){
		return Channel.CHROME_BROWSER.equals(application);
	}

	private boolean isOpera(){
		return Channel.OPERA_BROWSER.equals(application);
	}

	private boolean isFirefox(){
		return Channel.FIREFOX_BROWSER.equals(application);
	}

	private boolean isEdge(){
		return Channel.EDGE_BROWSER.equals(application);
	}

	private boolean isSafari(){
		return Channel.SAFARI_BROWSER.equals(application);
	}

	@Override
	public int getWaitAfterAction() {
		return waitAfterAction;
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
				code = scrollElementIfNeeded;
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

		if(element != null){
			if(element.getWidth() >= channel.getSubDimension().getWidth() + 1 || element.getHeight() >= channel.getSubDimension().getHeight() + 1){
				element = null;
			}
		}

		if(element == null){

			switchToDefaultframe();
			//driver.switchTo().defaultContent();

			x -= channel.getSubDimension().getX();
			y -= channel.getSubDimension().getY();

			element = loadElement(x, y, initElementX, initElementY);
		}

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

				driver.switchTo().frame(frm.getValue());

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
		//driver.switchTo().defaultContent();

		ArrayList<String>iframesData = te.getIframes();

		if(iframesData != null) {
			for (String rweId : iframesData) {
				RemoteWebElement rwe = new RemoteWebElement();
				rwe.setId(rweId);
				rwe.setParent(driver);
				driver.switchTo().frame(rwe);
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

		if(isFirefox()){
			testSubDimension.setY(testSubDimension.getY() + 2);
		}

		return new TestBound[]{testDimension, testSubDimension};
	}

	@Override
	public void close() {
		if(driver != null){

			Iterator<String> listWindows = driver.getWindowHandles().iterator();

			while(listWindows.hasNext()) {
				driver.switchTo().window(listWindows.next());
				driver.close();
			}

			try {
				driver.quit();
			}catch (Exception ex) {
			}

			/*int tryClose = 10;
			while(tryClose > 0 && driver != null){
				try{
					for (String winHandle : driver.getWindowHandles()) {
						driver.switchTo().window(winHandle);
						driver.close();
					}
					tryClose = 0;
				}catch(WebDriverException ex){
					tryClose--;
				}
			}*/

			//	if(!isFirefox()){
			//		driver.quit();
			//	}
			//	driver = null;
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	// Window management
	//-----------------------------------------------------------------------------------------------------------------------------------

	private void switchToLastWindow() {
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
			status.setPassed(false);
			status.setCode(ActionStatus.JAVASCRIPT_ERROR);
			status.setMessage(e.getMessage().replace("javascript error:", ""));
		}

		return result;
	}

	private Object runJavaScript(String javaScript, Object ... params) {
		Object result = driver.executeAsyncScript(javaScript + resultAsync, params);
		return result;
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		//driver.get(url);
		//driver.navigate().to(url);

		if(newWindow) {
			driver.executeScript("window.open('" + url.toString() + "', '_blank', 'height=" + channel.getSubDimension().getHeight() + ",width=" + channel.getSubDimension().getWidth() + "');");

			if(isEdge()) {

				channel.sleep(100);

				ArrayList<CalculatedProperty> attributes = new ArrayList<CalculatedProperty>(1);
				attributes.add(new CalculatedProperty("ClassName", "LandmarkTarget"));
				ArrayList<FoundElement> listElements = channel.findWindowsElement(null, "Group", attributes);

				if(listElements.size() > 0) {
					FoundElement parent = listElements.get(0);
					if(parent.isVisible()) {

						attributes = new ArrayList<CalculatedProperty>(1);
						attributes.add(new CalculatedProperty("ClassName", "NotificationBar"));
						listElements = channel.findWindowsElement(parent.getValue(), "ToolBar", attributes);

						if(listElements.size() > 0) {
							parent = listElements.get(0);
							if(parent.isVisible()) {

								attributes = new ArrayList<CalculatedProperty>(1);
								attributes.add(new CalculatedProperty("ClassName", "Button"));

								listElements = channel.findWindowsElement(parent.getValue(), "Button", attributes);

								if(listElements.size() > 1) {
									FoundElement button = listElements.get(1);
									if(button.isVisible()) {

										TestBound bound = button.getTestBound();
										Actions action = new Actions(windowsDriver);
										action.moveToElement(button.getValue(), bound.getWidth().intValue()/2, 40).perform();
										action.click().perform();
									}
								}
							}
						}
					}
				}
				switchToLastWindow();
			}

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
					driver.switchTo().frame(testObject.getParent().getWebElement());
				}catch(StaleElementReferenceException e) {
					//driver.switchTo().defaultContent();
					switchToDefaultframe();
					return webElementList;
				}
			}else {
				startElement = testObject.getParent().getWebElement();
			}
		}

		//try{
		ArrayList<Map<String, Object>> response = (ArrayList<Map<String, Object>>)runJavaScript(searchElementsJavaScript, startElement, tagName, attributes);
		if(response != null){
			response.parallelStream().filter(predicate).forEachOrdered(e -> addWebElement(webElementList, (Map<String, Object>) e.get("XelemX")));
		}
		//}catch (WebDriverException ex){
		//	System.err.println(ex.getMessage());
		//}

		return webElementList;
	}

	@Override
	public void switchToDefaultframe() {
		driver.switchTo().defaultContent();
	}

	@Override
	public void middleClick(WebElement element) {
		runJavaScript("var e=arguments[0];var evt=new MouseEvent(\"click\", {bubbles: true,cancelable: true,view: window, button: 1});e.dispatchEvent(evt);var result={}", element);
	}
}