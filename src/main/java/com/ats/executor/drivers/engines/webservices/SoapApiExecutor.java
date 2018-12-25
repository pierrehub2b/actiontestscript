package com.ats.executor.drivers.engines.webservices;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ats.executor.ActionStatus;
import com.ats.script.actions.ActionApi;

public class SoapApiExecutor extends AbstractApiExecutor {

	private static final String SOAP_ENVELOPE_OPEN = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"#NAMESPACE#\">";
	private static final String SOAP_BODY_OPEN = "<soapenv:Body>";
	private static final String SOAP_ACTION_OPEN = "<web:#ACTION# soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">";
	private static final String SOAP_ACTION_CLOSE = "</web:#ACTION#>";
	private static final String SOAP_BODY_CLOSE = "</soapenv:Body>";
	private static final String SOAP_ENVELOPE_CLOSE = "</soapenv:Envelope>";

	private String nameSpace;
	private String soapXmlMessage;
	private Set<String> operations;

	public SoapApiExecutor(File wsdlFile, String wsUrl) throws SAXException, IOException, ParserConfigurationException {
		super(wsUrl);

		this.loadDataFromWSDL(wsdlFile, wsUrl);

		StringBuilder builder = new StringBuilder(SOAP_ENVELOPE_OPEN).append(SOAP_BODY_OPEN).append(SOAP_ACTION_OPEN).append("#ACTIONDATA#").append(SOAP_ACTION_CLOSE).append(SOAP_BODY_CLOSE).append(SOAP_ENVELOPE_CLOSE);
		this.soapXmlMessage = builder.toString().replace("#NAMESPACE#", nameSpace);
	}

	@Override
	public String execute(ActionStatus status, ActionApi api) {

		final String action = api.getMethod().getCalculated();
		String xmlInput = soapXmlMessage.replaceAll("#ACTION#", action).replace("#ACTIONDATA#", api.getData().getCalculated());

		try {

			URLConnection connection = uri.toURL().openConnection();
			HttpURLConnection httpConn = (HttpURLConnection)connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			byte[] buffer = new byte[xmlInput.length()];
			buffer = xmlInput.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			httpConn.setRequestProperty("SOAPAction", action);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			OutputStream out = httpConn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = null;
			if (httpConn.getResponseCode() == 200) {
				isr = new InputStreamReader(httpConn.getInputStream());
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

			return builder.toString();

		}catch(IOException e) {}

		return null;
	}

	private void loadDataFromWSDL(File wsdlFile, String wsdlPath) throws SAXException, IOException, ParserConfigurationException {

		String tagPrefix = null;
		String location = null;
		NodeList nd = null;
		operations = new HashSet<String>(); 
		
		NodeList nodeListOfOperations = null;
		String attr ="http://www.w3.org/2001/XMLSchema";
	
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(wsdlFile);
		NodeList allNodesOfDocumnet = document.getChildNodes();

		for(int index = 0;index<allNodesOfDocumnet.getLength();index++){
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
				operations.add(operation.getAttributes().getNamedItem("name").getNodeValue()); 
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
					operations.add(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue()); 
				}
			}
		}     
	}
}