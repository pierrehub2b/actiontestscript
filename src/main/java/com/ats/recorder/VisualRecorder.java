package com.ats.recorder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.ProjectData;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.Action;
import com.ats.tools.Utils;

public class VisualRecorder implements IVisualRecorder {

	private Channel channel;
	private String outputPath;
	private ScriptHeader scriptHeader;

	private int visualQuality = 3;
	private boolean xml = false;
	
	private long started;

	public VisualRecorder(ScriptHeader header, ProjectData project, int quality, boolean xml) {

		Path output = project.getReportFolder().resolve(header.getPackagePath());
		output.toFile().mkdirs();

		initAndStart(output, header, quality, xml);
	}

	public VisualRecorder(File outputFolder, ScriptHeader header, int quality, boolean xml) {

		Path output = outputFolder.toPath();
		initAndStart(output, header, quality, xml);
	}

	//--------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------

	private void initAndStart(Path output, ScriptHeader header, int quality, boolean xml) {
		this.outputPath = output.toFile().getAbsolutePath();
		this.scriptHeader = header;
		this.xml = xml;

		if(quality > 0) {
			this.visualQuality = quality;
		}
		
		this.started = System.currentTimeMillis();
	}
	
	//--------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------
	
	@Override
	public void terminate() {
		if(channel != null) {
			channel.stopVisualRecord();
			if(xml) {
				Utils.createXmlReport(Paths.get(outputPath), scriptHeader.getQualifiedName());
			}
		}
	}
	
	private void setChannel(Channel channel) {
		if(this.channel == null) {
			channel.startVisualRecord(outputPath, scriptHeader, visualQuality, started);
		}
		this.channel = channel;
	}
	
	@Override
	public void createVisualAction(Action action, long duration, String name, String app) {
		setChannel(action.getStatus().getChannel());
		channel.createVisualAction(action.getClass().getName(), action.getLine(), System.currentTimeMillis() - started - duration);
		update(0, duration, name, app);
	}
	
	@Override
	public void createVisualAction(Action action) {
		setChannel(action.getStatus().getChannel());
		channel.createVisualAction(action.getClass().getName(), action.getLine(), System.currentTimeMillis() - started);
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
		channel.mouseMoveToElement(new ActionStatus(channel), element.getFoundElement(), new MouseDirection());
		channel.updateVisualAction(element);
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void updateScreen(int error, long duration) {
		update(error, duration);
		updateScreen(false);
	}
	
	@Override
	public void updateScreen(int error, long duration, String value) {
		update(error, duration, value);
		updateScreen(false);
	}
	
	@Override
	public void updateScreen(int error, long duration, String type, MouseDirection position) {
		update(error, duration);
		update(type, position);
		updateScreen(false);
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