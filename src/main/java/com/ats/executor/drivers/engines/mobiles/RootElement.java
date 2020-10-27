package com.ats.executor.drivers.engines.mobiles;

import com.ats.element.AtsMobileElement;
import com.ats.element.FoundElement;
import com.ats.element.MobileTestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public abstract class RootElement {

	protected Gson gson = new Gson();
	protected AtsMobileElement value;
	protected MobileDriverEngine driver;
	
	public RootElement(MobileDriverEngine driver) {
		this.driver = driver;
	}
	
	public AtsMobileElement getValue() {
		return value;
	}

	abstract public void refresh(@Nonnull JsonObject jsonObject);
	abstract public void tap(ActionStatus status, FoundElement element, MouseDirection position);
	abstract public void tap(FoundElement element, int count);
	abstract public void press(FoundElement element, ArrayList<String> paths, int duration);
	abstract public void swipe(MobileTestElement testElement, int hDirection, int vDirection);
	abstract public Object scripting(String script, FoundElement element);
	abstract public Object scripting(String script);
	abstract public MobileTestElement getCurrentElement(FoundElement element, MouseDirection position);
}