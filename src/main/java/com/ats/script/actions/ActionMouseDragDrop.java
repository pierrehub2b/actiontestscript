package com.ats.script.actions;

import java.util.ArrayList;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouseDragDrop extends ActionMouse {

	
	public ActionMouseDragDrop() {}
	
	public ActionMouseDragDrop(ScriptLoader script, String type, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stop, options, objectArray);
	}
		
	public ActionMouseDragDrop(Script script, boolean stop, int maxTry, SearchedElement element, Mouse mouse) {
		super(script, stop, maxTry, element, mouse);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);
		
		if(Mouse.DRAG.equals(getType())) {
			getTestElement().drag(status);
		}else if(Mouse.DROP.equals(getType())) {
			getTestElement().drop(status);
		}

		status.updateDuration(System.currentTimeMillis());
	}
}