package com.ats.script.actions;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

import java.util.ArrayList;

public class ActionGestureTap extends ActionExecuteElement {
	
	public static final String SCRIPT_LABEL = "tap";
	
	private int count;
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------------------------------------------------------------
	
	public ActionGestureTap(Script script, String count, boolean stop, ArrayList<String> options, ArrayList<String> element) {
		super(script, stop, options, element);
		setCount(Integer.parseInt(count));
	}
	
	public ActionGestureTap(Script script, boolean stop, int maxTry, int delay, SearchedElement element, int count) {
		super(script, stop, maxTry, delay, element);
		setCount(count);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		
		if (status.isPassed()) {
			ts.getRecorder().updateScreen(true);
			
			getTestElement().tap(count);
			
			status.endAction();
			ts.getRecorder().updateScreen(0, status.getDuration());
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ") .append(count);
		return codeBuilder;
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public int getCount() { return count; }
	public void setCount(int count) { this.count = count; }
}
