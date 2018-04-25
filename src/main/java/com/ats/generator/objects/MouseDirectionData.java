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

public class MouseDirectionData {

	private Cartesian type;
	private int value = 0;

	public MouseDirectionData() {}

	public MouseDirectionData(String name, int value) {
		this.setName(name);
		this.setValue(value);
	}
	
	public MouseDirectionData(String name, String value) {
		this.setName(name);
		try{
			this.setValue(Integer.parseInt(value));
		}catch (NumberFormatException e){}
	}
	
	public MouseDirectionData(Cartesian type, int value) {
		this.type = type;
		this.setValue(value);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode() {
		
		if(value == 0 && (Cartesian.MIDDLE.equals(type) || Cartesian.CENTER.equals(type))){
			return "";
		}
		return type.getJavacode() + ", " + value;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public String getName() {
		return type.toString();
	}

	public void setName(String value) {
		
		this.type = Cartesian.valueOf(value.toUpperCase());
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}