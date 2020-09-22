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

package com.ats.tools;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.Base64;

public class ResourceContent {

	private static String scrollElementJavaScript;
	private static String searchElementsJavaScript;
	private static String documentSizeJavaScript;
	private static String elementAttributesJavaScript;
	private static String parentElementJavaScript;
	private static byte[] tick24Icon;
	private static String atsLogo;
	private static String pageStyle;

	static {
		documentSizeJavaScript = getScript("documentSize");
		elementAttributesJavaScript = getScript("elementAttributes");
		parentElementJavaScript = getScript("parentElement");
		scrollElementJavaScript = getScript("scrollElement");
		searchElementsJavaScript = getScript("searchElements");

		tick24Icon = getIcon("tick", 24);
		atsLogo = Base64.getEncoder().encodeToString(getAtsByteLogo());
		pageStyle = getData("jsStyle");		
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------

	private static String getScript(String ressourceName){
		try {
			final String javaScript = Resources.toString(ResourceContent.class.getResource("/javascript/" + ressourceName + ".js"), Charsets.UTF_8);
			return javaScript.replaceAll("[\n\t\r]", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static byte[] getIcon(String iconName, int iconSize){
		try {
			return ByteStreams.toByteArray(ResourceContent.class.getResourceAsStream("/icon/" + iconSize + "/" + iconName + ".png"));
		} catch (IOException e) {
			return new byte[0];
		}
	}

	public static byte[] getAtsByteLogo(){
		try {
			return Resources.toByteArray(ResourceContent.class.getResource("/icon/ats_power.png"));
		} catch (IOException e1) {
			return new byte[] {};
		}
	}
	
	public static String getData(String name) {
		try {
			return Resources.toString(ResourceContent.class.getResource("/" + name), Charsets.UTF_8);
		} catch (IOException e1) {
			return "";
		}
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------

	public static String getPageStyle() {
		return pageStyle;
	}
	
	public static String getAtsLogo() {
		return atsLogo;
	}
	
	public static byte[] getTick24Icon() {
		return tick24Icon;
	}

	public static String getScrollElementJavaScript() {
		return scrollElementJavaScript;
	}

	public static String getSearchElementsJavaScript() {
		return searchElementsJavaScript;
	}

	public static String getDocumentSizeJavaScript() {
		return documentSizeJavaScript;
	}

	public static String getElementAttributesJavaScript() {
		return elementAttributesJavaScript;
	}

	public static String getParentElementJavaScript() {
		return parentElementJavaScript;
	}
}