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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;
import com.google.gson.JsonObject;

public abstract class Action {

	protected Script script;
	protected ActionStatus status;
	protected int line;
	protected boolean disabled = false;
	
	private Channel currentChannel;
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------------------------------------------------------------
	
	public Action(){}

	public Action(Script script){
		this.script = script;
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public StringBuilder getJavaCode(){
		
		StringBuilder codeBuilder = new StringBuilder(ActionTestScript.JAVA_EXECUTE_FUNCTION_NAME);
		codeBuilder.append("(")
		.append(getLine())
		.append(",")
		.append("new ")
		.append(this.getClass().getSimpleName()).append("(this, ");
		
		return codeBuilder;
	}
	
	public boolean isScriptComment() {
		return false;
	}
	
	public ArrayList<String> getKeywords() { return new ArrayList<String>(); }

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	public boolean execute(ActionTestScript ts, String testName, int line){
		
		currentChannel = ts.getCurrentChannel();
		status = currentChannel.newActionStatus(testName, line);
		currentChannel.startHarAction(this, status.getTestLine());
		
		ts.getRecorder().createVisualAction(this, testName, line);
		
		return true;
	}
	
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		if(!status.isPassed()) {
			data = new JsonObject();
			data.addProperty("status", "non blocking action");
			data.addProperty("message", status.getFailMessage());
		}
		data.addProperty("passed", status.isPassed());
		data.addProperty("duration", status.getDuration());
		return new StringBuilder(getClass().getSimpleName()).append(" (").append(scriptName).append(":").append(scriptLine).append(") -> ").append(data.toString());
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public ActionStatus getStatus() {
		return status;
	}

	public void setStatus(ActionStatus status) {
		this.status = status;
	}
	
	protected Channel getCurrentChannel() {
		return currentChannel;
	}
}