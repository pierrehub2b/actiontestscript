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
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionNeoloadStart extends ActionNeoloadRun {

	public static final String SCRIPT_LABEL = SCRIPT_NEOLOAD_LABEL + "-start";
	
	private static final String API_SERVICE_NAME = "StartRecording";

	private static final String INIT = "init";
	
	private boolean init = false;

	public ActionNeoloadStart() {}
	
	public ActionNeoloadStart(ScriptLoader script, String options, String userData) {
		super(script, options, userData);
		updateInit(options);
	}
	
	public ActionNeoloadStart(Script script, String options, CalculatedValue user) {
		super(script, options, user);
		updateInit(options);
	}
	
	private void updateInit(String options) {
		setInit(options.contains(INIT));
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		if(init) {
			setOptions(INIT);
		}else {
			setOptions("");
		}
		
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void executeRequest(Channel channel, String designApiUrl) {
		super.executeRequest(channel, designApiUrl + API_SERVICE_NAME);
	    if(postDesignData(new StartUser(getUser(), init))) {
	    	channel.setStopNeoloadRecord(new ActionNeoloadStop());
	    }
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public boolean isInit() {
		return init;
	}

	public void setInit(boolean init) {
		this.init = init;
	}
	
	//--------------------------------------------------------
	// Json serialization
	//--------------------------------------------------------
	
	@SuppressWarnings("unused")
	private class StartUser{
		public String VirtualUser = null;
		public String BaseContainer = "Actions";
		public boolean ProtocolWebSocket = true;
		public boolean ProtocolAdobeRTMP = false;
		public String UserAgent = null;
		
		public StartUser(CalculatedValue user, boolean init) {
			if(user != null) {
				this.VirtualUser = user.getCalculated();
			}
			if(init) {
				this.BaseContainer = "Init";
			}
		}
	}
}
