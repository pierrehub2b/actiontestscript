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

package com.ats.generator.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.generator.GeneratorReport;
import com.ats.generator.events.ScriptProcessedNotifier;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.EnvironmentValue;
import com.ats.generator.variables.ParameterValue;
import com.ats.generator.variables.Variable;
import com.ats.script.ProjectData;
import com.ats.script.ScriptLoader;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionAssertCount;
import com.ats.script.actions.ActionAssertProperty;
import com.ats.script.actions.ActionAssertValue;
import com.ats.script.actions.ActionCallscript;
import com.ats.script.actions.ActionChannelClose;
import com.ats.script.actions.ActionChannelStart;
import com.ats.script.actions.ActionChannelSwitch;
import com.ats.script.actions.ActionComment;
import com.ats.script.actions.ActionExecute;
import com.ats.script.actions.ActionGotoUrl;
import com.ats.script.actions.ActionJavascript;
import com.ats.script.actions.ActionMouse;
import com.ats.script.actions.ActionMouseDragDrop;
import com.ats.script.actions.ActionMouseKey;
import com.ats.script.actions.ActionMouseScroll;
import com.ats.script.actions.ActionMouseSwipe;
import com.ats.script.actions.ActionProperty;
import com.ats.script.actions.ActionSelect;
import com.ats.script.actions.ActionText;
import com.ats.script.actions.ActionWindow;
import com.ats.script.actions.ActionWindowClose;
import com.ats.script.actions.ActionWindowResize;
import com.ats.script.actions.ActionWindowSwitch;

public class Lexer {

	private final Pattern actionPattern = Pattern.compile("(.*)\\[(.*?)\\]", Pattern.CASE_INSENSITIVE);

	private GeneratorReport report;//TODO create report with list of failed ATS to java conversions steps

	private ProjectData projectData;

	private String charset = ScriptLoader.DEFAULT_CHARSET;

	public int countScript = 0;

	private boolean isGenerator = true;

	public boolean isGenerator() {
		return isGenerator;
	}

	public Lexer(GeneratorReport report){
		this.isGenerator = false;
		this.report = report;
	}

	public Lexer(ProjectData projectData, GeneratorReport report, String charset) {
		this.projectData = projectData;
		this.charset = charset;
		this.report = report;
	}

	public Pattern getActionPattern(){
		return actionPattern;
	}

	public void addScript(){
		countScript++;
	}

	public ScriptLoader loadScript(File f, ScriptProcessedNotifier notifier){
		ScriptLoader script = getScript(f);
		notifier.scriptProcessed();
		return script;
	}

	public ScriptLoader loadScript(File f) {
		return getScript(f);
	}

	private ScriptLoader getScript(File f) {
		if(f.exists() && f.isFile()){
			return new ScriptLoader(ScriptLoader.ATS_EXTENSION, this, f, projectData, charset);
		}
		return null;
	}

	//---------------------------------------------------------------------------------------------------------------
	// Steps creation
	//---------------------------------------------------------------------------------------------------------------

	public Action createAction(ScriptLoader script, String data, boolean actionDisabled){

		Action action = null;
		Matcher matcher = null;

		ArrayList<String> dataArray = new ArrayList<String>(Arrays.asList(data.split(ScriptParser.ATS_SEPARATOR)));

		if(dataArray.size() > 0){

			String actionType = dataArray.remove(0);

			if(actionType != null){

				actionType = actionType.trim().toLowerCase();

				ArrayList<String> options = new ArrayList<String>();
				boolean stopExec = true;

				matcher = actionPattern.matcher(actionType);
				if (matcher.find()){
					actionType = matcher.group(1).trim();
					options = new ArrayList<>(Arrays.asList(matcher.group(2).split(",")));
					stopExec = !options.remove(ActionExecute.NO_FAIL_LABEL);
				}

				//---------------------------------------------------------------------------
				// Mouse over
				//---------------------------------------------------------------------------

				if(Mouse.OVER.equals(actionType)){

					action = new ActionMouse(script, Mouse.OVER, stopExec, options, dataArray);

					//---------------------------------------------------------------------------
					// Drag Drop
					//---------------------------------------------------------------------------

				}else if(Mouse.DRAG.equals(actionType) || Mouse.DROP.equals(actionType)){

					action = new ActionMouseDragDrop(script, actionType, stopExec, options, dataArray);

					//---------------------------------------------------------------------------
					// Mouse button
					//---------------------------------------------------------------------------

				}else if(actionType.startsWith(Mouse.CLICK)){

					action = new ActionMouseKey(script, actionType, stopExec, options, dataArray);

				}else if(ActionChannelClose.SCRIPT_CLOSE_LABEL.equals(actionType)){

					if(dataArray.size() > 0) {
						action = new ActionChannelClose(script, dataArray.remove(0).trim());
					}else {
						action = new ActionChannelClose(script);
					}

				}else if(dataArray.size() > 0){

					String dataOne = dataArray.remove(0).trim();

					//---------------------------------------------------------------------------
					// Channel
					//---------------------------------------------------------------------------

					if(ActionChannelSwitch.SCRIPT_SWITCH_LABEL.equals(actionType)){	

						action = new ActionChannelSwitch(script, dataOne);

					}else if(ActionChannelStart.SCRIPT_START_LABEL.equals(actionType)){

						if(dataArray.size() > 0){
							CalculatedValue appData = new CalculatedValue(script, dataArray.remove(0).trim());
							action = new ActionChannelStart(script, dataOne, appData);
						}

						//---------------------------------------------------------------------------
						// Text
						//---------------------------------------------------------------------------

					}else if(ActionText.SCRIPT_LABEL.equals(actionType)){

						action = new ActionText(script, actionType, stopExec, options, dataOne, dataArray);

						//---------------------------------------------------------------------------
						// Window
						//---------------------------------------------------------------------------

					}else if(actionType.startsWith(ActionWindow.SCRIPT_LABEL)){

						if(ActionWindowResize.SCRIPT_RESIZE_LABEL.equals(actionType)){
							action = new ActionWindowResize(script, dataOne);													
						}else {
							int num = 0;
							try {
								num = Integer.parseInt(dataOne);
							}catch(NumberFormatException e) {}

							if(ActionWindowSwitch.SCRIPT_SWITCH_LABEL.equals(actionType)){
								action = new ActionWindowSwitch(script, num);
							}else {
								action = new ActionWindowClose(script, num);
							}
						}

						//---------------------------------------------------------------------------
						// Javascript
						//---------------------------------------------------------------------------

					}else if(ActionJavascript.SCRIPT_LABEL.equals(actionType)){

						String[] jsDataArray = dataOne.split(ScriptParser.ATS_ASSIGN_SEPARATOR);

						if(jsDataArray.length > 0) {

							Variable variable = null;
							String jsCode = jsDataArray[0].trim();

							if(jsDataArray.length > 1) {
								variable = script.getVariable(jsDataArray[1].trim(), true);
							}

							action = new ActionJavascript(script, stopExec, options, jsCode, variable, dataArray);
						}

						//---------------------------------------------------------------------------
						// Property
						//---------------------------------------------------------------------------

					}else if(ActionProperty.SCRIPT_LABEL.equals(actionType)){

						String[] propertyArray = dataOne.split(ScriptParser.ATS_ASSIGN_SEPARATOR);

						if(propertyArray.length > 1){
							String propertyName = propertyArray[0].trim();
							Variable variable = script.getVariable(propertyArray[1].trim(), true);
							action = new ActionProperty(script, stopExec, options, propertyName, variable, dataArray);
						}

						//---------------------------------------------------------------------------
						// Select
						//---------------------------------------------------------------------------

					}else if(ActionSelect.SCRIPT_LABEL_SELECT.equals(actionType) || ActionSelect.SCRIPT_LABEL_DESELECT.equals(actionType)){

						action = new ActionSelect(script, actionType, stopExec, options, dataOne, dataArray);

						//---------------------------------------------------------------------------
						// Callscript
						//---------------------------------------------------------------------------

					}else if(ActionCallscript.SCRIPT_LABEL.equals(actionType)){

						String[] parameters = null;
						String[] returnValues = null;

						if(dataOne.contains(ScriptParser.ATS_ASSIGN_SEPARATOR)) {
							String[] callscriptData = dataOne.split(ScriptParser.ATS_ASSIGN_SEPARATOR);
							dataOne = callscriptData[0].trim();
							returnValues = callscriptData[1].split(",");
						}

						matcher = actionPattern.matcher(dataOne);
						if (matcher.find()){
							dataOne = matcher.group(1).trim();
							String parametersData = matcher.group(2);

							Matcher mv = ParameterValue.PARAMETER_PATTERN.matcher(parametersData);
							while (mv.find()) {
								ParameterValue sp = new ParameterValue(mv);
								parametersData = parametersData.replace(sp.getReplace(), sp.getNoComma());
							}

							mv = EnvironmentValue.ENV_PATTERN.matcher(parametersData);
							while (mv.find()) {
								EnvironmentValue sp = new EnvironmentValue(mv);
								parametersData = parametersData.replace(sp.getReplace(), sp.getNoComma());
							}

							parameters = parametersData.split(",");
						}			

						action = new ActionCallscript(script, dataOne, parameters, returnValues);

						//---------------------------------------------------------------------------
						// Comments
						//---------------------------------------------------------------------------

					}else if(ActionComment.SCRIPT_LABEL.equals(actionType)){

						String textData = "";
						if(dataArray.size() > 0){
							textData = dataArray.remove(0).trim();
						}
						action = new ActionComment(script, dataOne, textData);

						//---------------------------------------------------------------------------
						// Goto url
						//---------------------------------------------------------------------------

					}else if(ActionGotoUrl.SCRIPT_LABEL.equals(actionType)){

						action = new ActionGotoUrl(script, stopExec, dataOne);

						//---------------------------------------------------------------------------
						// Mouse scroll
						//---------------------------------------------------------------------------

					}else if(ActionMouseScroll.SCRIPT_LABEL.equals(actionType)){

						int scrollValue = 0;
						try{
							scrollValue = Integer.parseInt(dataOne);
						}catch (NumberFormatException e) {}

						action = new ActionMouseScroll(script, scrollValue, stopExec, options, dataArray);

					}else if(ActionMouseSwipe.SCRIPT_LABEL.equals(actionType)){

						action = new ActionMouseSwipe(script, actionType, dataOne, stopExec, options, dataArray);

						//---------------------------------------------------------------------------
						// Assertions
						//---------------------------------------------------------------------------

					}else if(ActionAssertCount.SCRIPT_LABEL_COUNT.equals(actionType)){

						action = new ActionAssertCount(script, stopExec, options, dataArray, dataOne);

					}else if(ActionAssertProperty.SCRIPT_LABEL_PROPERTY.equals(actionType)){

						action = new ActionAssertProperty(script, stopExec, options, dataArray, dataOne);

					}else if(ActionAssertValue.SCRIPT_LABEL_VALUE.equals(actionType)){

						action = new ActionAssertValue(script, stopExec, dataOne.trim());

					}
				}
			}
		}

		if(action != null){
			action.setDisabled(actionDisabled);
		}

		return action;
	}
}