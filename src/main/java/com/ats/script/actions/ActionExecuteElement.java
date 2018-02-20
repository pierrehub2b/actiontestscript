package com.ats.script.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.TestElement;
import com.ats.script.Script;

public class ActionExecuteElement extends ActionExecute {

	public static final Pattern MAX_TRY_PATTERN = Pattern.compile("try\\((\\-?\\d+)\\)", Pattern.CASE_INSENSITIVE);

	private int maxTry = 0;
	private SearchedElement searchElement;
	private TestElement testElement;

	private int expectedCount = 1;

	private boolean async;

	public ActionExecuteElement() {}

	public ActionExecuteElement(Script script, boolean stop, ArrayList<String> options, ArrayList<String> element) {

		super(script, stop);

		if(element != null && element.size() > 0){
			setSearchElement(new SearchedElement(script, element));
		}

		Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			Matcher matcher = MAX_TRY_PATTERN.matcher(itr.next());
			if(matcher.find()){
				try {
					setMaxTry(Integer.parseInt(matcher.group(1)));
				}catch(NumberFormatException e){}
				itr.remove();
			}
		}
	}

	public ActionExecuteElement(Script script, boolean stop, int maxTry, SearchedElement element) {
		super(script, stop);
		setMaxTry(maxTry);
		setSearchElement(element);
	}

	protected void setExpectedCount(int value) {
		this.expectedCount = value;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		StringBuilder codeBuilder = new StringBuilder(super.getJavaCode());
		codeBuilder.append(maxTry);
		codeBuilder.append(", ");

		if(searchElement == null){
			codeBuilder.append(ActionTestScript.JAVA_ROOT_FUNCTION_NAME);
			codeBuilder.append("()");
		}else {
			codeBuilder.append(searchElement.getJavaCode());
		}
		return codeBuilder.toString();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {

		super.execute(ts);

		if(ts.getCurrentChannel() == null) {

			status.setPassed(false);
			status.setCode(ActionStatus.CHANNEL_NOT_FOUND);

		}else {

			if(testElement == null) {
				if(searchElement == null) {
					testElement = ts.findObject();
				}else {
					testElement = ts.findObject(maxTry, searchElement, expectedCount);
				}

				ts.updateVisualElement(testElement);

				status.setElement(testElement);
				status.setSearchDuration(testElement.getTotalSearchDuration());
				status.setData(testElement.getCount());

				if(testElement.isValidated()) {
					status.setPassed(true);
					asyncExec(ts);
				}else {
					status.setPassed(false);
					status.setCode(ActionStatus.OBJECT_NOT_FOUND);
					status.setMessage("Element not found");
				}
				
			}else {
				terminateExecution(ts);
			}
			
			status.updateDuration();
		}
	}	

	private void asyncExec(ActionTestScript ts) {
		if(!async) {
			terminateExecution(ts);
		}
	}

	public void terminateExecution(ActionTestScript ts) {
		getTestElement().terminateExecution();
	}

	public TestElement getTestElement() {
		return testElement;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public SearchedElement getSearchElement() {
		return searchElement;
	}

	public void setSearchElement(SearchedElement elem) {
		this.searchElement = elem;
	}

	public int getMaxTry() {
		return maxTry;
	}

	public void setMaxTry(int maxTry) {
		this.maxTry = maxTry;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
}