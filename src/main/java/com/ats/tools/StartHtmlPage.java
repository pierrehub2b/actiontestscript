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
import java.util.Base64;

import com.ats.driver.AtsManager;
import com.ats.executor.TestBound;
import com.google.common.io.Resources;

public final class StartHtmlPage {
    
    public static byte[] getAtsBrowserContent(String titleUid, String applicationVersion, String driverVersion, TestBound testBound) {
    	
    	if(driverVersion == null) {
    		driverVersion = "N/A";
    	}

    	StringBuilder htmlContent = new StringBuilder();
    	htmlContent.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>");
    	htmlContent.append(titleUid);
    	htmlContent.append("</title><style>span {font-family: Arial; color: #606b6f; text-shadow: 2px 2px 12px rgba(96,107,111,0.6)}</style></head><body bgcolor=\"#f2f2f2\"><a href=\"https://www.actiontestscript.com\"><img src=\"data:image/png;base64, ");
    	
    	try {
    		htmlContent.append(Base64.getEncoder().encodeToString(Resources.toByteArray(ResourceContent.class.getResource("/icon/ats_power.png"))));
		} catch (IOException e1) {}
    	
    	htmlContent.append("\" alt=\"ActionTestScript\"/></a><div style=\"padding-left: 40px;\"><span>- ActionTestScript version : ");
    	htmlContent.append(AtsManager.getVersion());
    	htmlContent.append("&nbsp;</span></div>");
    	
    	htmlContent.append("<div style=\"padding-left: 40px;\"><span>- Browser version : ");
    	htmlContent.append(applicationVersion);
    	htmlContent.append("&nbsp;</span></div>");
    	
    	htmlContent.append("<div style=\"padding-left: 40px;\"><span>- Driver version : ");
    	htmlContent.append(driverVersion);
    	htmlContent.append("&nbsp;</span></div>");
    	
    	htmlContent.append("<div style=\"padding-left: 40px;\"><span>- Browser position  : ");
    	htmlContent.append(testBound.getX());
    	htmlContent.append(" : ");
    	htmlContent.append(testBound.getY());
    	htmlContent.append("</span></div>");
    	
    	htmlContent.append("<div style=\"padding-left: 40px;\"><span>- Browser size  : ");
    	htmlContent.append(testBound.getWidth());
    	htmlContent.append(" x ");
    	htmlContent.append(testBound.getHeight());
    	htmlContent.append("</span></div></body></html>");
    			    	
    	return htmlContent.toString().getBytes();
    }
}
