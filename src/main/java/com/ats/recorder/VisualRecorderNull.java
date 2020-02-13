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

package com.ats.recorder;

import com.ats.element.TestElement;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionChannelStart;

public class VisualRecorderNull implements IVisualRecorder {
	
	@Override
	public void terminate() {
	}
	
	@Override
	public void createVisualAction(Action action) {
	}
	
	@Override
	public void updateScreen(boolean ref) {
	}

	@Override
	public void update(String value) {
	}

	@Override
	public void update(String value, String data) {
	}

	@Override
	public void update(String type, MouseDirection position) {
	}

	@Override
	public void update(TestElement element) {
	}

	@Override
	public void update(int error, long duration) {
	}

	@Override
	public void update(int error, long duration, String value) {
	}

	@Override
	public void update(int error, long duration, String value, String data) {
	}

	@Override
	public void createVisualStartChannelAction(ActionChannelStart action, long duration) {
	}

	@Override
	public void updateScreen(int error, long duration) {
	}

	@Override
	public void updateScreen(int i, long duration, String calculated) {
	}

	@Override
	public void update(int error, long duration, String value, String data, TestElement element) {
	}

	@Override
	public void update(int error, long duration, TestElement element) {
	}

	@Override
	public void updateScreen(int error, long duration, String type, MouseDirection position) {
	}

	@Override
	public void updateScreen(TestElement element) {
	}

	@Override
	public void updateTextScreen(int error, long duration, String value, String data) {
	}
}