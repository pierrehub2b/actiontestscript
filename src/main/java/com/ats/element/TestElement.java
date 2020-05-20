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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.engines.IDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;

public class TestElement{

	protected Channel channel;
	protected IDriverEngine engine;

	private Predicate<Integer> occurrences;

	private int count = 0;

	private long searchDuration = 0;
	private long totalSearchDuration = 0;

	protected TestElement parent;
	private List<FoundElement> foundElements = new ArrayList<FoundElement>();

	private int maxTry = 20;
	private int index;

	private String criterias = "";
	private String searchedTag = "";

	protected IVisualRecorder recorder;

	private boolean sysComp = false;

	public TestElement() {}

	public TestElement(Channel channel) {
		this.channel = channel;
		this.foundElements = new ArrayList<FoundElement>(Arrays.asList(new FoundElement(channel)));
		this.count = 1;
		this.index = 0;
		this.occurrences = p -> true;
		this.engine = channel.getDriverEngine();
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

	public TestElement(Channel channel, int maxTry, Predicate<Integer> occurrences) {
		this(channel, maxTry);
		this.occurrences = occurrences;
	}

	public TestElement(Channel channel, int maxTry, Predicate<Integer> predicate, int index) {
		this(channel, maxTry, predicate);
		this.setIndex(index);
	}

	public TestElement(Channel channel, SearchedElement searchedElement) {
		this(channel, 1, p -> true, searchedElement);
	}

	public TestElement(Channel channel, int maxTry, Predicate<Integer> predicate, SearchedElement searchedElement) {

		this(channel, maxTry, predicate, searchedElement.getIndex());

		if(searchedElement.getParent() != null){
			this.parent = new TestElement(channel, maxTry, predicate, searchedElement.getParent());
		}

		setEngine(channel.getDriverEngine());
		startSearch(false, searchedElement);
	}

	protected void setEngine(IDriverEngine engine) {
		this.engine = engine;
	}

	public void dispose() {
		channel = null;
		engine = null;
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

	public boolean isSysComp() {
		return sysComp;
	}

	protected int getMaxTry() {
		return maxTry;
	}

	protected Channel getChannel() {
		return channel;
	}

	protected void startSearch(boolean sysComp, SearchedElement searchedElement) {

		this.sysComp = sysComp;

		if(channel != null){

			searchedTag = searchedElement.getTag();
			criterias = searchedTag;

			searchDuration = System.currentTimeMillis();

			if(parent == null || (parent != null && parent.getCount() > 0)){
				foundElements = loadElements(searchedElement);
			}

			searchDuration = System.currentTimeMillis() - this.searchDuration;
			totalSearchDuration = getTotalDuration();
			count = getElementsCount();
		}
	}

	protected List<FoundElement> loadElements(SearchedElement searchedElement) {

		final int criteriasCount = searchedElement.getCriterias().size();
		final String[] attributes = new String[criteriasCount];
		final String[] attributesValues = new String[criteriasCount];

		Predicate<AtsBaseElement> fullPredicate = Objects::nonNull;

		for (int i=0; i<criteriasCount; i++) {
			final CalculatedProperty property = searchedElement.getCriterias().get(i);

			criterias += "," + property.getName() + ":" + property.getValue().getCalculated();

			fullPredicate = property.getPredicate(fullPredicate);
			attributes[i] = property.getName();

			if(property.isRegexp()) {
				attributesValues[i] = property.getName();
			}else {
				attributesValues[i] = property.getName() + "\t" + property.getValue().getCalculated();
			}
		}

		try {
			return engine.findElements(sysComp, this, searchedTag, attributes, attributesValues, fullPredicate, null, true);
		}catch (StaleElementReferenceException e) {
			return Collections.<FoundElement>emptyList();
		}

	}

	private int getElementsCount() {
		if(foundElements.size() > getStartOneIndex()){
			return foundElements.size();
		}else{
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
		return foundElements.get(getStartOneIndex()); 
	}

	public boolean isPassword() {
		return getFoundElement().isPassword();
	}

	public boolean isNumeric() {
		return getFoundElement().isNumeric();
	}

	public WebElement getWebElement() {
		return getFoundElement().getValue();
	}

	public boolean isBody() {
		return getFoundElement().getTag().equalsIgnoreCase("body");
	}

	public String getWebElementId() {
		return getFoundElement().getId();
	}

	public Rectangle getWebElementRectangle() {
		return getFoundElement().getRectangle();
	}

	public boolean isValidated() {
		return occurrences.test(getElementsCount());
	}

	public boolean isIframe() {
		if(foundElements.size() > getStartOneIndex()){
			return getFoundElement().isIframe();
		}else{
			return false;
		}
	}

	public String getSearchedTag() {
		return searchedTag;
	}

	protected void setDialogBox() {
		this.searchedTag = "AlertBox";
		this.criterias = "";
	}

	private int getStartOneIndex() {
		if(index > 1) {
			return index-1;
		}
		return 0;
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

	public List<FoundElement> getFoundElements() {
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
			status.setNoError();
		}else {
			status.setError(ActionStatus.OCCURRENCES_ERROR, "[" + expected + "] expected occurence(s) but [" + count + "] occurence(s) found", count);
			error = ActionStatus.OCCURRENCES_ERROR;
		}

		status.endDuration();
		ts.getRecorder().updateScreen(0, status.getDuration());
		terminateExecution(status, ts, error, status.getDuration(), count + "", operator + " " + expected);
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Text ...
	//-------------------------------------------------------------------------------------------------------------------

	public void clearText(ActionStatus status, MouseDirection md) {
		engine.clearText(status, this, md);
	}
	
	public String enterText(ActionStatus status, CalculatedValue text, IVisualRecorder recorder) {

		final MouseDirection md = new MouseDirection();

		over(status, md, false, 0, 0);
		if(status.isPassed()) {

			if(status.isPassed()) {

				recorder.updateScreen(false);

				if(!text.getCalculated().startsWith("$key")) {
					clearText(status, md);
				}
				
				final String enteredText = sendText(status, text);
				if(isPassword() || text.isCrypted()) {
					return "########";
				}else {
					return enteredText;
				}
			}
		}
		return "";
	}

	public String sendText(ActionStatus status, CalculatedValue text) {
		final ArrayList<SendKeyData> textData = text.getCalculatedText();
		int max = maxTry;
		while(!trySendText(status, textData) && max > 0) {
			channel.sleep(100);
			max--;
		}
		channel.actionTerminated(status);
		return text.getCalculated();
	}

	private boolean trySendText(ActionStatus status, ArrayList<SendKeyData> text) {
		try {
			engine.sendTextData(status, this, text);
			status.setNoError();
			return true;
		}catch (ElementNotInteractableException e) {
			status.setError(ActionStatus.OBJECT_NOT_INTERACTABLE, "element is not interactable");
			return false;
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Select ...
	//-------------------------------------------------------------------------------------------------------------------

	public void select(ActionStatus status, CalculatedProperty selectProperty) {
		if(isValidated()){
			engine.selectOptionsItem(status, this, selectProperty);
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Mouse ...
	//-------------------------------------------------------------------------------------------------------------------

	public void over(ActionStatus status, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
		engine.mouseMoveToElement(status, getFoundElement(), position, desktopDragDrop, offsetX, offsetY);
	}

	public void click(ActionStatus status, MouseDirection position, Keys key) {
		engine.keyDown(key);
		click(status, position);
		engine.keyUp(key);
	}	

	public void click(ActionStatus status, MouseDirection position) {

		int tryLoop = maxTry;
		mouseClick(status, position, 0, 0);

		while(tryLoop > 0 && !status.isPassed()) {
			channel.progressiveWait(tryLoop);
			mouseClick(status, position, 0, 0);
			tryLoop--;
		}
	}

	protected void mouseClick(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		engine.mouseClick(status, getFoundElement(), position, offsetX, offsetY);
		channel.actionTerminated(status);
	}

	public void drag(ActionStatus status, MouseDirection md, int offsetX, int offsetY) {
		engine.drag(status, getFoundElement(), md, offsetX, offsetY);
		channel.actionTerminated(status);
	}

	public void drop(ActionStatus status, MouseDirection md, boolean desktopDragDrop) {
		engine.drop(md, desktopDragDrop);
		status.setPassed(true);
	}

	public void swipe(ActionStatus status, MouseDirection position, MouseDirection direction) {
		drag(status, position, 0, 0);
		engine.moveByOffset(direction.getHorizontalDirection(), direction.getVerticalDirection());
		drop(status, null, false);
	}

	public void mouseWheel(int delta) {
		engine.scroll(getFoundElement(), delta);
	}

	public void wheelClick(ActionStatus status, MouseDirection position) {
		engine.middleClick(status, position, this);
	}

	public void doubleClick() {
		engine.doubleClick();
	}

	public void rightClick() {
		engine.rightClick();
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Attributes
	//-------------------------------------------------------------------------------------------------------------------

	public String getAttribute(ActionStatus status, String name){
		if(isValidated()){
			return engine.getAttribute(status, getFoundElement(), name, maxTry);
		}
		return null;
	}

	public CalculatedProperty[] getAttributes(boolean reload) {
		return engine.getAttributes(getFoundElement(), reload);
	}

	public CalculatedProperty[] getCssAttributes() {
		return engine.getCssAttributes(getFoundElement());
	}

	public Object executeScript(ActionStatus status, String script, boolean returnValue) {
		if(isValidated()){
			return engine.executeJavaScript(status, script, this);
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Element not found, cannot execute script action !");
		}
		return null;
	}

	public void terminateExecution(ActionStatus status, ActionTestScript script, int error, Long duration) {
		recorder = script.getRecorder();
		recorder.update(error, duration, this);
		channel.actionTerminated(status);
	}

	public void terminateExecution(ActionStatus status, ActionTestScript script, int error, Long duration, String value, String data) {
		recorder = script.getRecorder();
		recorder.update(error, duration, value, data, this);
		channel.actionTerminated(status);
	}

	public void updateScreen() {
		recorder.updateScreen(this);
	}
}