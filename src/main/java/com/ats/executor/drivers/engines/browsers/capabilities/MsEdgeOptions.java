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

package com.ats.executor.drivers.engines.browsers.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openqa.selenium.MutableCapabilities;

import com.ats.executor.drivers.engines.browsers.BrowserArgumentsParser;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class MsEdgeOptions extends MutableCapabilities {

	private static final String CAPABILITY = "ms:edgeOptions";

	private String binary;
	private List<String> args = new ArrayList<>();
	private Map<String, Object> experimentalOptions = new HashMap<>();

	public MsEdgeOptions(BrowserArgumentsParser browserArgs) {
		final Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		
		final Map<String, Object> profile = new HashMap<>();
		profile.put("password_manager_enabled", false);
		
		prefs.put("profile", profile);

		experimentalOptions.put("excludeSwitches", Collections.singletonList("enable-automation"));
		experimentalOptions.put("useAutomationExtension", false);
		experimentalOptions.put("prefs", prefs);

		args.add("--enable-automation");
		
		binary = browserArgs.getBinaryPath();
		
		if(browserArgs.isHeadless()) {
			args.add("--headless");
		}
		
		if(browserArgs.isIncognito()) {
			args.add("--inprivate");
		}
		
		if(browserArgs.getUserDataPath() != null) {
			args.add("user-data-dir=" + browserArgs.getUserDataPath());
		}
		
		for(String opt : browserArgs.getMoreOptions()) {
			args.add(opt);
		}
	}

	@Override
	public Map<String, Object> asMap() {
		Map<String, Object> toReturn = new TreeMap<>(super.asMap());

		Map<String, Object> options = new TreeMap<>();
		experimentalOptions.forEach(options::put);

		if (binary != null) {
			options.put("binary", binary);
		}

		options.put("args", ImmutableList.copyOf(args));
		options.put("extensions", Collections.EMPTY_LIST);

		toReturn.put(CAPABILITY, options);

		return Collections.unmodifiableMap(toReturn);
	}
}