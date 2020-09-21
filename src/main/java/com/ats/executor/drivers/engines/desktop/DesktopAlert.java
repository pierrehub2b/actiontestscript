package com.ats.executor.drivers.engines.desktop;

import com.ats.element.DialogBox;
import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.engines.DesktopDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import org.openqa.selenium.NoAlertPresentException;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class DesktopAlert extends DialogBox {

	private FoundElement dialog;
	private DesktopDriverEngine engine;
	
	public DesktopAlert(DesktopDriverEngine engine, TestBound dimension) {
		super(engine, dimension);
		
		waitBox = 200;
		
		final List<FoundElement> elements = engine.getDesktopDriver().getDialogBox(dimension);
		if(elements.size() > 0) {
			this.dialog = elements.get(0);
			this.engine = engine;
		}else {
			throw new NoAlertPresentException();
		}
	}

	@Override
	public void dismiss(ActionStatus status) {
		if(dialog != null) {
			
			FoundElement closebutton = null;
			
			List<FoundElement> buttons = dialog.getChildren().stream().filter(p -> p.getTag().equals("Button")).collect(Collectors.toList());
			if(buttons.size() > 1) {
				closebutton = buttons.get(1);
			}else {
				List<FoundElement> title = dialog.getChildren().stream().filter(p -> p.getTag().equals("TitleBar")).collect(Collectors.toList());
				if(title.size() > 0) {
					FoundElement close = title.get(0).getChildren().get(0);
					if("Button".equals(close.getTag())){
						closebutton = close;
					}
				}
			}
			
			if(closebutton != null) {
				engine.mouseMoveToElement(status, closebutton, new MouseDirection(), false, 0, 0);
				engine.getDesktopDriver().mouseClick();
			}else {
				//TODO click on close button of the window
			}
		}
	}

	@Override
	public void accept(ActionStatus status) {
		if(dialog != null) {
			List<FoundElement> buttons = dialog.getChildren().stream().filter(p -> p.getTag().equals("Button")).collect(Collectors.toList());
			if(buttons.size() > 0) {
				engine.mouseMoveToElement(status, buttons.get(0), new MouseDirection(), false, 0, 0);
				engine.getDesktopDriver().mouseClick();
			}else {
				//TODO execute enter key ?
			}
		}
	}

	@Override
	public void defaultButton(ActionStatus status) {
		if(dialog != null) {
			engine.mouseMoveToElement(status, dialog, new MouseDirection(), false, 0, 0);
			//engine.sendTextData(status, dialog, textActionList);
		}
	}

	@Override
	public String getText() {
		if(dialog != null) {
			List<FoundElement> labels = dialog.getChildren().stream().filter(p -> p.getTag().equals("Text")).collect(Collectors.toList());
			if(labels.size() > 0) {
				final StringJoiner joiner = new StringJoiner("\n");
				labels.forEach(l -> joiner.add(engine.getAttribute(null, l, "Name", 1)));
				return joiner.toString();
			}
		}
		return "";
	}

	@Override
	public void sendKeys(String text) {
		if(dialog != null) {
			List<FoundElement> edits = dialog.getChildren().stream().filter(p -> p.getTag().equals("Edit")).collect(Collectors.toList());
			if(edits.size() > 0) {
				engine.getDesktopDriver().executeScript(null, "SetValue(" + text + ")", edits.get(0));
			}
		}
	}

	@Override
	public String getTitle() {
		if(dialog != null) {
			List<FoundElement> title = dialog.getChildren().stream().filter(p -> p.getTag().equals("TitleBar")).collect(Collectors.toList());
			if(title.size() > 0) {
				return engine.getAttribute(null, title.get(0), "Value", 1);
			}
		}
		return "";
	}

	@Override
	public CalculatedProperty[] getAttributes() {
		return new CalculatedProperty[] {
				new CalculatedProperty("text", getText()),
				new CalculatedProperty("title", getTitle())};
	}
}