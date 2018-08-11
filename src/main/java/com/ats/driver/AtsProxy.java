package com.ats.driver;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;

import com.google.gson.JsonObject;

public class AtsProxy {

	public final static String AUTO = "auto";
	public final static String SYSTEM = "system";
	public final static String DIRECT = "direct";
	public final static String MANUAL = "manual";

	private String type;
	private String host;
	private int port;

	public AtsProxy(String type) {
		this.type = type;
	}

	public AtsProxy(String type, String host, int port) {
		this.type = type;
		this.host = host;
		this.port = port;
	}

	public String getType() {
		return type;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}

	//------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------

	public Proxy getProxy() {

		Proxy proxy = new Proxy();

		if(AUTO.equals(type)) {
			proxy.setProxyType(ProxyType.AUTODETECT);
		}else if(DIRECT.equals(type)) {
			proxy.setProxyType(ProxyType.DIRECT);
		}else if(MANUAL.equals(type)) {

			String proxyAddress = host + ":" + port;
			proxy.setHttpProxy(proxyAddress).setFtpProxy(proxyAddress).setSslProxy(proxyAddress);

		}else {
			proxy.setProxyType(ProxyType.SYSTEM);
		}

		return proxy;
	}

	public JsonObject getGeckoProxy() {

		JsonObject json = new JsonObject();
		
		if(MANUAL.equals(type)) {

			json.addProperty("proxyType", "MANUAL");
			json.addProperty("httpProxy", host);
			json.addProperty("httpProxyPort", port);
			json.addProperty("sslProxy", host);
			json.addProperty("sslProxyPort", port);

		}else {
			json.addProperty("proxyType", type.toUpperCase());
		}

		return json;
	}
}
