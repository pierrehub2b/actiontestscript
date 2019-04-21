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

package com.ats.script.actions.neoload;

import com.ats.executor.channels.Channel;
import com.ats.script.Script;

public class ActionNeoloadContainer extends ActionNeoload {

	public static final String SCRIPT_LABEL = SCRIPT_NEOLOAD_LABEL + "-container";

	private static final String API_SERVICE_NAME = "SetContainer";

	public static final String INIT = "Init";
	public static final String ACTIONS = "Actions";
	public static final String END = "End";

	private String name = ACTIONS;

	public ActionNeoloadContainer() {}

	public ActionNeoloadContainer(Script script, String name) {
		super(script);
		setName(name);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append("\"").append(name).append("\")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void executeRequest(Channel channel, String designApiUrl) {
		super.executeRequest(channel, designApiUrl + API_SERVICE_NAME);
		postDesignData(new Container(this));
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

	//--------------------------------------------------------
	// Json serialization
	//--------------------------------------------------------

	@SuppressWarnings("unused")
	private class Container{
		public String Name = null;
		public Container(ActionNeoloadContainer action) {
			this.Name = action.getName();
		}
	}
}