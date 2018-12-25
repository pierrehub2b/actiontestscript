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
import com.ats.script.Script;

public class ActionWindowClose extends ActionWindow {

	public static final String SCRIPT_CLOSE_LABEL = SCRIPT_LABEL + "close";

	public ActionWindowClose() {}

	public ActionWindowClose(Script script, int num) {
		super(script, num);
	}

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + getNum() + ")";
	}

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		final Channel channel = ts.getCurrentChannel();
		
		if(channel != null){
			channel.closeWindow(status, getNum());
			status.setPassed(true);
		}
		
		status.endDuration();
		ts.getRecorder().updateScreen(0, status.getDuration());
	}
}