package com.ats.script.actions;

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionAssertProperty extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_PROPERTY = "check-property";

	private CalculatedProperty value;

	public ActionAssertProperty() {}

	public ActionAssertProperty(ScriptLoader script, boolean stop, ArrayList<String> options, ArrayList<String> objectArray, String propertyData) {
		super(script, stop, options, objectArray);
		setValue(new CalculatedProperty(script, propertyData));
	}
	
	public ActionAssertProperty(Script script, boolean stop, int maxTry, SearchedElement element, CalculatedProperty property) {
		super(script, stop, maxTry, element);
		setValue(property);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + value.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		String attributeValue = getTestElement().getAttribute(value.getName());

		if(attributeValue == null) {
			status.setPassed(false);
			status.setCode(ActionStatus.ATTRIBUTE_NOT_SET);
			status.setData(value.getName());
			status.setMessage("Attribute '" + value.getName() + "' not found !");
		}else {
			if(value.checkProperty(attributeValue)) {
				status.setPassed(true);
				status.setMessage(attributeValue);
			}else {
				status.setPassed(false);
				status.setCode(ActionStatus.ATTRIBUTE_CHECK_FAIL);
				status.setData(attributeValue);
				status.setMessage("Attribute value '" + attributeValue + "' do not match expected value '" + value.getValue().getCalculated() + "'");
			}
		}
		
		if(status.isPassed()) {
			ts.updateVisualStatus(true);
		}

		status.updateDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedProperty getValue() {
		return value;
	}

	public void setValue(CalculatedProperty value) {
		this.value = value;
	}
}
