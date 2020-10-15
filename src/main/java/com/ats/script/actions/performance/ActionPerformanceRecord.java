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

package com.ats.script.actions.performance;

import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;

public class ActionPerformanceRecord extends ActionPerformance {

	public static final String SCRIPT_LABEL = ActionPerformance.SCRIPT_PERFORMANCE_LABEL + "-record";
	
	public static final String PAUSE = "pause";
	public static final String RESUME = "resume";

	private String type = PAUSE;

	public ActionPerformanceRecord() {}

	public ActionPerformanceRecord(Script script, String type) {
		super(script);
		setType(type);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append("\"").append(type).append("\"").append(")");
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		final Channel channel = ts.getCurrentChannel();
		setStatus(channel.newActionStatus(testName, testLine));
		
		if(PAUSE.equals(type)) {
			channel.pauseHarRecord();
		}else {
			channel.resumeHarRecord();
		}
		
		status.endDuration();
		return true;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if(RESUME.equals(type)) {
			this.type = type;
		}else {
			this.type = PAUSE;
		}
	}
}