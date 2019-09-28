package com.ats.element;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebElement;

public class DesktopRootElement extends RemoteWebElement {
	
	private int width = 0;
	private int height = 0;
	
	public DesktopRootElement(int w, int h) {
		super();
		this.width = w;
		this.height = h;
	}

	@Override
	public Dimension getSize() {
		return new Dimension(width, height);
	}

	@Override
	public String getAttribute(String name) {
		if("clientWidth".equals(name)){
			return width + "";
		}else if("clientHeight".equals(name)) {
			return height + "";
		}
		return super.getAttribute(name);
	}
	
	
}
