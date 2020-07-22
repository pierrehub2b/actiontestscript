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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;

import com.ats.script.actions.ActionGesturePress;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.ats.driver.ApplicationProperties;
import com.ats.element.AtsBaseElement;
import com.ats.element.DialogBox;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.webservices.ApiExecutor;
import com.ats.executor.drivers.engines.webservices.RestApiExecutor;
import com.ats.executor.drivers.engines.webservices.SoapApiExecutor;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.graphic.TemplateMatchingSimple;
import com.ats.script.ProjectData;
import com.ats.script.actions.ActionApi;
import com.ats.script.actions.ActionChannelStart;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

public class ApiDriverEngine extends DriverEngine implements IDriverEngine{

	private final static String API = "API";
	private ApiExecutor executor;
	
	private PrintStream logStream;

	public ApiDriverEngine(Channel channel, ActionStatus status, String path, DesktopDriver desktopDriver, ApplicationProperties props) {

		super(channel, desktopDriver, props, 0, 0);

		try {
			
			final File logsFolder = new File(ProjectData.TARGET_FOLDER + File.separator + ProjectData.LOGS_FOLDER);
			if(!logsFolder.exists()) {
				logsFolder.mkdirs();
			}
			
			final Path logFile = logsFolder.toPath().resolve("ws_" + System.currentTimeMillis() + ".log");
			logStream = new PrintStream(logFile.toFile());
			logStream.println("Start ATS ws channel ...");
			
		} catch (FileNotFoundException e1) {}

		final int maxTry = ChannelManager.ATS.getMaxTryWebservice();
		final int timeout = ChannelManager.ATS.getWebServiceTimeOut();

		final Builder builder = createHttpBuilder(
				timeout, 
				logStream,
				channel.getTopScriptPackage(),
				getClass().getClassLoader());

		if(channel.getPerformance() == ActionChannelStart.NEOLOAD) {
			channel.setNeoloadDesignApi(ChannelManager.ATS.getNeoloadDesignApi());
			builder.proxy(ChannelManager.ATS.getNeoloadProxy().getHttpProxy());
		}else {
			builder.proxy(ChannelManager.ATS.getProxy().getHttpProxy());
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
			response.close();

			if(wsContent.endsWith("definitions>")) {
				try {
					executor = new SoapApiExecutor(logStream, client, timeout, maxTry, channel, wsContent, applicationPath);
					channel.setApplicationData(API, ActionApi.SOAP, ((SoapApiExecutor)executor).getOperations());
				} catch (SAXException | IOException | ParserConfigurationException e) {
					status.setError(ActionStatus.CHANNEL_START_ERROR, e.getMessage());
				}
			}else {
				channel.setApplicationData(API, ActionApi.REST);
				executor = new RestApiExecutor(logStream, client, timeout, maxTry, channel, applicationPath);
			}

		} catch (IOException e) {
			status.setError(ActionStatus.CHANNEL_START_ERROR, "service is not responding -> " + e.getMessage());
			e.printStackTrace(logStream);
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
	public List<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, String[] attributes, String[] attributesValues, Predicate<AtsBaseElement> searchPredicate, WebElement startElement, boolean waitAnimation) {
		return executor.findElements(channel, sysComp, testObject, tagName, attributes, searchPredicate);
	}

	@Override
	public List<FoundElement> findElements(TestElement parent, TemplateMatchingSimple template) {
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
	public void setWindowToFront() {
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {
	}

	@Override
	public void close(boolean keepRunning) {
		logStream.println("Close ATS WebService channel");
	}
	
	@Override
	public List<FoundElement> findSelectOptions(TestBound dimension, TestElement element) {
		return Collections.<FoundElement>emptyList();
	}
	
	@Override
	public void selectOptionsItem(ActionStatus status, TestElement element, CalculatedProperty selectProperty) {
	}

	@Override
	public void loadParents(FoundElement hoverElement) {
	}	

	@Override
	public WebElement getRootElement(Channel cnl) {
		return null;
	}

	//------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void waitAfterAction(ActionStatus status) {}

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
	public void setAttribute(String attributeName, String attributeValue) {
	
	}
	
	@Override
	public void switchWindow(ActionStatus status, int index, int tries) {
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
	public void clearText(ActionStatus status, TestElement testElement, MouseDirection md) {}

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
	public DialogBox switchToAlert() {
		return null;
	}

	@Override
	public boolean switchToDefaultContent() {return true;}

	@Override
	public void switchToFrameId(String id) {}

	@Override
	public void buttonClick(String id) {}
	
	@Override
	public void buttonClick(ArrayList<String> ids) {}
	
	@Override
	public void tap(int count, FoundElement element) {}
	
	@Override
	public void press(int duration, ArrayList<String> paths, FoundElement element) {}

	@Override
	public void windowState(ActionStatus status, Channel channel, String state) {}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, TestElement element) {
		return null;
	}

	@Override
	public Object executeJavaScript(ActionStatus status, String script, boolean returnValue) {
		return null;
	}
	
	@Override
	public String getTitle() {
		return "";
	}

	//------------------------------------------------------------------------------------------------------------------------------------
	// init http client
	//------------------------------------------------------------------------------------------------------------------------------------

	private static Builder createHttpBuilder(int timeout, PrintStream logStream, String packageName, ClassLoader classLoader){

		final Path certsFolderPath = Paths.get("").toAbsolutePath().resolve(ProjectData.SRC_FOLDER).resolve(ProjectData.ASSETS_FOLDER).resolve(ProjectData.CERTS_FOLDER);
		
		File certsFolder = certsFolderPath.toFile();
		if(!certsFolder.exists()) {
			final URL certsFolderUrl = classLoader.getResource(ProjectData.ASSETS_FOLDER + "/" + ProjectData.CERTS_FOLDER);
			if(certsFolderUrl != null) {
				certsFolder = new File(certsFolderUrl.getPath());
			}
		}
		
		File pfxFile = null;

		if(certsFolder.exists()) {
			File[] pfxFiles = certsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pfx"));
			if(pfxFiles.length > 0) {
				pfxFile = pfxFiles[0];
				logStream.println("Using pfx file -> " + pfxFile.getAbsolutePath());
			}
		}
		
		final Builder builder = new Builder()
				.connectTimeout(timeout, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS)
				.readTimeout(timeout, TimeUnit.SECONDS)
				.cache(null)
				.followRedirects(true)
				.followSslRedirects(true);

		final TrustManager [] trustManager = getTrustManager ();

		try {

			final SSLSocketFactory sslSocketFactory = getSslSocketFactory(pfxFile, trustManager, logStream);

			builder.sslSocketFactory (sslSocketFactory, (X509TrustManager)trustManager [0]);
			builder.hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
		} catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | IOException e) {
			e.printStackTrace(logStream);
		}

		return builder;
	}

	private static SSLSocketFactory getSslSocketFactory(File pfxFile, TrustManager [] trustManager, PrintStream errorStream) throws KeyManagementException, NoSuchAlgorithmException, CertificateException, IOException {
		final SSLContext sslContext = SSLContext.getInstance ("SSL");
		
		KeyManager[] keyManager = null;
		
		if(pfxFile != null && pfxFile.exists()) {
						
			final String password = "";
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			
			try {
				final KeyStore keyStore = KeyStore.getInstance("PKCS12");
				final InputStream keyInput = new FileInputStream(pfxFile);
				
				keyStore.load(keyInput, password.toCharArray());
				keyInput.close();
				
				keyManagerFactory.init(keyStore, password.toCharArray());
				
				keyManager = keyManagerFactory.getKeyManagers();
				
			} catch (KeyStoreException | UnrecoverableKeyException e) {
				e.printStackTrace(errorStream);
			}

		}else {
			errorStream.println("No pfx files found in the project");
		}
		
		sslContext.init (keyManager, trustManager, new SecureRandom ());
		return sslContext.getSocketFactory();
	}

	private static TrustManager[] getTrustManager () {
		final X509TrustManager manager = new X509TrustManager () {

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

		return new TrustManager [] { manager };
	}

	@Override
	protected void setPosition(Point pt) {
	}

	@Override
	protected void setSize(Dimension dim) {
	}

	@Override
	public List<String[]> loadSelectOptions(TestElement element) {
		return Collections.<String[]>emptyList();
	}

	@Override
	public int getNumWindows() {
		return 0;
	}
}