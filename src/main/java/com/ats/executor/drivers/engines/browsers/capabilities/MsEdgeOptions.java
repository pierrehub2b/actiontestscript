package com.ats.executor.drivers.engines.browsers.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openqa.selenium.MutableCapabilities;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("serial")
public class MsEdgeOptions extends MutableCapabilities {

	private static final String CAPABILITY = "ms:edgeOptions";
	
	private boolean headless = false;

	public boolean isHeadless() {
		return headless;
	}

	private String binary;
	private List<String> args = new ArrayList<>();
	private Map<String, Object> experimentalOptions = new HashMap<>();

	public MsEdgeOptions(ApplicationProperties props, String userDataPath) {

		final Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		
		final Map<String, Object> profile = new HashMap<>();
		profile.put("password_manager_enabled", false);
		
		prefs.put("profile", profile);

		experimentalOptions.put("excludeSwitches", Collections.singletonList("enable-automation"));
		experimentalOptions.put("useAutomationExtension", false);
		experimentalOptions.put("prefs", prefs);

		args.add("--enable-automation");

		if(props != null) {
			binary = props.getUri();
			if(userDataPath != null) {
				args.add("user-data-dir=" + userDataPath);
			}

			if(props.getOptions() != null) {
				for (String opt : props.getOptions()){
					if(opt.length() > 0) {
						if(opt.contains(WebDriverEngine.INCOGNITO_OPTION) || opt.contains(WebDriverEngine.PRIVATE_OPTION)) {
							args.add("--incognito");
						}else if(opt.contains(WebDriverEngine.HEADLESS_OPTION)) {
							this.headless = true;
							args.add("--headless");
						}else {
							args.add(opt);
						}
					}
				}
			}
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