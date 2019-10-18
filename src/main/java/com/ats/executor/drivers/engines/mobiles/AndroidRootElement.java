package com.ats.executor.drivers.engines.mobiles;

import com.ats.element.AtsMobileElement;
import com.google.gson.JsonObject;

public class AndroidRootElement extends RootElement {

	@Override
	public void refresh(JsonObject jsonObject) {
		this.value = gson.fromJson(jsonObject, AtsMobileElement.class);
	}
}
