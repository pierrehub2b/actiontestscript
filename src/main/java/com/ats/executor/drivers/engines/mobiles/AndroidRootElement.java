package com.ats.executor.drivers.engines.mobiles;

import java.awt.Rectangle;

import com.ats.element.AtsMobileElement;
import com.ats.element.FoundElement;
import com.ats.element.MobileTestElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.google.gson.JsonObject;

public class AndroidRootElement extends RootElement {

	public AndroidRootElement(MobileDriverEngine drv) {
		super(drv);
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
		return new MobileTestElement(element.getId(), (int)(driver.getOffsetX(rect, position)), (int)(driver.getOffsetY(rect, position)));
	}
}