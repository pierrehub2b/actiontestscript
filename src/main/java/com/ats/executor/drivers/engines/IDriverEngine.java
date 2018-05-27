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

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;

public interface IDriverEngine{
	public void close();
	public DesktopDriver getDesktopDriver();
	public String getApplication();
	public String getApplicationPath();
	public void switchWindow(int index);
	public void closeWindow(ActionStatus status, int index);
	public Object executeScript(ActionStatus status, String script, Object ... params);
	public void goToUrl(URL url, boolean newWindow);
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, String[] attributes, Predicate<Map<String, Object>> searchPredicate);
	public void waitAfterAction();
	public TestBound[] getDimensions();
	public FoundElement getElementFromPoint(Double x, Double y);
	public String getAttribute(FoundElement element, String attributeName, int maxTry);
	public CalculatedProperty[] getAttributes(FoundElement element);
	public CalculatedProperty[] getCssAttributes(FoundElement element);
	public void loadParents(FoundElement hoverElement);
	//public void switchToDefaultframe();
	public void scroll(FoundElement foundElement, int delta);
	public void middleClick(WebElement element);
	public WebElement getRootElement();
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position);
	public void sendTextData(ActionStatus status, FoundElement foundElement, ArrayList<SendKeyData> textActionList, boolean clear);
	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height);
	public void forceScrollElement(FoundElement value);
	public void mouseClick(boolean hold);
	public void keyDown(Keys key);
	public void keyUp(Keys key);
	public void drop();
	public void moveByOffset(int hDirection, int vDirection);
	public void doubleClick();
	public void rightClick();
	public Alert switchToAlert();
	public void switchToDefaultContent();
	public void navigationRefresh();
	public void navigationForward();
	public void navigationBack();
	public String getCurrentUrl();
	public void switchToIframe(String iframe);
	public void setWindowToFront();
}