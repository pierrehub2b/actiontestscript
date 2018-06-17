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

package com.ats.script.actions;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionChannelStart extends ActionChannel {

	public static final String SCRIPT_START_LABEL = SCRIPT_LABEL + "start";

	private CalculatedValue application;
	private SearchedElement rootElement;

	public ActionChannelStart() {}
	
	public ActionChannelStart(Script script, String name, CalculatedValue value, SearchedElement rootElement) {
		super(script, name);
		setApplication(value);
		setRootElement(rootElement);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		
		ts.startChannel(status, getName(), application.getCalculated());
		status.updateDuration();
		
		ts.updateVisualValue(getName(), application.getCalculated());
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		String code = super.getJavaCode() + "\"" + getName() + "\", " + application.getJavaCode() + ", ";
		if(rootElement != null){
			code += rootElement.getJavaCode();
		}else {
			code += "null";
		}
		
		return code + ")";
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getApplication() {
		return application;
	}

	public void setApplication(CalculatedValue value) {
		this.application = value;
	}
	
	public SearchedElement getRootElement() {
		return rootElement;
	}

	public void setRootElement(SearchedElement rootElement) {
		this.rootElement = rootElement;
	}
}