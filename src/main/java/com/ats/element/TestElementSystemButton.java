package com.ats.element;

import org.openqa.selenium.Keys;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.recorder.IVisualRecorder;

public class TestElementSystemButton extends TestElement {

	private String id = "1";
	
	public TestElementSystemButton(Channel channel, SearchedElement searchElement) {
		super(channel);
		if(searchElement.getCriterias().size() > 0) {
			this.id = searchElement.getCriterias().get(0).getValue().getCalculated();
		}
	}
	
	@Override
	public void click(ActionStatus status, MouseDirection position) {
		getChannel().getDriverEngine().buttonClick(id);
	}

	@Override
	public void enterText(ActionStatus status, CalculatedValue text, IVisualRecorder recorder) {}

	@Override
	public void click(ActionStatus status, MouseDirection position, Keys key) {
		click(status, position);
	}

	@Override
	public void drag(ActionStatus status, MouseDirection position) {}

	@Override
	public void drop(ActionStatus status, MouseDirection md, boolean desktopDragDrop) {}

	@Override
	public void swipe(ActionStatus status, MouseDirection position, MouseDirection direction) {}

	@Override
	public void mouseWheel(int delta) {}

	@Override
	public void wheelClick(ActionStatus status, MouseDirection position) {}

	@Override
	public void doubleClick() {}

	@Override
	public String getAttribute(String name) {
		return "";
	}

	@Override
	public CalculatedProperty[] getAttributes() {
		return new CalculatedProperty[0];
	}

	@Override
	public Object executeScript(ActionStatus status, String script) {
		return null;
	}
}