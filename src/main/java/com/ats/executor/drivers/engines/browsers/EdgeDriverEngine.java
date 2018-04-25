package com.ats.executor.drivers.engines.browsers;

import java.net.URL;
import java.util.ArrayList;

import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.interactions.Actions;

import com.ats.driver.AtsManager;
import com.ats.driver.BrowserProperties;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.variables.CalculatedProperty;

public class EdgeDriverEngine extends WebDriverEngine {

	private int waitAfterAction = 200;

	public EdgeDriverEngine(Channel channel, DriverProcess driverProcess, WindowsDesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.EDGE_BROWSER, driverProcess, windowsDriver, ats);

		EdgeOptions options = new EdgeOptions();
		options.setPageLoadStrategy("normal");

		BrowserProperties props = ats.getBrowserProperties(DriverManager.EDGE_BROWSER);
		if(props != null) {
			waitAfterAction = props.getWait();
		}

		launchDriver(options);
	}

	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
		super.waitAfterAction();
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {
		channel.sleep(500);
		super.closeWindow(status, index);
	}

	@Override
	public void goToUrl(URL url, boolean newWindow) {
		super.goToUrl(url, newWindow);
		waitAfterAction();

		if(newWindow) {
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
	}
}
