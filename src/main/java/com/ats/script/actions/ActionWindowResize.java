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

package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.BoundData;
import com.ats.script.Script;

public class ActionWindowResize extends ActionWindow {

	public static final String SCRIPT_RESIZE_LABEL = SCRIPT_LABEL + "resize";

	private static final String X="x";
	private static final String Y="y";
	private static final String WIDTH="width";
	private static final String HEIGHT="height";

	private BoundData x;
	private BoundData y;
	private BoundData width;
	private BoundData height;

	public ActionWindowResize() {}

	public ActionWindowResize(Script script, String size) {
		super(script, -1);
		String[] sizeData = size.split(",");

		for(String data : sizeData) {
			String[] dataValue = data.split("=");
			if(dataValue.length == 2) {
				switch (dataValue[0].trim().toLowerCase()) {

				case X:
					x = getBoundData(dataValue[1].trim());
					break;
				case Y:
					y = getBoundData(dataValue[1].trim());
					break;
				case WIDTH:
					width = getBoundData(dataValue[1].trim());
					break;
				case HEIGHT:
					height = getBoundData(dataValue[1].trim());
					break;
				}
			}
		}
	}

	private BoundData getBoundData(String data) {
		try {
			return new BoundData(Integer.parseInt(data));
		}catch(NumberFormatException ex) {
			return null;
		}
	}

	public ActionWindowResize(Script script, Integer x, Integer y, Integer width, Integer height) {
		super(script, -1);
		setX(getBoundDataValue(x));
		setY(getBoundDataValue(y));
		setWidth(getBoundDataValue(width));
		setHeight(getBoundDataValue(height));
	}
	
	private BoundData getBoundDataValue(Integer value) {
		if(value != null) {
			return new BoundData(value.intValue());
		}
		return null;
	}
	
	private String getBoundDataJavaCode(BoundData value) {
		if(value != null) {
			return value.getValue() + "";
		}
		return null;
	}

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + getBoundDataJavaCode(x) + ", " + getBoundDataJavaCode(y) + ", " + getBoundDataJavaCode(width) + ", " + getBoundDataJavaCode(height) + ")";
	}

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		
		if(ts.getCurrentChannel() != null){
			ts.getCurrentChannel().setWindowBound(x, y, width, height);
		}

		status.endDuration();
		ts.updateVisualWithImage(0, status.getDuration());
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public BoundData getX() {
		return x;
	}

	public void setX(BoundData x) {
		this.x = x;
	}

	public BoundData getY() {
		return y;
	}

	public void setY(BoundData y) {
		this.y = y;
	}

	public BoundData getWidth() {
		return width;
	}

	public void setWidth(BoundData width) {
		this.width = width;
	}

	public BoundData getHeight() {
		return height;
	}

	public void setHeight(BoundData height) {
		this.height = height;
	}
}