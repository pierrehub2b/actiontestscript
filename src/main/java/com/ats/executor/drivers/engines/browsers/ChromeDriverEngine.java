package com.ats.executor.drivers.engines.browsers;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ats.driver.AtsManager;
import com.ats.driver.BrowserProperties;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class ChromeDriverEngine extends WebDriverEngine {

	private int waitAfterAction = 150;
	
	public ChromeDriverEngine(Channel channel, DriverProcess driverProcess, WindowsDesktopDriver windowsDriver,	AtsManager ats) {
		
		super(channel, DriverManager.CHROME_BROWSER, driverProcess, windowsDriver, ats);

		List<String> args = new ArrayList<String>();

		args.add("--disable-infobars");
		args.add("--disable-notifications");
		args.add("--no-default-browser-check");
		args.add("--disable-web-security");
		args.add("--allow-running-insecure-content");
		args.add("test-type");

		ChromeOptions options = new ChromeOptions();
		options.addArguments(args);
		
		BrowserProperties props = ats.getBrowserProperties(DriverManager.CHROME_BROWSER);
		if(props != null) {
			waitAfterAction = props.getWait();
			applicationPath = props.getPath();
			if(applicationPath != null) {
				options.setBinary(applicationPath);
			}
		}

		DesiredCapabilities caps = DesiredCapabilities.chrome(); 
		caps.setCapability(ChromeOptions.CAPABILITY, options); 
		
		launchDriver(caps);
	}
	
	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
		super.waitAfterAction();
	}
}
