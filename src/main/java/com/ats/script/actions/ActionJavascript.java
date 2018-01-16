package com.ats.script.actions;

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.parsers.ScriptParser;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionJavascript extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "javascript";

	private CalculatedValue jsCode;
	private Variable variable;

	public ActionJavascript() {}

	public ActionJavascript(ScriptLoader script, boolean stop, ArrayList<String> options, String code, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);

		String[] variableArray = code.split(ScriptParser.ATS_ASSIGN_SEPARATOR);

		setJsCode(new CalculatedValue(script, variableArray[0].trim()));

		if(variableArray.length == 2){
			setVariable(script.getVariable(variableArray[1].trim(), true));
		}
	}

	public ActionJavascript(Script script, boolean stop, int maxTry, SearchedElement element, CalculatedValue jsCode, Variable variable) {
		super(script, stop, maxTry, element);
		setJsCode(jsCode);
		setVariable(variable);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		String variableCode = "null";
		if(variable != null) {
			variableCode = variable.getName();
		}
		return super.getJavaCode() + ", " + jsCode.getJavaCode() + ", " + variableCode + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		ts.updateVisualValue(jsCode.getCalculated());
		
		status.resetDuration();
		Object result = getTestElement().executeScript(status, jsCode.getCalculated());
		status.updateDuration();
		
		if(variable != null && result != null) {
			variable.updateValue(result.toString());
		}

		ts.updateVisualImage();
		
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getJsCode() {
		return jsCode;
	}

	public void setJsCode(CalculatedValue jsCode) {
		this.jsCode = jsCode;
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
}