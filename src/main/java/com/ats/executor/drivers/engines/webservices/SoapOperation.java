package com.ats.executor.drivers.engines.webservices;

public class SoapOperation {

	private static final String SOAP_ENVELOPE_OPEN = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body>";
	private static final String SOAP_ACTION = "<%1$s xmlns=\"%2$s\">%3$s</%1$s>";
	private static final String SOAP_ENVELOPE_CLOSE = "</Body></Envelope>";
	
	private String headerName;
	private String messageName;
	
	public SoapOperation(String name) {
		this.headerName = name;
		this.messageName = name;
	}
	
	public String getEnvelope(String namespace, String data) {
		return new StringBuilder().append(SOAP_ENVELOPE_OPEN).append(String.format(SOAP_ACTION, messageName, namespace, data)).append(SOAP_ENVELOPE_CLOSE).toString();
	}

	public String getHeaderName() {
		return headerName;
	}
	
	public void setHeaderName(String name) {
		this.headerName = name;
	}
	
	public String getMessageName() {
		return messageName;
	}
	
	public void setMessageName(String messageName) {
		if(messageName != null) {
			this.messageName = messageName;
		}
	}
}