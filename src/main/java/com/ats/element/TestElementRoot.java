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
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;

public class TestElementRoot extends TestElement {

	public TestElementRoot() {}
	
	public TestElementRoot(Channel channel) {
		super(channel);
		setCriterias("root");
	}

	@Override
	public void enterText(ActionStatus status, CalculatedValue text, IVisualRecorder recorder) {
		channel.rootKeys(text.getCalculated());
		status.endDuration();
	}

	@Override
	public Object executeScript(ActionStatus status, String script) {
		return engine.executeJavaScript(status, script);
	}

	@Override
	public void mouseWheel(int delta) {
		engine.scroll(delta);
	}

	@Override
	public void over(ActionStatus status, MouseDirection position, boolean desktopDragDrop) {
		// do nothing, this is the root, no need to scroll over the root element
	}
}