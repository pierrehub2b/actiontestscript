import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ats.executor.drivers.engines.webservices.SoapApiExecutor;
import com.ats.executor.drivers.engines.webservices.SoapOperation;


public class Api {

	private static String soapXmlMessage;
	private static HashMap<String, SoapOperation> operations;
	private static Map<String, String> messages;

	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		//File wsdlFile = new File("C:\\Users\\agilitest\\Desktop\\docaposte.wsdl");
		/*File wsdlFile = new File("C:\\Users\\agilitest\\Desktop\\soapFlag.wsdl");

		try {
			loadDataFromWSDL(wsdlFile, "");
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WSDLParser parser = new WSDLParser();

		Definitions defs = parser.parse("C:\\Users\\agilitest\\Desktop\\docaposte.wsdl");

		List<Operation> ops = defs.getOperations();
		System.out.println(ops);*/

		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final Document document = builder.newDocument();
		
		final Element envelope = document.createElement("Envelope");
		envelope.setAttribute("xmlns", "http://schemas.xmlsoap.org/soap/envelope/");
		
		final Element body = document.createElement("Body");
		body.setAttribute("operationName", "hjkhk");
		envelope.appendChild(body);
		

		
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		StringWriter sw = new StringWriter();
		
		trans.transform(new DOMSource(envelope), new StreamResult(sw));
		System.out.println(sw.toString());


		operations = new HashMap<String, SoapOperation>(); 
		
		try {
			String[] data = SoapApiExecutor.parse(new File("C:\\Users\\agilitest\\Desktop\\docaposte.wsdl"), operations);
			//System.out.println(data[0] + " -- " + data[1]);
		} catch (SAXException | IOException | ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (Entry<String, SoapOperation> entry : operations.entrySet()) {
		    System.out.println(entry.getKey() + " / " + entry.getValue().getHeaderName() + " -- " + entry.getValue().getMessageName());
		}

		
	}

	public static String loadDataFromWSDL(File wsdlFile, HashMap<String, SoapOperation> operations) throws SAXException, IOException, ParserConfigurationException {
		
		String namespace = "";
		final HashMap<String, String> messages = new HashMap<String, String>();

		String attr ="http://www.w3.org/2001/XMLSchema";

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
			if(cmpAttribute.equals(attr)){
				tagPrefix =  document.getFirstChild().getAttributes().item(i).getNodeName().replace("xmlns:", "");
			}
		}


		Node ns = document.getFirstChild().getAttributes().getNamedItem("targetNamespace");
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
		if(nodeListOfOperations.getLength() == 0) {
			nodeListOfOperations = document.getElementsByTagName("wsdl:operation");
		}
		
		final int nodeLength = nodeListOfOperations.getLength();
		
		if(nodeLength > 0){
			
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
								if(action.getNodeValue() != null && action.getNodeValue().length() > 0) {
									operationAction = action.getNodeValue();
								}
								break;
							}
						}
					}
				}
				System.out.println(operationName + " -- " + operationAction);
				operations.put(operationName, new SoapOperation(operationAction));
			}
		}   

		if((document.getElementsByTagName("soap:address").getLength()>0)){
			NodeList addresses = document.getElementsByTagName("soap:address");
			if(addresses.getLength() > 0) {
				Node addressLocation = addresses.item(0).getAttributes().getNamedItem("location");
				if(addressLocation != null) {
					try {
						//setUri(addressLocation.getNodeValue());
					} catch (DOMException e) {}
				}
			}
		}
		
		return namespace;
	}

}
