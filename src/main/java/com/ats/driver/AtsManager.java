package com.ats.driver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ats.executor.TestBound;

public class AtsManager {

	public static final String ATS_FOLDER = ".actiontestscript";

	private static final String DRIVERS_FOLDER = "drivers";
	private static final String ATS_PROPERTIES_FILE = ".atsProperties";
	
	private static final Double APPLICATION_WIDTH = 1280.00;
	private static final Double APPLICATION_HEIGHT = 960.00;
	
	private static final int SCRIPT_TIMEOUT = 60;
	private static final int PAGELOAD_TIMEOUT = 120;
	
	private static final int MAX_TRY = 20;

	private Path driversFolderPath;
	private Properties properties;
	
	private double applicationWidth = APPLICATION_WIDTH;
	private double applicationHeight = APPLICATION_HEIGHT;
	
	private int scriptTimeOut = SCRIPT_TIMEOUT;
	private int pageloadTimeOut = PAGELOAD_TIMEOUT;
	
	private int maxTry = MAX_TRY;
	
	private Proxy proxy = new Proxy();
	
	private List<BrowserProperties> browsersList = new ArrayList<BrowserProperties>();

	public AtsManager() {

		String atsHome = System.getenv("ATS_HOME");
		if(atsHome == null || atsHome.length() == 0) {
			atsHome = System.getProperty("ats.home");
			if(atsHome == null || atsHome.length() == 0) {
				atsHome = System.getProperty("user.home") + File.separator + ATS_FOLDER;
			}
		}

		Path atsFolderPath = Paths.get(atsHome);

		properties = loadProperties(atsFolderPath.resolve(ATS_PROPERTIES_FILE));
		driversFolderPath = atsFolderPath.resolve(DRIVERS_FOLDER);
		proxy.setProxyType(ProxyType.SYSTEM);

		if(!Files.exists(driversFolderPath)) {

		}
	}

	private Properties loadProperties(Path propertiesPath) {

		File xmlFile = propertiesPath.toFile();
		if(xmlFile.exists()) {
			try {

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

				try {
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(xmlFile);

					doc.getDocumentElement().normalize();

					NodeList browsers = doc.getElementsByTagName("browser");
					if(browsers != null && browsers.getLength() > 0) {
						for (int temp = 0; temp < browsers.getLength(); temp++) {
							Node browser = browsers.item(temp);
							if (browser.getNodeType() == Node.ELEMENT_NODE) {

								Element browserElement = (Element) browser;
								if(browserElement.hasChildNodes() && browserElement.getChildNodes().getLength() > 1) {

									NodeList nodeList = browserElement.getElementsByTagName("name");
									if(nodeList != null && nodeList.getLength() > 0) {
										String name = nodeList.item(0).getChildNodes().item(0).getNodeValue();
										String path = null;
										String wait = null;

										nodeList = browserElement.getElementsByTagName("path");
										if(nodeList != null && nodeList.getLength() > 0) {
											path = nodeList.item(0).getChildNodes().item(0).getNodeValue();
											
											File checkFile = new File(path);
											if(!checkFile.exists() || !checkFile.isFile()) {
												path = null;
											}
										}

										nodeList = browserElement.getElementsByTagName("actionWait");
										if(nodeList != null && nodeList.getLength() > 0) {
											wait = nodeList.item(0).getChildNodes().item(0).getNodeValue();
										}

										addBrowserProperties(name, path, wait);
									}
								}
							}
						}
					}

					NodeList sizeNode = doc.getElementsByTagName("defaultSize");
					if(sizeNode != null && sizeNode.getLength() > 0) {
						NodeList size = ((Element)sizeNode.item(0)).getElementsByTagName("width");
						if(size != null && size.getLength() > 0) {
							try {
								applicationWidth = Double.parseDouble(size.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
						
						size = ((Element)sizeNode.item(0)).getElementsByTagName("height");
						if(size != null && size.getLength() > 0) {
							try {
								applicationHeight = Double.parseDouble(size.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
					}

					NodeList timeOutNode = doc.getElementsByTagName("timeOut");
					if(timeOutNode != null && timeOutNode.getLength() > 0) {
						NodeList timeOut = ((Element)timeOutNode.item(0)).getElementsByTagName("script");
						if(timeOut != null && timeOut.getLength() > 0) {
							try {
								scriptTimeOut = Integer.parseInt(timeOut.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
						
						timeOut = ((Element)timeOutNode.item(0)).getElementsByTagName("pageLoad");
						if(timeOut != null && timeOut.getLength() > 0) {
							try {
								pageloadTimeOut = Integer.parseInt(timeOut.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
					}

					NodeList maxTryNode = doc.getElementsByTagName("maxTry");
					if(maxTryNode != null && maxTryNode.getLength() > 0) {
						try {
							maxTry = Integer.parseInt(maxTryNode.item(0).getChildNodes().item(0).getNodeValue());
						}catch(NumberFormatException e){}
					}
					
					NodeList proxyNode = doc.getElementsByTagName("proxy");
					if(proxyNode != null && proxyNode.getLength() > 0) {
						String proxyValue = maxTryNode.item(0).getChildNodes().item(0).getNodeValue();
						
						switch (proxyValue){
						
						case "auto" :
							proxy.setProxyType(ProxyType.AUTODETECT);
							break;
							
						case "direct" :
							proxy.setProxyType(ProxyType.DIRECT);
							break;
							
						/*case "manual" :
							proxy.setProxyType(ProxyType.MANUAL);
							proxy.setHttpProxy(PROXY_ADDRESS); 
							break;*/
						}
					}
										
					
				} catch (ParserConfigurationException e) {

				} catch (SAXException e) {

				}

			} catch (IOException e) {

			}
		}

		return new Properties();
	}

	private void addBrowserProperties(String name, String path, String wait) {
		int waitValue = 50;
		try {
			waitValue = Integer.parseInt(wait);
		}catch(NumberFormatException e){}
		
		if(waitValue < 50) {
			waitValue = 50;
		}
		
		browsersList.add(new BrowserProperties(name, path, waitValue));
	}
	
	//------------------------------------------------------------------------------------------------------------------
	// Getters
	//------------------------------------------------------------------------------------------------------------------
	
	public BrowserProperties getBrowserProperties(String name) {
		 for (int i=0; i < this.browsersList.size(); i++) {
			 BrowserProperties properties = this.browsersList.get(i);
		        if (name.equals(properties.getName())){
		             return properties;
		        }
		    }
		    return null;
	}
		
	public Proxy getProxy() {
		return proxy;
	}
	
	public int getMaxTry() {
		return maxTry;
	}
	
	public int getScriptTimeOut() {
		return scriptTimeOut;
	}

	public int getPageloadTimeOut() {
		return pageloadTimeOut;
	}
	
	public TestBound getApplicationBound() {
		return new TestBound(10.0, 10.0, applicationWidth, applicationHeight);
	}
	
	public String getPropertyString(String key) {
		return properties.getProperty(key);
	}

	public Path getDriversFolderPath() {
		return driversFolderPath;
	}
}
