package actiontestscript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ats.recorder.VisualAction;
import com.ats.recorder.VisualReport;
import com.ats.tools.ResourceContent;
import com.ats.tools.Utils;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

public class Main {

	public static void main(String[] argsx) throws FileNotFoundException, ParserConfigurationException, TransformerException, InterruptedException, MalformedURLException {

		EdgeOptions options = new EdgeOptions();
		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
		options.setPageLoadStrategy("eager");
		
		RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:17556"), options);
		driver.get("https://www.w3schools.com/html/tryit.asp?filename=tryhtml_iframe");
		
		RemoteWebElement iframe = (RemoteWebElement) driver.findElementByName("iframeResult");
		
		String elementId = iframe.getId();
		
		driver.switchTo().defaultContent();
		
		RemoteWebElement rwe = new RemoteWebElement();
		rwe.setId(elementId);
		rwe.setParent(driver);
		
		String src = rwe.getAttribute("id");
		
		Object verif = driver.executeAsyncScript(ResourceContent.getElementAttributesJavaScript() + ";arguments[arguments.length-1](result);", rwe);
		
		
		driver.switchTo().frame(rwe);
		
		
		System.exit(0);

		File xmlFolder = new File("C:\\Users\\huber\\Desktop\\xml");
		File atsvFile = new File("C:\\Users\\huber\\Desktop\\subscripts.DemoCalculatrice.atsv");
		try {
			Utils.deleteRecursive(xmlFolder);
		} catch (FileNotFoundException e) {}

		xmlFolder.mkdirs();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e2) {
		}
		
		Document document= builder.newDocument();
		
		FileInputStream fis = null;
		try {
			
			fis = new FileInputStream(atsvFile);
			AMF3Deserializer amf3 = new AMF3Deserializer(fis);
						
			Element atsRoot = document.createElement("ats");
			document.appendChild(atsRoot);
			
			//------------------------------------------------------------------------------------------------------
			// script header
			//------------------------------------------------------------------------------------------------------
			
			VisualReport report = (VisualReport) amf3.readObject();
			
			Element script = document.createElement("script");
			atsRoot.appendChild(script);
			
			script.setAttribute("id", report.getId());					
			script.setAttribute("name", report.getName());
			
			Element description = document.createElement("description");
			description.setTextContent(report.getDescription());
			script.appendChild(description);
			
			Element author = document.createElement("author");
			author.setTextContent(report.getAuthor());
			script.appendChild(author);
			
			Element prerequisite = document.createElement("prerequisite");
			prerequisite.setTextContent(report.getPrerequisite());
			script.appendChild(prerequisite);
			
			Element executed = document.createElement("executed");
			executed.setTextContent(report.getExecuted());
			script.appendChild(executed);
			
			Element groups = document.createElement("groups");
			groups.setTextContent(report.getGroups());
			script.appendChild(groups);
			
			Element quality = document.createElement("quality");
			quality.setTextContent(report.getQuality()+"");
			script.appendChild(quality);
									
			//------------------------------------------------------------------------------------------------------
			//------------------------------------------------------------------------------------------------------
			
			Element actions = document.createElement("actions");
			atsRoot.appendChild(actions);
			
			while(amf3.available() > 0) {
				
				VisualAction va = (VisualAction) amf3.readObject();
				
				Element action = document.createElement("action");
				action.setAttribute("index", va.getIndex()+"");
				action.setAttribute("type", va.getType());
				actions.appendChild(action);
				
				Element line = document.createElement("line");
				line.setTextContent(va.getLine()+"");
				action.appendChild(line);
				
				Element timeLine = document.createElement("timeLine");
				timeLine.setTextContent(va.getTimeLine()+"");
				action.appendChild(timeLine);
				
				Element passed = document.createElement("passed");
				passed.setTextContent(va.isPassed()+"");
				action.appendChild(passed);
				
				Element value = document.createElement("value");
				value.setTextContent(va.getValue());
				action.appendChild(value);
				
				Element data = document.createElement("data");
				data.setTextContent(va.getData());
				action.appendChild(data);
				
				Element image = document.createElement("img");
				image.setAttribute("src", va.getImageFileName());
				image.setAttribute("width", va.getChannelBound().getWidth().intValue()+"");
				image.setAttribute("height", va.getChannelBound().getHeight().intValue()+"");
				action.appendChild(image);
				
				Element channel = document.createElement("channel");
				channel.setAttribute("name", va.getChannelName());

				Element channelBound = document.createElement("bound");
				Element channelX = document.createElement("x");
				channelX.setTextContent(va.getChannelBound().getX().intValue()+"");
				channelBound.appendChild(channelX);
				
				Element channelY = document.createElement("y");
				channelY.setTextContent(va.getChannelBound().getY().intValue()+"");
				channelBound.appendChild(channelY);
				
				Element channelWidth = document.createElement("width");
				channelWidth.setTextContent(va.getChannelBound().getWidth().intValue()+"");
				channelBound.appendChild(channelWidth);
				
				Element channelHeight = document.createElement("height");
				channelHeight.setTextContent(va.getChannelBound().getHeight().intValue()+"");
				channelBound.appendChild(channelHeight);
				
				channel.appendChild(channelBound);
				action.appendChild(channel);
								
				if(va.getElement() != null) {
					
					Element element = document.createElement("element");
					element.setAttribute("tag", va.getElement().getTag());
					
					Element criterias = document.createElement("criterias");
					criterias.setTextContent(va.getElement().getCriterias());
					element.appendChild(criterias);
					
					Element foundElements = document.createElement("foundElements");
					foundElements.setTextContent(va.getElement().getFoundElements()+"");
					element.appendChild(foundElements);
					
					Element searchDuration = document.createElement("searchDuration");
					searchDuration.setTextContent(va.getElement().getSearchDuration()+"");
					element.appendChild(searchDuration);
					
					Element elementBound = document.createElement("bound");
					Element elementX = document.createElement("x");
					elementX.setTextContent(va.getElement().getBound().getX().intValue()+"");
					elementBound.appendChild(elementX);
					
					Element elementY = document.createElement("y");
					elementY.setTextContent(va.getElement().getBound().getY().intValue()+"");
					elementBound.appendChild(elementY);
					
					Element elementWidth = document.createElement("width");
					elementWidth.setTextContent(va.getElement().getBound().getWidth().intValue()+"");
					elementBound.appendChild(elementWidth);
					
					Element elementHeight = document.createElement("height");
					elementHeight.setTextContent(va.getElement().getBound().getHeight().intValue()+"");
					elementBound.appendChild(elementHeight);
					
					element.appendChild(elementBound);
					action.appendChild(element);
				}								
				
				va.saveImageFile(xmlFolder.toPath());
			}
			
			amf3.close();
			
		} catch (IOException e1) {
			//e1.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				//ex.printStackTrace();
			}
		}
							
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(xmlFolder.toPath().resolve("actions.xml").toFile()));
		} catch (TransformerConfigurationException e) {
			//e.printStackTrace();
		} catch (TransformerException e) {
			//e.printStackTrace();
		}









	}

}
