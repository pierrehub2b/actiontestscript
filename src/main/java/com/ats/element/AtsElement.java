package com.ats.element;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.executor.drivers.desktop.DesktopData;

@SuppressWarnings("unchecked")
public class AtsElement extends AtsBaseElement {

	private static final String IFRAME = "IFRAME";
	private static final String FRAME = "FRAME";

	private RemoteWebElement element;
	protected boolean numeric;
	private Double screenX;
	private Double screenY;

	private boolean visible = true;

	public static boolean checkIframe(String value) {
		value = value.toUpperCase();
		return IFRAME.equals(value) || FRAME.equals(value);
	}

	public AtsElement() {}

	public AtsElement(ArrayList<Object> data) {
		this.element = (RemoteWebElement) data.get(0);
		this.tag = data.get(1).toString();
		this.numeric = (boolean) data.get(2);
		this.width = (Double) data.get(3);
		this.height = (Double) data.get(4);
		this.x = (Double) data.get(5);
		this.y = (Double) data.get(6);
		this.screenX = (Double) data.get(7);
		this.screenY = (Double) data.get(8);
		
		if(data.size() > 9) {
			this.attributes = (Map<String, String>) data.get(9);
		}
	}

	public RemoteWebElement getElement() {
		return element;
	}

	public Double getScreenX() {
		return screenX;
	}

	public Double getScreenY() {
		return screenY;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isNumeric() {
		return numeric;
	}

	//----------------------------------------------------------------------------------------
	// Desktop serialization
	//----------------------------------------------------------------------------------------

	public ArrayList<DesktopData> getAttributes() {
		return null;
	}

	public void setAttributes(ArrayList<DesktopData> attributes) {
		if(attributes != null) {
			this.attributes = attributes.parallelStream().collect(Collectors.toMap(s -> s.getName(), s -> s.getValue()));
		}
	}

	//----------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------

	public boolean isIframe() {
		return checkIframe(tag);
	}
}