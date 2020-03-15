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
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Utils;
import com.google.gson.JsonObject;

public class ActionText extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "keyboard";
	public static final Pattern INSERT_PATTERN = Pattern.compile("insert\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);

	private CalculatedValue text;

	private int insert = -1;

	public ActionText() {}

	public ActionText(ScriptLoader script, boolean stop, ArrayList<String> options, String text, ArrayList<String> objectArray) {
		super(script, stop, options, objectArray);
		this.text = new CalculatedValue(script, text);

		Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			final Matcher matcher = INSERT_PATTERN.matcher(itr.next().toLowerCase());
			if(matcher.find()){
				setInsert(Utils.string2Int(matcher.group(1), -1));
				break;
			}
		}
	}

	public ActionText(Script script, boolean stop, int maxTry, int delay, SearchedElement element, CalculatedValue text) {
		super(script, stop, maxTry, delay, element);
		setText(text);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ").append(text.getJavaCode()).append(")");
		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		if(status.isPassed()) {
			status.startDuration();
			getTestElement().enterText(status, text, ts.getRecorder());
		}
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("text", text.getCalculated().replaceAll("\"", "\\\""));
		return super.getActionLogs(scriptName, scriptLine, data);
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