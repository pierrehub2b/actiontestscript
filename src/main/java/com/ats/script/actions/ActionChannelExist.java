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
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.EmptyChannel;
import com.ats.script.Script;
import com.google.gson.JsonObject;

public class ActionChannelExist extends ActionChannel{

	public ActionChannelExist() {}

	public ActionChannelExist(Script script, String name) {
		super(script, name);
	}
	
	protected boolean channelEmpty = true;

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		final Channel channel = ts.getChannelManager().getChannel(getName());
		
		channelEmpty = (channel instanceof EmptyChannel);
		if(channelEmpty) {
			setStatus(channel.newActionStatus(testName, testLine));
		}else {
			super.execute(ts, testName, testLine);
			ts.getRecorder().update(getName());
		}
		
		return true;
	}
		
	@Override
	protected JsonObject getActionLogsData() {
		final JsonObject logs = super.getActionLogsData();
		if(channelEmpty) {
			logs.addProperty("warning", "channel does not exist");
		}
		return logs;
	}
}