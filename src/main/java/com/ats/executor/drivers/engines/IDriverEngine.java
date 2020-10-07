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

package com.ats.executor.drivers.engines;

import com.ats.element.AtsBaseElement;
import com.ats.element.DialogBox;
import com.ats.element.FoundElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.graphic.ImageTemplateMatchingSimple;
import com.ats.script.actions.ActionApi;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public interface IDriverEngine{
	
	public void started(ActionStatus status);
	public DesktopDriver getDesktopDriver();
	public WebElement getRootElement(Channel cnl);
	public void close(boolean keepRunning);

	public String getApplicationPath();
	public void switchWindow(ActionStatus status, int index, int tries);
	public void closeWindow(ActionStatus status);
	public Object executeScript(ActionStatus status, String script, Object ... params);
	public Object executeJavaScript(ActionStatus status, String script, TestElement element);
	public Object executeJavaScript(ActionStatus status, String script, boolean returnValue);
	public void goToUrl(ActionStatus status, String url);
	
	public List<FoundElement> findElements(boolean sysComp, TestElement testObject, String tagName, String[] attributes, String[] attributesValues, Predicate<AtsBaseElement> searchPredicate, WebElement startElement, boolean waitAnimation);
	public List<FoundElement> findElements(TestElement parent, ImageTemplateMatchingSimple template);
	
	public void waitAfterAction(ActionStatus status);
	public void updateDimensions();
	public FoundElement getElementFromPoint(Boolean syscomp, Double x, Double y);
	public FoundElement getElementFromRect(Boolean syscomp, Double x, Double y, Double w, Double h);
	public String getAttribute(ActionStatus status, FoundElement element, String attributeName, int maxTry);
	public CalculatedProperty[] getAttributes(FoundElement element, boolean reload);
	public CalculatedProperty[] getCssAttributes(FoundElement element);
	
	public void setSysProperty(String propertyName, String propertyValue);
	
	public List<String[]> loadSelectOptions(TestElement element);
	public List<FoundElement> findSelectOptions(TestBound dimension, TestElement element);
	public void selectOptionsItem(ActionStatus status, TestElement testElement, CalculatedProperty selectProperty);
	
	public void loadParents(FoundElement hoverElement);
	
	public void scroll(int delta);
	public void scroll(FoundElement element);
	public void scroll(FoundElement element, int delta);

	public void sendTextData(ActionStatus status, TestElement element, ArrayList<SendKeyData> textActionList);
	public void clearText(ActionStatus status, TestElement testElement, MouseDirection md);
	
	public void updateScreenshot(TestBound dimension, boolean isRef);
	public byte[] getScreenshot(Double x, Double y, Double width, Double height);
	
	public void createVisualAction(Channel channel, String actionType, int scriptLine, long timeline, boolean sync);
	public String setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height);
	
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY);
	public void mouseClick(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY);
	public void doubleClick();
	public void rightClick();
	public void middleClick(ActionStatus status, MouseDirection position, TestElement element);
	public void buttonClick(ActionStatus status, String id);
	public void tap(int count, FoundElement element);
	public void press(int duration, ArrayList<String> paths, FoundElement element);
	
	public void drag(ActionStatus status, FoundElement element, MouseDirection position, int offsetX, int offsetY);
	public void drop(MouseDirection md, boolean desktopDragDrop);
	public void keyDown(Keys key);
	public void keyUp(Keys key);
	public void moveByOffset(int hDirection, int vDirection);

	public DialogBox switchToAlert();
	public boolean switchToDefaultContent();
	public void setWindowToFront();
	public void switchToFrameId(String id);
	public void refreshElementMapLocation();
	public String getSource();
	public void api(ActionStatus status, ActionApi api);
	public int getCurrentWindow();
	public void windowState(ActionStatus status, Channel channel, String state);
	public String getTitle();
	public int getNumWindows();
}