package com.ats.executor.drivers.engines.browsers;

import java.util.Collections;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.ats.driver.ApplicationProperties;
import com.ats.element.SearchedElement;
import com.ats.element.TestElementSystem;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.variables.CalculatedProperty;

public class MsEdgeDriverEngine extends ChromiumBasedDriverEngine {

	public MsEdgeDriverEngine(Channel channel, ActionStatus status, DriverProcess driverProcess, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, status, DriverManager.MSEDGE_BROWSER, driverProcess, desktopDriver, props);
		
		final ChromeOptions options = initOptions(props);
		options.setCapability(CapabilityType.BROWSER_NAME, "MicrosoftEdge");
		options.setExperimentalOption("useAutomationExtension", false);
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		
		launchDriver(status, options, profileFolder);
	}
	
	@Override
	public void started(ActionStatus status) {
		final TestElementSystem closInfobarButton = new TestElementSystem(channel, 1, p -> p == 1, new SearchedElement(new SearchedElement(new SearchedElement(0, "syscomp", new CalculatedProperty[] {}), 0, "Group", new CalculatedProperty[] {new CalculatedProperty("ClassName", "InfoBarContainerView")}), 0, "Button", new CalculatedProperty[] {}));
		if(closInfobarButton.getCount() == 1) {
			closInfobarButton.executeScript(status, "Invoke()", false);
		}
	}
}