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

package com.ats.executor;

import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;

import com.ats.element.FoundElement;
import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;

public class TestElementDialog extends TestElement {

	public static final String DIALOG_TAG = "dialog";
	
	private static final int waitBox = 500;

	private Alert alert = null;
	private String alertAction = null;

	public TestElementDialog(Channel channel) {
		super(channel);
	}

	public TestElementDialog(Channel channel, int maxTry, SearchedElement searchElement) {
		super(channel, maxTry);
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

		if(properties.size() > 0) {
			alertAction = properties.get(0).getValue().getCalculated();
		}

		int tryLoop = getMaxTry();
		
		while (alert == null && tryLoop > 0) {
			try {
				alert = getChannel().getWebDriver().switchTo().alert();
				getChannel().sleep(waitBox);
				setCount(1);
			}catch(NoAlertPresentException ex) {
				getChannel().sleep(200);
				tryLoop--;
			}
		}
	}

	@Override
	public void sendText(ActionStatus status, boolean clear, CalculatedValue text) {
		getChannel().sleep(waitBox);
		alert.sendKeys(text.getCalculated());
	}

	@Override
	public String getAttribute(String name) {
		getChannel().sleep(waitBox);
		return alert.getText();
	}

	@Override
	public void over(ActionStatus status, MouseDirection position) {
		//do nothing
	}

	@Override
	public void click(ActionStatus status, boolean hold) {
		if(alertAction != null) {
			getChannel().sleep(waitBox);
			if("accept".equals(alertAction)) {
				alert.accept();
			}else {
				alert.dismiss();
			}
			getChannel().sleep(waitBox);
			getChannel().getWebDriver().switchTo().defaultContent();
		}

		status.setPassed(true);
	}

	@Override
	public ActionStatus doubleClick(ActionStatus status) {
		status.setPassed(true);
		return status;
	}

	@Override
	public void terminateExecution() {
		//do nothing
	}

	@Override
	public ActionStatus wheelClick(ActionStatus status) {
		status.setPassed(true);
		return status;
	}

	@Override
	public ActionStatus rightClick(ActionStatus status) {
		status.setPassed(true);
		return status;
	}

	@Override
	public CalculatedProperty[] getAttributes() {
		CalculatedProperty prop = new CalculatedProperty("text", alert.getText());
		return new CalculatedProperty[] {prop};
	}
}
