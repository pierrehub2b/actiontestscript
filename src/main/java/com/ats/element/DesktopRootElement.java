package com.ats.element;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebElement;

public class DesktopRootElement extends RemoteWebElement {
	
	private int width = 0;
	private int height = 0;
	
	public DesktopRootElement(FoundElement root) {
		super();
		this.id = root.getId();
		this.width = root.getWidth().intValue();
		this.height = root.getHeight().intValue();
	}

	@Override
	public Dimension getSize() {
		return new Dimension(width, height);
	}

	@Override
	public String getAttribute(String name) {
		if(TestElement.CLIENT_WIDTH.equals(name)){
			return String.valueOf(width);
		}else if(TestElement.CLIENT_HEIGTH.equals(name)) {
			return String.valueOf(height);
		}
		return super.getAttribute(name);
	}
}