package com.ats.recorder;

import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.actions.Action;

public class VisualRecorderNull implements IVisualRecorder {

	@Override
	public void setChannel(Channel channel) {
	}

	@Override
	public void terminate() {
	}
	
	@Override
	public void createVisualAction(Action action) {
	}

	@Override
	public void updateVisualImage() {
	}

	@Override
	public void updateVisualValue(String value) {
	}

	@Override
	public void updateVisualValue(String value, String data) {
	}

	@Override
	public void updateVisualValue(String type, MouseDirection position) {
	}

	@Override
	public void updateVisualElement(TestElement element) {
	}

	@Override
	public void updateVisualStatus(int error, Long duration) {
	}

	@Override
	public void updateVisualStatus(int error, Long duration, String value) {
	}

	@Override
	public void updateVisualStatus(int error, Long duration, String value, String data) {
	}
}
