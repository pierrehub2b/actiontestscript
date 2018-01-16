package com.ats.script.actions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Operators;

public class ActionAssertCount extends ActionExecuteElement {

	public static final String SCRIPT_LABEL_COUNT = "check-count";

	private final Pattern COUNT_PATTERN = Pattern.compile("(.*)(\\d+) ?(\\-?\\+?)");

	private int value = 1;
	private String operator = Operators.EQUAL;

	public ActionAssertCount() {}

	public ActionAssertCount(ScriptLoader script, boolean stop, ArrayList<String> options, ArrayList<String> objectArray, String data) {
		super(script, stop, options, objectArray);

		Matcher m = COUNT_PATTERN.matcher(data);
		if(m.matches()) {
			try {
				setValue(Integer.parseInt(m.group(2))); 
			}catch(NumberFormatException e) {}

			if(m.groupCount() > 2) {
				if("+".equals(m.group(3))) {
					setOperator(Operators.GREATER_EQUALS);
				}else if ("-".equals(m.group(3)) && this.value > 0){
					setOperator(Operators.LOWER_EQUALS);
				}else if ("!".equals(m.group(3))){
					setOperator(Operators.DIFFERENT);
				}
			}
		}
	}

	public ActionAssertCount(Script script, boolean stop, int maxTry, SearchedElement element, String operator, int value) {
		super(script, stop, maxTry, element);
		setOperator(operator);
		setValue(value);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + Operators.getJavaCode(operator) + ", " + value + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		
		super.terminateExecution(ts);
		
		int count = getTestElement().getCount();
		
		if(checkAssertion(count)) {
			status.setPassed(true);
		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.OCCURRENCES_ERROR);
			status.setData(count);
			status.setMessage("occurences found : " + count);
		}
		
		status.getElement().setFoundElements(null);
		status.updateDuration();

		ts.updateVisualValue("occurences", operator + " " + value);
	}
	
	private boolean checkAssertion(int count) {
		switch(operator){

		case Operators.EQUAL :
			return count == value;

		case Operators.GREATER :
			return count > value;

		case Operators.GREATER_EQUALS :
			return count >= value;

		case Operators.LOWER :
			return count < value;

		case Operators.LOWER_EQUALS :
			return count <= value;

		case Operators.DIFFERENT :
			return count != value;

		default :
			return false;
		}
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
	
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}
