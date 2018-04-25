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

import java.io.File;
import java.nio.file.Path;

import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.recorder.stream.PdfStream;
import com.ats.recorder.stream.VisualStream;
import com.ats.script.ProjectData;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.Action;

public class RecorderThread extends Thread {

	private boolean running = true;
	private boolean paused = false;

	private int frameIndex = 0;
	private VisualAction currentVisual;

	private VisualStream visualStream;
	private PdfStream pdfStream;

	public RecorderThread(ScriptHeader header, ProjectData project, boolean visual, boolean pdf, boolean xml) {
		
		Path output = project.getReportFolder().resolve(header.getPackagePath());
		output.toFile().mkdirs();

		initAndStart(output, header, visual, pdf, xml);
	}
	
	public RecorderThread(File outputFolder, String testName, boolean visual, boolean pdf, boolean xml) {
		
		Path output = outputFolder.toPath();
		
		ScriptHeader header = new ScriptHeader();
		header.setName(testName);
		
		initAndStart(output, header, visual, pdf, xml);
	}
	
	private void initAndStart(Path output, ScriptHeader header, boolean visual, boolean pdf, boolean xml) {
		
		this.setDaemon(true);
		
		if(visual) {
			this.visualStream = new VisualStream(output, header);
		}

		if(pdf) {
			this.pdfStream = new PdfStream(output, header);
		}
		
		start();
	}

	public void run() {
		while(running)
		{}
	}

	public void terminate(){
		if(running){
			saveVisual();

			running = false;
			interrupt();

			if(visualStream != null) {
				visualStream.terminate();
				visualStream = null;
			}

			if(pdfStream != null) {
				pdfStream.terminate();
				pdfStream = null;
			}
		}
	}

	public void setPause(boolean value) {
		paused = value;
	}

	public void addVisual(Channel channel, Action action) {
		if(!paused) {
			saveVisual();
			currentVisual = new VisualAction(channel, action);
		}
	}

	public void updateVisualImage(byte[] newScreen) {
		currentVisual.addImageFrame(newScreen);
	}

	public void updateVisualValue(String value) {
		currentVisual.setValue(value);
	}

	public void updateVisualValue(String value, String data) {
		currentVisual.setValue(value);
		currentVisual.setData(data);
	}
	
	public void updateVisualValue(String type, MouseDirection position) {
		currentVisual.setValue(type);
		currentVisual.setPosition(position);
	}
	
	public void updateVisualStatus(boolean value) {
		currentVisual.setPassed(value);
	}

	public void updateVisualElement(TestElement element) {
		if(element.getFoundElements().size() > 0) {
			currentVisual.setElementBound(element.getFoundElements().get(0).getTestBound());
			currentVisual.setTotalSearchDuration(element.getTotalSearchDuration());
			currentVisual.setNumElements(element.getFoundElements().size());
			currentVisual.setCriterias(element.getCriterias());
		}
	}

	private void saveVisual() {
		if(currentVisual != null) {

			currentVisual.setIndex(frameIndex);

			if(visualStream != null) {
				visualStream.flush(currentVisual);
			}

			if(pdfStream != null) {
				pdfStream.flush(currentVisual);
			}

			frameIndex++;
		}
	}
}