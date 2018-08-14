package com.ats.recorder;

import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.actions.Action;

public interface IVisualRecorder {
	public void setChannel(Channel channel);
	public void terminate();
	public void createVisualAction(Action action);
	public void updateVisualImage();
	public void updateVisualValue(String value);
	public void updateVisualValue(String value, String data);
	public void updateVisualValue(String type, MouseDirection position);
	public void updateVisualStatus(int error, Long duration);
	public void updateVisualElement(TestElement element);
	public void updateVisualStatus(int error, Long duration, String value);
	public void updateVisualStatus(int error, Long duration, String value, String data);
}
