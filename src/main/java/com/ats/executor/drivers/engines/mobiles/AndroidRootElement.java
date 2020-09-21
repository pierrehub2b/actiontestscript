package com.ats.executor.drivers.engines.mobiles;

import com.ats.element.AtsMobileElement;
import com.ats.element.FoundElement;
import com.ats.element.MobileTestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.ArrayList;

public class AndroidRootElement extends RootElement {

	public AndroidRootElement(MobileDriverEngine driver) {
		super(driver);
	}
	
	@Override
	public void refresh(JsonObject jsonObject) {
		this.value = gson.fromJson(jsonObject, AtsMobileElement.class);
	}

	@Override
	public void tap(ActionStatus status, FoundElement element, MouseDirection position) {
		final Rectangle rect = element.getRectangle();
		driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.TAP, (int)(element.getBoundX() + driver.getOffsetX(rect, position)) + "", (int)(element.getBoundY() + driver.getOffsetY(rect, position)) + "");	
	}
	
	@Override
	public void tap(FoundElement element, int count) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.TAP, String.valueOf(count));
	}
	
	@Override
	public void press(FoundElement element, ArrayList<String> paths, int duration) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.PRESS, String.join(":", paths));
	}
	
	@Override
	public void swipe(MobileTestElement testElement, int hDirection, int vDirection) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, testElement.getId(), MobileDriverEngine.SWIPE, testElement.getOffsetX() + "", testElement.getOffsetY() + "", hDirection + "", + vDirection + "");
	}

	@Override
	public Object scripting(String script, FoundElement element) {
		return driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.SCRIPTING, script);
	}

	@Override
	public Object scripting(String script) {
		return scripting(script, getValue().getFoundElement());
	}
	
	@Override
	public MobileTestElement getCurrentElement(FoundElement element, MouseDirection position) {
		final Rectangle rect = element.getRectangle();
		return new MobileTestElement(element.getId(), (driver.getOffsetX(rect, position)), (driver.getOffsetY(rect, position)));
	}
}