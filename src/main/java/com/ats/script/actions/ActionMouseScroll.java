package com.ats.script.actions;

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.objects.mouse.MouseScroll;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouseScroll extends ActionMouse {

	public static final String SCRIPT_LABEL = "scroll";

	public static final String JAVA_FUNCTION_NAME = "scroll";

	private int value = 0;

	public ActionMouseScroll(){}

	public ActionMouseScroll(ScriptLoader script, String type, int value, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stop, options, objectArray);
		setValue(value);
	}

	public ActionMouseScroll(Script script, boolean stop, int maxTry, SearchedElement element, MouseScroll mouse) {
		super(script, stop, maxTry, element, mouse);
		setValue(mouse.getValue());
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		setSpareCode(", " + value);
		return super.getJavaCode();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		ts.updateVisualValue(value + "");
		getTestElement().mouseWheel(status, value);
		ts.updateVisualImage();
		status.updateDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}