/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.ats.executor.drivers.engines;

import java.awt.Rectangle;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.tools.ResourceContent;

public abstract class DriverEngineAbstract {

	protected final String WEB_ELEMENT_REF = "element-6066-11e4-a52e-4f735466cecf";
		
	//-----------------------------------------------------------------------------------------------------------------------------
	// Javascript static code
	//-----------------------------------------------------------------------------------------------------------------------------
	
	protected final String JS_WAIT_READYSTATE = "var result=window.document.readyState=='complete';";
	protected final String JS_WAIT_BEFORE_SEARCH = "var interval=setInterval(function(){if(window.document.readyState==='complete'){clearInterval(interval);done();}},200);";
	
	protected final String JS_AUTO_SCROLL = "var e=arguments[0];e.scrollIntoView();var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected final String JS_AUTO_SCROLL_CALC = "var e=arguments[0];var r=e.getBoundingClientRect();var top=r.top + window.pageYOffset;window.scrollTo(0, top-(window.innerHeight / 2));r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected final String JS_AUTO_SCROLL_MOZ = "var e=arguments[0];e.scrollIntoView({behavior:'auto',block:'center',inline:'center'});var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	
	protected final String JS_ELEMENT_SCROLL = "var e=arguments[0];var d=arguments[1];e.scrollTop += d;var r=e.getBoundingClientRect();var result=[r.left+0.0001, r.top+0.0001]";
	protected final String JS_WINDOW_SCROLL = "window.scrollBy(0,arguments[0]);var result=[0.0001, 0.0001]";
	protected final String JS_ELEMENT_DATA = "var result=null;var e=document.elementFromPoint(arguments[0],arguments[1]);if(e){var r=e.getBoundingClientRect();result=[e, e.tagName, r.width+0.0001, r.height+0.0001, r.left+0.0001, r.top+0.0001];};";
	protected final String JS_ELEMENT_BOUNDING = "var rect=arguments[0].getBoundingClientRect();var result=[rect.left+0.0001, rect.top+0.0001];";
	protected final String JS_MIDDLE_CLICK = "var evt=new MouseEvent('click', {bubbles: true,cancelable: true,view: window, button: 1}),result={};arguments[0].dispatchEvent(evt);";
	protected final String JS_ELEMENT_CSS = "var result=[];var o=getComputedStyle(arguments[0]);for(var i=0, len=o.length; i < len; i++){result.push([o[i], o.getPropertyValue(o[i])]);};";
	
	protected final String JS_SEARCH_ELEMENT = ResourceContent.getSearchElementsJavaScript();
	protected final String JS_ELEMENT_AUTOSCROLL = ResourceContent.getScrollElementJavaScript();
	protected final String JS_ELEMENT_ATTRIBUTES = ResourceContent.getElementAttributesJavaScript();
	protected final String JS_ELEMENT_PARENTS = ResourceContent.getParentElementJavaScript();
	protected final String JS_DOCUMENT_SIZE = ResourceContent.getDocumentSizeJavaScript();
		
	//-----------------------------------------------------------------------------------------------------------------------------
	
	protected Channel channel;
	protected RemoteWebDriver driver;
	protected String application;
	protected String applicationPath;
	
	protected int currentWindow = 0;
	
	private int actionWait = -1;

	public DriverEngineAbstract(Channel channel, String application, ApplicationProperties props, int defaultWait){
		this.channel = channel;
		this.application = application;
		
		if(props != null) {
			actionWait = props.getWait();
			applicationPath = props.getPath();
		}
		
		if(actionWait == -1) {
			actionWait = defaultWait;
		}
	}

	public String getApplication() {
		return application;
	}
	
	public String getApplicationPath() {
		return applicationPath;
	}
	
	public void actionWait() {
		channel.sleep(actionWait);
	}
	
	public int getActionWait() {
		return actionWait;
	}

	protected int getDirectionValue(int value, MouseDirectionData direction,Cartesian cart1, Cartesian cart2) {
		if(cart1.equals(direction.getName())) {
			return direction.getValue();
		}else if(cart2.equals(direction.getName())) {
			return value - direction.getValue();
		}
		return 0;
	}

	protected int getNoDirectionValue(int value) {
		return value / 2;
	}

	private int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2) {
		if(direction != null) {
			return getDirectionValue(value, direction, cart1, cart2);
		}else {
			return getNoDirectionValue(value);
		}
	}

	protected int getOffsetX(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.width, position.getHorizontalPos(), Cartesian.LEFT, Cartesian.RIGHT);
	}

	protected int getOffsetY(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.height, position.getVerticalPos(), Cartesian.TOP, Cartesian.BOTTOM);
	}
	
	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {

		if(width != null || height != null){
			int newWidth = channel.getDimension().getWidth().intValue();
			if(width != null) {
				newWidth = width.getValue();
			}

			int newHeight = channel.getDimension().getHeight().intValue();
			if(height != null) {
				newHeight = height.getValue();
			}

			setSize(new Dimension(newWidth, newHeight));
		}

		if(x != null || y != null){
			int newX = channel.getDimension().getX().intValue();
			if(x != null) {
				newX = x.getValue();
			}

			int newY = channel.getDimension().getY().intValue();
			if(y != null) {
				newY = y.getValue();
			}

			setPosition(new Point(newX, newY));
		}
	}

	protected void setPosition(Point pt) {

	}

	protected void setSize(Dimension dim) {

	}
}
