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

public class ActionNeoloadRecord extends ActionNeoload {

	public static final String SCRIPT_LABEL = SCRIPT_NEOLOAD_LABEL + "-record";
	
	public static final String PAUSE = "pause";
	public static final String RESUME = "resume";
	public static final String SCREENSHOT = "screenshot";
	
	private String type = PAUSE;
	
	private static final String API_SERVICE_RESUME = "ResumeRecording";
	private static final String API_SERVICE_PAUSE = "PauseRecording";
	
	public ActionNeoloadRecord() {}
	
	public ActionNeoloadRecord(Script script, String type) {
		super(script);
		setType(type);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(getRecordType()).append(")");
		return codeBuilder;
	}
	
	private String getRecordType() {
		if(RESUME.equals(type) || SCREENSHOT.equals(type)) {
			return this.getClass().getSimpleName() + "." + type.toUpperCase();
		}
		return this.getClass().getSimpleName() + "." + PAUSE.toUpperCase();
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void executeRequest(Channel channel, String designApiUrl) {
		super.executeRequest(channel, designApiUrl + getServiceName());
		postData("{\"d\": {}}");
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	private String getServiceName() {
		if(RESUME.equals(type)){
			return API_SERVICE_RESUME;
		}else {
			return API_SERVICE_PAUSE;
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if(RESUME.equals(type) || SCREENSHOT.equals(type)) {
			this.type = type;
		}else {
			this.type = PAUSE;
		}
	}
}
