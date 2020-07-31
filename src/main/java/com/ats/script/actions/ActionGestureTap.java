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
import com.ats.script.Script;

import java.util.ArrayList;

public class ActionGestureTap extends ActionExecuteElement {
	
	public static final String SCRIPT_LABEL = "tap";
	
	private int count;
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------------------------------------------------------------
	
	public ActionGestureTap(Script script, String count, boolean stop, ArrayList<String> options, ArrayList<String> element) {
		super(script, stop, options, element);
		setCount(Integer.parseInt(count));
	}
	
	public ActionGestureTap(Script script, boolean stop, int maxTry, int delay, SearchedElement element, int count) {
		super(script, stop, maxTry, delay, element);
		setCount(count);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		
		if (status.isPassed()) {
			ts.getRecorder().updateScreen(true);
			
			getTestElement().tap(count);
			
			status.endAction();
			ts.getRecorder().updateScreen(0, status.getDuration());
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ") .append(count).append(")");
		return codeBuilder;
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public int getCount() { return count; }
	public void setCount(int count) { this.count = count; }
}
