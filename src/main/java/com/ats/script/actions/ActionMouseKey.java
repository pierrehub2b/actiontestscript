package com.ats.script.actions;

import java.util.ArrayList;
import java.util.Iterator;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.objects.mouse.MouseKey;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionMouseKey extends ActionMouse {

	public static final String CTRL_KEY = "ctrl";
	public static final String SHIFT_KEY = "shift";
	public static final String ALT_KEY = "alt";
	
	private String key = "";
	
	public ActionMouseKey(){}

	public ActionMouseKey(ScriptLoader script, String type, boolean stop, ArrayList<String> options, ArrayList<String> objectArray) {
		super(script, type, stop, options, objectArray);

		Iterator<String> itr = options.iterator();
		while (itr.hasNext()){
			String key = itr.next().trim();
			if(CTRL_KEY.equals(key) || SHIFT_KEY.equals(key) || ALT_KEY.equals(key)){
				setKey(key);
			}
		}
	}	

	public ActionMouseKey(Script script, boolean stop, int maxTry, SearchedElement element, MouseKey mouse) {
		super(script, stop, maxTry, element, mouse);
		setKey(mouse.getKey().toString());
	}
	
	public ActionMouseKey(Script script, boolean stop, int maxTry, SearchedElement element, Mouse mouse) {
		super(script, stop, maxTry, element, mouse);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		
		String keyCode = "";
		if(CTRL_KEY.equals(getKey())){
			keyCode = ", Keys.CONTROL";
		}else if(SHIFT_KEY.equals(getKey())){
			keyCode = ", Keys.SHIFT";
		}else if(ALT_KEY.equals(getKey())){
			keyCode = ", Keys.ALT";
		}

		setSpareCode(keyCode);
		
		return super.getJavaCode();
	}

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);

		if(status.isPassed()) {
			long currentTime = System.currentTimeMillis();

			if(Mouse.WHEEL_CLICK.equals(getType())) {
				getTestElement().wheelClick(status);
			}else if(Mouse.RIGHT_CLICK.equals(getType())) {
				getTestElement().rightClick(status);
			}else if(Mouse.DOUBLE_CLICK.equals(getType())) {
				getTestElement().doubleClick(status);
			}else {
				getTestElement().click(status, false);
			}

			status.updateDuration(currentTime);
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getKey() {
		return key;
	}

	public void setKey(String value) {
		this.key = value;
	}
}