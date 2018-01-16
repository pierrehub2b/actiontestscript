package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionWindowResize extends ActionWindow {

	public static final String SCRIPT_RESIZE_LABEL = SCRIPT_LABEL + "resize";
	
	private int width = 1024;
	private int height = 768;

	public ActionWindowResize() {}

	public ActionWindowResize(Script script, String size) {
		super(script, -1);
		String[] sizeData = size.split("x");
		if(sizeData.length > 1) {
			try {
				setWidth(Integer.parseInt(sizeData[0]));
				setHeight(Integer.parseInt(sizeData[1]));
			}catch(NumberFormatException e) {}
		}
	}

	public ActionWindowResize(Script script, int width, int height) {
		super(script, -1);
		setWidth(width);
		setHeight(height);
	}

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + width + ", " + height + ")";
	}

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);
		ts.resizeWindow(status, width, height);
		status.updateDuration();
		ts.updateVisualImage();
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}