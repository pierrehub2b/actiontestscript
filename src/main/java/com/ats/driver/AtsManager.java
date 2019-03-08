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

package com.ats.driver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ats.executor.TestBound;
import com.ats.generator.ATS;

public class AtsManager {

	public static final String ATS_FOLDER = ".actiontestscript";

	private static final String DRIVERS_FOLDER = "drivers";
	private static final String ATS_PROPERTIES_FILE = ".atsProperties";

	private static final Double APPLICATION_WIDTH = 1280.00;
	private static final Double APPLICATION_HEIGHT = 960.00;

	private static final Double APPLICATION_X = 10.00;
	private static final Double APPLICATION_Y = 10.00;

	private static final int SCRIPT_TIMEOUT = 60;
	private static final int PAGELOAD_TIMEOUT = 120;
	private static final int WATCHDOG_TIMEOUT = 200;

	private static final int MAX_TRY_SEARCH = 15;
	private static final int MAX_TRY_PROPERTY = 10;
	
	private static final int SCROLL_UNIT = 100;
	
	//-----------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------

	private Path driversFolderPath;
	private Properties properties;

	private double applicationWidth = APPLICATION_WIDTH;
	private double applicationHeight = APPLICATION_HEIGHT;

	private double applicationX = APPLICATION_X;
	private double applicationY = APPLICATION_Y;
	
	private int scrollUnit = SCROLL_UNIT;

	private int scriptTimeOut = SCRIPT_TIMEOUT;
	private int pageloadTimeOut = PAGELOAD_TIMEOUT;
	private int watchDogTimeOut = WATCHDOG_TIMEOUT;

	private int maxTrySearch = MAX_TRY_SEARCH;
	private int maxTryProperty = MAX_TRY_PROPERTY;

	private AtsProxy proxy;

	private List<ApplicationProperties> applicationsList = new ArrayList<ApplicationProperties>();

	public static String getVersion() {

		final InputStream resourceAsStream = AtsManager.class.getResourceAsStream("/version.properties");
		Properties prop = new Properties();
		try{
			prop.load( resourceAsStream );
			return prop.getProperty("version");
		}catch(Exception e) {}

		return null;
	}

	public AtsManager() {

		String atsHome = System.getProperty("ats.home");
		if(atsHome == null || atsHome.length() == 0) {
			atsHome = System.getenv("ATS_HOME");
			if(atsHome == null || atsHome.length() == 0) {
				atsHome = System.getProperty("user.home") + File.separator + ATS_FOLDER;
			}
		}

		final Path atsFolderPath = Paths.get(atsHome);
		if(atsFolderPath.toFile().exists()) {
			properties = loadProperties(atsFolderPath.resolve(ATS_PROPERTIES_FILE));
			driversFolderPath = atsFolderPath.resolve(DRIVERS_FOLDER);
		}else {
			ATS.logError("ATS folder not found -> " + atsHome);
			System.exit(0);
		}
		
		if(proxy == null) {
			proxy = new AtsProxy(AtsProxy.SYSTEM);
		}
	}

	private Properties loadProperties(Path propertiesPath) {

		final File xmlFile = propertiesPath.toFile();
		if(xmlFile.exists()) {
			try {
				
				final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

				try {
					final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					final Document doc = dBuilder.parse(xmlFile);

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
										if(nodeList.item(0).getChildNodes().getLength() > 0) {
											String name = nodeList.item(0).getChildNodes().item(0).getNodeValue();
											String path = null;
											String wait = null;
											String check = null;
											String lang = null;

											nodeList = browserElement.getElementsByTagName("path");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													path = nodeList.item(0).getChildNodes().item(0).getNodeValue();

													final File checkFile = new File(path);
													if(!checkFile.exists() || !checkFile.isFile()) {
														path = null;
													}
												}
											}

											nodeList = browserElement.getElementsByTagName("lang");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													lang = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}
											
											nodeList = browserElement.getElementsByTagName("waitAction");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													wait = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}
																					
											nodeList = browserElement.getElementsByTagName("waitProperty");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													check = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}
											addApplicationProperties(ApplicationProperties.BROWSER_TYPE, name, path, wait, check, lang);
										}
									}
								}
							}
						}
					}

					NodeList boundNode = doc.getElementsByTagName("appBounding");
					if(boundNode != null && boundNode.getLength() > 0) {
						NodeList bound = ((Element)boundNode.item(0)).getElementsByTagName("width");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationWidth = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}

						bound = ((Element)boundNode.item(0)).getElementsByTagName("height");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationHeight = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}

						bound = ((Element)boundNode.item(0)).getElementsByTagName("x");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationX = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}

						bound = ((Element)boundNode.item(0)).getElementsByTagName("y");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationY = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
					}

					final NodeList timeOutNode = doc.getElementsByTagName("timeOut");
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
						
						timeOut = ((Element)timeOutNode.item(0)).getElementsByTagName("watchDog");
						if(timeOut != null && timeOut.getLength() > 0) {
							try {
								watchDogTimeOut = Integer.parseInt(timeOut.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
					}

					final NodeList maxTryNodeList = doc.getElementsByTagName("maxTry");
					if(maxTryNodeList != null && maxTryNodeList.getLength() > 0) {
						NodeList maxTryNode = ((Element)maxTryNodeList.item(0)).getElementsByTagName("searchElement");
						if(maxTryNode != null && maxTryNode.getLength() > 0) {
							try {
								maxTrySearch = Integer.parseInt(maxTryNode.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
						
						maxTryNode = ((Element)maxTryNodeList.item(0)).getElementsByTagName("getProperty");
						if(maxTryNode != null && maxTryNode.getLength() > 0) {
							try {
								maxTryProperty = Integer.parseInt(maxTryNode.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
					}
					
					final NodeList proxyNode = doc.getElementsByTagName("proxy");
					if(proxyNode != null && proxyNode.getLength() > 0) {
						
						NodeList data = ((Element)proxyNode.item(0)).getElementsByTagName("type");
						if(data != null && data.getLength() > 0) {
							
							String type = data.item(0).getChildNodes().item(0).getNodeValue();
							
							if(AtsProxy.DIRECT.equals(type)) {
								
								proxy = new AtsProxy(AtsProxy.DIRECT);
								
							}else if(AtsProxy.AUTO.equals(type)) {
								
								proxy = new AtsProxy(AtsProxy.AUTO);
								
							}else if(AtsProxy.MANUAL.equals(type)) {
								
								String host = null;
								int port = -1;
								
								data = ((Element)proxyNode.item(0)).getElementsByTagName("host");
								if(data != null && data.getLength() > 0) {
									host = data.item(0).getChildNodes().item(0).getNodeValue();
								}
								
								data = ((Element)proxyNode.item(0)).getElementsByTagName("port");
								if(data != null && data.getLength() > 0) {
									try {
										port = Integer.parseInt(data.item(0).getChildNodes().item(0).getNodeValue());
									}catch(NumberFormatException e){}
								}
								
								if(host != null && port > 0) {
									proxy = new AtsProxy(AtsProxy.MANUAL, host, port);
								}
							}
						}
					}

					final NodeList applications = doc.getElementsByTagName("application");
					if(applications != null && applications.getLength() > 0) {
						for (int temp = 0; temp < applications.getLength(); temp++) {
							Node application = applications.item(temp);
							if (application.getNodeType() == Node.ELEMENT_NODE) {
								Element applicationElement = (Element) application;
								if(applicationElement.hasChildNodes() && applicationElement.getChildNodes().getLength() > 1) {
									NodeList nodeList = applicationElement.getElementsByTagName("name");
									if(nodeList != null && nodeList.getLength() > 0) {
										if(nodeList.item(0).getChildNodes().getLength() > 0) {
											String name = nodeList.item(0).getChildNodes().item(0).getNodeValue();
											String path = "";
											String wait = null;

											nodeList = applicationElement.getElementsByTagName("path");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													path = nodeList.item(0).getChildNodes().item(0).getNodeValue();

													File checkFile = new File(path);
													if(!checkFile.exists() || !checkFile.isFile()) {
														path = "";
													}
												}
											}
											
											nodeList = applicationElement.getElementsByTagName("waitAction");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													wait = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}
																							
											addApplicationProperties(ApplicationProperties.DESKTOP_TYPE, name, path, wait, "", null);
										}
									}
								}
							}
						}
					}
					
					final NodeList mobileApps = doc.getElementsByTagName("mobile");
					if(mobileApps != null && mobileApps.getLength() > 0) {
						for (int temp = 0; temp < mobileApps.getLength(); temp++) {
							Node app = mobileApps.item(temp);
							if (app.getNodeType() == Node.ELEMENT_NODE) {
								Element mobileApp = (Element) app;
								if(mobileApp.hasChildNodes() && mobileApp.getChildNodes().getLength() > 1) {
									NodeList nodeList = mobileApp.getElementsByTagName("name");
									if(nodeList != null && nodeList.getLength() > 0) {
										if(nodeList.item(0).getChildNodes().getLength() > 0) {
											String name = nodeList.item(0).getChildNodes().item(0).getNodeValue();
											String url = "";
											String wait = null;

											nodeList = mobileApp.getElementsByTagName("url");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													url = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}
											
											nodeList = mobileApp.getElementsByTagName("waitAction");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													wait = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}
											
											addApplicationProperties(ApplicationProperties.MOBILE_TYPE, name, url, wait, "", null);
										}
									}
								}
							}
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

	private void addApplicationProperties(int type, String name, String path, String wait, String check, String lang) {
		int waitValue = -1;
		try {
			waitValue = Integer.parseInt(wait);
		}catch(NumberFormatException e){}
		
		int checkProperty = -1;
		try {
			checkProperty = Integer.parseInt(check);
		}catch(NumberFormatException e){}

		applicationsList.add(new ApplicationProperties(type, name, path, waitValue, checkProperty, lang));
	}

	//------------------------------------------------------------------------------------------------------------------
	// Getters
	//------------------------------------------------------------------------------------------------------------------

	public ApplicationProperties getApplicationProperties(String name) {
		for (int i=0; i < this.applicationsList.size(); i++) {
			final ApplicationProperties properties = this.applicationsList.get(i);
			if (name.equals(properties.getName())){
				return properties;
			}
		}
		return null;
	}

	public AtsProxy getProxy() {
		return proxy;
	}

	public int getScriptTimeOut() {
		return scriptTimeOut;
	}

	public int getPageloadTimeOut() {
		return pageloadTimeOut;
	}
	
	public int getWatchDogTimeOut() {
		return watchDogTimeOut;
	}

	public TestBound getApplicationBound() {
		return new TestBound(applicationX, applicationY, applicationWidth, applicationHeight);
	}

	public String getPropertyString(String key) {
		return properties.getProperty(key);
	}

	public Path getDriversFolderPath() {
		return driversFolderPath;
	}

	public int getMaxTrySearch() {
		return maxTrySearch;
	}
	
	public int getMaxTryProperty() {
		return maxTryProperty;
	}
	
	public int getScrollUnit() {
		return scrollUnit;
	}
}