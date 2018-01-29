package com.ats.executor.drivers;

import com.ats.driver.AtsManager;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.executor.drivers.engines.WindowsDriverEngine;
import com.ats.executor.drivers.engines.browsers.ChromeDriverEngine;
import com.ats.executor.drivers.engines.browsers.EdgeDriverEngine;
import com.ats.executor.drivers.engines.browsers.FirefoxDriverEngine;
import com.ats.executor.drivers.engines.browsers.OperaDriverEngine;
import com.ats.tools.Utils;

public class DriverManager {

	public static final String CHROME_BROWSER = "chrome";
	public static final String FIREFOX_BROWSER = "firefox";
	public static final String EDGE_BROWSER = "edge";
	public static final String OPERA_BROWSER = "opera";
	public static final String SAFARI_BROWSER = "safari";
	
	public static final String WINDOWS_DESKTOP_FILE_NAME = "Windows.Desktop.Driver.exe";
	public static final String CHROME_DRIVER_FILE_NAME = "chromedriver.exe";
	public static final String MICROSOFT_WEBDRIVER_FILE_NAME = "MicrosoftWebDriver";
	public static final String OPERA_WEBDRIVER_FILE_NAME = "operadriver.exe";
	public static final String FIREFOX_DRIVER_FILE_NAME = "geckodriver.exe";

	private DriverProcess winDesktopDriver;
	private DriverProcess chromeDriver;
	private DriverProcess edgeDriver;
	private DriverProcess operaDriver;
	private DriverProcess firefoxDriver;
	
	private AtsManager ats;

	public DriverManager() {
		this.ats = new AtsManager();
	}
	
	public String getDriverFolderPath() {
		return ats.getDriversFolderPath().toFile().getAbsolutePath();
	}
	
	public TestBound getApplicationBound() {
		return ats.getApplicationBound();
	}
	
	public int getMaxTry() {
		return ats.getMaxTry();
	}
	
	//--------------------------------------------------------------------------------------------------------------
	
	public DriverProcess getWinDesktopDriver() {
		if(winDesktopDriver == null){
			winDesktopDriver = new DriverProcess(ats.getDriversFolderPath(), WINDOWS_DESKTOP_FILE_NAME);
		}
		return winDesktopDriver;
	}
	
	public IDriverEngine getDriverEngine(Channel channel, String application, WindowsDesktopDriver desktopDriver) {
		switch(application) {
		case CHROME_BROWSER :
			return new ChromeDriverEngine(channel, getChromeDriver(), desktopDriver, ats);
		case EDGE_BROWSER :
			return new EdgeDriverEngine(channel, getEdgeDriver(), desktopDriver, ats);
		case OPERA_BROWSER :
			return new OperaDriverEngine(channel, getOperaDriver(), desktopDriver, ats);
		case FIREFOX_BROWSER :
			return new FirefoxDriverEngine(channel, getFirefoxDriver(), desktopDriver, ats);
		default :
			return new WindowsDriverEngine(channel, application, desktopDriver, ats);
		}
	}
	
	public DriverProcess getFirefoxDriver() {
		if(firefoxDriver == null){
			firefoxDriver = new DriverProcess(ats.getDriversFolderPath(), FIREFOX_DRIVER_FILE_NAME);
		}
		return firefoxDriver;
	}

	public DriverProcess getChromeDriver() {
		if(chromeDriver == null){
			chromeDriver = new DriverProcess(ats.getDriversFolderPath(), CHROME_DRIVER_FILE_NAME);
		}
		return chromeDriver;
	}

	public DriverProcess getEdgeDriver() {
		if(edgeDriver == null){
			edgeDriver = new DriverProcess(ats.getDriversFolderPath(), MICROSOFT_WEBDRIVER_FILE_NAME + "-" + Utils.getWindowsBuildVersion() + ".exe");
		}
		return edgeDriver;
	}

	public DriverProcess getOperaDriver() {
		if(operaDriver == null){
			operaDriver = new DriverProcess(ats.getDriversFolderPath(), OPERA_WEBDRIVER_FILE_NAME);
		}
		return operaDriver;
	}

	public void tearDown(){
		
		if(winDesktopDriver != null){
			winDesktopDriver.close();
		}

		if(chromeDriver != null){
			chromeDriver.close();
		}
		
		if(edgeDriver != null){
			edgeDriver.close();
		}
		
		if(operaDriver != null){
			operaDriver.close();
		}
		
		if(firefoxDriver != null){
			firefoxDriver.close();
		}
	}
}