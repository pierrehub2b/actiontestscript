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

import com.ats.generator.variables.CalculatedValue;
import com.ats.tools.Utils;

public class MouseDirectionData {

	private Cartesian type;
	private CalculatedValue value = new CalculatedValue("0");

	public MouseDirectionData() {}

	public MouseDirectionData(String name, CalculatedValue value) {
		this.setName(name);
		this.setValue(value);
	}
	
	public MouseDirectionData(Cartesian type, CalculatedValue value) {
		this.type = type;
		this.setValue(value);
	}
	
	public int getHorizontalDirection() {
		if(Cartesian.RIGHT.equals(type)) {
			return getIntValue();
		}else if(Cartesian.LEFT.equals(type)) {
			return -getIntValue();
		}
		return 0;
	}
	
	public int getVerticalDirection() {
		if(Cartesian.BOTTOM.equals(type)) {
			return getIntValue();
		}else if(Cartesian.TOP.equals(type)) {
			return -getIntValue();
		}
		return 0;
	}
	
	public int getIntValue() {
	 	return value.getCalculated() != "" ? Utils.string2Int(value.getCalculated()) : Utils.string2Int(value.getData());
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode() {
		if(("0".equals(value.getCalculated()) || "".equals(value.getCalculated())) && (Cartesian.MIDDLE.equals(type) || Cartesian.CENTER.equals(type))){
			return "";
		}
		return type.getJavacode() + ", " + value.getJavaCode();
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

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;
	}
}