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

import com.ats.executor.ActionTestScript;
import com.ats.executor.TestBound;
import com.ats.generator.ATS;
import com.ats.tools.AtsClassLoader;
import com.ats.tools.Utils;
import com.ats.tools.performance.external.OctoperfApi;
import com.ats.tools.wait.IWaitGuiReady;

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
	private static final int WEBSERVICE_TIMEOUT = 20;

	private static final int MAX_TRY_SEARCH = 15;
	private static final int MAX_TRY_PROPERTY = 10;
	private static final int MAX_TRY_WEBSERVICE = 1;

	private static final int MAX_TRY_MOBILE = 5;

	private static final int SCROLL_UNIT = 120;
	private static final int MAX_STALE_OR_JAVASCRIPT_ERROR = 20;

	private static final int CAPTURE_PROXY_TRAFFIC_IDLE = 3;

	//-----------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------

	private Path driversFolderPath;
	private Properties properties;

	private double applicationWidth = APPLICATION_WIDTH;
	private double applicationHeight = APPLICATION_HEIGHT;

	private double applicationX = APPLICATION_X;
	private double applicationY = APPLICATION_Y;

	private int scriptTimeOut = SCRIPT_TIMEOUT;
	private int pageloadTimeOut = PAGELOAD_TIMEOUT;
	private int watchDogTimeOut = WATCHDOG_TIMEOUT;

	private int webServiceTimeOut = WEBSERVICE_TIMEOUT;

	private int maxTrySearch = MAX_TRY_SEARCH;
	private int maxTryProperty = MAX_TRY_PROPERTY;
	private int maxTryWebservice = MAX_TRY_WEBSERVICE;
	private int maxTryMobile = MAX_TRY_MOBILE;

	private AtsProxy proxy;
	private AtsProxy neoloadProxy;

	private String neoloadDesignApi;

	private OctoperfApi octoperf;
	private ArrayList<String> blackListServers;
	private int trafficIdle = CAPTURE_PROXY_TRAFFIC_IDLE;

	private List<ApplicationProperties> applicationsList = new ArrayList<ApplicationProperties>();

	private String error;

	public static int getScrollUnit() {
		return SCROLL_UNIT;
	}

	public static int getMaxStaleOrJavaScriptError() {
		return MAX_STALE_OR_JAVASCRIPT_ERROR;
	}

	private static String getElementText(Node node) {
		return getElementText((Element)node);
	}

	private static String getElementText(Element elem) {
		if(elem != null) {
			final String textContent = elem.getTextContent();
			if(textContent != null && textContent.length() > 0) {
				return textContent.replaceAll("\n", "").replaceAll("\r", "").trim();
			}
		}
		return null;
	}

	private static double getElementDouble(Element elem) {
		final String value = elem.getTextContent().replaceAll("\n", "").replaceAll("\r", "").trim();
		try {
			return Double.parseDouble(value);
		}catch(NumberFormatException e){}
		return 0D;
	}

	private static int getElementInt(Element elem) {
		final String value = elem.getTextContent().replaceAll("\n", "").replaceAll("\r", "").trim();
		try {
			return Integer.parseInt(value);
		}catch(NumberFormatException e){}
		return 0;
	}

	//-----------------------------------------------------------------------------------------------
	// Instance management
	//-----------------------------------------------------------------------------------------------

	private AtsClassLoader atsClassLoader;

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
			driversFolderPath = Paths.get("");
			ATS.logWarn("ATS driver folder not found -> " + atsHome);
			//System.exit(0);
		}

		if(proxy == null) {
			proxy = new AtsProxy(AtsProxy.SYSTEM);
		}

		atsClassLoader = new AtsClassLoader();
	}

	public IWaitGuiReady getWaitGuiReady() {
		return atsClassLoader.getWaitGuiReady();
	}

	public Class<ActionTestScript> loadTestScriptClass(String name) {
		return atsClassLoader.loadTestScriptClass(name);
	}

	//-----------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------

	private Properties loadProperties(Path propertiesPath) {

		final File xmlFile = propertiesPath.toFile();
		if(xmlFile.exists()) {

			try {
				final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				try {
					final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					final Document doc = dBuilder.parse(xmlFile);

					doc.getDocumentElement().normalize();

					final NodeList executeChildren = ((Element) doc.getChildNodes().item(0)).getChildNodes();
					for (int i = 0; i < executeChildren.getLength(); i++) {
						if (executeChildren.item(i) instanceof Element) {

							final Element type = (Element) executeChildren.item(i);

							switch (type.getNodeName().toLowerCase()) {

							case "performance":
								final NodeList perfChildren = type.getChildNodes();
								for (int j = 0; j < perfChildren.getLength(); j++) {
									if (perfChildren.item(j) instanceof Element) {

										final Element perfElement = (Element) perfChildren.item(j);
										switch(perfElement.getNodeName().toLowerCase()) {

										case "idle":
											trafficIdle = getElementInt(perfElement);
											break;
										case "octoperf":

											String host = null;
											String apiKey = null;
											String workspaceName = null;
											String projectName = null;

											NodeList nl = perfElement.getElementsByTagName("host");
											if(nl != null) {
												host = getElementText(nl.item(0));
											}

											nl = perfElement.getElementsByTagName("apiKey");
											if(nl != null) {
												apiKey = getElementText(nl.item(0));
											}

											nl = perfElement.getElementsByTagName("workspaceName");
											if(nl != null) {
												workspaceName = getElementText(nl.item(0));
											}

											nl = perfElement.getElementsByTagName("projectName");
											if(nl != null) {
												projectName = getElementText(nl.item(0));
											}

											if(host != null && apiKey != null) {
												octoperf = new OctoperfApi(host, apiKey, workspaceName, projectName);
											}

											break;

										case "blacklist":
											blackListServers = new ArrayList<String>();
											final NodeList blackList = perfElement.getElementsByTagName("url");
											for (int k = 0; k < blackList.getLength(); k++) {
												if (blackList.item(k) instanceof Element) {
													blackListServers.add(getElementText(blackList.item(k)));
												}
											}
											break;
										}
									}
								}
								break;

							case "neoload":
								final NodeList neoloadChildren = type.getChildNodes();
								for (int j = 0; j < neoloadChildren.getLength(); j++) {
									if (neoloadChildren.item(j) instanceof Element) {
										final Element neoloadElement = (Element) neoloadChildren.item(j);

										String host = null;
										String port = null;
										String api = null;
										String apiPort = null;

										if(neoloadElement.getTagName().equals("recorder")) {
											final NodeList recorderChildren = neoloadElement.getChildNodes();
											for (int k = 0; k < recorderChildren.getLength(); k++) {
												if (recorderChildren.item(k) instanceof Element) {
													final Element recorderElement = (Element) recorderChildren.item(k);
													if(recorderElement.getNodeName().equals("host")) {
														host = getElementText(recorderElement);
													}else if(recorderElement.getNodeName().equals("port")){
														port = getElementText(recorderElement);
													}
												}
											}
										}else if(neoloadElement.getTagName().equals("design")) {
											final NodeList designChildren = neoloadElement.getChildNodes();
											for (int k = 0; k < designChildren.getLength(); k++) {
												if (designChildren.item(k) instanceof Element) {
													final Element designElement = (Element) designChildren.item(k);
													if(designElement.getNodeName().equals("api")) {
														api = getElementText(designElement);
													}else if(designElement.getNodeName().equals("port")){
														apiPort = getElementText(designElement);
													}
												}
											}
										}

										if(host != null && port != null) {
											neoloadProxy = new AtsProxy(host, port);

											if(api != null && apiPort != null) {

												if(!api.startsWith("/")) {
													api = "/" + api;
												}
												if(!api.endsWith("/")) {
													api = api + "/";
												}

												neoloadDesignApi = "http://" + neoloadProxy.getHost() + ":" + apiPort + api;
											}
										}
									}
								}

								break;

							case "proxy":

								final NodeList proxyChildren = type.getChildNodes();

								String proxyType = null;
								String host = null;
								int port = 0;

								for (int j = 0; j < proxyChildren.getLength(); j++) {
									if (proxyChildren.item(j) instanceof Element) {
										final Element proxyElement = (Element) proxyChildren.item(j);
										switch (proxyElement.getNodeName().toLowerCase()) {
										case "type":
											proxyType = getElementText(proxyElement);
											break;
										case "host":
											host = getElementText(proxyElement);
											break;
										case "port":
											port = getElementInt(proxyElement);
											break;
										}
									}
								}

								if(AtsProxy.AUTO.equals(proxyType)) {
									proxy = new AtsProxy(AtsProxy.AUTO);
								}else if(AtsProxy.DIRECT.equals(proxyType)) {
									proxy = new AtsProxy(AtsProxy.DIRECT);
								}else if(AtsProxy.MANUAL.equals(proxyType) && host != null && port > 0) {
									proxy = new AtsProxy(AtsProxy.MANUAL, host, port);
								}else {
									proxy = new AtsProxy(AtsProxy.SYSTEM);
								}
								break;

							case "appbounding":
								final NodeList boundingChildren = type.getChildNodes();
								for (int j = 0; j < boundingChildren.getLength(); j++) {
									if (boundingChildren.item(j) instanceof Element) {
										final Element boundingElement = (Element) boundingChildren.item(j);

										switch(boundingElement.getTagName()) {
										case "x":
											applicationX = getElementDouble(boundingElement);
											break;
										case "y":
											applicationY = getElementDouble(boundingElement);
											break;
										case "width":
											applicationWidth = getElementDouble(boundingElement);
											break;
										case "height":
											applicationHeight = getElementDouble(boundingElement);
											break;
										}
									}
								}
								break;

							case "maxtry":
								final NodeList maxtryChildren = type.getChildNodes();
								for (int j = 0; j < maxtryChildren.getLength(); j++) {
									if (maxtryChildren.item(j) instanceof Element) {
										final Element maxtryElement = (Element) maxtryChildren.item(j);

										switch(maxtryElement.getTagName().toLowerCase()) {
										case "searchelement":
											maxTrySearch = getElementInt(maxtryElement);
											break;
										case "getproperty":
											maxTryProperty = getElementInt(maxtryElement);
											break;
										case "webservice":
											maxTryWebservice = getElementInt(maxtryElement);
											break;
										}
									}
								}
								break;

							case "timeout":
								final NodeList timeoutChildren = type.getChildNodes();
								for (int j = 0; j < timeoutChildren.getLength(); j++) {
									if (timeoutChildren.item(j) instanceof Element) {
										final Element timeoutElement = (Element) timeoutChildren.item(j);

										switch(timeoutElement.getTagName().toLowerCase()) {
										case "script":
											scriptTimeOut = getElementInt(timeoutElement);
											break;
										case "pageload":
											pageloadTimeOut = getElementInt(timeoutElement);
											break;
										case "watchdog":
											watchDogTimeOut = getElementInt(timeoutElement);
											break;
										case "webservice":
											webServiceTimeOut = getElementInt(timeoutElement);
											break;
										}
									}
								}
								break;

							case "browsers":
								final NodeList browsers = type.getElementsByTagName("browser");
								for (int j = 0; j < browsers.getLength(); j++) {

									String name = null;
									String waitAction = null;
									String path = null;
									String driver = null;
									String userDataDir = null;
									String check = null;
									String lang = null;
									
									String title = "";

									if (browsers.item(j) instanceof Element) {
										final Element browser = (Element) browsers.item(j);
										final NodeList browserAttributes = browser.getChildNodes();
										for (int k = 0; k < browserAttributes.getLength(); k++) {
											if (browserAttributes.item(k) instanceof Element) {
												final Element browserElement = (Element) browserAttributes.item(k);

												switch (browserElement.getNodeName().toLowerCase()) {
												case "name":
													name = getElementText(browserElement);
													break;
												case "waitaction":
													waitAction = getElementText(browserElement);
													break;
												case "title":
													title = getElementText(browserElement);
													break;
												case "driver":
													driver = getElementText(browserElement);
													break;
												case "path":
													path = getElementText(browserElement);
													break;
												case "userdatadir":
													userDataDir = getElementText(browserElement);
													break;
												case "waitProperty":
													check = getElementText(browserElement);
													break;
												case "lang":
													lang = getElementText(browserElement);
													break;
												}
											}
										}

										if(name != null) {
											if(userDataDir != null && userDataDir.length() > 0) {
												File userProfileDir = new File(userDataDir);
												if(!userProfileDir.exists()) {
													final Path userDataPath = Paths.get(userDataDir);
													if(userDataPath.isAbsolute()) {
														userProfileDir = userDataPath.toFile();
													}else {
														userProfileDir = Paths.get(System.getProperty("user.home"), ".AtsDrivers", "user_profile", userDataDir).toFile();	
													}
													userProfileDir.mkdirs();
												}
												userDataDir = userProfileDir.getAbsolutePath();
											}else {
												userDataDir = null;
											}
											addApplicationProperties(ApplicationProperties.BROWSER_TYPE, name, driver, path, waitAction, check, lang, userDataDir, title);
										}
									}
								}
								break;

							case "applications":
								final NodeList applications = type.getElementsByTagName("application");
								for (int j = 0; j < applications.getLength(); j++) {

									String name = null;
									String waitAction = null;
									String path = null;

									if (applications.item(j) instanceof Element) {
										final Element application = (Element) applications.item(j);
										final NodeList applicationAttributes = application.getChildNodes();
										for (int k = 0; k < applicationAttributes.getLength(); k++) {
											if (applicationAttributes.item(k) instanceof Element) {
												final Element applicationElement = (Element) applicationAttributes.item(k);

												switch (applicationElement.getNodeName().toLowerCase()) {
												case "name":
													name = getElementText(applicationElement);
													break;
												case "waitaction":
													waitAction = getElementText(applicationElement);
													break;
												case "path":
													path = getElementText(applicationElement);
													break;
												}
											}
										}

										if(name != null && path != null) {
											addApplicationProperties(ApplicationProperties.DESKTOP_TYPE, name, path, waitAction, "", null, null, null);
										}
									}
								}

								break;

							case "mobiles":
								final NodeList mobiles = type.getElementsByTagName("mobile");
								for (int j = 0; j < mobiles.getLength(); j++) {

									String name = null;
									String waitAction = null;
									String endpoint = null;
									String packageName = null;

									if (mobiles.item(j) instanceof Element) {
										final Element mobile = (Element) mobiles.item(j);
										final NodeList mobileAttributes = mobile.getChildNodes();
										for (int k = 0; k < mobileAttributes.getLength(); k++) {
											if (mobileAttributes.item(k) instanceof Element) {
												final Element mobileElement = (Element) mobileAttributes.item(k);

												switch (mobileElement.getNodeName().toLowerCase()) {
												case "name":
													name = getElementText(mobileElement);
													break;
												case "waitaction":
													waitAction = getElementText(mobileElement);
													break;
												case "endpoint":
													endpoint = getElementText(mobileElement);
													break;
												case "package":
													packageName = getElementText(mobileElement);
													break;
												}
											}
										}

										if(name != null && endpoint != null && packageName != null) {
											addApplicationProperties(ApplicationProperties.MOBILE_TYPE, name, endpoint + "/" + packageName, waitAction, "", null, null, null);
										}
									}
								}
								break;

							case "apis":
								final NodeList apis = type.getElementsByTagName("api");
								for (int j = 0; j < apis.getLength(); j++) {

									String name = null;
									String waitAction = null;
									String url = null;

									if (apis.item(j) instanceof Element) {
										final Element api = (Element) apis.item(j);
										final NodeList apiAttributes = api.getChildNodes();
										for (int k = 0; k < apiAttributes.getLength(); k++) {
											if (apiAttributes.item(k) instanceof Element) {
												final Element apiElement = (Element) apiAttributes.item(k);

												switch (apiElement.getNodeName().toLowerCase()) {
												case "name":
													name = getElementText(apiElement);
													break;
												case "waitaction":
													waitAction = getElementText(apiElement);
													break;
												case "url":
													url = getElementText(apiElement);
													break;
												}
											}
										}

										if(name != null && url != null) {
											addApplicationProperties(ApplicationProperties.API_TYPE, name, url, waitAction, "", null, null, null);
										}
									}
								}
								break;
							}
						}
					}

				} catch (ParserConfigurationException e) {
					error = e.getMessage();
				} catch (SAXException e) {
					error = e.getMessage();
				}

			} catch (IOException e) {
				error = e.getMessage();
			}


			/*try {

				final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

				try {
					final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					final Document doc = dBuilder.parse(xmlFile);

					doc.getDocumentElement().normalize();

					NodeList nl0 = doc.getElementsByTagName("browser");
					if(nl0 != null && nl0.getLength() > 0) {
						for (int temp = 0; temp < nl0.getLength(); temp++) {
							Node browser = nl0.item(temp);
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
											String driver = null;
											String userDataDir = null;

											nodeList = browserElement.getElementsByTagName("path");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													path = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}

											nodeList = browserElement.getElementsByTagName("userDataDir");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													userDataDir = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}

											nodeList = browserElement.getElementsByTagName("driver");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													driver = nodeList.item(0).getChildNodes().item(0).getNodeValue();
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
											addApplicationProperties(ApplicationProperties.BROWSER_TYPE, name, driver, path, wait, check, lang, userDataDir);
										}
									}
								}
							}
						}
					}

					nl0 = doc.getElementsByTagName("appBounding");
					if(nl0 != null && nl0.getLength() > 0) {
						NodeList bound = ((Element)nl0.item(0)).getElementsByTagName("width");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationWidth = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}

						bound = ((Element)nl0.item(0)).getElementsByTagName("height");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationHeight = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}

						bound = ((Element)nl0.item(0)).getElementsByTagName("x");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationX = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}

						bound = ((Element)nl0.item(0)).getElementsByTagName("y");
						if(bound != null && bound.getLength() > 0) {
							try {
								applicationY = Double.parseDouble(bound.item(0).getChildNodes().item(0).getNodeValue());
							}catch(NumberFormatException e){}
						}
					}

					nl0 = doc.getElementsByTagName("timeOut");
					if(nl0 != null && nl0.getLength() > 0) {
						NodeList timeOut = ((Element)nl0.item(0)).getElementsByTagName("script");
						if(timeOut != null && timeOut.getLength() > 0) {
							scriptTimeOut = Utils.string2Int(timeOut.item(0).getChildNodes().item(0).getNodeValue(), SCRIPT_TIMEOUT);
						}

						timeOut = ((Element)nl0.item(0)).getElementsByTagName("pageLoad");
						if(timeOut != null && timeOut.getLength() > 0) {
							pageloadTimeOut = Utils.string2Int(timeOut.item(0).getChildNodes().item(0).getNodeValue(), PAGELOAD_TIMEOUT);
						}

						timeOut = ((Element)nl0.item(0)).getElementsByTagName("watchDog");
						if(timeOut != null && timeOut.getLength() > 0) {
							watchDogTimeOut = Utils.string2Int(timeOut.item(0).getChildNodes().item(0).getNodeValue(), WATCHDOG_TIMEOUT); 
						}

						timeOut = ((Element)nl0.item(0)).getElementsByTagName("webService");
						if(timeOut != null && timeOut.getLength() > 0) {
							webServiceTimeOut = Utils.string2Int(timeOut.item(0).getChildNodes().item(0).getNodeValue(), WEBSERVICE_TIMEOUT);
						}
					}

					nl0 = doc.getElementsByTagName("maxTry");
					if(nl0 != null && nl0.getLength() > 0) {
						NodeList maxTryNode = ((Element)nl0.item(0)).getElementsByTagName("searchElement");
						if(maxTryNode != null && maxTryNode.getLength() > 0) {
							maxTrySearch = Utils.string2Int(maxTryNode.item(0).getChildNodes().item(0).getNodeValue(), MAX_TRY_SEARCH);
						}

						maxTryNode = ((Element)nl0.item(0)).getElementsByTagName("getProperty");
						if(maxTryNode != null && maxTryNode.getLength() > 0) {
							maxTryProperty = Utils.string2Int(maxTryNode.item(0).getChildNodes().item(0).getNodeValue(), MAX_TRY_PROPERTY);
						}

						maxTryNode = ((Element)nl0.item(0)).getElementsByTagName("webService");
						if(maxTryNode != null && maxTryNode.getLength() > 0) {
							maxTryWebservice = Utils.string2Int(maxTryNode.item(0).getChildNodes().item(0).getNodeValue(), MAX_TRY_WEBSERVICE);
							if(maxTryWebservice < 1) {
								maxTryWebservice = MAX_TRY_WEBSERVICE;
							}
						}
					}

					nl0 = doc.getElementsByTagName("proxy");
					if(nl0 != null && nl0.getLength() > 0) {
						NodeList data = ((Element)nl0.item(0)).getElementsByTagName("type");
						if(data != null && data.getLength() > 0) {

							String type = data.item(0).getChildNodes().item(0).getNodeValue();

							if(AtsProxy.DIRECT.equals(type)) {

								proxy = new AtsProxy(AtsProxy.DIRECT);

							}else if(AtsProxy.AUTO.equals(type)) {

								proxy = new AtsProxy(AtsProxy.AUTO);

							}else if(AtsProxy.MANUAL.equals(type)) {

								String host = null;
								int port = -1;

								data = ((Element)nl0.item(0)).getElementsByTagName("host");
								if(data != null && data.getLength() > 0) {
									host = data.item(0).getChildNodes().item(0).getNodeValue();
								}

								data = ((Element)nl0.item(0)).getElementsByTagName("port");
								if(data != null && data.getLength() > 0) {
									port = Utils.string2Int(data.item(0).getChildNodes().item(0).getNodeValue());
								}

								if(host != null && port > 0) {
									proxy = new AtsProxy(AtsProxy.MANUAL, host, port);
								}
							}
						}
					}

					nl0 = doc.getElementsByTagName("recorder");
					if(nl0 != null && nl0.getLength() > 0) {
						for (int temp = 0; temp < nl0.getLength(); temp++) {
							Node recorder = nl0.item(temp);
							if (recorder.getNodeType() == Node.ELEMENT_NODE) {
								Element recorderElement = (Element) recorder;
								if(recorderElement.hasChildNodes() && recorderElement.getChildNodes().getLength() > 1) {

									String host = null;
									NodeList nl = recorderElement.getElementsByTagName("host");
									if(nl != null && nl.getLength() > 0) {
										if(nl.item(0).getChildNodes().getLength() > 0) {
											host = nl.item(0).getChildNodes().item(0).getNodeValue();
										}
									}

									String port = null;
									nl = recorderElement.getElementsByTagName("port");
									if(nl != null && nl.getLength() > 0) {
										if(nl.item(0).getChildNodes().getLength() > 0) {
											port = nl.item(0).getChildNodes().item(0).getNodeValue();
										}
									}

									if(host != null && port != null) {
										neoloadProxy = new AtsProxy(host, port);
									}
								}
							}
						}
					}

					if(neoloadProxy != null) {
						nl0 = doc.getElementsByTagName("design");
						if(nl0 != null && nl0.getLength() > 0) {
							for (int temp = 0; temp < nl0.getLength(); temp++) {
								Node design = nl0.item(temp);
								if (design.getNodeType() == Node.ELEMENT_NODE) {
									Element designElement = (Element) design;
									if(designElement.hasChildNodes() && designElement.getChildNodes().getLength() > 1) {

										String api = null;
										NodeList nl = designElement.getElementsByTagName("api");
										if(nl != null && nl.getLength() > 0) {
											if(nl.item(0).getChildNodes().getLength() > 0) {
												api = nl.item(0).getChildNodes().item(0).getNodeValue();
												if(!api.startsWith("/")) {
													api = "/" + api;
												}
												if(!api.endsWith("/")) {
													api = api + "/";
												}
											}
										}

										int port = 0;
										nl = designElement.getElementsByTagName("port");
										if(nl != null && nl.getLength() > 0) {
											if(nl.item(0).getChildNodes().getLength() > 0) {
												port = Utils.string2Int(nl.item(0).getChildNodes().item(0).getNodeValue());
											}
										}

										if(api != null && port > 0) {
											neoloadDesignApi = "http://" + neoloadProxy.getHost() + ":" + port + api;
										}
									}
								}
							}
						}
					}

					nl0 = doc.getElementsByTagName("application");
					if(nl0 != null && nl0.getLength() > 0) {
						for (int temp = 0; temp < nl0.getLength(); temp++) {
							Node application = nl0.item(temp);
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

											addApplicationProperties(ApplicationProperties.DESKTOP_TYPE, name, path, wait, "", null, null);
										}
									}
								}
							}
						}
					}

					nl0 = doc.getElementsByTagName("mobile");
					if(nl0 != null && nl0.getLength() > 0) {
						for (int temp = 0; temp < nl0.getLength(); temp++) {
							Node app = nl0.item(temp);
							if (app.getNodeType() == Node.ELEMENT_NODE) {
								Element mobileApp = (Element) app;
								if(mobileApp.hasChildNodes() && mobileApp.getChildNodes().getLength() > 1) {
									NodeList nodeList = mobileApp.getElementsByTagName("name");
									if(nodeList != null && nodeList.getLength() > 0) {
										if(nodeList.item(0).getChildNodes().getLength() > 0) {
											String name = nodeList.item(0).getChildNodes().item(0).getNodeValue();
											String url = "";
											String wait = null;

											nodeList = mobileApp.getElementsByTagName("endpoint");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													url = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}

											nodeList = mobileApp.getElementsByTagName("package");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													url += "/" + nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}

											nodeList = mobileApp.getElementsByTagName("waitAction");
											if(nodeList != null && nodeList.getLength() > 0) {
												if(nodeList.item(0).getChildNodes().getLength() > 0) {
													wait = nodeList.item(0).getChildNodes().item(0).getNodeValue();
												}
											}

											addApplicationProperties(ApplicationProperties.MOBILE_TYPE, name, url, wait, "", null, null);
										}
									}
								}
							}
						}
					}

					nl0 = doc.getElementsByTagName("api");
					if(nl0 != null && nl0.getLength() > 0) {
						for (int temp = 0; temp < nl0.getLength(); temp++) {
							Node app = nl0.item(temp);
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

											addApplicationProperties(ApplicationProperties.API_TYPE, name, url, wait, "", null, null);
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

			}*/
		}

		return new Properties();
	}

	private void addApplicationProperties(int type, String name, String path, String wait, String check, String lang, String userDataDir, String title) {
		addApplicationProperties(type, name, null, path, wait, check, lang, userDataDir, title);
	}

	private void addApplicationProperties(int type, String name, String driver, String path, String wait, String check, String lang, String userDataDir, String title) {
		applicationsList.add(new ApplicationProperties(type, name, driver, path, Utils.string2Int(wait, -1), Utils.string2Int(check, -1), lang, userDataDir, title));
	}

	//------------------------------------------------------------------------------------------------------------------
	// Getters
	//------------------------------------------------------------------------------------------------------------------

	public String getError() {
		return error;
	}

	public ApplicationProperties getApplicationProperties(String name) {
		for (int i=0; i < this.applicationsList.size(); i++) {
			final ApplicationProperties properties = this.applicationsList.get(i);
			if (name.equals(properties.getName())){
				return properties;
			}
		}
		return new ApplicationProperties(name);
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

	public int getWebServiceTimeOut() {
		return webServiceTimeOut;
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

	public int getMaxTryMobile() {
		return maxTryMobile;
	}

	public int getMaxTryWebservice() {
		return maxTryWebservice;
	}

	public String getNeoloadDesignApi() {
		return neoloadDesignApi;
	}

	public AtsProxy getNeoloadProxy() {
		if(neoloadProxy != null) {
			return neoloadProxy;
		}
		return getProxy();
	}

	public AtsProxy getProxy() {
		return proxy;
	}

	public ArrayList<String> getBlackListServers() {
		return blackListServers;
	}

	public OctoperfApi getOctoperf() {
		return octoperf;
	}

	public int getTrafficIdle() {
		return trafficIdle;
	}
}