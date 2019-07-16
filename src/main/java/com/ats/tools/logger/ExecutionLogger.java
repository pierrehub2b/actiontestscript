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

public class ExecutionLogger implements IExecutionLogger {

	private final static String ERROR_LEVEL = "error";
	private final static String WARNING_LEVEL = "warning";
	private final static String INFO_LEVEL = "info";
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";

	private PrintStream printOut;

	private int level = 0;

	public ExecutionLogger() {
		this.printOut = new NullPrintStream();
	}

	public ExecutionLogger(PrintStream sysout, String verbose) {

		if(ERROR_LEVEL.equalsIgnoreCase(verbose)) {
			level = 1;
		}else if(INFO_LEVEL.equalsIgnoreCase(verbose)) {
			level = 2;
		}else if(WARNING_LEVEL.equalsIgnoreCase(verbose)) {
			level = 3;
		}		

		if(level > 0) {
			this.printOut = sysout;
			sysout.println("[ATS-INFO] log level -> " + verbose);
		}else {
			this.printOut = new NullPrintStream();
			sysout.println("[ATS-INFO] log disabled");
		}
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
	
	@Override
	public void sendWarning(String message, String value) {
		if(level >= 3) {
			print("WARNING", message + value);
		}
	}

	@Override
	public void sendInfo(String message, String value) {
		if(level >= 2) {
			print("INFO", message + value);
		}
	}

	@Override
	public void sendError(String message, String value) {
		if(level >= 1) {
			print("ERROR",  message + value);
		}
	}

	private void print(String type, String data) {
		printOut.println("[ATS-" + type + "] " + data);
	}
}