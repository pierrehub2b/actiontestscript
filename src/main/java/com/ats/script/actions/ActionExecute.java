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

import com.ats.script.Script;
import com.google.gson.JsonObject;

public abstract class ActionExecute extends Action {

	public static final String NO_FAIL_LABEL = "nofail";
	public static final String TEST_FAIL_LABEL = "testfail";
	
	public static final int TEST_STOP_FAIL = 0;
	public static final int TEST_CONTINUE_PASS = 1;
	public static final int TEST_CONTINUE_FAIL = 2;

	protected int stopPolicy = TEST_STOP_FAIL;

	public ActionExecute() {}

	public ActionExecute(Script script, int stop) {
		super(script);
		setStopPolicy(stop);
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("passed", status.isPassed());
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(stopPolicy).append(", ");
		return codeBuilder;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getStopPolicy() {
		return stopPolicy;
	}

	public void setStopPolicy(int value) {
		this.stopPolicy = value;
	}
}