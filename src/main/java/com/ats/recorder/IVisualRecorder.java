package com.ats.recorder;

import com.ats.element.TestElement;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.actions.Action;

public interface IVisualRecorder {
	public void terminate();
	public void createVisualAction(Action action);
	public void createVisualAction(Action action, long duration, String name, String app);
	public void updateScreen(boolean ref);
	public void updateScreen(int error, long duration);
	public void updateScreen(int error, long duration, String value);
	public void updateTextScreen(int error, long duration, String value, String data);
	public void updateScreen(int error, long duration, String type, MouseDirection position);
	public void updateScreen(TestElement element);
	public void update(String value);
	public void update(String value, String data);
	public void update(String type, MouseDirection position);
	public void update(int error, long duration);
	public void update(TestElement element);
	public void update(int error, long duration, String value);
	public void update(int error, long duration, String value, String data);
	public void update(int error, long duration, String value, String data, TestElement element);
	public void update(int error, long duration, TestElement element);
}