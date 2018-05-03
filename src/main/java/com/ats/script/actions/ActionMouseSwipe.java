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

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.mouse.MouseSwipe;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouseSwipe extends ActionMouse {

	public static final String SCRIPT_LABEL = "swipe";

	private final String HORIZONTAL = "horizontal";
	private final String VERTICAL = "vertical";

	private int verticalDirection = 0;
	private int horizontalDirection = 0;

	public ActionMouseSwipe(){}

	public ActionMouseSwipe(ScriptLoader script, String type, String direction, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stop, options, objectArray);

		String[] data = direction.split(",");
		for(String value : data) {
			String[] directionData = value.split("=");
			if(directionData.length > 1) {

				if(directionData[0].toLowerCase().contains(HORIZONTAL)) {
					try{
						this.setHorizontalDirection(Integer.parseInt(directionData[1].trim()));
					}catch (NumberFormatException e){}
				}

				if(directionData[0].toLowerCase().contains(VERTICAL)) {
					try{
						this.setVerticalDirection(Integer.parseInt(directionData[1].trim()));
					}catch (NumberFormatException e){}
				}
			}
		}
	}

	public ActionMouseSwipe(Script script, boolean stop, int maxTry, SearchedElement element, MouseSwipe mouse) {
		super(script, stop, maxTry, element, mouse);
		this.setHorizontalDirection(mouse.getHdir());
		this.setVerticalDirection(mouse.getVdir());
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		setSpareCode(horizontalDirection + ", " + verticalDirection);
		return super.getJavaCode();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		getTestElement().swipe(status, horizontalDirection, verticalDirection);
		ts.updateVisualImage();
		status.updateDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getVerticalDirection() {
		return verticalDirection;
	}

	public void setVerticalDirection(int verticalDirection) {
		this.verticalDirection = verticalDirection;
	}

	public int getHorizontalDirection() {
		return horizontalDirection;
	}

	public void setHorizontalDirection(int horizontalDirection) {
		this.horizontalDirection = horizontalDirection;
	}
}