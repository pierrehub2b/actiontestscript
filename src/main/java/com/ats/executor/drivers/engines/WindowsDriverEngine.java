package com.ats.executor.drivers.engines;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.AtsManager;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelProcessData;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;

public class WindowsDriverEngine extends DriverEngineAbstract implements IDriverEngine {

	public static final String windows = "windows";
	public static final String desktop = "desktop";

	private Process applicationProcess = null;

	public WindowsDriverEngine(Channel channel, String application, WindowsDesktopDriver windowsDriver, AtsManager ats) {

		super(channel, application);

		this.driver = windowsDriver;

		int firstSpace = application.indexOf(" ");
		String applicationArguments = "";

		if(firstSpace > 0){
			applicationArguments = application.substring(firstSpace);
			application = application.substring(0, firstSpace);
		}

		URL fileUrl = null;
		File exeFile = null;
		try {
			fileUrl = new URL(application);
			exeFile = Paths.get(fileUrl.toURI()).toFile();
		} catch (MalformedURLException e1) {} catch (URISyntaxException e) {}

		if(exeFile != null && exeFile.exists()){
			Runtime runtime = Runtime.getRuntime();
			try{
				this.applicationProcess = runtime.exec(exeFile.getAbsolutePath() + applicationArguments);
			} catch (IOException e) {
				e.printStackTrace();
			}

			ChannelProcessData data = new ChannelProcessData(getWindowsPid());

			List<WebElement> childs = new ArrayList<WebElement>();
			int maxTry = 30;

			while(childs.isEmpty() && maxTry > 0){
				childs = ((WindowsDesktopDriver)this.driver).getChildrenByPid(data.getPid());
				maxTry--;
				channel.sleep(200);
			}

			for(WebElement elem : childs){
				data.addWindowHandle(Integer.parseInt(elem.getAttribute("NativeWindowHandle")));
			}

			channel.setProcessData(data);
		}
	}
	
	@Override
	public boolean isDesktop() {
		return true;
	}
	
	@Override
	public void waitAfterAction() {

	}

	private Long getWindowsPid(){
		
		return applicationProcess.pid();
		
		/*try {
			Field f = applicationProcess.getClass().getDeclaredField("handle");
			//f.setAccessible(true);		
			long handl = f.getLong(applicationProcess);

			W32API.HANDLE handle = new W32API.HANDLE();
			handle.setPointer(Pointer.createConstant(handl));

			return Kernel32.INSTANCE.GetProcessId(handle);

		} catch (Throwable e) {}
		return -1;*/
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

	public boolean waitElementIsVisible(WebElement element) {
		return true;
	}

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
	public int switchWindow(int index) {
		return 1;
	}
	
	@Override
	public void resizeWindow(int width, int height) {
		// Do nothing
	}

	@Override
	public int closeWindow(int index) {
		return 1;
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

		return null;
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
	public void mouseMoveToElement(WebElement element, Rectangle elementRectangle, MouseDirection position) {
		// TODO Auto-generated method stub
		
	}
}
