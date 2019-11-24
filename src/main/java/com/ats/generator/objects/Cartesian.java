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

package com.ats.generator.objects;

import org.apache.commons.lang3.StringUtils;

public enum Cartesian {
	LEFT("left"),
	RIGHT("right"),
	TOP("top"),
	BOTTOM("bottom"),
	MIDDLE("middle"),
	CENTER("center");

	private final String text;

	/**
	 * @param text
	 */
	private Cartesian(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

	public boolean equals(String value) {
		return text.equals(value);
	}

	public String getData(String value) {
		
		if(StringUtils.containsIgnoreCase(value, text)) {
			String data;
			int found = value.indexOf("=");
			if(found > -1) {
				data = value.substring(found+1);
			}else {
				found = value.indexOf("(");
				if(found > -1) {
					data = value.substring(found+1).replace(")", "");
				}else {
					return null;
				}
			}
			
			if(StringUtils.isAllBlank(data)) {
				return "0";
			}
			return data.trim();
		}
		return null;
	}

	public String getJavacode() {
		return this.getClass().getSimpleName() + "." + text.toUpperCase();
	}
}