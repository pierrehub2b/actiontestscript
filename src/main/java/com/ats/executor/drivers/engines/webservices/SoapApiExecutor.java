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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpHost;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ats.executor.ActionStatus;
import com.ats.script.actions.ActionApi;

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

	private Proxy proxy;

	public SoapApiExecutor(HttpHost proxy, int timeout, String authentication, String authenticationValue, File wsdlFile, String wsUrl) throws SAXException, IOException, ParserConfigurationException {

		super(timeout, authentication, authenticationValue);

		if(proxy != null) {
			this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHostName(), proxy.getPort()));
		}

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

		try {

			HttpURLConnection httpConn;
			if(proxy != null) {
				httpConn = (HttpURLConnection)uri.toURL().openConnection(proxy);
			}else {
				httpConn = (HttpURLConnection)uri.toURL().openConnection();
			}
			httpConn.setConnectTimeout(timeout); 
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			byte[] buffer = new byte[xmlInput.length()];
			buffer = xmlInput.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			httpConn.setRequestProperty("SOAPAction", operations.get(action));
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			OutputStream out = httpConn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = null;
			String responseType = "";
			if (httpConn.getResponseCode() == 200) {
				isr = new InputStreamReader(httpConn.getInputStream());
				responseType = httpConn.getContentType();
			} else {

				isr = new InputStreamReader(httpConn.getErrorStream());

				status.setCode(ActionStatus.WEB_DRIVER_ERROR);
				status.setMessage("Execute soap action error");
				status.setPassed(false);
			}

			final BufferedReader in = new BufferedReader(isr);

			StringBuilder builder = new StringBuilder("");
			String responseString = "";
			while ((responseString = in.readLine()) != null) {
				builder.append(responseString);
			}

			in.close();

			parseResponse(responseType, builder.toString());

		}catch(IOException e) {
			status.setCode(ActionStatus.WEB_DRIVER_ERROR);
			status.setMessage("Execute soap action error : " + e.getMessage());
			status.setPassed(false);
		}
	}

	private void loadDataFromWSDL(File wsdlFile, String wsdlPath) throws SAXException, IOException, ParserConfigurationException {

		String tagPrefix = null;
		String location = null;
		NodeList nd = null;
		operations = new HashMap<String, String>(); 

		NodeList nodeListOfOperations = null;
		String attr ="http://www.w3.org/2001/XMLSchema";

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdlFile);
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
				nd =document.getElementsByTagName("wsdl:import");  

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
				nodeListOfOperations =document.getElementsByTagName(str3);
			}else if (document.getElementsByTagName(str4).getLength()>0) {
				nodeListOfOperations =document.getElementsByTagName(str4);
			}

			for (int i = 0; i < nodeListOfOperations.getLength(); i++) {
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

				Document documentForOperation = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdlFile);
				NodeList nodesOfNewDoc = documentForOperation.getChildNodes();
				for(int index = 0; index<nodesOfNewDoc.getLength(); index++){
					if( documentForOperation.getFirstChild().getNodeName().equalsIgnoreCase("#comment")){
						document.removeChild(document.getFirstChild());
					}       
				}

				NodeList nodeList  = documentForOperation.getElementsByTagName(str4);
				for (int i = 0; i < nodeList.getLength(); i++) {
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
						this.uri = new URI(addressLocation.getNodeValue());
					} catch (DOMException | URISyntaxException e) {}
				}
			}
		}
	}
}