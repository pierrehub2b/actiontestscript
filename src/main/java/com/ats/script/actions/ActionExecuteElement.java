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

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.StaleElementReferenceException;

import com.ats.driver.AtsManager;
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

	private static final String TRY_LABEL = "try";
	private static final String[] LABEL_REPLACE = new String[]{TRY_LABEL, "=", " ", "(", ")"};
	private static final String[] LABEL_REPLACEMENT = new String[]{"", "", "", "", ""};
	
	private static final String DELAY_LABEL = "delay";
	private static final String[] DELAY_REPLACE = new String[]{DELAY_LABEL, "=", " "};
	private static final String[] DELAY_REPLACEMENT = new String[]{"", "", ""};

	private int maxTry = 0;
	private int delay = 0;
	
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

		final Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			final String opt = itr.next().toLowerCase();
			if(opt.contains(TRY_LABEL)) {
				setMaxTry(Utils.string2Int(StringUtils.replaceEach(opt, LABEL_REPLACE, LABEL_REPLACEMENT)));
				itr.remove();
			}else if(opt.contains(DELAY_LABEL)) {
				setDelay(Utils.string2Int(StringUtils.replaceEach(opt, DELAY_REPLACE, DELAY_REPLACEMENT)));
				itr.remove();
			}
		}
	}

	public ActionExecuteElement(Script script, boolean stop, int maxTry, int delay, SearchedElement element) {
		super(script, stop);
		setMaxTry(maxTry);
		setDelay(delay);
		setSearchElement(element);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		final StringBuilder codeBuilder = super.getJavaCode().append(maxTry).append(", ").append(delay).append(", ");
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

				}else if(searchElement.isSysButton()) {	
					setTestElement(new TestElementSystemButton(channel, searchElement));
				}else {

					if(searchElement.isSysComp()){
						while (trySearch < actionMaxTry) {

							setTestElement(new TestElementSystem(channel, actionMaxTry, predicate, searchElement));

							if(testElement.isValidated()) {
								trySearch = actionMaxTry;
							}else {
								trySearch++;
								channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", actionMaxTry - trySearch);
								channel.progressiveWait(trySearch);
							}
						}
					}else {
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
			
			ts.getCurrentChannel().sleep(delay);

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

	private int maxExecution = 0;
	@Override
	public void execute(ActionTestScript ts) {
		try {
			execute(ts, Operators.GREATER, 0);
			maxExecution = 0;
		}catch (StaleElementReferenceException e) {
			if(maxExecution < AtsManager.getMaxStaleError()) {
				maxExecution++;
				ts.getCurrentChannel().sleep(200);
				setTestElement(null);
				execute(ts);
			}else {
				throw e;
			}
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
			status.setError(ActionStatus.OBJECT_NOT_FOUND, "element not found");
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
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, StringBuilder data) {
		return super.getActionLogs(scriptName, scriptLine, data.append("\"duration\":").append(status.getDuration()-status.getSearchDuration()-delay).append(", \"delay\":").append(delay).append(", \"occurrences\":").append(status.getElement().getCount()));
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

	public int getMaxTry() {
		return maxTry;
	}

	public void setMaxTry(int value) {
		this.maxTry = value;
	}
	
	public int getDelay() {
		return delay;
	}

	public void setDelay(int value) {
		this.delay = value;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean value) {
		this.async = value;
	}
}