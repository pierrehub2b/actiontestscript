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

import java.net.InetSocketAddress;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;

import com.ats.tools.Utils;

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
	
	public AtsProxy(String host, String port) {
		this.type = MANUAL;
		this.host = host;
		this.port = Utils.string2Int(port, 8080);
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

	public Proxy getValue() {

		Proxy proxy = new Proxy();

		if(AUTO.equals(type)) {
			proxy.setProxyType(ProxyType.AUTODETECT);
		}else if(DIRECT.equals(type)) {
			proxy.setProxyType(ProxyType.DIRECT);
		}else if(MANUAL.equals(type)) {

			final String proxyAddress = host + ":" + port;
			proxy.setHttpProxy(proxyAddress).setFtpProxy(proxyAddress).setSslProxy(proxyAddress);

		}else {
			proxy.setProxyType(ProxyType.SYSTEM);
		}

		return proxy;
	}
	
	public java.net.Proxy getHttpProxy() {
		if(MANUAL.equals(type)){
			return new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port));
		}
		return null;
	}

	/*public JsonObject getGeckoProxy() {

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
	}*/
}
