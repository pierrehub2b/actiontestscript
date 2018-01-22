package com.ats.recorder;

import java.nio.file.Path;

import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
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

		Path videoFolderPath = project.getReportFolder().resolve(header.getPackagePath());
		videoFolderPath.toFile().mkdirs();

		this.setDaemon(true);
		if(visual) {
			this.visualStream = new VisualStream(videoFolderPath, header);
		}

		if(pdf) {
			this.pdfStream = new PdfStream(videoFolderPath, header);
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