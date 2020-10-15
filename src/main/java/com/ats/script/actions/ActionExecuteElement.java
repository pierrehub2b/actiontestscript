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

import com.ats.driver.AtsManager;
import com.ats.element.*;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.script.Script;
import com.ats.tools.Operators;
import com.ats.tools.logger.MessageCode;
import com.google.gson.JsonObject;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.function.Predicate;

public class ActionExecuteElement extends ActionExecuteElementAbstract {

	private SearchedElement searchElement;
	private TestElement testElement;

	private boolean async;
	private int actionMaxTry;

	public ActionExecuteElement() {}

	public ActionExecuteElement(Script script, boolean stop, ArrayList<String> options, ArrayList<String> element) {
		super(script, stop, options);
		if(element != null && element.size() > 0 && !element.get(0).contains(MobileDriverEngine.SYS_BUTTON)){
			setSearchElement(new SearchedElement(script, element));
		}
	}

	public ActionExecuteElement(Script script, boolean stop, int maxTry, int delay, SearchedElement element) {
		super(script, stop, maxTry, delay);
		setSearchElement(element);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		final StringBuilder codeBuilder = super.getJavaCode();
		if(searchElement == null){
			codeBuilder.append("null");
		}else {
			codeBuilder.append(searchElement.getJavaCode());
		}
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		
		int maxTry = AtsManager.getMaxStaleOrJavaScriptError();
		while(maxTry > 0) {
			try {
				execute(ts, testName, testLine, Operators.GREATER, 0);
				return true;
			} catch(JavascriptException e) {
				ts.getTopScript().sendWarningLog("Javascript error", "try again : " + maxTry);
				status.setException(ActionStatus.JAVASCRIPT_ERROR, e);
			} catch(StaleElementReferenceException e) {
				ts.getTopScript().sendWarningLog("StaleReference error", "try again : " + maxTry);
				status.setException(ActionStatus.WEB_DRIVER_ERROR, e);
			}
			
			ts.getCurrentChannel().sleep(500);
			setTestElement(null);
			maxTry--;
		}
		
		throw new WebDriverException("execute element error : " + status.getFailMessage());
	}
	
	public void execute(ActionTestScript ts, String testName, int testLine, String operator, int value) {

		super.execute(ts, testName, testLine);
		
		if(status.isPassed()) {

			final Channel channel = getCurrentChannel();
			
			final int delay = getDelay();
			if(delay > 0) {
				ts.getTopScript().sendInfoLog("Delay before action", delay + "s");
				channel.sleep(delay * 1000);
			}
			
			actionMaxTry = ts.getChannelManager().getMaxTry() + getMaxTry();
			if (actionMaxTry < 1) {
				actionMaxTry = 1;
			}

			if(testElement == null) {

				if(searchElement == null) {
					setTestElement(new TestElementRoot(channel));
				}else {

					int trySearch = 0;

					final Predicate<Integer> predicate = getPredicate(operator, value);

					if(searchElement.isDialog()) {
						while (trySearch < actionMaxTry) {

							setTestElement(new TestElementDialog(channel, actionMaxTry, searchElement, predicate));

							if(testElement.isValidated()) {
								trySearch = actionMaxTry;
							}else {
								trySearch++;
								channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", actionMaxTry - trySearch);
								channel.progressiveWait(trySearch);
							}
						}
					} else {

						if (searchElement.isSysComp()) {
							while (trySearch < actionMaxTry) {

								setTestElement(new TestElementSystem(channel, actionMaxTry, predicate, searchElement));

								if (testElement.isValidated()) {
									trySearch = actionMaxTry;
								} else {
									trySearch++;
									channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", actionMaxTry - trySearch);
									channel.progressiveWait(trySearch);
								}
							}
						} else {
							while (trySearch < actionMaxTry) {

								if(searchElement.isImageSearch()) {
									setTestElement(new TestElementImage(channel, actionMaxTry, predicate, searchElement));
								}else {
									setTestElement(new TestElement(channel, actionMaxTry, predicate, searchElement));
								}

								if(testElement.isValidated()) {
									trySearch = actionMaxTry;
								}else {
									trySearch++;
									channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", actionMaxTry - trySearch);
									channel.progressiveWait(trySearch);
								}
							}
						}
					}
				}

				status.setElement(testElement);
				status.setSearchDuration(testElement.getTotalSearchDuration());
				status.setData(testElement.getCount());

				ts.getCurrentChannel().sleep(getDelay());

				asyncExec(ts);

			}else {
				terminateExecution(ts);
			}
		}
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
			status.setError(ActionStatus.OBJECT_NOT_FOUND, "element not found");
			error = ActionStatus.OBJECT_NOT_FOUND;
		}

		status.endDuration();
		testElement.terminateExecution(status, ts, error, status.getDuration());
	}



	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("searchDuration", status.getSearchDuration());
		data.addProperty("delay", getDelay());
		data.addProperty("occurrences", status.getElement().getCount());
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public SearchedElement getSearchElement() {
		return searchElement;
	}

	public void setSearchElement(SearchedElement value) {
		this.searchElement = value;
	}
	
	public TestElement getTestElement() {
		return testElement;
	}
	
	private void setTestElement(TestElement element) {
		if (testElement != null) {
			testElement.dispose();
		}
		testElement = element;
	}
	
	public int getActionMaxTry() {
		return actionMaxTry;
	}
	
	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean value) {
		this.async = value;
	}
}