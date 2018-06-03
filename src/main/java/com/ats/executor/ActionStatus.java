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
	public static final int WINDOW_NO_SWITCH = -14;
	public static final int WINDOW_INDEX_OUT = -15;
	public static final int WEB_DRIVER_ERROR = -16;
	public static final int DRIVER_NOT_REACHABLE = -17;
	public static final int JAVA_EXCEPTION = -18;
	public static final int CHANNEL_START_ERROR = -19;

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

	public ActionStatus(Channel channel) {
		this.channel = channel;
		this.startedAt = System.currentTimeMillis();
	}

	public void updateDuration() {
		duration = System.currentTimeMillis() - startedAt;
	}

	public void updateDuration(long currentTime) {
		duration += System.currentTimeMillis() - currentTime;
	}

	public void resetDuration() {
		duration = System.currentTimeMillis();
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

	public String getFailMessage() {
		return message + " after " + duration + " ms";
	}

	public String getChannelInfo() {
		return "   - Channel : " + channel.getName() + "\n   - Application : " + channel.getApplication();
	}
}