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

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ats.element.AtsBaseElement;
import com.ats.element.AtsElement;
import com.ats.element.FoundElement;
import com.ats.executor.TestBound;
import com.ats.generator.variables.CalculatedProperty;

public class DesktopResponse {
			
	public ArrayList<AtsElement> elements;
	public ArrayList<DesktopWindow> windows;
	public ArrayList<DesktopData> data;

	public int errorCode = 0;
	public String errorMessage = null;
	
	public byte[] image;
	
	public DesktopResponse() {}
	
	public DesktopResponse(String error) {
		this.errorMessage = error;
		this.errorCode = -999;
	}

	@Transient
	public ArrayList<FoundElement> getFoundElements(TestBound channelDimension) {
		if(elements == null) {
			return new ArrayList<FoundElement>();
		}
		return elements.stream().map(e -> new FoundElement(e, channelDimension)).collect(Collectors.toCollection(ArrayList::new));
	}

	@Transient
	public ArrayList<DesktopData> getData() {
		if(data == null) {
			return new ArrayList<DesktopData>();
		}
		return data;
	}

	@Transient
	public List<DesktopWindow> getWindows() {
		if(windows == null) {
			return new ArrayList<DesktopWindow>();
		}
		return windows;
	}

	@Transient
	public DesktopWindow getWindow() {
		if(windows != null && windows.size() > 0) {
			return windows.get(0);
		}
		return null;
	}

	@Transient
	public FoundElement getParentsElement(TestBound dimension) {
		
		FoundElement current = null;
		
		if(elements != null) {
			for(Object obj : elements) {
				FoundElement elem = new FoundElement((AtsElement)obj, dimension);
				if(current == null) {
					current = elem;
				}else {
					current.setParent(elem);
				}
				current = elem;
			}
		}
		
		return current;
	}

	@Transient
	public CalculatedProperty[] getAttributes() {
		
		if(data == null) {
			return new CalculatedProperty[0];
		}
		
		CalculatedProperty[] result = new CalculatedProperty[data.size()];
		for(int i= 0; i< data.size(); i++) {
			result[i] = new CalculatedProperty(data.get(i).getName(), data.get(i).getValue());
		}
		return result;
	}

	@Transient
	public ArrayList<FoundElement> getFoundElements(Predicate<AtsBaseElement> predicate, TestBound dimension) {
		
		if(elements == null) {
			return new ArrayList<FoundElement>();
		}
		return elements.parallelStream().filter(predicate).map(e -> new FoundElement(e, dimension)).collect(Collectors.toCollection(ArrayList::new));
	}

	@Transient
	public String getFirstAttribute() {
		if(data != null && data.size() > 0) {
			return data.get(0).getValue();
		}
		return null;
	}
}