package com.ats.script.actions;

import com.ats.generator.variables.Variable;
import com.ats.script.Script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class ActionReturnVariableArray extends Action {
	
	protected ArrayList<Variable> variables;
	
	public ActionReturnVariableArray() { }
	
	
	public ActionReturnVariableArray(Script script) {
		super(script);
	}
	
	public ActionReturnVariableArray(Script script, ArrayList<Variable> variables) {
		super(script);
		setVariables(variables);
	}
	
	public ActionReturnVariableArray(Script script, Variable variable) {
		super(script);
		setVariables(new ArrayList<>(Collections.singletonList(variable)));
	}
	
	public ActionReturnVariableArray(Script script, Variable[] variables) {
		super(script);
		setVariables(new ArrayList<>(Arrays.asList(variables)));
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public ArrayList<Variable> getVariables() {
		return variables;
	}
	
	public void setVariables(ArrayList<Variable> variables) {
		this.variables = variables;
	}
	
}
