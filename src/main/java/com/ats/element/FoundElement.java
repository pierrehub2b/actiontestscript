package com.ats.element;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.google.common.collect.ImmutableList;

public class FoundElement{

	public static final String IFRAME = "IFRAME";
	
	public static final ImmutableList<String> WINDOWS_UI_PROPERTIES = 
			ImmutableList.of(
					"ClassName", 
					"Name", 
					"AutomationId", 
					"LocalizedControlType",
					"FrameworkId", 
					"LabeledBy", 
					"IsEnabled",
					"ItemType", 
					"HelpText", 
					"IemStatus", 
					"AccessKey", 
					"AcceleratorKey");

	private String id;

	private Double x = 0.0;
	private Double y = 0.0;
	
	private Double screenX = 0.0;
	private Double screenY = 0.0;
	
	private Double width = 0.0;
	private Double height = 0.0;

	private String tag;

	private boolean desktop = false;

	private RemoteWebElement value;
	private FoundElement parent;
	private boolean visible = true;

	private String iframesList;

	public FoundElement() {}

	public FoundElement(Map<String, Object> data) {
		this.value = (RemoteWebElement) data.get("value");
		this.id = value.getId();
		this.tag = (String) data.get("tag");
		this.width = (Double)data.get("width");
		this.height = (Double)data.get("height");
	}

	public FoundElement(Map<String, Object> data, Channel channel, Double offsetX, Double offsetY, ArrayList<String> iframes) {
		this(data, channel, offsetX, offsetY);
		if(this.isIframe()) {
			this.id = iframes.remove(iframes.size()-1);
		}
		this.iframesList  = String.join(",", iframes);
	}

	public FoundElement(Map<String, Object> data, Channel channel, Double offsetX, Double offsetY) {
		this(data);
		this.updatePosition((Double)data.get("x"), (Double)data.get("y"), channel, offsetX, offsetY);
	}
	
	public FoundElement(ArrayList<Map<String, Object>> listElements, Channel channel, Double initElementX, Double initElementY, ArrayList<String> frm) {
		this(listElements.remove(0), channel, initElementX, initElementY, frm);
		if(listElements.size() > 0) {
			setParent(new FoundElement (listElements, channel, initElementX, initElementY, frm));
		}
	}

	public FoundElement(Channel channel) {
		this.value = (RemoteWebElement) channel.getRootElement();
		setWidth(channel.getDimension().getWidth());
		setHeight(channel.getDimension().getHeight());
	}

	public FoundElement(RemoteWebElement element, Double channelX, Double channelY) {
		this.desktop = true;
		this.setRemoteWebElement(element);

		String info = element.getAttribute("InfoData");
		if(info != null){
			String[] infoData = info.split(":");
			this.tag = infoData[0];

			String[] boundData = infoData[1].split(",");
			
			this.screenX = Double.parseDouble(boundData[0]);
			this.screenY = Double.parseDouble(boundData[1]);
			
			this.x = this.screenX - channelX;
			this.y = this.screenY - channelY;
			
			this.width = Double.parseDouble(boundData[2]);
			this.height = Double.parseDouble(boundData[3]);

			this.visible = !"True".equals(infoData[2]);

		}else{
			this.tag = "undefined";
			this.visible = false;
		}
	}

	public FoundElement(ArrayList<WebElement> parentsList, Double channelX, Double channelY) {
		this((RemoteWebElement) parentsList.remove(0), channelX, channelY);
		if(parentsList.size() > 0) {
			setParent(new FoundElement(parentsList, channelX, channelY));
		}
	}
	
	public void dispose() {
		if(parent != null) {
			parent.dispose();
			parent = null;
		}
		value = null;
	}
	
	public void updatePosition(Double x, Double y, Channel channel, Double offsetX, Double offsetY) {
		this.setX(x + channel.getSubDimension().getX() + offsetX);
		this.setY(y + channel.getSubDimension().getY() + offsetY);
	}

	private void setRemoteWebElement(RemoteWebElement rwe){
		this.value = rwe;
		this.id = rwe.getId();
	}

	public boolean isIframe(){
		return IFRAME.equals(this.tag.toUpperCase());
	}

	public WebElement getValue(){
		return value;
	}
	
	public String getElementId() {
		return id;
	}

	public void activate(RemoteWebDriver remoteWebDriver) {
		value = new RemoteWebElement();
		value.setParent(remoteWebDriver);
		value.setId(id);
	}

	public Rectangle getRectangle(){
		return new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
	}

	public ArrayList<String> getIframes() {
		if(iframesList != null && iframesList.length() > 0) {
			return new ArrayList<String>(Arrays.asList(iframesList.split(",")));
		}else {
			return new ArrayList<String>();
		}
	}
	
	public Double getScreenX() {
		return screenX;
	}
	
	public Double getScreenY() {
		return screenY;
	}
	
	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean value){
		this.visible = value;
	}

	public boolean isDesktop() {
		return desktop;
	}

	public void setDesktop(boolean b) {
		this.desktop = b;
	}

	public FoundElement getParent() {
		return parent;
	}

	public void setParent(FoundElement parent) {
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getIframesList() {
		return iframesList;
	}

	public void setIframesList(String s) {
		this.iframesList = s;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Remote element
	//----------------------------------------------------------------------------------------------------------------------

	public RemoteWebElement getRemoteWebElement(RemoteWebDriver driver) {
		RemoteWebElement element = new RemoteWebElement();
		element.setId(id);
		element.setParent(driver);
		return element;
	}

	public TestBound getTestBound() {
		return new TestBound(x, y, width, height);
	}
}