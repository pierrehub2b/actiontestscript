package com.ats.recorder;

import com.ats.executor.TestElement;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.actions.Action;

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
	public void createVisualAction(Action action, long duration, String name, String app) {
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
}