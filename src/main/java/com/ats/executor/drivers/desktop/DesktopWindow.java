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

package com.ats.executor.drivers.desktop;

import com.ats.element.AtsElement;

public class DesktopWindow extends AtsElement {
	
	private int pid = -1;
	private int handle = -1;
	
	private String appName = "";
	private String appVersion = "";
	private String appBuildVersion = "";
	private String appPath = "";
	private byte[] appIcon;
	
	public byte[] getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(byte[] appIcon) {
		this.appIcon = appIcon;
	}

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public DesktopWindow() {
		super();
	}
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getHandle() {
		return handle;
	}

	public void setHandle(int handle) {
		this.handle = handle;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppBuildVersion() {
		return appBuildVersion;
	}

	public void setAppBuildVersion(String appBuildVersion) {
		this.appBuildVersion = appBuildVersion;
	}
}