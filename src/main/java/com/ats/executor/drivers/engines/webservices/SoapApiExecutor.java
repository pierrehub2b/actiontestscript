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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.script.actions.ActionApi;
import com.google.common.base.Charsets;

import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

public class SoapApiExecutor extends ApiExecutor {

	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	private String namespace = "";
	private Map<String, SoapOperation> operations;

	public SoapApiExecutor(PrintStream logStream, OkHttpClient client, int timeout, int maxTry, Channel channel, String wsdlContent, String wsUrl) 
			throws SAXException, IOException, ParserConfigurationException {

		super(logStream, client, timeout, maxTry, channel);

		final File wsdlFile = File.createTempFile("atsWs_", ".txt");
		wsdlFile.deleteOnExit();

		final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wsdlFile), Charsets.UTF_8));
		writer.write(wsdlContent);
		writer.flush();
		writer.close();

		this.setUri(wsUrl);

		operations = new HashMap<String, SoapOperation>();
		final String[] wsdlData = parse(wsdlFile, operations);

		this.namespace = wsdlData[0];
		this.setUri(wsdlData[1]);
	}

	public ArrayList<String> getOperations() {
		return new ArrayList<String>(operations.keySet());
	}

	@Override
	public void execute(ActionStatus status, ActionApi api) {

		super.execute(status, api);

		final String action = api.getMethod().getCalculated();
		final SoapOperation soapAction = operations.get(action);

		if(soapAction != null) {
			final String xmlInput = soapAction.getEnvelope(namespace, api.getData().getCalculated());
			final Builder requestBuilder = new Builder().
					post(RequestBody.create(null, xmlInput)).
					url(getUri().toString());

			requestBuilder.addHeader("Content-Type", "text/xml; charset=utf-8");
			requestBuilder.addHeader("SOAPAction", soapAction.getHeaderName());

			for (Entry<String,String> header : headerProperties.entrySet()) {
				requestBuilder.addHeader(header.getKey(), header.getValue());
			}

			executeRequest(status, requestBuilder.build());
		}else {
			status.setError(ActionStatus.WEB_DRIVER_ERROR, "SOAP operation does not exists ->  " + action);
		}
	}

	//------------------------------------------------------------------------------------------------------------------------
	// Static functions
	//------------------------------------------------------------------------------------------------------------------------

	public static String[] parse(File wsdlFile, Map<String, SoapOperation> operations) throws SAXException, IOException, ParserConfigurationException {

		String namespace = "";
		final HashMap<String, String> messages = new HashMap<String, String>();

		final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdlFile);
		if( document.getFirstChild().getNodeName().equalsIgnoreCase("#comment")){
			document.removeChild(document.getFirstChild());
		}  

		final NodeList messageList = document.getFirstChild().getChildNodes();
		for(int index = 0; index < messageList.getLength(); index++){
			final Node node = messageList.item(index);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				if("wsdl:message".equals(node.getNodeName()) || "message".equals(node.getNodeName())) {
					final NamedNodeMap attributes = node.getAttributes();
					if(attributes.getLength() > 0) {
						final String messageName = attributes.item(0).getNodeValue();
						final NodeList children = node.getChildNodes();
						for(int k = 0; k < children.getLength(); k++){
							if(children.item(k).getNodeType() == Node.ELEMENT_NODE) {
								String messageElement = children.item(k).getAttributes().getNamedItem("element").getTextContent();
								if(messageElement != null && messageElement.length() > 0) {
									final int doubleDot = messageElement.indexOf(":");
									if(doubleDot > -1){
										messageElement = messageElement.substring(doubleDot + 1);
									}
									messages.put(messageName, messageElement);
								}
							}
						}
					}
				}
			}
		}

		String tagPrefix = "";

		int l =  document.getFirstChild().getAttributes().getLength();
		for (int i = 0; i < l; i++) {
			String cmpAttribute =  document.getFirstChild().getAttributes().item(i).getNodeValue();
			if(cmpAttribute.equals(XML_SCHEMA)){
				tagPrefix =  document.getFirstChild().getAttributes().item(i).getNodeName().replace("xmlns:", "");
			}
		}

		final Node ns = document.getFirstChild().getAttributes().getNamedItem("targetNamespace");
		if(ns != null) {
			namespace = ns.getNodeValue();
		}	

		NodeList nd = document.getElementsByTagName(tagPrefix + ":import");
		if(nd.getLength() == 0) {
			nd = document.getElementsByTagName("wsdl:import");
		}

		if(nd.getLength() > 0){
			for (int k = 0; k < nd.item(0).getAttributes().getLength(); k++) {
				final String strAttributes = nd.item(0).getAttributes().item(k).getNodeName();
				if(namespace == null && strAttributes.equalsIgnoreCase("namespace")){
					namespace = nd.item(0).getAttributes().item(k).getNodeValue();
				}
			}
		}

		//Getting  Operations 
		NodeList nodeListOfOperations = document.getElementsByTagName("operation");
		int nodeLength = nodeListOfOperations.getLength();

		if(nodeLength == 0) {
			nodeListOfOperations = document.getElementsByTagName("wsdl:operation");
		}

		nodeLength = nodeListOfOperations.getLength();

		if(nodeLength > 0){

			for (int i = 0; i < nodeLength; i++) {
				final Node operation = nodeListOfOperations.item(i);

				final String operationName = operation.getAttributes().getNamedItem("name").getNodeValue();

				SoapOperation op = operations.get(operationName);
				if(op == null) {
					op = new SoapOperation(operationName);
					operations.put(operationName, op);
				}

				if(operation.getChildNodes() != null) {

					final String parentName = operation.getParentNode().getNodeName();
					if(parentName.endsWith("portType")) {

						final NodeList children = operation.getChildNodes();
						for(int j = 0; j < children.getLength(); j++) {
							final Node child = children.item(j);

							if(child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().endsWith("input")) {
								String messageElement = child.getAttributes().getNamedItem("message").getNodeValue();
								final int doubleDot = messageElement.indexOf(":");
								if(doubleDot > -1){
									messageElement = messageElement.substring(doubleDot + 1);
								}
								op.setMessageName(messages.get(messageElement));
							}
						}

					}else if(parentName.endsWith("binding")) {
						for(int j= 0; j<operation.getChildNodes().getLength(); j++) {
							final NamedNodeMap attributes = operation.getChildNodes().item(j).getAttributes();
							if(attributes != null) {
								final Node action = attributes.getNamedItem("soapAction");
								if(action != null) {
									if(action.getNodeValue() != null && action.getNodeValue().length() > 0) {
										op.setHeaderName(action.getNodeValue());
									}
									break;
								}
							}
						}
					}
				}
			}
		}   

		String addressLocation = null;
		if((document.getElementsByTagName("soap:address").getLength()>0)){
			NodeList addresses = document.getElementsByTagName("soap:address");
			if(addresses.getLength() > 0) {
				final Node location = addresses.item(0).getAttributes().getNamedItem("location");
				if(location != null) {
					try {
						addressLocation = location.getNodeValue();
					} catch (DOMException e) {}
				}
			}
		}

		return new String[] {namespace, addressLocation};
	}
}