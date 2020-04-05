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

package com.ats.executor.channels;

import com.ats.driver.AtsManager;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.script.ScriptHeader;

public class EmptyChannel extends Channel {
	
	public EmptyChannel() {
		super(ChannelManager.ATS);
	}
	
	public EmptyChannel(AtsManager ats) {
		super(ats);
	}

	@Override
	public ActionStatus newActionStatus(String testName, int testLine) {
		return new ActionStatus(null, testName, testLine);
	}
	
	@Override
	public DesktopResponse startVisualRecord(ScriptHeader script, int quality, long started) {
		return null;
	}

	@Override
	public String getApplicationPath() {
		return "";
	}

	@Override
	public boolean isDesktop() {
		return false;
	}
	
	@Override
	public boolean isMobile() {
		return false;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getAuthentication() {
		return "";
	}

	@Override
	public int getPerformance() {
		return 0;
	}

	@Override
	public boolean isCurrent() {
		return false;
	}

	@Override
	public byte[] getIcon() {
		return new byte[0];
	}

	@Override
	public String getScreenServer() {
		return "";
	}
}