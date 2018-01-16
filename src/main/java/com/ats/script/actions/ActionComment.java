package com.ats.script.actions;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionComment extends Action {

	public static final String SCRIPT_LABEL = "comment";

	private static final String STEP_TYPE = "step";
	private static final String LOG_TYPE = "log";
	private static final String SCRIPT_TYPE = "script";

	private CalculatedValue comment;
	private String type = STEP_TYPE;

	public ActionComment() {}

	public ActionComment(ScriptLoader script, String type, String text) {
		super(script);
		if(type.equals(LOG_TYPE) || type.equals(SCRIPT_TYPE)){
			setType(type);
		}
		setComment(new CalculatedValue(script, text));
	}

	public ActionComment(Script script, String type, CalculatedValue value) {
		super(script);
		setType(type);
		setComment(value);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		if(SCRIPT_TYPE.equals(type)) {
			return null;
		}
		return super.getJavaCode() + "\"" + type + "\", " + comment.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		if(SCRIPT_TYPE.equals(type) || LOG_TYPE.equals(type)) {
			status = new ActionStatus(ts.getCurrentChannel());
		}else {
			super.execute(ts);
			ts.updateVisualValue(type, comment.getCalculated());
		}
		status.updateDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getComment() {
		return comment;
	}

	public void setComment(CalculatedValue comment) {
		this.comment = comment;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		this.type = value;
	}
}