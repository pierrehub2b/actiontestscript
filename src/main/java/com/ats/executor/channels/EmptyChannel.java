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

import java.nio.file.Path;

import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ScriptStatus;
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.recorder.ReportSummary;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.ActionExecute;
import com.ats.tools.logger.ExecutionLogger;

public class EmptyChannel extends Channel {
			
	@Override
	public void checkStatus(ActionExecute actionExecute, String testName, int testLine) {
		final ActionStatus status = newActionStatus(testName, testLine);
		status.setError(ActionStatus.CHANNEL_NOT_FOUND, "No running channel found, please check that 'start channel action' has been added to the script");
		status.endDuration();
		actionExecute.setStatus(status);
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
	
	//----------------------------------------------------------------------------------------------------------
	// Override Visual reporting
	//----------------------------------------------------------------------------------------------------------

	@Override
	public DesktopResponse startVisualRecord(ScriptHeader script, int quality, long started) {
		return null;
	}

	@Override
	public void stopVisualRecord(ScriptStatus status, ReportSummary summary) {
	}

	@Override
	public void saveVisualReportFile(Path path, String fileName, ExecutionLogger logger) {
	}

	@Override
	public void createVisualAction(boolean stop, String actionName, int scriptLine, String scriptName, long timeline, boolean sync) {
	}
	
	@Override
	public void updateVisualAction(boolean isRef) {
	}

	@Override
	public void updateVisualAction(String value) {
	}

	@Override
	public void updateVisualAction(String value, String data) {
	}

	@Override
	public void updateVisualAction(String type, MouseDirectionData hdir, MouseDirectionData vdir) {
	}

	@Override
	public void updateVisualAction(TestElement element) {
	}

	@Override
	public void updateVisualAction(int error, long duration) {
	}

	@Override
	public void updateVisualAction(int error, long duration, String value) {
	}

	@Override
	public void updateVisualAction(int error, long duration, String value, String data) {
	}
}