package com.ats.script.actions;

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;

public class ActionProperty extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "property";

	private String name;
	private Variable variable;

	public ActionProperty() {}

	public ActionProperty(Script script, boolean stop, ArrayList<String> options, String name, String varName, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setName(name);
		setVariable(script.getVariable(varName, true));
	}
	
	public ActionProperty(Script script, boolean stop, int maxTry, SearchedElement element, String name, Variable variable) {
		super(script, stop, maxTry, element);
		setName(name);
		setVariable(variable);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", \"" + name + "\", " + variable.getName() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		String attributeValue = getTestElement().getAttribute(name);

		if(attributeValue == null) {
			status.setPassed(false);
			status.setCode(ActionStatus.ATTRIBUTE_NOT_SET);
		}else {
			status.setMessage(attributeValue);
			variable.updateValue(attributeValue);
		}

		status.updateDuration();
		ts.updateVisualValue(name, status.getElement().toString());
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
}