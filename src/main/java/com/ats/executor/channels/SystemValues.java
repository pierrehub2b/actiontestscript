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

package com.ats.executor.channels;

public class SystemValues {

	public static final String SYS_OS_NAME = "os-name";
	public static final String SYS_OS_VERSION = "os-version";
	public static final String SYS_OS_BUILD = "os-build";
	public static final String SYS_COUNTRY = "country";
	public static final String SYS_MACHINE_NAME = "machine-name";
	public static final String SYS_APP_NAME = "app-name";
	public static final String SYS_APP_VERSION = "app-version";
	public static final String SYS_APP_PATH = "app-path";
	public static final String SYS_USER_NAME = "user-name";

	public final static String[] VALUES_NAME = new String[]{SYS_OS_NAME, SYS_OS_VERSION, SYS_OS_BUILD, SYS_COUNTRY, SYS_MACHINE_NAME, SYS_APP_NAME, SYS_APP_VERSION, SYS_USER_NAME};

	//----------------------------------------------------------------------------------------------------------------------
	// Instance
	//----------------------------------------------------------------------------------------------------------------------

	private String osName = "";
	private String osVersion = "";
	private String osBuild = "";
	private String country = "";
	private String machineName = "";
	private String applicationName = "";
	private String applicationPath = "";
	private String applicationVersion = "";
	private String userName = System.getProperty("user.name");

	public String get(String name) {
		switch(name) {
		case SYS_OS_NAME:
			return osName;
		case SYS_OS_VERSION:
			return osVersion;
		case SYS_OS_BUILD:
			return osBuild;
		case SYS_COUNTRY:
			return country;
		case SYS_MACHINE_NAME:
			return machineName;
		case SYS_APP_NAME:
			return applicationName;
		case SYS_APP_VERSION:
			return applicationVersion;
		case SYS_USER_NAME:
			return userName;
		}
		return "";
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public String getOsName() {
		return osName;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	public String getOsBuild() {
		return osBuild;
	}
	public void setOsBuild(String osBuild) {
		this.osBuild = osBuild;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getApplicationPath() {
		return applicationPath;
	}
	public void setApplicationPath(String applicationPath) {
		this.applicationPath = applicationPath;
	}
	public String getApplicationVersion() {
		return applicationVersion;
	}
	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}