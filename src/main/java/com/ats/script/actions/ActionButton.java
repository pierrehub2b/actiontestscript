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

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionButton extends Action {

	public static final String SCRIPT_LABEL = "button";

	private String buttonType = "home";

	public ActionButton() { }

	public ActionButton(Script script, String buttonType) {
		super(script);
		setButtonType(buttonType);
	}

	public ActionButton(ScriptLoader script, ArrayList<String> dataArray) {
		String[] data = dataArray.get(0).split("=");
		if(data.length == 2) {
			setButtonType(data[1].replace("]", "").trim());
		}
	}

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);

		if (status.isPassed()) {
			
			if(ts.getCurrentChannel() != null){
				ts.getCurrentChannel().buttonClick(status, getButtonType());
			}
			
			status.endAction();
			ts.getRecorder().update(0, status.getDuration(), getButtonType());
		}
		
		return true;
	}

	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append("\"").append(buttonType).append("\")");
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getButtonType() {
		return buttonType;
	}

	public void setButtonType(String value) {
		this.buttonType = value;
	}
}
