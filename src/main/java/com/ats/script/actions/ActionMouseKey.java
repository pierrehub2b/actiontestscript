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
import java.util.Iterator;

import org.openqa.selenium.Keys;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.objects.mouse.MouseKey;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouseKey extends ActionMouse {

	public static final String CTRL_KEY = "ctrl";
	public static final String SHIFT_KEY = "shift";
	public static final String ALT_KEY = "alt";

	private Keys key = null;

	public ActionMouseKey(){}

	public ActionMouseKey(ScriptLoader script, String type, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stop, options, objectArray);

		Iterator<String> itr = options.iterator();
		while (itr.hasNext()){
			String key = itr.next().trim();
			setKey(key);
		}
	}	

	public ActionMouseKey(Script script, boolean stop, int maxTry, SearchedElement element, MouseKey mouse) {
		super(script, stop, maxTry, element, mouse);
		key = mouse.getKey();
	}

	public ActionMouseKey(Script script, boolean stop, int maxTry, SearchedElement element, Mouse mouse) {
		super(script, stop, maxTry, element, mouse);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {

		String keyCode = "";
		if(Keys.CONTROL.equals(key)){
			keyCode = ", Keys.CONTROL";
		}else if(Keys.SHIFT.equals(key)){
			keyCode = ", Keys.SHIFT";
		}else if(Keys.ALT.equals(key)){
			keyCode = ", Keys.ALT";
		}

		setSpareCode(keyCode);

		return super.getJavaCode();
	}

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);

		if(status.isPassed()) {

			ts.getRecorder().updateScreen(true);
			
			if(Mouse.WHEEL_CLICK.equals(getType())) {
				getTestElement().wheelClick(status);
			}else if(Mouse.RIGHT_CLICK.equals(getType())) {
				getTestElement().rightClick();
			}else if(Mouse.DOUBLE_CLICK.equals(getType())) {
				getTestElement().doubleClick();
			}else {
				if(key != null) {
					getTestElement().click(status, key);
				}else {
					getTestElement().click(status, false);
				}
			}
			
			status.endDuration();
			ts.getRecorder().updateScreen(0, status.getDuration());
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getKey() {
		if(Keys.CONTROL.equals(key)) {
			return CTRL_KEY;
		}else if(Keys.SHIFT.equals(key)) {
			return SHIFT_KEY;
		}else if(Keys.ALT.equals(key)) {
			return ALT_KEY;
		}
		return "";
	}

	public void setKey(String value) {
		if(CTRL_KEY.equals(value)) {
			this.key = Keys.CONTROL;
		}else if(SHIFT_KEY.equals(value)) {
			this.key = Keys.SHIFT;
		}else if(ALT_KEY.equals(value)) {
			this.key = Keys.ALT;
		}else {
			this.key = null;
		}
	}
}