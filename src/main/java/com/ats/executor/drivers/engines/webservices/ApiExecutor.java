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

package com.ats.executor.drivers.engines.webservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ats.element.AtsBaseElement;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.element.api.AtsApiElement;
import com.ats.element.api.AtsJsonElement;
import com.ats.element.api.AtsXmlElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionApi;
import com.ats.tools.logger.MessageCode;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class ApiExecutor implements IApiDriverExecutor {

	private final static String TRUNCATED_DATA = "TRUNCATED_DATA";
	private final static Pattern xmlPropertyPattern = Pattern.compile(".*:");

	private final static short TEXT_TYPE = 0;
	private final static short JSON_TYPE = 1;
	private final static short XML_TYPE = 2;

	private final static String ELEMENT = "ELEMENT";
	private final static String NODE = "NODE";
	private final static String OBJECT = "OBJECT";
	private final static String ARRAY = "ARRAY";	
	public final static String RESPONSE = "RESPONSE";
	public final static String DATA = "DATA";

	private URI uri;
	private String source;
	private short type;

	private ActionApi lastAction;

	private ArrayList<AtsApiElement> atsElements;

	protected Map<String, String> headerProperties;

	protected String authentication;
	protected String authenticationValue;

	protected int timeout;
	protected int maxTry;

	protected Channel channel;

	protected OkHttpClient client;
	private Response response;

	private PrintStream logStream;

	public ApiExecutor(PrintStream logStream, OkHttpClient client, int timeout, int maxTry, Channel channel) {
		this.logStream = logStream;
		this.client = client;
		this.timeout = timeout;
		this.maxTry = maxTry;
		this.channel = channel;
		this.authentication = channel.getAuthentication();
		this.authenticationValue = channel.getAuthenticationValue();
	}

	protected void setUri(String value) {
		try {
			this.uri = new URI(value);
		} catch (URISyntaxException e) {}
	}

	protected URI getUri() {
		if(lastAction.getPort() > -1) {
			try {
				final URL originalURL = uri.toURL();
				return (new URL(originalURL.getProtocol(), originalURL.getHost(), lastAction.getPort(), originalURL.getFile())).toURI();
			} catch (MalformedURLException | URISyntaxException e) {}
		}
		return uri;
	}

	protected URI getMethodUri() {
		return getUri().resolve(lastAction.getMethod().getCalculated());
	}

	@Override
	public void execute(ActionStatus status, ActionApi action) {
		source = "";
		type = TEXT_TYPE;
		lastAction = action;

		status.setMessage("authentication");

		headerProperties = new HashMap<String, String>();
		if(authentication != null && authenticationValue != null && authentication.length() > 0 && authenticationValue.length() > 0) {
			headerProperties.put("Authorization", authentication + " " + authenticationValue);
			status.setData(authentication);
		}else {
			status.setData("");
		}

		for (CalculatedProperty property : action.getHeader()) {
			headerProperties.put(property.getName(), property.getValue().getCalculated());
		}
	}

	protected void addHeader(String key, String value) {
		if(!headerProperties.containsKey(key)) {
			headerProperties.put(key, value);
		}
	}

	private void refresh(Channel channel) {
		if(lastAction != null && !lastAction.isUseCache()) {
			execute(channel.newActionStatus(), lastAction);
		}
	}

	//------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------

	protected void executeRequest(ActionStatus status, final Request request) {

		logStream.println("call request -> " + request.url().toString());

		int max = maxTry;
		while(!clientCall(status, request) && max > 0) {
			channel.sendLog(MessageCode.PROPERTY_TRY_ASSERT, "Call webservice failed", max);
			channel.sleep(500);
			max--;
		}

		if(max == 0) {
			logStream.println("call request failed -> " + status.getFailMessage());
		}
	}

	private boolean clientCall(ActionStatus status, Request request) {

		String type = "";

		try {

			response = client.newCall(request).execute();
			final List<String> contentTypes = response.headers("Content-Type");

			if(contentTypes != null && contentTypes.size() > 0) {
				type = contentTypes.get(0);
			}

			File tempFile = Files.createTempFile("ats-", ".tmp").toFile();
			try (OutputStream output = new FileOutputStream(tempFile)) {
				response.body().byteStream().transferTo(output);
			} catch (IOException ioException) {

			}

			source = parseResponse(type, tempFile);
			tempFile.delete();

			response.close();

			return true;

		} catch (IOException e) {
			status.setError(ActionStatus.WEB_DRIVER_ERROR, "call Webservice error -> " + e.getMessage());
		}

		return false;
	}

	//------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------

	private String parseResponse(String type, File temp) throws FileNotFoundException {

		final InputStreamReader isr = new InputStreamReader(new FileInputStream(temp), StandardCharsets.UTF_8);
		initAtsElements();

		if(type.contains("/xml")) {

			try {
				final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(isr));

				this.type = XML_TYPE;	
				
				final NodeList streams = doc.getElementsByTagName("Stream");
				if(streams != null && streams.getLength() > 0) {

					for(int i=0; i<streams.getLength(); i++) {

						final Element stream = (Element)streams.item(i);
						final int len = stream.getTextContent().length();

						if(len > 100000) {
							final Node streamParent = stream.getParentNode();
							streamParent.removeChild(stream);

							final Element newStream = doc.createElement("Stream");
							newStream.setTextContent("[" + TRUNCATED_DATA + ", size:" + len + "]");
							streamParent.appendChild(newStream);
						}
					}
					doc.normalize();
				}

				loadElementsList(doc.getElementsByTagName("*"));

				final Transformer transform = TransformerFactory.newInstance().newTransformer();
				transform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				
				final StringWriter writer = new StringWriter();
				transform.transform(new DOMSource(doc), new StreamResult(writer));
				
				return "<root><RESPONSE code=\"" + response.code() + "\"><data>" + writer.getBuffer().toString() + "</data></RESPONSE></root>";

			}catch(SAXException | IOException | TransformerException | ParserConfigurationException e) {

			}

		}else {

			try {
				final JsonElement root = JsonParser.parseReader(isr);

				this.type = JSON_TYPE;
				loadElementsList(root, "root");

				return "{\"response\":{\"name\":\"response\",\"code\":" + response.code() + ",\"data\":" + root.toString() + "}}"; 

			}catch(JsonIOException | JsonSyntaxException e) {

			}
		}
		
		this.type = TEXT_TYPE;
		
		final String content = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
		atsElements.add(new AtsApiElement(DATA, ImmutableMap.of("value", content)));

		return "<ats_response><code>" + response.code() + "</code><data><![CDATA[" + content + "]]></data></ats_response>";
	}

	private void initAtsElements(){
		this.atsElements = new ArrayList<AtsApiElement>();
		atsElements.add(new AtsApiElement(RESPONSE, ImmutableMap.of("name", "response", "code", response.code() + "")));
	}

	public String getSource() {
		return source;
	}

	public ArrayList<FoundElement> findElements(Channel channel, boolean sysComp, TestElement testObject, String tagName, String[] attributes, Predicate<AtsBaseElement> predicate) {

		final String searchedTag = tagName.toUpperCase();
		final ArrayList<FoundElement> result = new ArrayList<FoundElement>();

		if(testObject.getParent() == null){
			refresh(channel);
		}else {
			final Optional<AtsApiElement> parentElement = atsElements.stream().filter(e -> e.getId().equals(testObject.getParent().getFoundElement().getId())).findFirst();
			if(parentElement.isPresent()) {
				if(!parentElement.get().isResponse()) {
					if(type == XML_TYPE) {
						final Element elem = ((AtsXmlElement)parentElement.get()).getElement();
						if(elem != null) {
							initAtsElements();
							loadElementsList(elem.getElementsByTagName("*"));
						}
					}else if(type == JSON_TYPE) {
						final AtsJsonElement elem = (AtsJsonElement)parentElement.get();
						if(elem != null) {
							initAtsElements();
							loadElementsList(elem.getElement(), elem.getAttribute("name"));
						}
					}
				}
			}
		}

		if("*".equals(tagName)) {
			atsElements.stream().filter(predicate).forEach(e -> result.add(new FoundElement(e)));
		}else {
			atsElements.stream().filter(e -> e.getTag().equals(searchedTag)).filter(predicate).forEach(e -> result.add(new FoundElement(e)));
		}

		return result;
	}


	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	// Json
	//-------------------------------------------------------------------------------------------------------------------------------------------------------

	private void loadElementsList(JsonElement json, String name) {

		final HashMap<String, String> attributes = new HashMap<String, String>(Map.of("name", name));

		if(json.isJsonArray()) {

			final JsonArray array = json.getAsJsonArray();
			attributes.put("size", array.size() + "");

			atsElements.add(new AtsJsonElement(json, ARRAY, attributes));

			int index = 0;
			for (JsonElement el : array) {
				if(el.isJsonPrimitive()) {
					loadArrayList(el, "index" + index, el.getAsString());
				}else {
					loadElementsList(el, "index" + index);
				}

				index++;
			}

		}else if(json.isJsonObject()) {

			for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
				final String attributeName = entry.getKey();
				final JsonElement attributeValue = entry.getValue();

				if(attributeValue.isJsonPrimitive()) {
					if(attributes.containsKey(attributeName)) {
						attributes.replace(attributeName, attributeValue.getAsJsonPrimitive().getAsString());
					}else {
						attributes.put(attributeName, attributeValue.getAsJsonPrimitive().getAsString());
					}
				}else {
					loadElementsList(attributeValue, attributeName);
				}
			}

			atsElements.add(new AtsJsonElement(json, OBJECT, attributes));
		}
	}

	private void loadArrayList(JsonElement el, String item, String value) {
		atsElements.add(new AtsJsonElement(el, ELEMENT, ImmutableMap.of("name", item, "value", value)));
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	// Xml
	//-------------------------------------------------------------------------------------------------------------------------------------------------------

	private void loadElementsList(NodeList nodeList) {

		for (int i = 0; i < nodeList.getLength(); i++) {

			final Node node = nodeList.item(i);
			final String nodeName = node.getNodeName();

			final Map<String, String> foundAttributes = new HashMap<String, String>();
			final NamedNodeMap nodeAttributes = node.getAttributes();

			if(nodeName != null) {
				foundAttributes.put("name", xmlPropertyPattern.matcher(nodeName).replaceFirst(""));
			}

			for(int j=0; j<nodeAttributes.getLength(); j++) {
				final Node attribute = nodeAttributes.item(j);
				addAttribute(foundAttributes, xmlPropertyPattern.matcher(attribute.getNodeName()).replaceFirst(""), attribute.getNodeValue());
			}

			boolean hasChild = false;
			final NodeList nodeChild = node.getChildNodes();
			for(int j=0; j<nodeChild.getLength(); j++) {
				Node nodeChildElement = nodeChild.item(j);
				if(nodeChildElement.getNodeType() == Node.ELEMENT_NODE) {
					hasChild = true;
				}else {

					String nodeChildValue = nodeChildElement.getNodeValue();
					if(nodeChildValue != null) {
						nodeChildValue = nodeChildValue.trim();
						if(nodeChildValue.length() > 0) {
							if(nodeChildElement.getNodeType() == Node.TEXT_NODE) {
								foundAttributes.put("text", nodeChildValue);
							}else if(nodeChildElement.getNodeType() == Node.COMMENT_NODE) {
								foundAttributes.put("comment", nodeChildValue);
							}
						}
					}
				}
			}

			if(hasChild) {//Node
				atsElements.add(new AtsXmlElement(node, NODE, foundAttributes));
			}else{//Element
				final NodeList properties = node.getChildNodes();
				for(int j=0; j<properties.getLength(); j++) {
					Node property = properties.item(j);
					String propertyValue = property.getNodeValue();
					if(propertyValue != null) {
						propertyValue = propertyValue.trim();
						if(propertyValue.length() > 0) {
							String propertyName = property.getNodeName();
							if("#text".equals(propertyName)) {
								propertyName = "value";
							}else {
								xmlPropertyPattern.matcher(propertyName).replaceFirst("");
							}
							addAttribute(foundAttributes, propertyName, propertyValue);
						}
					}
				}
				atsElements.add(new AtsXmlElement(node, ELEMENT, foundAttributes));
			}
		}
	}

	private void addAttribute(Map<String, String> map, String propertyName, String propertyValue) {
		if(!"xsd".equals(propertyName) && !"xsi".equals(propertyName)) {
			map.put(propertyName, propertyValue);
		}
	}

	public String getElementAttribute(String id, String attributeName, int maxTry) {
		final Optional<AtsApiElement> elem = atsElements.stream().filter(e -> e.getId().equals(id)).findFirst();
		if(elem.isPresent()) {
			return elem.get().getAttribute(attributeName);
		}
		return null;
	}

	public CalculatedProperty[] getElementAttributes(String id) {
		final Optional<AtsApiElement> elem = atsElements.stream().filter(e -> e.getId().equals(id)).findFirst();
		if(elem.isPresent()) {
			return elem.get().getAttributesMap().entrySet().stream().parallel().map(e -> new CalculatedProperty(e.getKey(), e.getValue())).toArray(c -> new CalculatedProperty[c]);
		}
		return null;
	}
}