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

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.script.actions.ActionApi;
import com.google.common.base.Charsets;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SoapApiExecutor extends ApiExecutor {

	private static final String SOAP_ENVELOPE_OPEN = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">";
	private static final String SOAP_BODY_OPEN = "<Body>";
	private static final String SOAP_ACTION_OPEN = "<#ACTION# xmlns=\"#NAMESPACE#\">";
	private static final String SOAP_ACTION_CLOSE = "</#ACTION#>";
	private static final String SOAP_BODY_CLOSE = "</Body>";
	private static final String SOAP_ENVELOPE_CLOSE = "</Envelope>";

	private String nameSpace;
	private String soapXmlMessage;
	private Map<String, String> operations;

	public SoapApiExecutor(PrintStream logStream, OkHttpClient client, int timeout, int maxTry, Channel channel, String wsdlContent, String wsUrl) throws SAXException, IOException, ParserConfigurationException {

		super(logStream, client, timeout, maxTry, channel);

		final File wsdlFile = File.createTempFile("atsWs_", ".txt");
		wsdlFile.deleteOnExit();

		final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wsdlFile), Charsets.UTF_8));
		writer.write(wsdlContent);
		writer.flush();
		writer.close();

		this.setUri(wsUrl);
		this.loadDataFromWSDL(wsdlFile, wsUrl);

		final StringBuilder builder = new StringBuilder(SOAP_ENVELOPE_OPEN).append(SOAP_BODY_OPEN).append(SOAP_ACTION_OPEN).append("#ACTIONDATA#").append(SOAP_ACTION_CLOSE).append(SOAP_BODY_CLOSE).append(SOAP_ENVELOPE_CLOSE);
		this.soapXmlMessage = builder.toString().replace("#NAMESPACE#", nameSpace);
	}

	public ArrayList<String> getOperations() {
		return new ArrayList<String>(operations.keySet());
	}

	@Override
	public void execute(ActionStatus status, ActionApi api) {

		super.execute(status, api);

		final String action = api.getMethod().getCalculated();
		final String xmlInput = soapXmlMessage.replaceAll("#ACTION#", action).replace("#ACTIONDATA#", api.getData().getCalculated());
		
		final Builder requestBuilder = new Builder().post(RequestBody.create(null, xmlInput)).url(getUri().toString());

		requestBuilder.addHeader("Content-Type", "text/xml; charset=utf-8");
		requestBuilder.addHeader("SOAPAction", operations.get(action));

		for (Entry<String,String> header : headerProperties.entrySet()) {
			requestBuilder.addHeader(header.getKey(), header.getValue());
		}
		
		executeRequest(status, requestBuilder.build());
	}

	private void loadDataFromWSDL(File wsdlFile, String wsdlPath) throws SAXException, IOException, ParserConfigurationException {

		String tagPrefix = null;
		String location = null;
		NodeList nd = null;
		operations = new HashMap<String, String>(); 

		NodeList nodeListOfOperations = null;
		String attr ="http://www.w3.org/2001/XMLSchema";

		final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdlFile);
		NodeList allNodesOfDocumnet = document.getChildNodes();

		for(int index = 0; index<allNodesOfDocumnet.getLength(); index++){
			if( document.getFirstChild().getNodeName().equalsIgnoreCase("#comment")){
				document.removeChild(document.getFirstChild());
			}    
		}

		int l =  document.getFirstChild().getAttributes().getLength();
		for (int i = 0; i < l; i++) {
			String cmpAttribute =  document.getFirstChild().getAttributes().item(i).getNodeValue();
			if(cmpAttribute.equals(attr)){
				tagPrefix =  document.getFirstChild().getAttributes().item(i).getNodeName().replace("xmlns:", "");
			}
		}

		String str1 = tagPrefix + ":import";
		String str2 = "wsdl:import";
		String str3 = "operation"; 
		String str4 = "wsdl:operation"; 

		Node ns = document.getFirstChild().getAttributes().getNamedItem("targetNamespace");
		if(ns != null) {
			nameSpace = ns.getNodeValue();
		}		

		if((document.getElementsByTagName(str1).getLength() > 0) || (document.getElementsByTagName(str2).getLength() > 0)){

			if(document.getElementsByTagName(tagPrefix + ":import").getLength() >0 )
				nd = document.getElementsByTagName(tagPrefix + ":import");

			else if (document.getElementsByTagName("wsdl:import").getLength() > 0) 
				nd = document.getElementsByTagName("wsdl:import");  

			for (int k = 0; k < nd.item(0).getAttributes().getLength(); k++) {
				String strAttributes = nd.item(0).getAttributes().item(k).getNodeName();

				if(nameSpace == null && strAttributes.equalsIgnoreCase("namespace")){
					nameSpace = nd.item(0).getAttributes().item(k).getNodeValue();
				}else {
					location = nd.item(0).getAttributes().item(k).getNodeValue();
				}
			}
		}   

		//Getting  Operations 

		if((document.getElementsByTagName(str3).getLength()>0)||(document.getElementsByTagName(str4).getLength()>0)){

			if(document.getElementsByTagName(str3).getLength()>0){
				nodeListOfOperations = document.getElementsByTagName(str3);
			}else if (document.getElementsByTagName(str4).getLength()>0) {
				nodeListOfOperations = document.getElementsByTagName(str4);
			}

			final int nodeLength = nodeListOfOperations.getLength();
			for (int i = 0; i < nodeLength; i++) {
				final Node operation = nodeListOfOperations.item(i);
				final String operationName = operation.getAttributes().getNamedItem("name").getNodeValue();
				String operationAction = operationName;

				if(operation.getChildNodes() != null) {
					for(int j= 0; j<operation.getChildNodes().getLength(); j++) {
						NamedNodeMap attributes = operation.getChildNodes().item(j).getAttributes();
						if(attributes != null) {
							Node action = attributes.getNamedItem("soapAction");
							if(action != null) {
								operationAction = action.getNodeValue();
								break;
							}
						}
					}
				}
				operations.put(operationName, operationAction);
			}
		}   

		if(location != null){ 
			if(operations.isEmpty()){   

				final Document documentForOperation = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdlFile);
				int nodeLength = documentForOperation.getChildNodes().getLength();

				for(int index = 0; index < nodeLength; index++){
					if( documentForOperation.getFirstChild().getNodeName().equalsIgnoreCase("#comment")){
						document.removeChild(document.getFirstChild());
					}       
				}

				final NodeList nodeList  = documentForOperation.getElementsByTagName(str4);
				nodeLength = nodeList.getLength();
				for (int i = 0; i < nodeLength; i++) {
					final Node operation = nodeList.item(i);

					final String operationName = operation.getAttributes().getNamedItem("name").getNodeValue();
					String operationAction = operationName;

					if(operation.getChildNodes() != null) {
						for(int j= 0; j<operation.getChildNodes().getLength(); j++) {
							NamedNodeMap attributes = operation.getChildNodes().item(j).getAttributes();
							if(attributes != null) {
								Node action = attributes.getNamedItem("soapAction");
								if(action != null) {
									operationAction = action.getNodeValue();
									break;
								}
							}
						}
					}
					operations.put(operationName, operationAction);
				}
			}
		} 

		if((document.getElementsByTagName("soap:address").getLength()>0)){
			NodeList addresses = document.getElementsByTagName("soap:address");
			if(addresses.getLength() > 0) {
				Node addressLocation = addresses.item(0).getAttributes().getNamedItem("location");
				if(addressLocation != null) {
					try {
						setUri(addressLocation.getNodeValue());
					} catch (DOMException e) {}
				}
			}
		}
	}
}