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

public class ApplicationProperties {
	
	public static int BROWSER_TYPE = 0;
	public static int DESKTOP_TYPE = 1;
	public static int MOBILE_TYPE = 2;
	public static int API_TYPE = 3;
	
	private int type = BROWSER_TYPE;
	
	private String name;
	private String uri;
	private int wait;
	private int check;
	
	public ApplicationProperties(int type, String name, String uri, int wait, int check) {
		this.type = type;
		this.name = name;
		this.uri = uri;
		this.wait = wait;
		this.check = check;
	}
	
	public boolean isWeb() {
		return type == BROWSER_TYPE;
	}
	
	public boolean isDesktop() {
		return type == DESKTOP_TYPE;
	}
	
	public boolean isMobile() {
		return type == MOBILE_TYPE;
	}
	
	public boolean isApi() {
		return type == API_TYPE;
	}
	
	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}
	
	public int getWait() {
		return wait;
	}
	
	public int getCheck() {
		return check;
	}
}