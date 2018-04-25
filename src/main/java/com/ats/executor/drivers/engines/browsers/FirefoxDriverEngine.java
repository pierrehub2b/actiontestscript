package com.ats.executor.drivers.engines.browsers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.driver.AtsManager;
import com.ats.driver.BrowserProperties;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.DriverProcess;
import com.ats.executor.drivers.WindowsDesktopDriver;
import com.ats.executor.drivers.engines.WebDriverEngine;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirectionData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FirefoxDriverEngine extends WebDriverEngine {

	private int waitAfterAction = 300;

	private final String WEB_ELEMENT_REF = "element-6066-11e4-a52e-4f735466cecf";
	private java.net.URI driverSessionUri;
	private RequestConfig requestConfig;

	public FirefoxDriverEngine(Channel channel, DriverProcess driverProcess, WindowsDesktopDriver windowsDriver, AtsManager ats) {
		super(channel, DriverManager.FIREFOX_BROWSER, driverProcess, windowsDriver, ats);

		initElementY = 5.0;

		DesiredCapabilities cap = new DesiredCapabilities();

		FirefoxOptions options = new FirefoxOptions();
		options.setCapability(FirefoxDriver.MARIONETTE, true);
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

		BrowserProperties props = ats.getBrowserProperties(DriverManager.FIREFOX_BROWSER);
		if(props != null) {
			waitAfterAction = props.getWait();
			applicationPath = props.getPath();
			if(applicationPath != null) {
				options.setBinary(applicationPath);
			}
		}

		cap.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
		
		launchDriver(cap);

		requestConfig = RequestConfig.custom()
				.setConnectTimeout(20 * 1000)
				.setConnectionRequestTimeout(20 * 1000)
				.setSocketTimeout(20 * 1000).build();
		try {
			driverSessionUri = new URI(driverProcess.getDriverServerUrl() + "/session/" + driver.getSessionId().toString() + "/actions");
		} catch (URISyntaxException e) {}

	}
	
	@Override
	public void switchToDefaultframe() {
		String mainWindow = (String) driver.getWindowHandles().toArray()[0];
		driver.switchTo().window(mainWindow);
	}
	
	@Override
	public TestBound[] getDimensions() {
		TestBound[] dimension = super.getDimensions();
		dimension[1].setY(dimension[1].getY() + 2);
		return dimension;
	}

	@Override
	public void waitAfterAction() {
		channel.sleep(waitAfterAction);
		try {
			super.waitAfterAction();
		}catch(WebDriverException e) {}
	}

	@Override
	protected int getDirectionValue(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2) {
		int offset = super.getDirectionValue(value, direction, cart1, cart2);
		offset -= value/2;
		return offset;
	}

	@Override
	protected int getNoDirectionValue(int value) {
		return 0;
	}

	@Override
	protected Object runJavaScript(String javaScript, Object... params) {
		Object result = super.runJavaScript(javaScript, params);
		channel.sleep(waitAfterAction);
		return result;
	}

	@Override
	public void sendTextData(WebElement webElement, ArrayList<SendKeyData> textActionList) {
		for(SendKeyData sequence : textActionList) {
			webElement.sendKeys(sequence.getSequenceFirefox());
		}
	}

	@Override
	protected void move(WebElement element, int offsetX, int offsetY) {

		JsonObject postData = getElementAction((RemoteWebElement)element,offsetX, offsetY);
		StringEntity postDataEntity = null;
		try {
			postDataEntity = new StringEntity(postData.toString());
		} catch (UnsupportedEncodingException e) {
			return;
		}

		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpPost request = new HttpPost(driverSessionUri);
		request.addHeader("content-type", "application/json");

		try {

			request.setEntity(postDataEntity);
			httpClient.execute(request);

		} catch (SocketTimeoutException e) {

			closeDriver();
			throw new WebDriverException("Geckodriver hangup issue after mouse move action (try to raise up 'actionWait' value in ATS properties for firefox)");

		} catch (IOException e) {

		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {}
		}
	}

	private JsonObject getElementAction(RemoteWebElement elem, int offsetX, int offsetY) {

		String elemId = elem.getId();

		JsonObject origin = new JsonObject();
		origin.addProperty("ELEMENT", elemId);
		origin.addProperty(WEB_ELEMENT_REF, elemId);

		JsonObject action = new JsonObject();
		action.addProperty("duration", 200);
		action.addProperty("x", offsetX);
		action.addProperty("y", offsetY);
		action.addProperty("type", "pointerMove");
		action.add("origin", origin);

		JsonArray actionList = new JsonArray();
		actionList.add(action);

		JsonObject parameters = new JsonObject();
		parameters.addProperty("pointerType", "mouse");

		JsonObject actions = new JsonObject();
		actions.addProperty("id", "default mouse");
		actions.addProperty("type", "pointer");
		actions.add("parameters", parameters);

		actions.add("actions", actionList);

		JsonArray chainedAction = new JsonArray();
		chainedAction.add(actions);

		JsonObject postData = new JsonObject();
		postData.add("actions", chainedAction);

		return postData;
	}
}
