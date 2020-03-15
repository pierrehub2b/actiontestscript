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
import java.util.Arrays;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.mouse.MouseSwipe;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.google.gson.JsonObject;

public class ActionMouseSwipe extends ActionMouse {

	public static final String SCRIPT_LABEL = "swipe";

	private MouseDirection direction;

	public ActionMouseSwipe(){}

	public ActionMouseSwipe(ScriptLoader script, String type, String direction, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stop, options, objectArray);
		this.setDirection(new MouseDirection(script, new ArrayList<String>(Arrays.asList(direction.split(","))), false));
	}

	public ActionMouseSwipe(Script script, boolean stop, int maxTry, int delay, SearchedElement element, MouseSwipe mouse) {
		super(script, stop, maxTry, delay, element, mouse);
		this.setDirection(mouse.getDirection());
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		setSpareCode(direction.getDirectionJavaCode());
		return super.getJavaCode();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);

		if(status.isPassed()) {
			getTestElement().swipe(status, getPosition(), direction);

			status.endDuration();
			ts.getRecorder().updateScreen(0, status.getDuration());
		}
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public MouseDirection getDirection() {
		return direction;
	}

	public void setDirection(MouseDirection value) {
		this.direction = value;
	}
}