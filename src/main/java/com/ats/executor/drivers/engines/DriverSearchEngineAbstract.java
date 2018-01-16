package com.ats.executor.drivers.engines;

import java.util.ArrayList;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.FoundElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedProperty;

public abstract class DriverSearchEngineAbstract {

	protected Channel channel;
	protected RemoteWebDriver driver;
	protected String application;
	
	public DriverSearchEngineAbstract(Channel channel, String application){
		this.channel = channel;
		this.application = application;
	}

	public String getApplication() {
		return application;
	}
	
	public RemoteWebDriver getWebDriver(){
		return driver;
	}
		
	public CalculatedProperty[] getWindowsAttributes(WebElement element) {
		ArrayList<CalculatedProperty> listAttributes = new ArrayList<CalculatedProperty>();
		for (String attribute : FoundElement.WINDOWS_UI_PROPERTIES) {
			String value = element.getAttribute(attribute);
			if (value == null) continue;
			listAttributes.add(new CalculatedProperty(attribute, value));
		}

		try{
			listAttributes.add(new CalculatedProperty("Text", element.getText()));
		}catch(Exception e){}

		return listAttributes.toArray(new CalculatedProperty[listAttributes.size()]);
	}
}
