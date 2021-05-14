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

import java.util.List;
import java.util.function.Predicate;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.graphic.ImageTemplateMatchingSimple;
import com.ats.tools.Utils;

public class TestElementImage extends TestElement {
	
	public final static String ATS_X = "-ats-image-x";
	public final static String ATS_Y = "-ats-image-y";
	public final static String ATS_WIDTH = "-ats-image-width";
	public final static String ATS_HEIGHT = "-ats-image-height";

	public TestElementImage(Channel channel, int maxTry, Predicate<Integer> predicate, SearchedElement searchElement) {
		super(channel, maxTry,predicate, searchElement);
	}

	@Override
	protected List<FoundElement> loadElements(SearchedElement searchedElement) {
		
		if(parent != null) {
			engine.mouseMoveToElement(parent.getFoundElement());
		}		
		
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

		if(parent != null) {
			final FoundElement parentElement = parent.getFoundElement();
			final Double innerX = fe.getScreenX() - parentElement.getScreenX() + (getFoundElement().getWidth()/2) - (parentElement.getWidth()/2);
			final Double innerY = fe.getScreenY() - parentElement.getScreenY() + (getFoundElement().getHeight()/2) - (parentElement.getHeight()/2);
			
			parent.over(status, position, false, innerX.intValue(), innerY.intValue());
		}else {
			super.over(status, position, desktopDragDrop, fe.getInnerX(), fe.getInnerY());
		}
	}

	@Override
	protected void mouseClick(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		final FoundElement fe = getFoundElement();
		
		if(parent != null) {
			final FoundElement parentElement = parent.getFoundElement();
			final Double innerX = fe.getScreenX() - parentElement.getScreenX() + (getFoundElement().getWidth()/2) - (parentElement.getWidth()/2);
			final Double innerY = fe.getScreenY() - parentElement.getScreenY() + (getFoundElement().getHeight()/2) - (parentElement.getHeight()/2);
				
			engine.mouseClick(status, parent.getFoundElement(), position, innerX.intValue(), innerY.intValue());
			channel.actionTerminated(status);
		}else {
			super.mouseClick(status, position, fe.getInnerX(), fe.getInnerY());
		}
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

		final String enterText = text.getCalculated();
		channel.rootKeys(status, enterText);
		channel.actionTerminated(status);
		
		return enterText;
	}

	@Override
	public String enterText(ActionStatus status, CalculatedValue text, ActionTestScript script) {
		final MouseDirection md = new MouseDirection();
		mouseClick(status, md, 0, 0);
		return finalizeEnterText(status, text, md, script);
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Drag drop ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public String getAttribute(ActionStatus status, String name) {
		if(ATS_OCCURRENCES.equals(name)) {
			return String.valueOf(getCount());
		}else if(ATS_OCCURRENCES_INDEX.equals(name)) {
			return String.valueOf(getIndex());
		}else if(ATS_TABLE_DATA.equals(name)) {
			return null;
		}else if(ATS_X.equals(name)) {
			return String.valueOf(getFoundElement().getBoundX().intValue());
		}else if(ATS_Y.equals(name)) {
			return String.valueOf(getFoundElement().getBoundY().intValue());
		}else if(ATS_WIDTH.equals(name)) {
			return String.valueOf(getFoundElement().getWidth().intValue());
		}else if(ATS_HEIGHT.equals(name)) {
			return String.valueOf(getFoundElement().getHeight().intValue());
		}
		return "";
	}

	@Override
	public void drag(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		super.drag(status, position, getFoundElement().getBoundX().intValue(), getFoundElement().getBoundY().intValue());
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		return new CalculatedProperty[] {
				getAtsProperty(ATS_OCCURRENCES), 
				getAtsProperty(ATS_OCCURRENCES_INDEX),
				getAtsProperty(ATS_TABLE_DATA),
				new CalculatedProperty(ATS_X, String.valueOf(getFoundElement().getBoundX().intValue())),
				new CalculatedProperty(ATS_Y, String.valueOf(getFoundElement().getBoundY().intValue())),
				new CalculatedProperty(ATS_WIDTH, String.valueOf(getFoundElement().getWidth().intValue())),
				new CalculatedProperty(ATS_HEIGHT, String.valueOf(getFoundElement().getHeight().intValue()))};
	}

	@Override
	public Object executeScript(ActionStatus status, String script, boolean returnValue) {
		return null;
	}
}