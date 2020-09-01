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

import java.util.Base64;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;

public class TestElementRoot extends TestElement {

	private final static String SOURCE = "source";
	private final static String VERSION = "version";
	private final static String RECTANGLE = "rectangle";
	private final static String TITLE = "title";
	private final static String PROCESS_ID = "process-id";
	private final static String SCREEN_SHOT = "screenshot";
	private final static String WINDOWS = "windows";
	
	public TestElementRoot() {}

	public TestElementRoot(Channel channel) {
		super(channel, 1, 0);
		setCriterias("root");
	}	

	@Override
	protected void startSearch(boolean sysComp, SearchedElement searchedElement) {
	}

	@Override
	public String enterText(ActionStatus status, CalculatedValue text, ActionTestScript script) {
		final String enterText = text.getCalculated();
		channel.rootKeys(status, enterText);
		return enterText;
	}

	@Override
	public Object executeScript(ActionStatus status, String script, boolean returnValue) {
		return engine.executeJavaScript(status, script, returnValue);
	}

	@Override
	public void mouseWheel(int delta) {
		engine.scroll(delta);
	}

	@Override
	public void over(ActionStatus status, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
		// do nothing, this is the root, no need to scroll over the root element
		channel.toFront();
		channel.refreshLocation();
		
		super.over(status, position, desktopDragDrop, offsetX, offsetY);
	}

	@Override
	public String getAttribute(ActionStatus status, String name) {
		switch (name.toLowerCase()) {
		case SOURCE:
			return engine.getSource();
		case RECTANGLE:
			return channel.getBoundDimension();
		case TestElement.CLIENT_WIDTH:
			return channel.getSubDimension().getWidth().toString();
		case TestElement.CLIENT_HEIGTH:
			return channel.getSubDimension().getHeight().toString();
		case SCREEN_SHOT:
			return Base64.getEncoder().encodeToString(channel.getScreenShot());
		case VERSION:
			return channel.getApplicationVersion();
		case PROCESS_ID:
			return String.valueOf(channel.getProcessId());
		case TITLE:
			return engine.getTitle();
		case WINDOWS:
			return String.valueOf(engine.getNumWindows());
		default :
			reloadFoundElements();
			return engine.getSysProperty(status, name, getFoundElement());
		}
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		
		final CalculatedProperty[] props = new CalculatedProperty[6];
		props[0] = new CalculatedProperty(SOURCE, "[...]");
		props[1] = new CalculatedProperty(VERSION, channel.getApplicationVersion());
		props[2] = new CalculatedProperty(RECTANGLE, channel.getBoundDimension());
		props[3] = new CalculatedProperty(PROCESS_ID, String.valueOf(channel.getProcessId()));
		props[4] = new CalculatedProperty(TITLE, engine.getTitle());
		props[5] = new CalculatedProperty(WINDOWS, String.valueOf(engine.getNumWindows()));
		
		return props;
	}

	@Override
	public CalculatedProperty[] getCssAttributes() {
		reloadFoundElements();
		return super.getCssAttributes();
	}
}