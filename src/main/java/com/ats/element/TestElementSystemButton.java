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

import org.openqa.selenium.Keys;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;

public class TestElementSystemButton extends TestElement {

	private String id = "1";
	
	public TestElementSystemButton(){}
	
	public TestElementSystemButton(Channel channel, SearchedElement searchElement) {
		super(channel);
		if(searchElement.getCriterias().size() > 0) {
			this.id = searchElement.getCriterias().get(0).getValue().getCalculated();
		}
	}
	
	@Override
	public void click(ActionStatus status, MouseDirection position) {
		getChannel().getDriverEngine().buttonClick(id);
	}

	@Override
	public void enterText(ActionStatus status, CalculatedValue text, IVisualRecorder recorder) {}

	@Override
	public void click(ActionStatus status, MouseDirection position, Keys key) {
		click(status, position);
	}

	@Override
	public void drag(ActionStatus status, MouseDirection position) {}

	@Override
	public void drop(ActionStatus status, MouseDirection md, boolean desktopDragDrop) {}

	@Override
	public void swipe(ActionStatus status, MouseDirection position, MouseDirection direction) {}

	@Override
	public void mouseWheel(int delta) {}

	@Override
	public void wheelClick(ActionStatus status, MouseDirection position) {}

	@Override
	public void doubleClick() {}

	@Override
	public String getAttribute(ActionStatus status, String name) {
		return "";
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		return new CalculatedProperty[0];
	}

	@Override
	public Object executeScript(ActionStatus status, String script) {
		return null;
	}
}