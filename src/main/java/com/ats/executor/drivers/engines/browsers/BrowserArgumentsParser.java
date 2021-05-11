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

package com.ats.executor.drivers.engines.browsers;

import java.io.File;
import java.util.ArrayList;

import com.ats.driver.ApplicationProperties;
import com.ats.generator.variables.CalculatedValue;

public class BrowserArgumentsParser {

	private final static String INCOGNITO_OPTION = "incognito";
	private final static String PRIVATE_OPTION = "private";
	private final static String HEADLESS_OPTION = "headless";

	private String userDataPath;
	private String binaryPath;
	private boolean incognito = false;
	private boolean headless = false;

	private String[] moreOptions = null;

	public BrowserArgumentsParser(ArrayList<CalculatedValue> arguments, ApplicationProperties props, String browserName, String binary) {
		
		final ArrayList<String> args = new ArrayList<String>();
		
		for (CalculatedValue calcv : arguments) {
			final String arg = calcv.getCalculated();
			if(arg.startsWith("profile=") || arg.startsWith("profile:") || arg.startsWith("profile ")) {
				userDataPath = ApplicationProperties.getUserDataPath(arg.substring(8).trim(), browserName);
			}else if(arg.contains(HEADLESS_OPTION)) {
				headless = true;
			}else if(arg.contains(INCOGNITO_OPTION) || arg.contains(PRIVATE_OPTION)) {
				incognito = true;
			}else {
				args.add(arg);
			}
		}

		if(userDataPath == null) {
			userDataPath = props.getUserDataDirPath(browserName);
		}

		if(binary != null) {
			final File browserBinaryFile = new File(binary);
			if(browserBinaryFile.exists()) {
				binaryPath = browserBinaryFile.getAbsolutePath();
			}
		}

		if(props.getOptions() != null) {
			
			for (String s: props.getOptions()) {
				if(s.length() > 0) {
					if(INCOGNITO_OPTION.equals(s) || PRIVATE_OPTION.equals(s)) {
						incognito = true;
					}else if(s.contains(HEADLESS_OPTION)) {
						headless = true;
					}else if(s.length() > 0) {
						args.add(s);
					}
				}
			}
		}
		
		if(args.size() > 0) {
			moreOptions = args.toArray(new String[args.size()]);
		}
	}

	public String getBinaryPath() {
		return binaryPath;
	}

	public String[] getMoreOptions() {
		if(moreOptions == null) {
			return new String[0];
		}
		return moreOptions;
	}

	public String getUserDataPath() {
		return userDataPath;
	}

	public boolean isIncognito() {
		return incognito;
	}

	public boolean isHeadless() {
		return headless;
	}
}