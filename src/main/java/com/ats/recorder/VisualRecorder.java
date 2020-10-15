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
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.Project;
import com.ats.script.Script;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.*;
import com.ats.tools.XmlReport;
import com.ats.tools.logger.ExecutionLogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VisualRecorder implements IVisualRecorder {

	private Channel channel;
	private String outputPath;
	private ScriptHeader scriptHeader;

	private int visualQuality = 3;
	private boolean xml = false;
	
	private long started;
	
	private ReportSummary summary = new ReportSummary();
	private ActionTestScript topScript;
	
	private ExecutionLogger logger;

	public VisualRecorder(ActionTestScript topScript, ScriptHeader header, Project project, boolean xml, int quality) {

		this.topScript = topScript;
		this.logger = new ExecutionLogger();
		
		final Path output = project.getReportFolder().resolve(header.getPackagePath());
		output.toFile().mkdirs();

		initAndStart(output, header, xml, quality);
	}

	public VisualRecorder(ActionTestScript topScript, File outputFolder, ScriptHeader header, boolean xml, int quality, ExecutionLogger logger) {
		
		this.topScript = topScript;
		this.logger = logger;
		
		final Path output = outputFolder.toPath();
		initAndStart(output, header, xml, quality);
	}

	//--------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------

	private void initAndStart(Path output, ScriptHeader header, boolean xml, int quality) {
		this.outputPath = output.toFile().getAbsolutePath();
		this.scriptHeader = header;
		this.xml = xml;

		if(quality > 0) {
			this.visualQuality = quality;
		}
		
		this.started = System.currentTimeMillis();
	}
	
	@Override
	public void updateSummary(String testName, int testLine, String data) {
		summary.appendData(data);
	}
	
	@Override
	public void updateSummaryFail(String testName, int testLine, String app, String errorMessage) {
		summary.setFailData(testName, testLine, errorMessage);
	}
	
	//--------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------
	
	@Override
	public void terminate() {
		if(channel != null) {
			final Path path = Paths.get(outputPath);
			
			logger.sendInfo("Stop visual recording", scriptHeader.getQualifiedName());
			channel.stopVisualRecord(topScript.getStatus(), summary);
			channel.saveVisualReportFile(path, scriptHeader.getQualifiedName() + Script.ATS_VISUAL_FILE_EXTENSION, logger);
			
			if(xml) {
				XmlReport.createReport(path, scriptHeader.getQualifiedName(), logger);
			}
		}
	}
	
	private void setChannel(Channel channel) {
		if(this.channel == null && channel != null) {
			final DesktopResponse resp = channel.startVisualRecord(scriptHeader, visualQuality, started);
			if(resp != null && resp.errorCode < 0) {
				channel.sendLog(resp.errorCode, "Unable to start visual recording", resp.errorMessage);
			}
		}
		this.channel = channel;
	}
	
	private boolean isSyncAction(String actionName) {
		if(actionName == ActionMouse.class.getName() || actionName == ActionGotoUrl.class.getName() || actionName == ActionMouseKey.class.getName() ||  actionName == ActionMouseScroll.class.getName() || actionName == ActionText.class.getName()
			|| actionName == ActionScripting.class.getName() || actionName == ActionWindowState.class.getName() || actionName == ActionWindowSwitch.class.getName() || actionName == ActionMouseDragDrop.class.getName() || actionName == ActionMouseSwipe.class.getName()
			|| actionName == ActionChannelSwitch.class.getName() || actionName == ActionWindowResize.class.getName()
		) {
			return true;
		}
		return false;
	}
	
	@Override
	public void createVisualStartChannelAction(ActionChannelStart action, long duration) {
		setChannel(action.getStatus().getChannel());
		channel.createVisualAction(action.getClass().getName(), action.getLine(), System.currentTimeMillis() - started - duration, isSyncAction(action.getClass().getName()));
		channel.sleep(100);
		update(0, duration, action.getName(), action.getActionLogsData().toString());
	}
	
	@Override
	public void createVisualAction(Action action) {
		setChannel(action.getStatus().getChannel());
		channel.createVisualAction(action.getClass().getName(), action.getLine(), System.currentTimeMillis() - started, isSyncAction(action.getClass().getName()));
	}
	
	@Override
	public void update(int error, long duration, String value, String data) {
		channel.updateVisualAction(error, duration, value, data);
	}

	@Override
	public void update(int error, long duration, String value) {
		channel.updateVisualAction(error, duration, value);
	}
	
	@Override
	public void updateScreen(boolean ref) {
		channel.sleep(100);
		channel.updateVisualAction(ref);
	}

	@Override
	public void update(String value) {
		channel.updateVisualAction(value);
	}

	@Override
	public void update(String value, String data) {
		channel.updateVisualAction(value, data);
	}

	@Override
	public void update(String type, MouseDirection position) {
		channel.updateVisualAction(type, position.getHorizontalPos(), position.getVerticalPos());
	}

	@Override
	public void update(int error, long duration) {
		channel.updateVisualAction(error, duration);
	}

	@Override
	public void update(TestElement element) {
		channel.updateVisualAction(element);
	}

	@Override
	public void updateScreen(TestElement element) {
		channel.mouseMoveToElement(channel.newActionStatus(), element.getFoundElement(), new MouseDirection());
		channel.updateVisualAction(element);
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void updateScreen(int error, long duration) {
		update(error, duration);
		//updateScreen(false);
	}
	
	@Override
	public void updateScreen(int error, long duration, String value) {
		update(error, duration, value);
		//updateScreen(false);
	}
	
	@Override
	public void updateTextScreen(int error, long duration, String value, String data) {
		update(error, duration, value, data);
		//updateScreen(false);
	}
	
	@Override
	public void updateScreen(int error, long duration, String type, MouseDirection position) {
		update(error, duration);
		update(type, position);
		//updateScreen(false);
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void update(int error, long duration, TestElement element) {
		update(error, duration);
		update(element);
	}
	
	@Override
	public void update(int error, long duration, String value, String data, TestElement element) {
		update(error, duration, value, data);
		update(element);
	}
}