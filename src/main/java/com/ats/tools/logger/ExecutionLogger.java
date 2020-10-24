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

import com.ats.script.actions.Action;
import com.ats.tools.logger.levels.*;
import org.testng.Reporter;

import java.io.PrintStream;

public class ExecutionLogger {
	
	private final static String ERROR_LEVEL = "error";
	private final static String ALL_LEVEL = "all";
	private final static String WARNING_LEVEL = "warning";
	private final static String INFO_LEVEL = "info";
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";

	private AtsLogger levelLogger;

	public ExecutionLogger() {
		levelLogger = new AtsLogger();
	}
	
	public ExecutionLogger(PrintStream sysout) {
		this(sysout, INFO_LEVEL);
	}

	public ExecutionLogger(PrintStream sysout, String verbose) {

		if(ERROR_LEVEL.equalsIgnoreCase(verbose)) {
			levelLogger = new ErrorLevelLogger(sysout, "Error");
		}else if(INFO_LEVEL.equalsIgnoreCase(verbose)) {
			levelLogger = new InfoLevelLogger(sysout, "Error + Info");
		}else if(WARNING_LEVEL.equalsIgnoreCase(verbose)) {
			levelLogger = new WarningLevelLogger(sysout, "Error + Info + Warning");
		}else if(ALL_LEVEL.equalsIgnoreCase(verbose)) {
			levelLogger = new FullLevelLogger(sysout, "Error + Info + Warning + Details");
		}else {
			levelLogger = new AtsLogger(sysout, "Disabled");
		}
	}

	public void sendLog(int code, String message, Object value) {
		if(code < 100 ) {
			sendInfo(message, value.toString());
		}else if (code < 399){
			sendWarning(message, value.toString());
		}else {
			sendError(message, value.toString());
		}
	}
	
	public void sendExecLog(String type, String message) {
		levelLogger.log(type, message);
		Reporter.log("- " + message);
	}
	
	public void sendAction(Action action, String testName, int line) {
		levelLogger.action(action, testName, line);
	}
	
	public void sendWarning(String message, String value) {
		levelLogger.warning(message + " -> " + value);
	}

	public void sendInfo(String message, String value) {
		levelLogger.info(message + " -> " + value);
	}

	public void sendError(String message, String value) {
		levelLogger.error(message + " -> " + value);
	}
}