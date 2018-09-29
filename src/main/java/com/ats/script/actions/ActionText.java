/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.ats.script.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionText extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "keyboard";

	public static final Pattern INSERT_PATTERN = Pattern.compile("insert\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
	//public static final Pattern KEY_REGEXP = Pattern.compile("\\$key\\s?\\((\\w+)\\-?([^\\)]*)?\\)");

	private CalculatedValue text;

	private int insert = -1;

	public ActionText() {}

	public ActionText(ScriptLoader script, String type, boolean stop, ArrayList<String> options, String text, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		this.text = new CalculatedValue(script, text);

		Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			String data = itr.next();
			Matcher matcher = INSERT_PATTERN.matcher(data);
			if(matcher.find()){
				try {
					setInsert(Integer.parseInt(matcher.group(1)));
				}catch(NumberFormatException e){}
				break;
			}
		}
	}

	public ActionText(Script script, boolean stop, int maxTry, SearchedElement element, CalculatedValue text) {
		super(script, stop, maxTry, element);
		setText(text);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		return super.getJavaCode() + ", " + text.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {

		super.terminateExecution(ts);

		String dataText = "";
		if(text != null){
			dataText = text.getCalculated();
		}

		status.startDuration();
		MouseDirection md = new MouseDirection();

		getTestElement().over(status, md);
		if(status.isPassed()) {
			getTestElement().click(status, md, false);
			if(status.isPassed()) {
				getTestElement().clearText(status);
				if(status.isPassed()) {
					
					ts.getRecorder().updateScreen(true);
					getTestElement().sendText(status, text);
					
					status.endDuration();
					ts.getRecorder().updateScreen(0, status.getDuration(), dataText);
				}
			}
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public CalculatedValue getText() {
		return text;
	}

	public void setText(CalculatedValue text) {
		this.text = text;
	}

	public int getInsert() {
		return insert;
	}

	public void setInsert(int insert) {
		this.insert = insert;
	}
}