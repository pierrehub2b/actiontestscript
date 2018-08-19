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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;
import com.ats.tools.Operators;

public class ActionExecuteElement extends ActionExecute {

	public static final Pattern MAX_TRY_PATTERN = Pattern.compile("try\\((\\-?\\d+)\\)", Pattern.CASE_INSENSITIVE);

	private int maxTry = 0;
	private SearchedElement searchElement;
	private TestElement testElement;

	private boolean async;

	public ActionExecuteElement() {}

	public ActionExecuteElement(Script script, boolean stop, ArrayList<String> options, ArrayList<String> element) {

		super(script, stop);

		if(element != null && element.size() > 0){
			setSearchElement(new SearchedElement(script, element));
		}

		Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			Matcher matcher = MAX_TRY_PATTERN.matcher(itr.next());
			if(matcher.find()){
				try {
					setMaxTry(Integer.parseInt(matcher.group(1)));
				}catch(NumberFormatException e){}
				itr.remove();
			}
		}
	}

	public ActionExecuteElement(Script script, boolean stop, int maxTry, SearchedElement element) {
		super(script, stop);
		setMaxTry(maxTry);
		setSearchElement(element);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		StringBuilder codeBuilder = new StringBuilder(super.getJavaCode());
		codeBuilder.append(maxTry);
		codeBuilder.append(", ");

		if(searchElement == null){
			codeBuilder.append(ActionTestScript.JAVA_ROOT_FUNCTION_NAME);
			codeBuilder.append("()");
		}else {
			codeBuilder.append(searchElement.getJavaCode());
		}
		return codeBuilder.toString();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	public void execute(ActionTestScript ts, Channel channel, String operator, int value) {

		super.execute(ts, channel);

		if(channel == null) {

			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_NOT_FOUND);
			status.endDuration();
			
		}else {

			if(testElement == null) {
				if(searchElement == null) {
					testElement = ts.findObject();
				}else {
					testElement = ts.findObject(maxTry, searchElement, operator, value);
				}

				status.setElement(testElement);
				status.setSearchDuration(testElement.getTotalSearchDuration());
				status.setData(testElement.getCount());

				asyncExec(ts);

			}else {
				terminateExecution(ts);
			}
		}
	}

	@Override
	public void execute(ActionTestScript ts, Channel channel) {
		execute(ts, channel, Operators.GREATER, 0);
	}	

	private void asyncExec(ActionTestScript ts) {
		if(!async) {
			terminateExecution(ts);
		}
	}

	public void terminateExecution(ActionTestScript ts) {

		int error = 0;
		
		if(testElement.isValidated()) {
			status.setPassed(true);
		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Element not found");
			error = ActionStatus.OBJECT_NOT_FOUND;
		}

		status.endDuration();
		testElement.terminateExecution(ts, error, status.getDuration());
	}

	public TestElement getTestElement() {
		return testElement;
	}

	public void reinit() {
		if(testElement != null) {
			testElement.dispose();
			testElement = null;
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public SearchedElement getSearchElement() {
		return searchElement;
	}

	public void setSearchElement(SearchedElement elem) {
		this.searchElement = elem;
	}

	public int getMaxTry() {
		return maxTry;
	}

	public void setMaxTry(int maxTry) {
		this.maxTry = maxTry;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
}