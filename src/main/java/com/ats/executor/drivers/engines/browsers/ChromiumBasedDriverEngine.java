package com.ats.executor.drivers.engines.browsers;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ChromiumBasedDriverEngine extends WebDriverEngine {

	protected String profileFolder = null;

	public ChromiumBasedDriverEngine(Channel channel, ActionStatus status, String browser, DriverProcess driverProcess, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, browser, driverProcess, desktopDriver, props);
	}

	protected ChromeOptions initOptions(ApplicationProperties props, String browserName) {

		final ChromeOptions options = new ChromeOptions();
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
		options.addArguments("--disable-popup-blocking");
		
		options.addArguments("--ignore-certificate-errors");
		
		if(props.getOptions() != null) {
			for (String s: props.getOptions()) {
				if(s.length() > 0) {
					options.addArguments(s);
				}
			}
		}

		profileFolder = props.getUserDataDir();
		if(profileFolder != null) {
			removeMetricsData();
			options.addArguments("--user-data-dir=" + profileFolder);
		}

		if(lang != null) {
			options.addArguments("--lang=" + lang);
		}

		if(applicationPath != null) {
			final File browserBinaryFile = new File(applicationPath);
			if(browserBinaryFile.exists()) {
				options.setBinary(browserBinaryFile.getAbsolutePath());
			}
		}

		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

		final Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		options.setExperimentalOption("prefs", prefs);

		return options;
	}

	private void removeMetricsData() {

		final Path atsProfilePath = Paths.get(profileFolder);
		if(atsProfilePath.toFile().exists()) {
			final Path localStatePath = atsProfilePath.resolve("Local State");
			if(localStatePath.toFile().exists()) {
				try {
					final JsonObject localStateObject = JsonParser.parseString(new String(Files.readAllBytes(localStatePath))).getAsJsonObject();
					final JsonObject metrics = localStateObject.get("user_experience_metrics").getAsJsonObject();
					if(metrics != null) {
						final JsonObject stability = metrics.get("stability").getAsJsonObject();
						if(stability != null) {
							if(!stability.get("exited_cleanly").getAsBoolean()) {
								stability.remove("exited_cleanly");
								stability.addProperty("exited_cleanly", true);

								metrics.remove("stability");
								metrics.add("stability", stability);

								localStateObject.remove("user_experience_metrics");
								localStateObject.add("user_experience_metrics", metrics);

								final FileWriter fileWriter = new FileWriter(localStatePath.toFile());
								new Gson().toJson(localStateObject, fileWriter);
								fileWriter.close();
							}
						}
					}
				} catch (Exception e) {	}
			}

			final Path preferencesPath = atsProfilePath.resolve("Default").resolve("Preferences");
			if(preferencesPath.toFile().exists()) {
				try {
					final JsonObject PreferencesObject = JsonParser.parseString(new String(Files.readAllBytes(preferencesPath))).getAsJsonObject();

					final JsonObject profile = PreferencesObject.get("profile").getAsJsonObject();
					if(profile != null) {

						final JsonElement exitType = profile.get("exit_type");
						
						if(exitType != null) {
							profile.remove("exit_type");
						}
						
						profile.addProperty("exit_type", "Normal");

						PreferencesObject.remove("profile");
						PreferencesObject.add("profile", profile);

						final FileWriter fileWriter = new FileWriter(preferencesPath.toFile());
						new Gson().toJson(PreferencesObject, fileWriter);
						fileWriter.close();
						
					}
				} catch (Exception e) {	}
			}
		}
	}
}