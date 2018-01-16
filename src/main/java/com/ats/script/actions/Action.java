package com.ats.script.actions;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.recorder.VisualAction;
import com.ats.script.Script;

public class Action extends AbstractAction {

	protected Script script;
	protected int line;
	protected boolean disabled = false;
	protected ActionStatus status;
	
	protected VisualAction visual;

	public Action(){}

	public Action(Script script){
		this.script = script;
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode(){
		return "new " + this.getClass().getSimpleName() + "(this, ";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	public void execute(ActionTestScript ts){
		setStatus(new ActionStatus(ts.getCurrentChannel()));
		ts.newVisual(this);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public ActionStatus getStatus() {
		return status;
	}

	public void setStatus(ActionStatus status) {
		this.status = status;
	}
}