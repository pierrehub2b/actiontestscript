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

package com.ats.recorder;

import com.ats.executor.TestBound;

public class VisualElement {
	
	private TestBound bound;
	private int foundElements;
	private Long searchDuration;
	private String tag;
	private String criterias;
	
	private String hpos;
	private int hposValue;
	
	private String vpos;
	private int vposValue;
	
	public VisualElement() {}
	
	public TestBound getBound() {
		return bound;
	}

	public void setBound(TestBound bound) {
		this.bound = bound;
	}

	public int getFoundElements() {
		return foundElements;
	}

	public void setFoundElements(int foundElements) {
		this.foundElements = foundElements;
	}

	public Long getSearchDuration() {
		return searchDuration;
	}

	public void setSearchDuration(Long searchDuration) {
		this.searchDuration = searchDuration;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCriterias() {
		return criterias;
	}

	public void setCriterias(String criterias) {
		this.criterias = criterias;
	}
	
	public String getHpos() {
		return hpos;
	}

	public void setHpos(String hpos) {
		this.hpos = hpos;
	}

	public int getHposValue() {
		return hposValue;
	}

	public void setHposValue(int hposValue) {
		this.hposValue = hposValue;
	}

	public String getVpos() {
		return vpos;
	}

	public void setVpos(String vpos) {
		this.vpos = vpos;
	}

	public int getVposValue() {
		return vposValue;
	}

	public void setVposValue(int vposValue) {
		this.vposValue = vposValue;
	}
}