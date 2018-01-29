package com.ats.executor.drivers.engines.browsers;

import java.util.Iterator;

import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ats.driver.AtsManager;
import com.ats.driver.BrowserProperties;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;

public class OperaDriverEngine extends WebDriverEngine {

	private int waitAfterAction = 50;
	
	public OperaDriverEngine(Channel channel, DriverProcess driverProcess, WindowsDesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.OPERA_BROWSER, driverProcess, windowsDriver, ats);
				
		OperaOptions options = new OperaOptions();
		options.setCapability("opera.log.level", "SEVERE");
				
		BrowserProperties props = ats.getBrowserProperties(DriverManager.OPERA_BROWSER);
		if(props != null) {
			waitAfterAction = props.getWait();
			if(props.getPath() != null) {
				options.setBinary(props.getPath());
			}
		}
				
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(OperaOptions.CAPABILITY, options);
		
		launchDriver(cap, false);
	}

	@Override
	public void close() {

		Iterator<String> listWindows = driver.getWindowHandles().iterator();
		while(listWindows.hasNext()) {
			driver.switchTo().window(listWindows.next());
			driver.close();
		}

		super.close();
	}
	
	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
	}
}
