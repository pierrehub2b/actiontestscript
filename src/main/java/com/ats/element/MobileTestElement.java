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

package com.ats.element;

public class MobileTestElement {

	private String id;
	private int offsetX;
	private int offsetY;
	private String coordinates;
	
	public MobileTestElement(String id, int offsetX, int offsetY) {
		this.id = id;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public MobileTestElement(String id, int offsetX, int offsetY, String coordinates) {
		this.id = id;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.coordinates = coordinates;
	}
		
	public String getId() {
		return id;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}
	
	public String getCoordinates() {
		return coordinates;
	}
}
