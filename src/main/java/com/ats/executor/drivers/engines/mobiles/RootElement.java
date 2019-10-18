package com.ats.executor.drivers.engines.mobiles;

import com.ats.element.AtsMobileElement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RootElement {

	protected Gson gson = new Gson();
	protected AtsMobileElement value;
	
	public AtsMobileElement getValue() {
		return value;
	}

	public void refresh(JsonObject jsonObject) {
	}
}
