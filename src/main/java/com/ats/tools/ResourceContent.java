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

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

public class ResourceContent {

	private static String visibilityJavaScript;
	private static String scrollElementJavaScript;
	private static String searchElementsJavaScript;
	private static String documentSizeJavaScript;
	private static String hoverElementJavaScript;
	private static String elementAttributesJavaScript;
	private static String parentElementJavaScript;
	private static String elementCssJavaScript;
	private static String readyStatesJavaScript;
	
	private static byte[] tick24Icon;

	static {
		visibilityJavaScript = getScript("visibility");
		scrollElementJavaScript = getScript("scrollElement");
		searchElementsJavaScript = getScript("searchElements");
		documentSizeJavaScript = getScript("documentSize");
		hoverElementJavaScript = getScript("hoverElement");
		elementAttributesJavaScript = getScript("elementAttributes");
		parentElementJavaScript = getScript("parentElement");
		elementCssJavaScript = getScript("elementCss");
		readyStatesJavaScript = getScript("readyStates");
		
		tick24Icon = getIcon("tick", 24);
	}

	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------
	
	private static String getScript(String ressourceName){
		try {
			String javaScript = Resources.toString(ResourceContent.class.getResource("/javascript/" + ressourceName + ".js"), Charsets.UTF_8);
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
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------
	
	public static byte[] getTick24Icon() {
		return tick24Icon;
	}
	
	public static String getVisibilityJavaScript() {
		return visibilityJavaScript;
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

	public static String getHoverElementJavaScript() {
		return hoverElementJavaScript;
	}

	public static String getElementAttributesJavaScript() {
		return elementAttributesJavaScript;
	}

	public static String getParentElementJavaScript() {
		return parentElementJavaScript;
	}

	public static String getElementCssJavaScript() {
		return elementCssJavaScript;
	}

	public static String getReadyStatesJavaScript() {
		return readyStatesJavaScript;
	}
}