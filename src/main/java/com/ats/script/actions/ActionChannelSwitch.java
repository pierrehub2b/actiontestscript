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

public class ActionChannelSwitch extends ActionChannel {

	public static final String SCRIPT_SWITCH_LABEL = SCRIPT_LABEL + "switch";
	
	public ActionChannelSwitch() {}

	public ActionChannelSwitch(Script script, String name) {
		super(script, name);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);

		ts.getRecorder().update(getName());
		ts.getChannelManager().switchChannel(status, getName());
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append("\"").append(getName()).append("\")");
		return codeBuilder;
	}
}