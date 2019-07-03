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

package com.ats.executor.drivers.engines;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.ats.driver.ApplicationProperties;
import com.ats.element.AtsBaseElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.webservices.ApiExecutor;
import com.ats.executor.drivers.engines.webservices.RestApiExecutor;
import com.ats.executor.drivers.engines.webservices.SoapApiExecutor;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.graphic.TemplateMatchingSimple;
import com.ats.script.actions.ActionApi;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

public class ApiDriverEngine extends DriverEngine implements IDriverEngine{

	private final static String API = "API";
	private ApiExecutor executor;

	public ApiDriverEngine(Channel channel, ActionStatus status, String path, DesktopDriver desktopDriver, ApplicationProperties props) {

		super(channel, desktopDriver, path, props, 0, 0);
		
		final File file = new File("ws_error.log");
		PrintStream errorStream = null;
		try {
			errorStream = new PrintStream(file);
		} catch (FileNotFoundException e1) {}
		

		final int maxTry = DriverManager.ATS.getMaxTryWebservice();
		final int timeout = DriverManager.ATS.getWebServiceTimeOut();

		final Builder builder = createHttpBuilder(timeout, errorStream);

		if(channel.isNeoload()) {
			channel.setNeoloadDesignApi(DriverManager.ATS.getNeoloadDesignApi());
			builder.proxy(DriverManager.ATS.getNeoloadProxy().getHttpProxy());
		}else {
			builder.proxy(DriverManager.ATS.getProxy().getHttpProxy());
		}

		final OkHttpClient client = builder.build();

		if(applicationPath == null) {
			applicationPath = path;
		}

		final Request request = new Request.Builder().url(applicationPath).get().build();

		String wsContent = null;
		try {

			final Response response = client.newCall(request).execute();
			wsContent = CharStreams.toString(new InputStreamReader(response.body().byteStream(), Charsets.UTF_8)).trim();

			if(wsContent.endsWith("definitions>")) {
				try {
					executor = new SoapApiExecutor(client, timeout, maxTry, channel, wsContent, applicationPath);
					channel.setApplicationData(API, ActionApi.SOAP, ((SoapApiExecutor)executor).getOperations());
				} catch (SAXException | IOException | ParserConfigurationException e) {
					status.setCode(ActionStatus.CHANNEL_START_ERROR);
					status.setMessage(e.getMessage());
					status.setPassed(false);
				}
			}else {
				channel.setApplicationData(API, ActionApi.REST);
				executor = new RestApiExecutor(client, timeout, maxTry, channel, applicationPath);
			}

		} catch (IOException e) {
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			status.setMessage("Service is not responding -> " + e.getMessage());
			status.setPassed(false);
			
			e.printStackTrace(errorStream);
		}
	}

	@Override
	public void api(ActionStatus status, ActionApi api) {
		executor.execute(status, api);
	}

	@Override
	public String getSource() {
		return executor.getSource();
	}

	@Override
	public ArrayList<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> searchPredicate) {
		return executor.findElements(channel, sysComp, testObject, tagName, attributes, searchPredicate);
	}

	@Override
	public ArrayList<FoundElement> findElements(TestElement parent, TemplateMatchingSimple template) {
		return null;
	}

	@Override
	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry) {
		return executor.getElementAttribute(element.getId(), attributeName, maxTry);
	}

	@Override
	public CalculatedProperty[] getAttributes(FoundElement element, boolean reload) {
		return executor.getElementAttributes(element.getId());
	}

	@Override
	public void refreshElementMapLocation() {}

	@Override
	public boolean setWindowToFront() {
		return false;
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {
	}

	@Override
	public void close() {
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		return new CalculatedProperty[0];
	}

	@Override
	public void loadParents(FoundElement hoverElement) {
	}	

	@Override
	public WebElement getRootElement() {
		return null;
	}

	//------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void waitAfterAction() {}

	@Override
	public void updateDimensions() {}

	@Override
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y) {
		return null;
	}

	@Override
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h) {
		return null;
	}

	@Override
	public void switchWindow(ActionStatus status, int index) {
	}

	@Override
	public void closeWindow(ActionStatus status) {
	}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		return null;
	}

	@Override
	public void scroll(FoundElement element) {}

	@Override
	public void scroll(int value) {}

	@Override
	public void scroll(FoundElement element, int delta) {}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {}

	@Override
	public void clearText(ActionStatus status, FoundElement foundElement) {}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY) {}

	@Override
	public void keyDown(Keys key) {}

	@Override
	public void keyUp(Keys key) {}

	@Override
	public void drop(MouseDirection md, boolean desktopDriver) {}

	@Override
	public void moveByOffset(int hDirection, int vDirection) {}

	@Override
	public void doubleClick() {}

	@Override
	public void rightClick() {}

	@Override
	public Alert switchToAlert() {
		return null;
	}

	@Override
	public boolean switchToDefaultContent() {return true;}

	@Override
	public void switchToFrameId(String id) {}

	@Override
	public void buttonClick(String id) {}

	@Override
	public void windowState(ActionStatus status, Channel channel, String state) {
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, TestElement element) {
		return null;
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script) {
		return null;
	}

	//------------------------------------------------------------------------------------------------------------------------------------
	// init http client
	//------------------------------------------------------------------------------------------------------------------------------------

	private static Builder createHttpBuilder(int timeout, PrintStream errorStream){

		final Builder builder = new Builder()
				.connectTimeout(timeout, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS)
				.readTimeout(timeout, TimeUnit.SECONDS)
				.cache(null)
				.followRedirects(true)
				.followSslRedirects(true);

		final TrustManager [] trustAllCerts = new TrustManager [] { trustManager () };

		try {
			final SSLContext sslContext = SSLContext.getInstance ("SSL");
			sslContext.init (null, trustAllCerts, new SecureRandom ());

			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory ();

			builder.sslSocketFactory (sslSocketFactory, (X509TrustManager)trustAllCerts [0]);
			builder.hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace(errorStream);
		}

		return builder;
	}

	private static TrustManager trustManager () {
		return new X509TrustManager () {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[]{};
			}
		};
	}
}