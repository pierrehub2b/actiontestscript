package com.ats.element;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.ats.executor.ActionStatus;
import com.ats.executor.SendKeyData;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.graphic.TemplateMatchingSimple;
import com.ats.tools.Utils;

public class TestElementImage extends TestElement {

	public TestElementImage(Channel channel, int maxTry, Predicate<Integer> predicate, SearchedElement searchElement) {
		super(channel, maxTry,predicate, searchElement);
	}

	@Override
	protected ArrayList<FoundElement> loadElements(SearchedElement searchedElement) {
		final TemplateMatchingSimple template = new TemplateMatchingSimple(searchedElement.getImage());

		for (CalculatedProperty property : searchedElement.getCriterias()){
			if("error".equals(property.getName())){
				final String value = property.getValue().getCalculated();
				if(value.endsWith("%")) {
					template.setPercentError(Utils.string2Double(value.replace("%", "").trim()));
				}else{
					template.setError(Utils.string2Int(value.trim()));
				}
				break;
			}
		}
		return engine.findElements(parent, template);
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Mouse ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public void over(ActionStatus status, MouseDirection position, boolean desktopDragDrop, int offsetX, int offsetY) {
		final FoundElement fe = getFoundElement();

		int halfWidth = Utils.string2Int(fe.getValue().getAttribute("clientWidth"))/2;
		int halfHeight = Utils.string2Int(fe.getValue().getAttribute("clientHeight"))/2;

		super.over(status, position, desktopDragDrop, fe.getBoundX().intValue() - halfWidth, fe.getBoundY().intValue() - halfHeight);
	}

	@Override
	protected void mouseClick(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		final FoundElement fe = getFoundElement();

		final int halfWidth = Utils.string2Int(fe.getValue().getAttribute("clientWidth"))/2;
		final int halfHeight = Utils.string2Int(fe.getValue().getAttribute("clientHeight"))/2;

		super.mouseClick(status, position, fe.getBoundX().intValue() - halfWidth, fe.getBoundY().intValue() - halfHeight);
	}

	@Override
	public void mouseWheel(int delta) {
		// do nothing for the moment
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Text ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public void clearText(ActionStatus status) {
		engine.getDesktopDriver().clearText();
	}

	@Override
	public void sendText(ActionStatus status, CalculatedValue text) {
		for(SendKeyData sequence : text.getCalculatedText()) {
			engine.getDesktopDriver().sendKeys(sequence.getSequenceDesktop());
		}
		channel.actionTerminated(status);
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Drag drop ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public String getAttribute(ActionStatus status, String name) {
		if("x".equals(name)) {
			return getFoundElement().getBoundX() + "";
		}else if("y".equals(name)) {
			return getFoundElement().getBoundY() + "";
		}
		return "";
	}

	@Override
	public void drag(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		super.drag(status, position, getFoundElement().getBoundX().intValue(), getFoundElement().getBoundY().intValue());
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		return new CalculatedProperty[0];
	}

	@Override
	public Object executeScript(ActionStatus status, String script, boolean returnValue) {
		return null;
	}
}