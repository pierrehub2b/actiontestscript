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

public class ActionChannelClose extends ActionChannel {

	public static final String SCRIPT_CLOSE_LABEL = SCRIPT_LABEL + "close";
	
	public ActionChannelClose() {}

	public ActionChannelClose(Script script) {
		super(script, "");
	}
	
	public ActionChannelClose(Script script, String name) {
		super(script, name);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		ts.updateVisualValue(getName());
		
		status.resetDuration();
		ts.closeChannel(status, getName());
		status.updateDuration();
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		if(getName().length() > 0) {
			return super.getJavaCode() + "\"" + getName() + "\")";
		}
		return "new " + this.getClass().getSimpleName() + "(this)";
	}
}