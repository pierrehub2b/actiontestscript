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

import com.ats.executor.channels.Channel;
import com.ats.script.Script;
import com.google.gson.JsonObject;

public class ActionWindowState extends ActionWindow {

	public static final String MAXIMIZE = "maximize";
	public static final String REDUCE = "reduce";
	public static final String RESTORE = "restore";
	public static final String CLOSE = "close";
	
	public static final String SCRIPT_STATE_LABEL = SCRIPT_LABEL + "state";
	public static final String SCRIPT_CLOSE_LABEL = SCRIPT_LABEL + CLOSE;

	private String state = RESTORE;

	public ActionWindowState() {}

	public ActionWindowState(Script script, String state) {
		super(script);
		setState(state);
	}

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append("\"").append(state).append("\")");
		return codeBuilder;
	}

	@Override
	public String exec(Channel channel) {
		if(CLOSE.equals(state)) {
			channel.closeWindow(status);
		}else {
			channel.windowState(status, state);
			if(!REDUCE.equals(state)) {
				channel.getDriverEngine().updateDimensions();
			}
		}
		return state;
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("state", state);
		return super.getActionLogs(scriptName, scriptLine, data);
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public String getState() {
		return state;
	}

	public void setState(String state) {
		if(state != null) {
			state = state.toLowerCase();
			if(MAXIMIZE.equals(state) || REDUCE.equals(state) || CLOSE.equals(state)) {
				this.state = state;
				return;
			}
		}
		this.state = RESTORE;
	}
}