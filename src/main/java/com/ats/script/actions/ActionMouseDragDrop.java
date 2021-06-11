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
import com.ats.generator.objects.mouse.Mouse;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionMouseDragDrop extends ActionMouse {

	public ActionMouseDragDrop() {}

	public ActionMouseDragDrop(ScriptLoader script, String type, int stopPolicy, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stopPolicy, options, objectArray);
	}

	public ActionMouseDragDrop(Script script, int stopPolicy, int maxTry, int delay, SearchedElement element, Mouse mouse) {
		super(script, stopPolicy, maxTry, delay, element, mouse);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		if(status.isPassed()) {
			if(Mouse.DRAG.equals(getType())) {
				ts.startDrag();
				getTestElement().drag(status, getPosition(), 0, 0);
			}else if(Mouse.DROP.equals(getType())) {
				getTestElement().drop(status, getPosition(), ts.isDesktopDragDrop());
				ts.endDrag();
			}
			status.updateDuration(System.currentTimeMillis());
		}
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		return super.getActionLogs(scriptName, scriptLine, new JsonObject());
	}
}