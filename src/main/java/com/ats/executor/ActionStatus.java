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

package com.ats.executor;

import com.ats.element.TestElement;
import com.ats.executor.channels.Channel;

public class ActionStatus {

	public static final int OBJECT_NOT_FOUND = -1;
	public static final int OBJECT_NOT_VISIBLE = -2;
	public static final int OBJECT_NOT_INTERACTABLE = -3;
	public static final int ENTER_TEXT_FAIL = -4;
	public static final int ATTRIBUTE_NOT_SET = -5;
	public static final int ATTRIBUTE_CHECK_FAIL = -6;
	public static final int VALUES_COMPARE_FAIL = -7;
	public static final int OCCURRENCES_ERROR = -8;
	public static final int CHANNEL_NOT_FOUND = -9;	
	public static final int MALFORMED_GOTO_URL = -10;	
	public static final int UNREACHABLE_GOTO_URL = -11;
	public static final int UNKNOWN_HOST_GOTO_URL = -12;
	public static final int JAVASCRIPT_ERROR = -13;
	public static final int WINDOW_NOT_FOUND = -14;
	public static final int WINDOW_INDEX_OUT = -15;
	public static final int WEB_DRIVER_ERROR = -16;
	public static final int DRIVER_NOT_REACHABLE = -17;
	public static final int JAVA_EXCEPTION = -18;
	public static final int CHANNEL_START_ERROR = -19;
	public static final int FILE_NOT_FOUND = -20;
	public static final int PERF_NOT_STARTED = -21;
	public static final int PERF_NOT_RECORDING = -22;
	
	public static final int OCTOPERF_FILE_ERROR = -30;
	
	public static final int NEOLOAD_POST_ERROR = -50;
	
	public static final int SYS_BUTTON_ERROR = -60;
	
	private static final String ATS_TECHNICAL_ERROR = "AtsTechnicalError";
	private static final String ATS_FUNCTIONAL_ERROR = "AtsFunctionalError";

	private Channel channel;

	private long startedAt = 0;
	private long duration = 0;
	private long cpuUsage = 0;

	private long searchDuration = 0;
	private int occurences = 0;

	private boolean passed = true;
	private int code = 0;
	private String message = "";
	private TestElement element = null;
	private Object data = null;
	
	private String testLine;
			
	private String errorType = ATS_FUNCTIONAL_ERROR;

	public ActionStatus(Channel channel, String testName, int testLine) {
		this.channel = channel;
		this.startedAt = System.currentTimeMillis();
		this.testLine = testName + ":" + testLine;
	}
	
	public String getTestLine() {
		return testLine;
	}

	public void updateDuration(long currentTime) {
		duration += System.currentTimeMillis() - currentTime;
	}

	public void startDuration() {
		duration = System.currentTimeMillis();
	}
	
	public void endDuration() {
		duration = System.currentTimeMillis() - startedAt;
	}	
	
	public void endAction() {
		channel.endHarAction();
		endDuration();
	}
		
	public void setException(int code, Exception ex) {
		this.code = code;
		this.passed = false;
		this.errorType = ex.getClass().getName();
		this.message = ex.getMessage();
	}

	public void setTechnicalError(int code, String message) {
		this.passed = false;
		this.code = code;
		this.errorType = ATS_TECHNICAL_ERROR;
		this.message = message;
	}
	
	public String getErrorType() {
		return errorType;
	}
	
	public void setNoError() {
		setPassed(true);
		setCode(0);
		setMessage("");
	}
	
	public void setNoError(String message) {
		setPassed(true);
		setCode(0);
		setMessage(message);
		setData(null);
	}
	
	public void setError(int code, String message) {
		setPassed(false);
		setCode(code);
		setMessage(message);
	}
	
	public void setError(int code, String message, Object data) {
		setError(code, message);
		setData(data);
	}
			
	//--------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------
	
	public String getFailMessage() {
		return message + " after " + duration + " ms";
	}
	
	public String getChannelApplication() {
		if(channel == null) {
			return "";
		}
		return channel.getApplication();
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public TestElement getElement() {
		return element;
	}

	public void setElement(TestElement element) {
		this.element = element;
	}

	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSearchDuration() {
		return searchDuration;
	}

	public void setSearchDuration(long searchDuration) {
		this.searchDuration = searchDuration;
	}

	public int getOccurences() {
		return occurences;
	}

	public void setOccurences(int value) {
		this.occurences = value;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public long getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(long cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
}