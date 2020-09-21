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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.graphic.ImageTemplateMatchingSimple;
import com.ats.tools.Utils;

import java.util.List;
import java.util.function.Predicate;

public class TestElementImage extends TestElement {

	public TestElementImage(Channel channel, int maxTry, Predicate<Integer> predicate, SearchedElement searchElement) {
		super(channel, maxTry,predicate, searchElement);
	}

	@Override
	protected List<FoundElement> loadElements(SearchedElement searchedElement) {
		final ImageTemplateMatchingSimple template = new ImageTemplateMatchingSimple(searchedElement.getImage());

		for (CalculatedProperty property : searchedElement.getCriterias()){
			if("error".equals(property.getName())){
				final String value = property.getValue().getCalculated();
				if(value.endsWith("%")) {
					template.setPercentError(Utils.string2Double(value.replace("%", "").trim()));
				}else{
					template.setError(Utils.string2Int(value.trim()));
				}
				break;
			}
		}
		return engine.findElements(parent, template);
	}
	
	//-------------------------------------------------------------------------------------------------------------------
	// Mouse actions ...
	//-------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void over(ActionStatus status, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
		final FoundElement fe = getFoundElement();
		super.over(status, position, desktopDragDrop, fe.getInnerX(), fe.getInnerY());
	}

	@Override
	protected void mouseClick(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		final FoundElement fe = getFoundElement();
		super.mouseClick(status, position, fe.getInnerX(), fe.getInnerY());
	}

	@Override
	public void mouseWheel(int delta) {
		// do nothing for the moment
	}
	
	//-------------------------------------------------------------------------------------------------------------------
	// Text ...
	//-------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void clearText(ActionStatus status, MouseDirection md) {
		engine.getDesktopDriver().clearText();
	}

	@Override
	public String sendText(ActionTestScript script, ActionStatus status, CalculatedValue text) {
		for(SendKeyData sequence : text.getCalculatedText(script)) {
			engine.getDesktopDriver().sendKeys(sequence.getSequenceDesktop(), "");
		}
		channel.actionTerminated(status);
		return text.getCalculated();
	}
	
	@Override
	public String enterText(ActionStatus status, CalculatedValue text, ActionTestScript script) {

		final MouseDirection md = new MouseDirection();

		mouseClick(status, md, 0, 0);

			if(status.isPassed()) {

				recorder.updateScreen(false);

				if(!text.getCalculated().startsWith("$key")) {
					clearText(status, md);
				}
				
				final String enteredText = sendText(script, status, text);
				if(isPassword() || text.isCrypted()) {
					return "########";
				}else {
					return enteredText;
				}
			}
		return "";
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Drag drop ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public String getAttribute(ActionStatus status, String name) {
		if("x".equals(name)) {
			return getFoundElement().getBoundX() + "";
		}else if("y".equals(name)) {
			return getFoundElement().getBoundY() + "";
		}
		return "";
	}

	@Override
	public void drag(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		super.drag(status, position, getFoundElement().getBoundX().intValue(), getFoundElement().getBoundY().intValue());
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		return new CalculatedProperty[0];
	}

	@Override
	public Object executeScript(ActionStatus status, String script, boolean returnValue) {
		return null;
	}
}