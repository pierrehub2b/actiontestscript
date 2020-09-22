package com.ats.element;

import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.engines.DesktopDriverEngine;
import com.ats.generator.variables.CalculatedProperty;
import org.openqa.selenium.Alert;

public class DialogBox {
	
	private Alert alert;
	
	protected int waitBox = 500;
	
	public DialogBox(Alert alert) {
		this.alert = alert;
	}
	
	public DialogBox(DesktopDriverEngine engine, TestBound dimension) {

	}
	
	public int getWaitBox() {
		return waitBox;
	}
	
	public String getTitle() {
		return getText();
	}
	
	public String getText() {
		return alert.getText();
	}
	
	public void dismiss(ActionStatus status) {
		alert.dismiss();
	}

	public void accept(ActionStatus status) {
		alert.accept();
	}
	
	public void defaultButton(ActionStatus status) {
		alert.accept();
	}
	
	public void sendKeys(String txt) {
		alert.sendKeys(txt);
	}

	public CalculatedProperty[] getAttributes() {
		return new CalculatedProperty[] {new CalculatedProperty("text", alert.getText())};
	}
}