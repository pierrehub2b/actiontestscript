package com.ats.script.actions;

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionSelect extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_SELECT = "select";
	public static final String SCRIPT_LABEL_DESELECT = "deselect";
	
	public static final String SELECT_TEXT = "text";
	public static final String SELECT_VALUE = "value";
	public static final String SELECT_INDEX = "index";

	private CalculatedProperty selectValue;
	
	private boolean select = true;
	private boolean ctrl = false;

	public ActionSelect() {}

	public ActionSelect(ScriptLoader script, String type, boolean stop, ArrayList<String> options, String select, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		setSelect(!SCRIPT_LABEL_DESELECT.toLowerCase().equals(type.toLowerCase()));
		setSelectValue(new CalculatedProperty(script, select));
		if(options != null && options.size() > 0) {
			this.ctrl = options.remove(ActionMouseKey.CTRL_KEY);
		}
	}

	public ActionSelect(Script script, boolean stop, int maxTry, SearchedElement element, boolean select, CalculatedProperty selectValue) {
		super(script, stop, maxTry, element);
		setSelect(select);
		setSelectValue(selectValue);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + select + ", " + selectValue.getJavaCode() + ")";
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		getTestElement().select(status, selectValue, select, ctrl);
		ts.updateVisualImage();
		status.updateDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedProperty getSelectValue() {
		return selectValue;
	}

	public void setSelectValue(CalculatedProperty value) {
		this.selectValue = value;
	}

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}
	
	public boolean isCtrl() {
		return ctrl;
	}

	public void setCtrl(boolean ctrl) {
		this.ctrl = ctrl;
	}
}