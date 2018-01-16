package com.ats.script.actions;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionGotoUrl extends ActionExecute {

	public static final String SCRIPT_LABEL = "goto-url";

	private CalculatedValue url;

	public ActionGotoUrl() {}
	
	public ActionGotoUrl(Script script, boolean stop, CalculatedValue url) {
		super(script, stop);
		setUrl(url);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + url.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);

		String urlString = url.getCalculated();
		if(!urlString.startsWith("https://") && !urlString.startsWith("http://") && !urlString.startsWith("file://") ) {
			urlString = "http://" + urlString;
		}

		try {
			URL remote = new URL(urlString);
			
			status.setMessage(remote.toURI().toString());
			status.setPassed(true);
			
			ts.goToUrl(status, remote, false);
			status.updateDuration();

			ts.updateVisualValue(urlString);
			ts.updateVisualImage();
			
		} catch (MalformedURLException | URISyntaxException e) {
			status.setPassed(false);
			status.setData(urlString);
			status.setCode(ActionStatus.MALFORMED_GOTO_URL);
		} 
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getUrl() {
		return url;
	}

	public void setUrl(CalculatedValue url) {
		this.url = url;
	}
}