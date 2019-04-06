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
	protected boolean numeric = false;
	private Double screenX = 0.0;
	private Double screenY = 0.0;
	
	private Double boundX = 0.0;
	private Double boundY = 0.0;

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
		this.boundX = (Double) data.get(3);
		this.boundY = (Double) data.get(4);
		this.width = (Double) data.get(5);
		this.height = (Double) data.get(6);
		this.x = (Double) data.get(7);
		this.y = (Double) data.get(8);
		this.screenX = (Double) data.get(9);
		this.screenY = (Double) data.get(10);
		this.attributes = (Map<String, String>) data.get(11);
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
	
	public Double getBoundX() {
		return boundX;
	}

	public void setBoundX(Double boundX) {
		this.boundX = boundX;
	}

	public Double getBoundY() {
		return boundY;
	}

	public void setBoundY(Double boundY) {
		this.boundY = boundY;
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