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

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StopExecutionThread extends Thread{

	private ActionTestScript actionTestScript;
	private Scanner scanner;

	public StopExecutionThread(ActionTestScript script, InputStream in) {
		super("ats-stop-execution");
		this.actionTestScript = script;
		this.setDaemon(true);
		this.scanner = new Scanner(in);
	}

	@Override
	public void run() {
		while (true) {
			try {
				String input = scanner.nextLine();
				if ("q".equals(input)) {
					actionTestScript.cleanup();
					break;
				}
			}catch(NoSuchElementException e) {

			}
		}
		scanner.close();
		System.exit(0);
	}
}