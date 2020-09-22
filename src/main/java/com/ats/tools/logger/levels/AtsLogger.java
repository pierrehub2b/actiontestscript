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

package com.ats.tools.logger.levels;

import com.ats.script.actions.Action;
import com.ats.tools.logger.NullPrintStream;

import java.io.PrintStream;

public class AtsLogger {

	private final static String LABEL = "LOGGER";
	public final static String FAILED = "FAILED";
	
	public static String getAtsFailed() {
		return getAtsLogsPrefix(FAILED).toString();
	}
	
	private static StringBuilder getAtsLogsPrefix(String type) {
		final StringBuilder sb = 
				new StringBuilder("[")
				.append("ATS-")
				.append(type)
				.append("] ");
		return sb;
	}
	
	protected PrintStream out;

	public AtsLogger() {
		this.out = new NullPrintStream();
	}

	public AtsLogger(PrintStream out, String level) {
		this.out = out;
		print(LABEL, new StringBuilder("Logger level -> ").append(level));
	}

	public void log(String type, String message) {
		print(type, new StringBuilder(message));
	}

	protected void print(String type, StringBuilder data) {
		final StringBuilder sb = 
				getAtsLogsPrefix(type)
				.append(data);
		out.println(sb.toString());
	}

	public void warning(String message) {}
	public void info(String message) {}
	public void error(String message) {}

	public void action(Action action, String testName, int line) {}
}