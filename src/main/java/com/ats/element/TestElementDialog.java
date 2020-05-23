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

package com.ats.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;

public class TestElementDialog extends TestElement {

	private static final int WAIT_BOX = 500;
	private static final String ACCEPT = "accept";
	private static final String DISMISS = "dismiss";

	private Alert alert = null;
	private String alertAction = ACCEPT;

	public TestElementDialog() {}

	public TestElementDialog(Channel channel) {
		super(channel);
	}

	public TestElementDialog(Channel channel, int maxTry, SearchedElement searchElement, Predicate<Integer> predicate) {
		super(channel, maxTry, predicate);
		initSearch(searchElement.getCriterias());
	}

	public TestElementDialog(Channel channel, int maxTry, List<CalculatedProperty> criterias) {
		super(channel, maxTry);
		initSearch(criterias);
	}

	public TestElementDialog(FoundElement element, Channel currentChannel) {
		super(element, currentChannel);
	}

	private void initSearch(List<CalculatedProperty> properties) {

		this.setDialogBox();

		if(properties.size() > 0) {
			alertAction = properties.get(0).getValue().getData();
			if(!ACCEPT.equals(alertAction) && !DISMISS.equals(alertAction)) {
				alertAction = ACCEPT;
			}
		}

		try {

			getChannel().sleep(WAIT_BOX);
			alert = getChannel().switchToAlert();

			setFoundElements(new ArrayList<FoundElement>(Arrays.asList(new FoundElement())));
			setCount(1);

		}catch(NoAlertPresentException ex) {
			getChannel().sleep(WAIT_BOX);
			setCount(0);
		}
	}

	@Override
	public void updateScreen() {
		recorder.updateScreen(true);
	}

	@Override
	public FoundElement getFoundElement() {
		return null;
	}

	@Override
	public void over(ActionStatus status, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
	}

	@Override
	public String enterText(ActionStatus status, CalculatedValue text, ActionTestScript script) {
		sendText(script, status, text);
		return text.getCalculated();
	}

	@Override
	public void clearText(ActionStatus status, MouseDirection md) {
	}

	@Override
	public String sendText(ActionTestScript script, ActionStatus status, CalculatedValue text) {
		final String enterText = text.getCalculated();
		getChannel().sleep(WAIT_BOX);
		alert.sendKeys(enterText);
		
		return enterText;
	}

	@Override
	public String getAttribute(ActionStatus status, String name) {
		getChannel().sleep(WAIT_BOX);
		return alert.getText();
	}

	@Override
	public void click(ActionStatus status, MouseDirection position) {

		getChannel().sleep(WAIT_BOX);
		if(DISMISS.equals(alertAction)) {
			alert.dismiss();
		}else {
			alert.accept();
		}
		
		getChannel().sleep(WAIT_BOX);
		getChannel().switchToDefaultContent();

		status.setPassed(true);
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		return new CalculatedProperty[] {new CalculatedProperty("text", alert.getText())};
	}
}