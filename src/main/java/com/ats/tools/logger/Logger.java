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

package com.ats.tools.logger;

import java.io.PrintStream;

import com.ats.executor.channels.Channel;

public class Logger {

	private final static String NO_CHANNEL = "NO_CHANNEL";

	private PrintStream printOut;
	private String channelName = NO_CHANNEL;

	public Logger() {
		this(0);
	}
	
	public Logger(int level) {
		if(level > 1) {
			this.printOut = new PrintStream(System.out);
		}else {
			this.printOut = new NullPrintStream();
		}
	}

	public void setChannel(Channel channel) {
		if(channel == null) {
			this.channelName = NO_CHANNEL;
		}else {
			this.channelName = channel.getName();
		}
	}

	public void setChannelName(String name) {
		this.channelName = name;
	}

	public void sendLog(int code, String message, Object value) {

		String data = value.toString();
		if(data.length() > 0) {
			data = " -> " + data;
		}

		if(code < 100 ) {
			sendInfo(message, data);
		}else if (code < 399){
			sendWarning(message, data);
		}else {
			sendError(message, data);
		}
	}

	public void sendInfo(String message, String value) {
		print("INFO", message + value);
	}

	public void sendWarning(String message, String value) {
		print("WARNING", message + value);
	}

	public void sendError(String message, String value) {
		print("ERROR",  message + value);
	}

	private void print(String type, String data) {
		printOut.println("[ATS-" + type + "] | " + "channel '" + channelName + "' | " + data);
	}
}