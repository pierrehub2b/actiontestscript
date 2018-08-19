package com.ats.element;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.executor.drivers.desktop.DesktopData;

@SuppressWarnings("unchecked")
public class AtsElement {

	private static final String IFRAME = "IFRAME";
	private static final String FRAME = "FRAME";
	private static final String DIALOG = "DIALOG";
	private static final String DESKTOP = "DESK"; //TODO manage desktop elements

	private RemoteWebElement element;
	private String id;
	private String tag;
	private Double width;
	private Double height;
	private Double x;
	private Double y;
	private Double screenX;
	private Double screenY;
	private Map<String, String> attributes;

	private boolean visible = true;

	public static boolean checkIframe(String value) {
		value = value.toUpperCase();
		return IFRAME.equals(value) || FRAME.equals(value);
	}
	
	public static boolean checkDialog(String value) {
		return DIALOG.equals(value.toUpperCase());
	}

	public AtsElement() {}

	public AtsElement(ArrayList<Object> data) {
		this.element = (RemoteWebElement) data.get(0);
		this.tag = data.get(1).toString();
		this.width = (Double) data.get(2);
		this.height = (Double) data.get(3);
		this.x = (Double) data.get(4);
		this.y = (Double) data.get(5);
		this.screenX = (Double) data.get(6);
		this.screenY = (Double) data.get(7);
		
		if(data.size() > 8) {
			this.attributes = (Map<String, String>) data.get(8);
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
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
	// Predicate search
	//----------------------------------------------------------------------------------------

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	//----------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------

	public boolean isIframe() {
		return checkIframe(tag);
	}
}