package com.ats.executor.drivers.engines.browsers;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.desktop.DesktopWindow;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.executor.drivers.engines.desktop.DesktopDriverEngine;
import com.ats.tools.Utils;

public class JxDriverEngine extends WebDriverEngine {

	private final static int DEFAULT_WAIT = 150;
	private final static int DEFAULT_PROPERTY_WAIT = 200;

	private Process process;
	private ChromeDriverService service;

	public JxDriverEngine(Channel channel, ActionStatus status, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, desktopDriver, props.getUri(), props, DEFAULT_WAIT, DEFAULT_PROPERTY_WAIT);

		final ProcessBuilder builder = new ProcessBuilder(getApplicationPath());
		builder.redirectErrorStream(true);
		builder.redirectInput(Redirect.INHERIT);

		try {
			process = builder.start();
			Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
			status.setPassed(true);
		} catch (IOException e1) {
			status.setPassed(false);
			status.setMessage(e1.getMessage());
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
		}

		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File("C:\\Users\\agilitest\\.actiontestscript\\drivers\\jxdriver.exe"))
				.usingAnyFreePort()
				.build();


		try {
			service.start();

			DesiredCapabilities capabilities = new DesiredCapabilities();

			ChromeOptions options = new ChromeOptions();
			/*options.addArguments("--no-sandbox");
			options.addArguments("--no-default-browser-check");
			options.addArguments("--test-type");
			options.addArguments("--allow-file-access-from-files");
			options.addArguments("--allow-running-insecure-content");
			options.addArguments("--allow-file-access-from-files");
			options.addArguments("--allow-cross-origin-auth-prompt");
			options.addArguments("--allow-file-access");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--disable-extensions");
			options.addArguments("--disable-infobars");
			options.addArguments("--disable-notifications");
			options.addArguments("--disable-web-security");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--user-data-dir=" + Utils.createDriverFolder(DriverManager.CHROMIUM_BROWSER));

			if(lang != null) {
				options.addArguments("--lang=" + lang);
			}*/

			options.setExperimentalOption("debuggerAddress", "localhost:9222");
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);

			driver = new RemoteWebDriver(service.getUrl(), capabilities);


		} catch (Exception e) {
			close();
			e.printStackTrace();
		}
		
		
		if(driver != null) {
			status.setPassed(true);

			actions = new Actions(driver);

			driver.manage().timeouts().setScriptTimeout(50, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

			try{
				driver.manage().window().setSize(channel.getDimension().getSize());
				driver.manage().window().setPosition(channel.getDimension().getPoint());
			}catch(Exception ex){
				System.err.println(ex.getMessage());
			}

			String applicationVersion = null;
			String driverVersion = null;

			Map<String, ?> infos = driver.getCapabilities().asMap();
			for (Map.Entry<String, ?> entry : infos.entrySet()){
				if("browserVersion".equals(entry.getKey()) || "version".equals(entry.getKey())){
					applicationVersion = entry.getValue().toString();
				}else if("chrome".equals(entry.getKey())) {
					Map<String, String> chromeData = (Map<String, String>) entry.getValue();
					driverVersion = chromeData.get("chromedriverVersion");
					if(driverVersion != null) {
						driverVersion = driverVersion.replaceFirst("\\(.*\\)", "").trim();
					}
				}else if("moz:geckodriverVersion".equals(entry.getKey())) {
					driverVersion = entry.getValue().toString();
				}
			}

			final String titleUid = UUID.randomUUID().toString();
			try {
				final File tempHtml = File.createTempFile("ats_", ".html");
				tempHtml.deleteOnExit();

				Files.write(tempHtml.toPath(), Utils.getAtsBrowserContent(titleUid, channel.getApplication(), applicationPath, applicationVersion, driverVersion, channel.getDimension(), getActionWait(), getPropertyWait(), 20, 20, 20, 20, 20, getDesktopDriver()));
				driver.get(tempHtml.toURI().toString());
			} catch (IOException e) {}

			int maxTry = 10;
			while(maxTry > 0) {
				final DesktopWindow window = desktopDriver.getWindowByTitle(titleUid);
				if(window != null) {
					desktopDriver.setEngine(new DesktopDriverEngine(channel, window));
					channel.setApplicationData(
							"windows",
							applicationVersion,
							driverVersion,
							process.pid());
					maxTry = 0;
				}else {
					channel.sleep(300);
					maxTry--;
				}
			}
			

			requestConfig = RequestConfig.custom()
					.setConnectTimeout(5000)
					.setConnectionRequestTimeout(5000)
					.setSocketTimeout(10000).build();
			//try {
			//	driverSession = new URI(driverProcess.getDriverServerUrl() + "/session/" + driver.getSessionId().toString());
			//} catch (URISyntaxException e) {}

		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			//status.setMessage(errorMessage);

			//driverProcess.close();
		}

	}

	@Override
	public void close() {
		driver.close();
		process.destroyForcibly();
		service.stop();
		super.close();
	}
}
