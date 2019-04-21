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
import com.ats.generator.objects.mouse.MouseScroll;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouseScroll extends ActionMouse {

	public static final String SCRIPT_LABEL = "scroll";

	public static final String JAVA_FUNCTION_NAME = "scroll";

	private int value = 0;

	public ActionMouseScroll(){}

	public ActionMouseScroll(ScriptLoader script, String value, boolean stop, ArrayList<String> options, ArrayList<String> elementArray) {
		super(script, stop, options, elementArray);
		
		try{
			setValue(Integer.parseInt(value));
		}catch (NumberFormatException e) {}
	}

	public ActionMouseScroll(Script script, boolean stop, int maxTry, SearchedElement element, MouseScroll mouse) {
		super(script, stop, maxTry, element, mouse);
		setValue(mouse.getValue());
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		setSpareCode(value + "");
		return super.getJavaCode();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		if(status.isPassed()) {
			getTestElement().mouseWheel(value);

			status.endDuration();
			ts.getRecorder().updateScreen(0, status.getDuration(), value + "");
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}