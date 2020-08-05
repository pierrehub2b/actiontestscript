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
import java.util.List;
import java.util.stream.Collectors;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionSysButton extends Action {
	
	public static final String SCRIPT_LABEL = "sysbutton";
	
	/* public static final String SOUND_UP = "sound-up";
	public static final String SOUND_DOWN = "sound-down";
	public static final String POWER = "power";
	public static final String BACK = "back";
	public static final String HOME = "home";
	public static final String MENU = "menu";
	public static final String PREVIEW = "preview"; */
	
	private ArrayList<String> buttonTypes;
	
	public ActionSysButton() { }
	
	public ActionSysButton(Script script, ArrayList<String> buttonTypes) {
		super(script);
		setButtonTypes(buttonTypes);
	}
	
	public ActionSysButton(Script script, String[] buttonTypes) {
		super(script);
		ArrayList<String> list = new ArrayList<>(Arrays.asList(buttonTypes));
		setButtonTypes(list);
	}
	
	@Override
	public void execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);
		
		if (status.isPassed()) {
			if (ts.getCurrentChannel() != null) {
				ts.getCurrentChannel().buttonClick(buttonTypes);
			}
			status.endDuration();
		}
	}
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder builder = super.getJavaCode();
		builder.append("new String[]{");
		
		List<String> myFinalList = getButtonTypes().stream().map(b -> "\"" + b.trim() +"\"").collect(Collectors.toList());
		String test = String.join(",", myFinalList);
		builder.append(test);
		
		builder.append("}").append(")");
		
		return builder;
	}
	
	public ArrayList<String> getButtonTypes() {
		return buttonTypes;
	}
	
	public void setButtonTypes(ArrayList<String> buttonTypes) {
		this.buttonTypes = buttonTypes;
	}
}
