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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;
import com.ats.script.actions.ActionSelect;
import com.ats.tools.Operators;
import com.ats.tools.logger.MessageCode;

public class TestElement{

	public static Predicate<Integer> isOccurrencesMoreThan(Integer value) {return p -> p > value;}
	public static Predicate<Integer> isOccurrencesLessThan(Integer value) {return p -> p < value;}
	public static Predicate<Integer> isOccurrencesLessOrEqualThan(Integer value) {return p -> p <= value;}
	public static Predicate<Integer> isOccurrencesMoreOrEqualThan(Integer value) {return p -> p >= value;}
	public static Predicate<Integer> isOccurrencesDifferent(Integer value) {return p -> p != value;}
	public static Predicate<Integer> isOccurrencesEqual(Integer value) {return p -> p == value;}
	
	private Channel channel;

	private Predicate<Integer> occurrences;

	private int count = 0;

	private long searchDuration = 0;
	private long totalSearchDuration = 0;

	private TestElement parent;
	private ArrayList<FoundElement> foundElements = new ArrayList<FoundElement>();

	private int maxTry = 20;
	private int index;

	private String criterias;
	private String searchedTag;
	
	protected IVisualRecorder recorder;
	
	public void dispose() {

		channel = null;
		recorder = null;
		occurrences = null;

		if(parent != null) {
			parent.dispose();
			parent = null;
		}

		while(foundElements.size() > 0) {
			foundElements.remove(0).dispose();
		}
	}

	public TestElement(Channel channel) {
		this.channel = channel;
		this.index = 0;
		this.foundElements = new ArrayList<FoundElement>();
		this.foundElements.add(new FoundElement(channel));
		this.count = 1;
	}
	
	public TestElement(Channel channel, int maxTry) {
		this.channel = channel;
		this.maxTry = maxTry;
	}
	
	public TestElement(FoundElement element, Channel currentChannel) {
		this(currentChannel);
		this.foundElements.add(element);
		this.count = getElementsCount();
	}

	public TestElement(Channel channel, int maxTry, String operator, int expectedCount) {

		this(channel, maxTry);

		switch (operator) {
		case Operators.DIFFERENT :
			this.occurrences = isOccurrencesDifferent(expectedCount);
			break;
		case Operators.GREATER :
			this.occurrences = isOccurrencesMoreThan(expectedCount);
			break;
		case Operators.GREATER_EQUALS :
			this.occurrences = isOccurrencesMoreOrEqualThan(expectedCount);
			break;
		case Operators.LOWER :
			this.occurrences = isOccurrencesLessThan(expectedCount);
			break;
		case Operators.LOWER_EQUALS :
			this.occurrences = isOccurrencesLessOrEqualThan(expectedCount);
			break;
		default :
			this.occurrences = isOccurrencesEqual(expectedCount);
			break;
		}
	}

	public TestElement(Channel channel, int maxTry, String operator, int expectedCount, TestElement parent, String tag, List<CalculatedProperty> criterias) {

		this(channel, maxTry, operator, expectedCount);
		this.parent = parent;

		initSearch(tag, criterias);
	}

	public TestElement(Channel channel, int maxTry, String operator, int expectedCount, SearchedElement searchElement) {

		this(channel, maxTry, operator, expectedCount);
		this.index = searchElement.getIndex();

		if(searchElement.getParent() != null){
			this.parent = new TestElement(channel, maxTry, operator, expectedCount, searchElement.getParent());
		}

		initSearch(searchElement.getTag(), searchElement.getCriterias());
	}

	protected int getMaxTry() {
		return maxTry;
	}

	protected Channel getChannel() {
		return channel;
	}

	private void initSearch(String tag, List<CalculatedProperty> properties) {

		if(channel != null){

			searchedTag = tag;
			criterias = tag;

			searchDuration = System.currentTimeMillis();

			int trySearch = 0;

			if(parent == null || (parent != null && parent.getCount() > 0)){

				ArrayList<String> attributes = new ArrayList<String>();
				Predicate<AtsElement> fullPredicate = Objects::nonNull;

				for (CalculatedProperty property : properties){
					criterias += "," + property.getName() + ":" + property.getValue().getCalculated();
					fullPredicate = property.getPredicate(fullPredicate);

					attributes.add(property.getName());
				}

				while (trySearch < maxTry) {
					foundElements = channel.getDriverEngine().findElements(channel, this, tag, attributes, fullPredicate);
					if(isValidated()) {
						trySearch = maxTry;
					}else {
						channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "Searching element", maxTry - trySearch);
						progressiveWait(trySearch);
						trySearch++;
					}
				}
			}

			searchDuration = System.currentTimeMillis() - this.searchDuration;
			totalSearchDuration = getTotalDuration();
			count = getElementsCount();

			if(index > 0) {
				index--;
			}
		}
	}

	private void progressiveWait(int current) {
		channel.sleep(200 + current*50);
	}

	private int getElementsCount() {
		if(index == 0) {
			return foundElements.size();
		}else if(foundElements.size() >= index){
			return 1;
		}else {
			return 0;
		}
	}

	private Long getTotalDuration(){
		if(parent != null){
			return searchDuration + parent.getTotalDuration();
		}else{
			return searchDuration;
		}
	}

	public FoundElement getFoundElement() {
		return foundElements.get(index); 
	}

	public WebElement getWebElement() {
		return getFoundElement().getValue();
	}

	public String getWebElementId() {
		return getFoundElement().getId();
	}

	public Rectangle getWebElementRectangle() {
		return getFoundElement().getRectangle();
	}

	public boolean isValidated() {
		if(occurrences == null) {
			return true;
		}
		return occurrences.test(getElementsCount());
	}

	public boolean isIframe() {
		if(isValidated() && foundElements.size() > 0){
			return getFoundElement().isIframe();
		}else{
			return false;
		}
	}

	public String getSearchedTag() {
		return searchedTag;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public TestElement getParent() {
		return parent;
	}

	public void setParent(TestElement parent) {
		this.parent = parent;
	}

	public long getSearchDuration() {
		return searchDuration;
	}

	public void setSearchDuration(long searchDuration) {
		this.searchDuration = searchDuration;
	}

	public long getTotalSearchDuration() {
		return totalSearchDuration;
	}

	public void setTotalSearchDuration(long totalSearchDuration) {
		this.totalSearchDuration = totalSearchDuration;
	}

	public ArrayList<FoundElement> getFoundElements() {
		return foundElements;
	}

	public void setFoundElements(ArrayList<FoundElement> data) {
		this.foundElements = data;
	}	

	public String getCriterias() {
		return criterias;
	}

	public void setCriterias(String criterias) {
		this.criterias = criterias;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Assertion ...
	//-------------------------------------------------------------------------------------------------------------------

	public void checkOccurrences(ActionTestScript ts, ActionStatus status, String operator, int expected) {

		int error = 0;

		if(isValidated()) {
			status.setPassed(true);
		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.OCCURRENCES_ERROR);
			status.setData(count);
			status.setMessage("[" + expected + "] expected occurence(s) but [" + count + "] occurence(s) found");

			error = ActionStatus.OCCURRENCES_ERROR;
		}

		status.endDuration();
		terminateExecution(ts, error, status.getDuration(), count + "", operator + " " + expected);
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Text ...
	//-------------------------------------------------------------------------------------------------------------------

	public void sendText(ActionStatus status, CalculatedValue text) {
		channel.sendTextData(status, getFoundElement(), text.getCalculatedText());
	}

	public void clearText(ActionStatus status) {
		channel.clearText(status, getFoundElement());
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Select ...
	//-------------------------------------------------------------------------------------------------------------------

	public void select(ActionStatus status, CalculatedProperty selectProperty, boolean select, boolean ctrl) {
		if(isValidated()){

			Select selectElement = new Select(getWebElement());

			if(ActionSelect.SELECT_INDEX.equals(selectProperty.getName())){

				int idx = 0;
				try {
					idx = Integer.parseInt(selectProperty.getValue().getCalculated());
				}catch(NumberFormatException e) {}

				if(select) {
					selectElement.selectByIndex(idx);
				}else {
					selectElement.deselectByIndex(idx);
				}

			}else if (ActionSelect.SELECT_VALUE.equals(selectProperty.getName())){
				if(select) {
					selectElement.selectByValue(selectProperty.getValue().getCalculated());
				}else {
					selectElement.deselectByValue(selectProperty.getValue().getCalculated());
				}
			}else {

				if(select) {
					selectElement.selectByVisibleText(selectProperty.getValue().getCalculated());
				}else {
					selectElement.deselectByVisibleText(selectProperty.getValue().getCalculated());
				}
			}

			status.setPassed(true);

		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Element not found, cannot select index !");
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Mouse ...
	//-------------------------------------------------------------------------------------------------------------------

	public void over(ActionStatus status, MouseDirection position) {
		if(isValidated()){
			channel.mouseMoveToElement(status, getFoundElement(), position);
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Element not found, cannot execute over action");
		}
	}

	public void click(ActionStatus status, Keys key) {
		channel.keyDown(key);
		click(status, false);
		channel.keyUp(key);
	}	

	public void click(ActionStatus status, boolean hold) {

		int tryLoop = maxTry;
		mouseClick(status, hold);

		while(tryLoop > 0 && !status.isPassed()) {
			progressiveWait(tryLoop);
			mouseClick(status, hold);
		}
	}

	private void mouseClick(ActionStatus status, boolean hold) {

		status.setPassed(false);

		try {

			channel.mouseClick(getFoundElement(), hold);
			status.setPassed(true);

			return;

		}catch(ElementNotVisibleException e0) {	

			status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
			mouseWheel(0);

		}/*catch(WebDriverException e0) {	
			if(e0.getMessage().contains("is not clickable") || e0.getMessage().contains("Element is obscured")) {
				status.setCode(ActionStatus.OBJECT_NOT_INTERACTABLE);
			}
		}catch (Exception e) {
			status.setMessage(e.getMessage());
		}*/
	}

	public void drag(ActionStatus status) {
		click(status, true);
	}

	public void drop(ActionStatus status) {
		channel.drop();
		status.setPassed(true);
	}

	public void swipe(ActionStatus status, int hDirection, int vDirection) {
		drag(status);
		channel.moveByOffset(hDirection, vDirection);
		drop(status);
	}

	public void mouseWheel(int delta) {
		if(delta == 0) {
			channel.forceScrollElement(getFoundElement());
		}else {
			channel.scroll(getFoundElement(), delta);
		}
	}

	public void wheelClick(ActionStatus status) {
		channel.middleClick(status, this);
	}

	public void doubleClick() {
		channel.doubleClick();
	}

	public void rightClick() {
		channel.rightClick();
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Attributes
	//-------------------------------------------------------------------------------------------------------------------

	public String getAttribute(String name){

		if(isValidated()){
			return channel.getAttribute(getFoundElement(), name, maxTry);
		}
		return null;
	}

	public CalculatedProperty[] getAttributes() {
		return channel.getAttributes(getFoundElement());
	}

	public CalculatedProperty[] getCssAttributes() {
		return channel.getCssAttributes(getFoundElement());
	}

	public Object executeScript(ActionStatus status, String script) {
		if(isValidated()){
			return channel.executeScript(status, "arguments[0]." + script, getWebElement());
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Element not found, cannot execute script action !");
		}
		return null;
	}

	public void terminateExecution(ActionTestScript script, int error, Long duration) {
		recorder = script.getRecorder();
		recorder.update(error, duration, this);
		channel.actionTerminated();
	}
	
	public void terminateExecution(ActionTestScript script, int error, Long duration, String value, String data) {
		recorder = script.getRecorder();
		recorder.update(error, duration, value, data, this);
		channel.actionTerminated();
	}
	
	public void updateScreen() {
		recorder.updateScreen(this);
	}
}