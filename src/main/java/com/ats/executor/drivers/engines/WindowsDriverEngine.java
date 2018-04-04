package com.ats.executor.drivers.engines;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.ApplicationProperties;
import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.platform.win32.VersionUtil;

public class WindowsDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	public static final String windows = "windows";
	public static final String desktop = "desktop";

	private Process applicationProcess = null;

	public WindowsDriverEngine(Channel channel, String application, WindowsDesktopDriver windowsDriver, AtsManager ats) {

		super(channel, application);

		driver = windowsDriver;

		int firstSpace = application.indexOf(" ");
		String applicationArguments = "";

		if(firstSpace > 0){
			applicationArguments = application.substring(firstSpace);
			application = application.substring(0, firstSpace);
		}

		URI fileUri = null;
		File exeFile = null;

		ApplicationProperties properties = ats.getApplicationProperties(application);
		if(properties != null) {
			exeFile = new File(properties.getPath());
			if(exeFile.exists()) {
				fileUri = exeFile.toURI();
			}
		}

		if(fileUri == null) {
			try {
				fileUri = new URI(application);
				exeFile = new File(fileUri);
			} catch (URISyntaxException e) {}
		}

		if(exeFile == null) {//last chance
			exeFile = new File(application);
		}

		if(exeFile != null && exeFile.exists() && exeFile.isFile()){

			applicationPath = exeFile.getAbsolutePath();

			Runtime runtime = Runtime.getRuntime();
			try{
				applicationProcess = runtime.exec(exeFile.getAbsolutePath() + applicationArguments);
			} catch (IOException e) {
				e.printStackTrace();
			}

			List<WebElement> childs = new ArrayList<WebElement>();
			int maxTry = 30;

			while(childs.isEmpty() && maxTry > 0){
				childs = ((WindowsDesktopDriver)driver).getChildrenByPid(applicationProcess.pid());
				maxTry--;
				channel.sleep(200);
			}

			ArrayList<String> windows = new ArrayList<String>();
			for(WebElement elem : childs){
				windows.add(windowsDriver.getWindowHandle(elem.getAttribute("NativeWindowHandle")));
			}

			String version = "N/A";
			try {
				VS_FIXEDFILEINFO info = VersionUtil.getFileVersionInfo(exeFile.getAbsolutePath());
				version = info.getFileVersionMajor() + "." + info.getFileVersionMinor() + "." + info.getFileVersionRevision() + "." + info.getFileVersionBuild();
			}catch(Exception e) {}

			String driverVersion = null;
			try {
				Map<String, String> infos = (Map<String, String>) driver.getCapabilities().asMap();
				driverVersion = infos.get("driverVersion");
			}catch(Exception e) {}

			channel.setApplicationData(version, driverVersion, applicationProcess.pid(), windows);
		}
	}

	@Override
	public boolean isDesktop() {
		return true;
	}

	@Override
	public void waitAfterAction() {

	}

	public void loadParents(FoundElement hoverElement){
		//hoverElement.loadParents((WindowsDesktopDriver)driver, channel);
		hoverElement.setParent(((WindowsDesktopDriver)driver).getTestElementParent(hoverElement.getValue(), channel));
	}

	public CalculatedProperty[] getAttributes(FoundElement te){
		RemoteWebElement element = new RemoteWebElement();
		element.setParent((RemoteWebDriver) driver);
		element.setId(te.getId());

		return getAttributes(true, element);
	}

	public CalculatedProperty[] getAttributes(boolean windowsDomain, RemoteWebElement element){
		return getWindowsAttributes(element);
	}

	public CalculatedProperty[] getAttributes(RemoteWebElement element){
		return getWindowsAttributes(element);
	}

	public CalculatedProperty[] getCssAttributes(FoundElement te){
		return getAttributes(te);
	}

	public CalculatedProperty[] getCssAttributes(RemoteWebElement element){
		return getWindowsAttributes(element);
	}

	//---------------------------------------------------------------------------------------------------------------------
	// 
	//---------------------------------------------------------------------------------------------------------------------

	public FoundElement getElementFromPoint(Double x, Double y){
		return ((WindowsDesktopDriver)driver).getElementFromPoint(x, y);
	}

	@Override
	public TestBound[] getDimensions() {

		List<WebElement> childs = ((WindowsDesktopDriver)driver).getChildrenByPid(channel.getProcessId());
		if(childs != null && childs.size() > 0){
			String data = childs.get(0).getAttribute("InfoData");
			if(data != null){
				String[] infoData = data.split(":");
				String[] boundData = infoData[1].split(",");

				return new TestBound[]{new TestBound(
						Double.parseDouble(boundData[0]),
						Double.parseDouble(boundData[1]),
						Double.parseDouble(boundData[2]),
						Double.parseDouble(boundData[3])), 
						channel.getSubDimension()};
			}
		}
		return new TestBound[]{channel.getDimension(), channel.getSubDimension()};
	}

	@Override
	public void close() {
		driver.close();
		driver.quit();
		applicationProcess.destroyForcibly();
	}

	@Override
	public void switchWindow(int index) {

	}

	@Override
	public void closeWindow(ActionStatus status, int index) {

	}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		status.setPassed(true);
		return null;
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		// Do nothing
	}

	@Override
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, String[] attributes,
			Predicate<Map<String, Object>> searchPredicate) {

		ArrayList<FoundElement> result = new ArrayList<FoundElement>();

		return result;
	}

	//@Override
	//public byte[] getScreenShot(TestDimension dimension){
	//	return ((WindowsDesktopDriver)driver).getScreenshotByte(dimension.getX(), dimension.getY(), dimension.getWidth(), dimension.getHeight());

	//}

	//@Override
	//public void toFront(){
	//	((WindowsDesktopDriver)driver).setWindowToFront(channel.getProcessId());
	//}

	@Override
	public void switchToDefaultframe() {
		//do nothing
	}

	@Override
	public void scroll(FoundElement element, int delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void middleClick(WebElement element) {
		// TODO Auto-generated method stub

	}

	@Override
	public WebElement getRootElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {

		/*Rectangle rect = foundElement.getRectangle();

		//		int offsetX = (int) (foundElement.getScreenX() + getOffsetX(rect, position) + channel.getDimension().getX());
		//		int offsetY = (int) (foundElement.getScreenY() + getOffsetY(rect, position) + channel.getDimension().getY());;

		Actions act = new Actions(driver);
		act.moveToElement(foundElement.getValue(), 12, 44).perform();
		//act.moveToElement(foundElement.getValue(), offsetX, offsetY).perform();*/

		Actions act = new Actions(driver);
		act.moveToElement(foundElement.getValue()).perform();
	}

	@Override
	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void forceScrollElement(FoundElement value) {
		// TODO Auto-generated method stub

	}
}
