package com.ats.executor.drivers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ats.executor.channels.Channel;
import com.ats.tools.Utils;

public class DriverManager {

	public static final String WINDOWS_DESKTOP_FILE_NAME = "Windows.Desktop.Driver.exe";
	public static final String CHROME_DRIVER_FILE_NAME = "chromedriver.exe";
	public static final String MICROSOFT_WEBDRIVER_FILE_NAME = "MicrosoftWebDriver";
	public static final String OPERA_WEBDRIVER_FILE_NAME = "operadriver.exe";
	
	public static final String FIREFOX_DRIVER_FILE_NAME = "geckodriver.exe";
	
	public static final String ATS_DRIVERS_DIRECTORY = "/.actiontestscript/drivers";
		
	private DriverProcess winDesktopDriver;
	private DriverProcess chromeDriver;
	private DriverProcess edgeDriver;
	private DriverProcess operaDriver;
	
	private DriverProcess firefoxDriver;
	
	private Path driverFolderPath;

	public DriverManager() {
		driverFolderPath = getDriverFoler(System.getProperty("driver.folder"));
		if(driverFolderPath == null) {
			driverFolderPath = getDriverFoler(System.getProperty("user.home"), ATS_DRIVERS_DIRECTORY);
			if(driverFolderPath == null) {
				driverFolderPath = getDriverFoler("");
			}
		}
	}
	
	private Path getDriverFoler(String folder) {
		return getDriverFoler(folder, "");
	}
	
	private Path getDriverFoler(String folder, String subFolder) {
		if(folder != null) {
			Path path = Paths.get(folder + subFolder);
			if(Files.exists(path)) {
				return path;
			}
		}
		return null;
	}
	
	public String getDriverFolderPath() {
		return driverFolderPath.toFile().getAbsolutePath();
	}
	
	public DriverProcess getWinDesktopDriver() {
		if(winDesktopDriver == null){
			winDesktopDriver = new DriverProcess(driverFolderPath, WINDOWS_DESKTOP_FILE_NAME);
		}
		return winDesktopDriver;
	}
	
	public DriverProcess getBrowserDriver(String browserName){
		if(Channel.CHROME_BROWSER.equals(browserName)){
			return getChromeDriver();
		}else if(Channel.EDGE_BROWSER.equals(browserName)){
			return getEdgeDriver();
		}else if(Channel.OPERA_BROWSER.equals(browserName)){
			return getOperaDriver();
		}else if(Channel.SAFARI_BROWSER.equals(browserName)){
			
		}else if(Channel.FIREFOX_BROWSER.equals(browserName)){
			return getFirefoxDriver();
		}
		return null;
	}
	
	public DriverProcess getFirefoxDriver() {
		if(firefoxDriver == null){
			firefoxDriver = new DriverProcess(driverFolderPath, FIREFOX_DRIVER_FILE_NAME);
		}
		return firefoxDriver;
	}

	public DriverProcess getChromeDriver() {
		if(chromeDriver == null){
			chromeDriver = new DriverProcess(driverFolderPath, CHROME_DRIVER_FILE_NAME);
		}
		return chromeDriver;
	}

	public DriverProcess getEdgeDriver() {
		if(edgeDriver == null){
			edgeDriver = new DriverProcess(driverFolderPath, MICROSOFT_WEBDRIVER_FILE_NAME + "-" + Utils.getWindowsBuildVersion() + ".exe");
		}
		return edgeDriver;
	}

	public DriverProcess getOperaDriver() {
		if(operaDriver == null){
			operaDriver = new DriverProcess(driverFolderPath, OPERA_WEBDRIVER_FILE_NAME);
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