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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionChannelStart extends ActionChannel {

	public static final String SCRIPT_START_LABEL = SCRIPT_LABEL + "start";

	private CalculatedValue application;

	public ActionChannelStart() {}
	
	public ActionChannelStart(Script script, String name, CalculatedValue value) {
		super(script, name);
		setApplication(value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts, Channel channel) {
		setStatus(new ActionStatus(channel));
		ts.getChannelManager().startChannel(status, this, getName(), application.getCalculated());
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + "\"" + getName() + "\", " + application.getJavaCode() + ")";
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getApplication() {
		return application;
	}

	public void setApplication(CalculatedValue value) {
		this.application = value;
	}
}