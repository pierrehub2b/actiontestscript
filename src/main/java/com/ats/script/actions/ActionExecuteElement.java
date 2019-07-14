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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.StaleElementReferenceException;

import com.ats.element.SearchedElement;
import com.ats.element.TestElement;
import com.ats.element.TestElementDialog;
import com.ats.element.TestElementImage;
import com.ats.element.TestElementRoot;
import com.ats.element.TestElementSystem;
import com.ats.element.TestElementSystemButton;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;
import com.ats.tools.Operators;
import com.ats.tools.Utils;
import com.ats.tools.logger.MessageCode;

public class ActionExecuteElement extends ActionExecute {

	public static final Pattern MAX_TRY_PATTERN = Pattern.compile("try\\((\\-?\\d+)\\)", Pattern.CASE_INSENSITIVE);

	private int maxTry = 0;
	private SearchedElement searchElement;
	private TestElement testElement;

	private boolean async;
	private int actionMaxTry;

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
				setMaxTry(Utils.string2Int(matcher.group(1)));
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
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(maxTry).append(", ");

		if(searchElement == null){
			codeBuilder.append("null");
		}else {
			codeBuilder.append(searchElement.getJavaCode());
		}
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	public void execute(ActionTestScript ts, String operator, int value) {

		super.execute(ts);
		final Channel channel = ts.getCurrentChannel();

		actionMaxTry = ts.getChannelManager().getMaxTry() + maxTry;
		if(actionMaxTry < 1) {
			actionMaxTry = 1;
		}

		if(channel == null) {

			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_NOT_FOUND);
			status.endDuration();

		}else if(testElement == null) {

			if(searchElement == null) {
				setTestElement(new TestElementRoot(channel));
			}else {

				int searchMaxTry = actionMaxTry;

				if(searchElement.isDialog()) {
					setTestElement(new TestElementDialog(channel, searchMaxTry, searchElement));
				}else if(searchElement.isSysButton()) {	
					setTestElement(new TestElementSystemButton(channel, searchElement));
				}else {

					final Predicate<Integer> predicate = getPredicate(operator, value);
					int trySearch = 0;

					if(searchElement.isSysComp()){
						while (trySearch < searchMaxTry) {

							setTestElement(new TestElementSystem(channel, searchMaxTry, predicate, searchElement));

							if(testElement.isValidated()) {
								trySearch = searchMaxTry;
							}else {
								trySearch++;
								channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", searchMaxTry - trySearch);
								channel.progressiveWait(trySearch);
							}
						}
					}else {
						while (trySearch < searchMaxTry) {

							if(searchElement.isImageSearch()) {
								setTestElement(new TestElementImage(channel, searchMaxTry, predicate, searchElement));
							}else {
								setTestElement(new TestElement(channel, searchMaxTry, predicate, searchElement));
							}

							if(testElement.isValidated()) {
								trySearch = searchMaxTry;
							}else {
								trySearch++;
								channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", searchMaxTry - trySearch);
								channel.progressiveWait(trySearch);
							}
						}
					}
				}
			}

			status.setElement(testElement);
			status.setSearchDuration(testElement.getTotalSearchDuration());
			status.setData(testElement.getCount());

			asyncExec(ts);

		}else {
			terminateExecution(ts);
		}
	}

	public int getActionMaxTry() {
		return actionMaxTry;
	}

	private Predicate<Integer> getPredicate(String operator, int value) {
		switch (operator) {
		case Operators.DIFFERENT :
			return p -> p != value;
		case Operators.GREATER :
			return p -> p > value;
		case Operators.GREATER_EQUAL :
			return p -> p >= value;
		case Operators.LOWER :
			return p -> p < value;
		case Operators.LOWER_EQUAL :
			return p -> p <= value;
		}
		return p -> p == value;
	}

	@Override
	public void execute(ActionTestScript ts) {
		try {
			execute(ts, Operators.GREATER, 0);
		}catch (StaleElementReferenceException e) {
			ts.getCurrentChannel().sleep(300);
			setTestElement(null);
			execute(ts);
		}
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
		testElement.terminateExecution(status, ts, error, status.getDuration());
	}

	public TestElement getTestElement() {
		return testElement;
	}

	private void setTestElement(TestElement element) {
		if(testElement != null) {
			testElement.dispose();
		}
		testElement = element;
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