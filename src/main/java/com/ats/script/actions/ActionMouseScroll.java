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

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.mouse.MouseScroll;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Utils;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionMouseScroll extends ActionMouse {

	public static final String SCRIPT_LABEL = "scroll";

	public static final String JAVA_FUNCTION_NAME = "scroll";

	private int value = 0;

	public ActionMouseScroll(){}

	public ActionMouseScroll(ScriptLoader script, String value, boolean stop, ArrayList<String> options, ArrayList<String> elementArray) {
		super(script, stop, options, elementArray);
		setValue(Utils.string2Int(value));
	}

	public ActionMouseScroll(Script script, boolean stop, int maxTry, int delay, SearchedElement element, MouseScroll mouse) {
		super(script, stop, maxTry, delay, element, mouse);
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
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("value", value);
		return super.getActionLogs(scriptName, scriptLine, data);
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