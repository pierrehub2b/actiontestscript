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

package com.ats.generator.variables.parameter;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;

public class Parameter {
	
	private int index;
	private String name;
	private String data;
	private CalculatedValue value;
		
	public Parameter() {}
	
	public Parameter(int index, String name, String data) {
		this.index = index;
		this.name = name;
		this.data = data;
	}
	
	public Parameter(int index, CalculatedProperty prop) {
		this(index, prop.getName(), prop.getValue());
	}
	
	public Parameter(int index, String name, CalculatedValue value) {
		this(index, name, value.getCalculated());
		this.value = value;
	}
	
	public Parameter(int index, String data) {
		this(index, "p" + index, data);
	}

	public Parameter(int index, CalculatedValue value) {
		this(index, value.getCalculated());
		this.value = value;
	}

	public CharSequence getJavaCode() {
		return value.getJavaCode();
	}
	
	public String getCalculated() {
		if(value != null) {
			return value.getCalculated();
		}
		return data;
	}
	
	public void updateCalculated(ActionTestScript ts) {
		if(value == null) {
			value = new CalculatedValue(ts, data);
		}
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

	public CalculatedValue getValue() {
		return value;
	}

	public void setValue(CalculatedValue value) {
		this.value = value;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}