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
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouse extends ActionExecuteElement {

	private String type = Mouse.OVER;

	private MouseDirection position;

	public ActionMouse(){}

	public ActionMouse(ScriptLoader script, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setPosition(new MouseDirection(options));
		setType("undefined");
	}
	
	public ActionMouse(Script script, boolean stop, int maxTry, SearchedElement element, Mouse mouse) {
		super(script, stop, maxTry, element);
		setPosition(mouse.getPosition());
		setType(mouse.getType());
	}
	
	public ActionMouse(ScriptLoader script, String type, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		this(script, stop, options, objectArray);
		setType(type);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);

		ts.updateVisual(getType(), position);

		status.startDuration();
		getTestElement().over(status, position);
		status.endDuration();
		
		ts.updateVisualWithImage(0, status.getDuration());
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	private String spareCode = "";
	public void setSpareCode(String spare) {
		this.spareCode = spare;
	}

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + ActionTestScript.JAVA_MOUSE_FUNCTION_NAME + "(" + getMouseType() + "" + spareCode + position.getJavaCode() + "))";
	}

	private static final String MOUSE_CLASS = Mouse.class.getSimpleName() + ".";
	private String getMouseType() {
		switch (type){
		case Mouse.OVER :
			return MOUSE_CLASS + "OVER";
		case Mouse.DOUBLE_CLICK :
			return MOUSE_CLASS + "DOUBLE_CLICK";
		case Mouse.RIGHT_CLICK :
			return MOUSE_CLASS + "RIGHT_CLICK";
		case Mouse.WHEEL_CLICK :
			return MOUSE_CLASS + "WHEEL_CLICK";
		case Mouse.DRAG :
			return MOUSE_CLASS + "DRAG";
		case Mouse.DROP :
			return MOUSE_CLASS + "DROP";
		case Mouse.CLICK :
			return MOUSE_CLASS + "CLICK";
		}
		return "";
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getType() {
		return type;
	}

	public void setType(String value) {
		this.type = value;
	}

	public MouseDirection getPosition() {
		return position;
	}

	public void setPosition(MouseDirection value) {
		this.position = value;
	}
}