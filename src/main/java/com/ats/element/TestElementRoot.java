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
import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;

public class TestElementRoot extends TestElement {

	public TestElementRoot() {}

	public TestElementRoot(Channel channel) {
		super(channel);
		setCriterias("root");
	}	

	@Override
	protected void startSearch(boolean sysComp, SearchedElement searchedElement) {
	}

	@Override
	public void enterText(ActionStatus status, CalculatedValue text, IVisualRecorder recorder) {
		channel.rootKeys(status, text.getCalculated());
		status.endDuration();
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
		
		final TestBound bound = channel.getDimension();
		final int x = bound.getX().intValue();
		final int y = bound.getY().intValue();
		
		super.over(status, position, desktopDragDrop, offsetX, offsetY);
	}

	@Override
	public String getAttribute(ActionStatus status, String name) {
		switch (name.toLowerCase()) {
		case "source":
			return engine.getSource();
		case "rectangle":
			return getRectangle();
		case "screenshot":
			return Base64.getEncoder().encodeToString(channel.getScreenShot());
		case "version":
			return channel.getApplicationVersion();
		case "processid":
			return channel.getProcessId() + "";
		case "title":
			return engine.getTitle();
		default :
			return engine.getAttribute(status, getFoundElement(), name, 5);
		}
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		
		final CalculatedProperty[] props = new CalculatedProperty[5];
		props[0] = new CalculatedProperty("source", "[...]");
		props[1] = new CalculatedProperty("version", channel.getApplicationVersion());
		props[2] = new CalculatedProperty("rectangle", getRectangle());
		props[3] = new CalculatedProperty("processId", channel.getProcessId() + "");
		props[4] = new CalculatedProperty("title", engine.getTitle());
		
		return props;
	}
	
	private String getRectangle() {
		final TestBound bound = channel.getDimension();
		return bound.getX().intValue() + "," + bound.getY().intValue() + "," + bound.getWidth().intValue() + "," + bound.getHeight().intValue();
	}
}