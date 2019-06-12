package com.ats.element;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
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
		super.over(status, position, desktopDragDrop, getFoundElement().getBoundX().intValue(), getFoundElement().getBoundY().intValue());
	}

	@Override
	protected void mouseClick(ActionStatus status, MouseDirection position, int offsetX, int offsetY) {
		super.mouseClick(status, position, getFoundElement().getBoundX().intValue(), getFoundElement().getBoundY().intValue());
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Text ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public void clearText(ActionStatus status) {
		engine.clearText(status, getFoundElement());
	}
	
	/*@Override
	public void sendText(ActionStatus status, CalculatedValue text) {

	}*/

	//-------------------------------------------------------------------------------------------------------------------
	// Drag drop ...
	//-------------------------------------------------------------------------------------------------------------------

	@Override
	public void drag(ActionStatus status, MouseDirection position) {

	}

	@Override
	public void drop(ActionStatus status, MouseDirection md, boolean desktopDragDrop) {

	}

	@Override
	public void swipe(ActionStatus status, MouseDirection position, MouseDirection direction) {

	}

	@Override
	public void mouseWheel(int delta) {

	}

	@Override
	public void wheelClick(ActionStatus status, MouseDirection position) {

	}

	@Override
	public void doubleClick() {

	}

	@Override
	public String getAttribute(ActionStatus status, String name) {
		return "";
	}

	@Override
	public CalculatedProperty[] getAttributes(boolean reload) {
		return new CalculatedProperty[0];
	}

	@Override
	public Object executeScript(ActionStatus status, String script) {
		return null;
	}
}