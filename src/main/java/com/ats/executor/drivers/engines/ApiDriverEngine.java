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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.webservices.AbstractApiExecutor;
import com.ats.executor.drivers.engines.webservices.RestApiExecutor;
import com.ats.executor.drivers.engines.webservices.SoapApiExecutor;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

public class ApiDriverEngine extends DriverEngineAbstract implements IDriverEngine{

	private AbstractApiExecutor executor;

	public ApiDriverEngine(Channel channel, ActionStatus status, String path, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, desktopDriver, props);

		if(applicationPath == null) {
			applicationPath = path;
		}

		String wsContent = null;
		File wsContentFile = null;

		try {

			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(20000)
					.setConnectionRequestTimeout(10000)
					.setSocketTimeout(5000).build();

			final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
			final HttpResponse response = httpClient.execute(new HttpGet(applicationPath));

			if(response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {

				final BufferedInputStream buff = new BufferedInputStream(response.getEntity().getContent());
				wsContent = new String(ByteStreams.toByteArray(buff), Charsets.UTF_8).trim();
				buff.close();

				wsContentFile = File.createTempFile("atsWs_", ".txt");
				wsContentFile.deleteOnExit();

				final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wsContentFile), Charsets.UTF_8));
				writer.write(wsContent);
				writer.flush();
				writer.close();

			}else {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("Service not reachable");
				status.setPassed(false);
			}

		} catch (IOException e) {
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			status.setMessage(e.getMessage());
			status.setPassed(false);
		}

		if(wsContent != null) {
			if(wsContent.endsWith("definitions>")) {
				application = ActionApi.SOAP.toUpperCase();
				try {
					executor = new SoapApiExecutor(wsContentFile, applicationPath);
					channel.setApplicationData(application, ((SoapApiExecutor)executor).getOperations());
				} catch (SAXException | IOException | ParserConfigurationException e) {
					status.setCode(ActionStatus.CHANNEL_START_ERROR);
					status.setMessage(e.getMessage());
					status.setPassed(false);
				}
			}else {
				application = ActionApi.REST.toUpperCase();
				executor = new RestApiExecutor(applicationPath);
				channel.setApplicationData(application);
			}
		}else {
			status.setCode(ActionStatus.CHANNEL_START_ERROR);
			status.setMessage("service is not responding");
			status.setPassed(false);
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
	public ArrayList<FoundElement> findElements(Channel channel, boolean sysComp, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> searchPredicate) {
		return executor.findElements(channel, sysComp, testObject, tagName, attributes, searchPredicate);
	}
	
	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		return executor.getElementAttribute(element.getId(), attributeName, maxTry);
	}
	
	@Override
	public CalculatedProperty[] getAttributes(FoundElement element) {
		return executor.getElementAttributes(element.getId());
	}
	
	@Override
	public void refreshElementMapLocation(Channel channel) {}

	@Override
	public boolean setWindowToFront() {
		return false;
	}

	@Override
	public void goToUrl(ActionStatus status, String url) {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

	@Override
	public CalculatedProperty[] getCssAttributes(FoundElement element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadParents(FoundElement hoverElement) {
		// TODO Auto-generated method stub

	}	

	@Override
	public WebElement getRootElement() {
		// TODO Auto-generated method stub
		return null;
	}

	//------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void waitAfterAction() {}

	@Override
	public void updateDimensions(Channel channel) {}

	@Override
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y) {
		return null;
	}

	@Override
	public void switchWindow(int index) {
	}

	@Override
	public void closeWindow(ActionStatus status, int index) {
	}

	@Override
	public Object executeScript(ActionStatus status, String script, Object... params) {
		return null;
	}

	@Override
	public void scroll(FoundElement foundElement, int delta) {}

	@Override
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element) {}

	@Override
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop) {}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {}

	@Override
	public void clearText(ActionStatus status, FoundElement foundElement) {}

	@Override
	public void forceScrollElement(FoundElement value) {}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position) {}

	@Override
	public void drag(ActionStatus status, FoundElement element, MouseDirection position) {}
	
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
	public void switchToDefaultContent() {}

	@Override
	public void switchToFrameId(String id) {}
}
