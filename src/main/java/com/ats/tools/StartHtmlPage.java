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

import com.ats.driver.AtsManager;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.desktop.DesktopDriver;

public final class StartHtmlPage {

	public static byte[] getAtsBrowserContent(
			String titleUid,
			String browserName,
			String browserPath,
			String browserVersion, 
			String driverVersion, 
			TestBound testBound,
			int actionWait,
			int propertyWait,
			int maxtry,
			int maxTryProperty,
			int scriptTimeout,
			int pageLoadTimeout,
			int watchdog, DesktopDriver desktopDriver) {

		if(driverVersion == null) {
			driverVersion = "--";
		}
		
		if(browserVersion == null) {
			browserVersion = "--";
		}
		
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");

		htmlContent.append(ResourceContent.getPageStyle());
		
		htmlContent.append("<title>");
		htmlContent.append(titleUid);
		htmlContent.append("</title></head><body bgcolor=\"#f2f2f2\"><div><div id=\"header\"><div class=\"clearfix\">");
		htmlContent.append("ActionTestScript (ver. ");
		htmlContent.append(AtsManager.getVersion());
		htmlContent.append(")");
		htmlContent.append("</div></div><div id=\"content-wrapper\"><div class=\"site\"><div class=\"article js-hide-during-search\"><a href=\"https://www.actiontestscript.com\"><img src=\"data:image/png;base64, ");

		htmlContent.append(ResourceContent.getAtsLogo());

		htmlContent.append("\" alt=\"ActionTestScript\"/></a><div class=\"article-body content-body wikistyle markdown-format\"><div class=\"intro\" style=\"margin-left:30px\">");

		htmlContent.append("<p><strong>ActionTestScript version : </strong>");
		htmlContent.append(AtsManager.getVersion());

		htmlContent.append("<br><strong>Browser driver version : </strong>");
		htmlContent.append(driverVersion);
		
		htmlContent.append("<br><strong>Desktop driver version : </strong>");
		htmlContent.append(desktopDriver.getDriverVersion());
		
		htmlContent.append("<br><strong>Search element max try : </strong>");
		htmlContent.append(maxtry);
		
		htmlContent.append("<br><strong>Get property max try : </strong>");
		htmlContent.append(maxTryProperty);
		
		htmlContent.append("<br>JavaScript execution time out : ");
		htmlContent.append(scriptTimeout);
		htmlContent.append(" s");
		
		htmlContent.append("<br>Page load time out : ");
		htmlContent.append(pageLoadTimeout);
		htmlContent.append(" s");
		
		htmlContent.append("<br>Action execution watchdog : ");
		htmlContent.append(watchdog);
		htmlContent.append(" s");

		htmlContent.append("</p></div><div class=\"alert note\" style=\"margin-left:30px;min-width: 360px;display: inline-block\"><p>");
		htmlContent.append("<strong>Browser : </strong>");
		htmlContent.append("<br><strong>  - Name : </strong>");
		htmlContent.append(browserName);
		htmlContent.append("<br><strong>  - Version : </strong>");
		htmlContent.append(browserVersion);

		if(browserPath != null) {
			htmlContent.append("<br><strong>  - Binary path : </strong>");
			htmlContent.append(browserPath);
		}

		htmlContent.append("<br><strong>  - Start position : </strong>");
		htmlContent.append(testBound.getX().intValue());
		htmlContent.append(" x ");
		htmlContent.append(testBound.getY().intValue());
		
		htmlContent.append("<br><strong>  - Start size : </strong>");
		htmlContent.append(testBound.getWidth().intValue());
		htmlContent.append(" x ");
		htmlContent.append(testBound.getHeight().intValue());
		
		htmlContent.append("<br><strong>  - Wait after action : </strong>");
		htmlContent.append(actionWait);
		htmlContent.append(" ms");
		
		htmlContent.append("<br><strong>  - Double check property : </strong>");
		htmlContent.append(propertyWait);
		htmlContent.append(" ms");		
		
		htmlContent.append("</p></div><div class=\"alert warning\" style=\"margin-left:30px;min-width: 360px;display: inline-block\"><p>");
		htmlContent.append("<strong>Operating System : </strong>");
		htmlContent.append("<br><strong>  - Machine name : </strong>");
		htmlContent.append(desktopDriver.getMachineName());
		htmlContent.append("<br><strong>  - System name : </strong>");
		htmlContent.append(desktopDriver.getOsName());
		htmlContent.append("<br><strong>  - System version : </strong>");
		htmlContent.append(desktopDriver.getOsVersion());
		htmlContent.append("<br><strong>  - Build version : </strong>");
		htmlContent.append(desktopDriver.getOsBuildVersion());
		htmlContent.append("<br><strong>  - Country code : </strong>");
		htmlContent.append(desktopDriver.getCountryCode());
		htmlContent.append("<br><strong>  - DotNET version : </strong>");
		htmlContent.append(desktopDriver.getDotNetVersion());

		htmlContent.append("<br><strong>  - Resolution : </strong>");
		htmlContent.append(desktopDriver.getScreenResolution());
		
		htmlContent.append("<br><strong>  - Processor name : </strong>");
		htmlContent.append(desktopDriver.getCpuName());
		htmlContent.append("<br><strong>  - Processor socket : </strong>");
		htmlContent.append(desktopDriver.getCpuSocket());
		htmlContent.append("<br><strong>  - Processor architecture : </strong>");
		htmlContent.append(desktopDriver.getCpuArchitecture());
		htmlContent.append("<br><strong>  - Processor max speed : </strong>");
		htmlContent.append(desktopDriver.getCpuMaxClock());
		htmlContent.append("<br><strong>  - Processor cores : </strong>");
		htmlContent.append(desktopDriver.getCpuCores());
		
		htmlContent.append("<br><strong>  - Current drive letter : </strong>");
		htmlContent.append(desktopDriver.getDriveLetter());
		
		htmlContent.append("<br><strong>  - Disk total size : </strong>");
		htmlContent.append(desktopDriver.getDiskTotalSize());
		
		htmlContent.append("<br><strong>  - Disk free space : </strong>");
		htmlContent.append(desktopDriver.getDiskFreeSpace());		

		htmlContent.append("</p></div></div></body></html>");

		return htmlContent.toString().getBytes();
	}
}
