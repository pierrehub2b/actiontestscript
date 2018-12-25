package com.ats.executor.drivers.engines;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import com.ats.executor.drivers.engines.webservices.AbstractApiExecutor;
import com.ats.executor.drivers.engines.webservices.RestApiExecutor;
import com.ats.executor.drivers.engines.webservices.SoapApiExecutor;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;

public class ApiDriverEngine extends DriverEngineAbstract implements IDriverEngine{
	
	private AbstractApiExecutor executor;
	private String source;

	public ApiDriverEngine(Channel channel, ActionStatus status, String path, ApplicationProperties props) {
		super(channel, props);

		if(applicationPath == null) {
			applicationPath = path;
		}

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(3500)
				.setConnectionRequestTimeout(3500)
				.setSocketTimeout(3500).build();

		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

		if(applicationPath.toLowerCase().endsWith("wsdl")) {

			this.application = "SOAP";

			final HttpGet request = new HttpGet(applicationPath);

			try {
				final HttpResponse response = httpClient.execute(request);

				if(response.getStatusLine().getStatusCode() == 200) {

					final HttpEntity entity = response.getEntity();

					BufferedInputStream bis = new BufferedInputStream(entity.getContent());

					File wsdlFile = File.createTempFile("atsWsdl_", ".wsdl");
					wsdlFile.deleteOnExit();

					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(wsdlFile));
					int inByte;
					while((inByte = bis.read()) != -1) bos.write(inByte);
					bis.close();
					bos.close();
					
					executor = new SoapApiExecutor(wsdlFile, applicationPath);

				}else {
					status.setCode(ActionStatus.CHANNEL_START_ERROR);
					status.setMessage("Wsdl file does not exists");
					status.setPassed(false);
				}

			} catch (ClientProtocolException e) {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage(e.getMessage());
				status.setPassed(false);
			} catch (IOException e) {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage(e.getMessage());
				status.setPassed(false);
			} catch (SAXException e) {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("Wsdl parse failed : " + e.getMessage());
				status.setPassed(false);
			} catch (ParserConfigurationException e) {
				status.setCode(ActionStatus.CHANNEL_START_ERROR);
				status.setMessage("Wsdl parse configuration error : " + e.getMessage());
				status.setPassed(false);
			}

		}else {
			this.application = "REST";

			executor = new RestApiExecutor(httpClient, applicationPath);
		}
	}

	@Override
	public void api(ActionStatus status, ActionApi api) {
		source = executor.execute(status, api);
	}
	
	@Override
	public String getSource() {
		return source;
	}

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
	public ArrayList<FoundElement> findElements(Channel channel, boolean sysComp, TestElement testObject,
			String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> searchPredicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttribute(FoundElement element, String attributeName, int maxTry) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CalculatedProperty[] getAttributes(FoundElement element) {
		// TODO Auto-generated method stub
		return null;
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
	public void refreshElementMapLocation(Channel channel) {
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
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position) {}

	@Override
	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList) {}

	@Override
	public void clearText(ActionStatus status, FoundElement foundElement) {}

	@Override
	public void forceScrollElement(FoundElement value) {}

	@Override
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, boolean hold) {}

	@Override
	public void keyDown(Keys key) {}

	@Override
	public void keyUp(Keys key) {}

	@Override
	public void drop() {}

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
