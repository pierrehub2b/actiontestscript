package com.ats.executor.drivers.engines.browsers;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.tools.Utils;

public class ChromiumBasedDriverEngine extends WebDriverEngine {

	private String profileFolder = null;

	public ChromiumBasedDriverEngine(Channel channel, ActionStatus status, String browser, DriverProcess driverProcess, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, browser, driverProcess, desktopDriver, props);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--no-sandbox");
		options.addArguments("--no-default-browser-check");
		options.addArguments("--test-type");
		options.addArguments("--allow-file-access-from-files");
		options.addArguments("--allow-running-insecure-content");
		options.addArguments("--allow-file-access-from-files");
		options.addArguments("--allow-cross-origin-auth-prompt");
		options.addArguments("--allow-file-access");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-web-security");
		options.addArguments("--disable-dev-shm-usage");

		final String profilePath = checkProfileFolder(options, props, DriverManager.CHROMIUM_BROWSER);

		if(lang != null) {
			options.addArguments("--lang=" + lang);
		}

		if(applicationPath != null) {
			options.setBinary(applicationPath);
		}

		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);

		launchDriver(status, options, profilePath);
	}

	private String checkProfileFolder(ChromeOptions options, ApplicationProperties props, String browser) {

		String path = "";
		if(props.getUserDataDir() != null) {
			path = new File(props.getUserDataDir()).getAbsolutePath();
			profileFolder = path;
		}else {
			path = Utils.createDriverFolder(browser).getAbsolutePath();
		}
		
		options.addArguments("--user-data-dir=" + path);

		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		options.setExperimentalOption("prefs", prefs);
		
		return path;
	}

	@Override
	public void close() {
		if(profileFolder != null) {
			Arrays.asList(getWindowsHandle(0)).stream().sorted(Collections.reverseOrder()).forEach(s -> closeWindowHandler(s));

			ChromeOptions options = new ChromeOptions();
			options.addArguments("user-data-dir=" + profileFolder);     
			options.addArguments("--no-startup-window");
			try {
				new RemoteWebDriver(driverProcess.getDriverServerUrl(), options);
			}catch(Exception ex){   
			}
		}else {
			super.close();
		}
	}
}