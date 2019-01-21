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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.text.StringEscapeUtils;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public abstract class AbstractApiExecutor implements IApiDriverExecutor {

	private final Pattern xmlPattern = Pattern.compile("<\\?xml (?:(?!>).)*>");
	private final Pattern xmlPropertyPattern = Pattern.compile(".*:");

	private final Pattern jsonObjectPattern = Pattern.compile("(?s)^\\{.*\\}$");
	private final Pattern jsonArrayPattern = Pattern.compile("(?s)^\\[.*\\]$");

	private final short TEXT_TYPE = 0;
	private final short JSON_TYPE = 1;
	private final short XML_TYPE = 2;

	private final String ELEMENT = "ELEMENT";
	private final String NODE = "NODE";
	private final String OBJECT = "OBJECT";
	private final String ARRAY = "ARRAY";	

	protected URI uri;
	private String source;
	private short type;

	private ActionApi lastAction;

	private ArrayList<AtsApiElement> atsElements;

	protected void setUri(String value) {
		try {
			this.uri = new URI(value);
		} catch (URISyntaxException e) {}
	}

	@Override
	public void execute(ActionStatus status, ActionApi action) {
		source = "";
		type = TEXT_TYPE;
		lastAction = action;
	}

	private void refresh(Channel channel) {
		if(lastAction != null) {
			execute(new ActionStatus(channel), lastAction);
		}
	}

	protected void parseResponse(String type, String source) {

		if(type.contains("/xml")) {

			this.type = XML_TYPE;
			this.source = xmlPattern.matcher(StringEscapeUtils.unescapeXml(source)).replaceAll("");

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				final DocumentBuilder db = dbf.newDocumentBuilder();
				final Document doc = db.parse(new InputSource(new StringReader(this.source)));
				loadElementsList(doc.getElementsByTagName("*"));
			} catch (SAXException | IOException | ParserConfigurationException e) {

			} 

		}else if(type.contains("/json")){
			this.type = JSON_TYPE;
			this.source = source.trim();

			if(jsonObjectPattern.matcher(this.source).matches() || jsonArrayPattern.matcher(this.source).matches()) {
				this.atsElements = new ArrayList<AtsApiElement>();
				loadElementsList(new JsonParser().parse(source), "root");
			}else {
				this.type = TEXT_TYPE;
			}

		}else {
			this.source = source;
		}
	}

	public String getSource() {
		return source;
	}

	public ArrayList<FoundElement> findElements(Channel channel, boolean sysComp, TestElement testObject, String tagName, ArrayList<String> attributes, Predicate<AtsBaseElement> predicate) {

		final String searchedTag = tagName.toUpperCase();
		final ArrayList<FoundElement> result = new ArrayList<FoundElement>();

		if(testObject.getParent() == null){
			refresh(channel);
		}else {
			Optional<AtsApiElement> parentElement = atsElements.stream().filter(e -> e.getId().equals(testObject.getParent().getFoundElement().getId())).findFirst();
			if(parentElement.isPresent()) {
				if(type == XML_TYPE) {
					Element elem = ((AtsXmlElement)parentElement.get()).getElement();
					if(elem != null) {
						loadElementsList(elem.getElementsByTagName("*"));
					}
				}else if(type == JSON_TYPE) {
					AtsJsonElement elem = (AtsJsonElement)parentElement.get();
					if(elem != null) {
						this.atsElements = new ArrayList<AtsApiElement>();
						loadElementsList(elem.getElement(), elem.getAttribute("name"));
					}
				}
			}
		}

		if("*".equals(tagName)) {
			atsElements.parallelStream().filter(predicate).forEach(e -> result.add(new FoundElement(e)));
		}else {
			atsElements.parallelStream().filter(e -> e.getTag().equals(searchedTag)).filter(predicate).forEach(e -> result.add(new FoundElement(e)));
		}

		return result;
	}


	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	// Json
	//-------------------------------------------------------------------------------------------------------------------------------------------------------

	private void loadElementsList(JsonElement json, String name) {

		HashMap<String, String> attributes = new HashMap<String, String>(Map.of("name", name));

		if(json.isJsonArray()) {
			atsElements.add(new AtsJsonElement(json, ARRAY, attributes));

			int index = 0;
			for (JsonElement el : json.getAsJsonArray()) {
				loadElementsList(el, "index" + index);
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

	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	// Xml
	//-------------------------------------------------------------------------------------------------------------------------------------------------------

	private void loadElementsList(NodeList nodeList) {

		this.atsElements = new ArrayList<AtsApiElement>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			Map<String, String> foundAttributes = new HashMap<String, String>();
			NamedNodeMap nodeAttributes = node.getAttributes();

			if(node.getNodeName() != null) {
				foundAttributes.put("name", xmlPropertyPattern.matcher(node.getNodeName()).replaceFirst(""));
			}

			for(int j=0; j<nodeAttributes.getLength(); j++) {
				Node attribute = nodeAttributes.item(j);
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
				NodeList properties = node.getChildNodes();
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
		Optional<AtsApiElement> elem = atsElements.stream().filter(e -> e.getId().equals(id)).findFirst();
		if(elem.isPresent()) {
			return elem.get().getAttribute(attributeName);
		}
		return null;
	}

	public CalculatedProperty[] getElementAttributes(String id) {
		Optional<AtsApiElement> elem = atsElements.stream().filter(e -> e.getId().equals(id)).findFirst();
		if(elem.isPresent()) {
			return elem.get().getAttributesMap().entrySet().stream().parallel().map(e -> new CalculatedProperty(e.getKey(), e.getValue())).toArray(c -> new CalculatedProperty[c]);
		}
		return null;
	}
}