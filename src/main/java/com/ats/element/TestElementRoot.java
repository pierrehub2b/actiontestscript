package com.ats.element;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;

public class TestElementRoot extends TestElement {

	public TestElementRoot(Channel channel) {
		super(channel);
		setCriterias("root");
	}

	@Override
	public void enterText(ActionStatus status, CalculatedValue text, IVisualRecorder recorder) {
		channel.rootKeys(text.getCalculated());
		status.endDuration();
	}
}