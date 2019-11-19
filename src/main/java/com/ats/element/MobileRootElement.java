package com.ats.element;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebElement;

public class MobileRootElement extends RemoteWebElement {
	
	private int width = 0;
	private int height = 0;

	public MobileRootElement(AtsMobileElement value) {
		super();
		this.id = value.getId();
		this.width = value.getWidth().intValue();
		this.height = value.getHeight().intValue();
	}

	@Override
	public Dimension getSize() {
		return new Dimension(width, height);
	}

	@Override
	public String getAttribute(String name) {
		if("width".equals(name)){
			return width + "";
		}else if("height".equals(name)) {
			return height + "";
		}
		return super.getAttribute(name);
	}
	
	
}
