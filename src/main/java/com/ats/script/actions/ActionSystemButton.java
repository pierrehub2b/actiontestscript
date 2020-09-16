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
import com.ats.script.Script;

public class ActionSystemButton extends Action {

	public static final String SCRIPT_LABEL = "button";

	private String buttonType;

	public ActionSystemButton() { }

	public ActionSystemButton(Script script, String buttonType) {
		super(script);
		setButtonType(buttonType);
	}

	@Override
	public void execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);

		if (status.isPassed()) {
			ts.getCurrentChannel().buttonClick(getButtonType());
			status.endDuration();
		}
	}

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder builder = super.getJavaCode();
		builder.append("\"").append(buttonType).append("\"");
		return builder;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getButtonType() {
		return buttonType;
	}

	public void setButtonType(String buttonType) {
		this.buttonType = buttonType;
	}
}
