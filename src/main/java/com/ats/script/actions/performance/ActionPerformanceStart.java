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
import com.ats.script.Script;

public class ActionPerformanceStart extends ActionPerformance {

	public static final String SCRIPT_LABEL = ActionPerformance.SCRIPT_PERFORMANCE_LABEL + "-start";
	
	private String name = "";
	private String version = "";
	private String comment = "";
	
	public ActionPerformanceStart() {}

	public ActionPerformanceStart(Script script) {
		super(script);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}