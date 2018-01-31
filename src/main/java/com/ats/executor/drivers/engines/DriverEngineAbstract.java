package com.ats.executor.drivers.engines;

import java.awt.Rectangle;
import java.util.ArrayList;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.element.FoundElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.variables.CalculatedProperty;

public abstract class DriverEngineAbstract {

	protected Channel channel;
	protected RemoteWebDriver driver;
	protected String application;

	public DriverEngineAbstract(Channel channel, String application){
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



	protected int getDirectionValue(int value, MouseDirectionData direction,Cartesian cart1, Cartesian cart2) {
		if(cart1.equals(direction.getName())) {
			return direction.getValue();
		}else if(cart2.equals(direction.getName())) {
			return value - direction.getValue();
		}
		return 0;
	}

	protected int getNoDirectionValue(int value) {
		return value / 2;
	}

	private int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2) {
		if(direction != null) {
			return getDirectionValue(value, direction, cart1, cart2);
		}else {
			return getNoDirectionValue(value);
		}
	}

	protected int getOffsetX(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.width, position.getHorizontalPos(), Cartesian.LEFT, Cartesian.RIGHT);
	}

	protected int getOffsetY(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.height, position.getVerticalPos(), Cartesian.TOP, Cartesian.BOTTOM);
	}
}
