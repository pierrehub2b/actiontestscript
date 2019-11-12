package com.ats.executor.drivers.engines.browsers;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;

public class MsEdgeDriverEngine extends ChromiumBasedDriverEngine {

	public MsEdgeDriverEngine(Channel channel, ActionStatus status, String browser, DriverProcess driverProcess, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, status, DriverManager.MSEDGE_BROWSER, driverProcess, desktopDriver, props);
		
		ChromeOptions options = initOptions(props, DriverManager.MSEDGE_BROWSER);
		options.setCapability(CapabilityType.BROWSER_NAME, "MicrosoftEdge");
		launchDriver(status, options, profileFolder);
	}
}