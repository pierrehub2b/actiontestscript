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
import com.ats.script.actions.ActionApi;
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
import com.ats.script.actions.ActionScripting;
import com.ats.script.actions.ActionMouse;
import com.ats.script.actions.ActionMouseDragDrop;
import com.ats.script.actions.ActionMouseKey;
import com.ats.script.actions.ActionMouseScroll;
import com.ats.script.actions.ActionMouseSwipe;
import com.ats.script.actions.ActionProperty;
import com.ats.script.actions.ActionSelect;
import com.ats.script.actions.ActionText;
import com.ats.script.actions.ActionWindow;
import com.ats.script.actions.ActionWindowResize;
import com.ats.script.actions.ActionWindowState;
import com.ats.script.actions.ActionWindowSwitch;
import com.ats.script.actions.neoload.ActionNeoloadContainer;
import com.ats.script.actions.neoload.ActionNeoloadRecord;
import com.ats.script.actions.neoload.ActionNeoloadStart;
import com.ats.script.actions.neoload.ActionNeoloadStop;
import com.ats.tools.Utils;

public class Lexer {

	private final Pattern ACTION_PATTERN = Pattern.compile("(.*)\\[(.*?)\\]", Pattern.CASE_INSENSITIVE);

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

	public void createAction(ScriptLoader script, String data, boolean disabled){

		final ArrayList<String> dataArray = new ArrayList<String>(Arrays.asList(data.split(ScriptParser.ATS_SEPARATOR)));

		if(dataArray.size() > 0){

			String actionType = dataArray.remove(0).trim().toLowerCase();

			String optionsFlat;
			ArrayList<String> options;
			boolean stopExec;

			Matcher matcher = ACTION_PATTERN.matcher(actionType);
			if (matcher.find()){
				actionType = matcher.group(1).trim();
				optionsFlat = matcher.group(2).trim();
				options = new ArrayList<>(Arrays.asList(optionsFlat.split(",")));
				stopExec = !options.remove(ActionExecute.NO_FAIL_LABEL);
			}else {
				optionsFlat = "";
				options = new ArrayList<String>();
				stopExec = true;
			}

			if(Mouse.OVER.equals(actionType)){

				//-----------------------
				// Mouse over action
				//-----------------------

				script.addAction(new ActionMouse(script, Mouse.OVER, stopExec, options, dataArray), disabled);

			}else if(Mouse.DRAG.equals(actionType) || Mouse.DROP.equals(actionType)){

				//-----------------------
				// Drag drop action
				//-----------------------

				script.addAction(new ActionMouseDragDrop(script, actionType, stopExec, options, dataArray), disabled);

			}else if(actionType.startsWith(Mouse.CLICK)){

				//-----------------------
				// Mouse button action
				//-----------------------

				script.addAction(new ActionMouseKey(script, actionType, stopExec, options, dataArray), disabled);

			}else if(ActionChannelClose.SCRIPT_CLOSE_LABEL.equals(actionType)){

				//-----------------------
				// Channel close action
				//-----------------------

				String cname = "";
				if(dataArray.size() > 0) {
					cname = dataArray.remove(0).trim();
				}				
				script.addAction(new ActionChannelClose(script, cname), disabled);

			}else if(ActionNeoloadStart.SCRIPT_LABEL.equals(actionType) || ActionNeoloadStop.SCRIPT_LABEL.equals(actionType)){

				//-------------------------------
				// Neoload start and stop actions
				//-------------------------------

				String userData = null;
				if(dataArray.size() > 0) {
					userData = dataArray.remove(0).trim();
				}

				optionsFlat = optionsFlat.toLowerCase();

				if(ActionNeoloadStart.SCRIPT_LABEL.equals(actionType)) {
					script.addAction(new ActionNeoloadStart(script, optionsFlat, userData), disabled);
				}else {
					String userOptions = "";
					if(userData != null) {
						matcher = ACTION_PATTERN.matcher(userData);
						if (matcher.find()){
							userData = matcher.group(1).trim();
							userOptions = matcher.group(2).trim().toLowerCase();
						}
					}
					script.addAction(new ActionNeoloadStop(script, optionsFlat, userData, userOptions), disabled);
				}

			}else if(dataArray.size() > 0){

				String dataOne = dataArray.remove(0).trim();

				if(ActionChannelSwitch.SCRIPT_SWITCH_LABEL.equals(actionType)){	

					//-----------------------
					// Channel switch
					//-----------------------

					script.addAction(new ActionChannelSwitch(script, dataOne), disabled);

				}else if(ActionChannelStart.SCRIPT_START_LABEL.equals(actionType)){

					//-----------------------
					// Channel start action
					//-----------------------

					if(dataArray.size() > 0){
						script.addAction(new ActionChannelStart(script, dataOne, options, new CalculatedValue(script, dataArray.remove(0).trim()), dataArray), disabled);
					}

				}else if(ActionText.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Text action
					//-----------------------

					script.addAction(new ActionText(script, stopExec, options, dataOne, dataArray), disabled);

				}else if(actionType.startsWith(ActionApi.SCRIPT_LABEL)){

					//-----------------------
					// Api action
					//-----------------------

					String headerData = "";

					matcher = ACTION_PATTERN.matcher(dataOne);
					if (matcher.find()){
						dataOne = matcher.group(1).trim();
						headerData = matcher.group(2).trim();
					}
					script.addAction(new ActionApi(script, actionType, optionsFlat, dataOne, headerData, dataArray), disabled);

				}else if(ActionWindowResize.SCRIPT_RESIZE_LABEL.equals(actionType)){

					//-----------------------
					// Window resize action
					//-----------------------

					script.addAction(new ActionWindowResize(script, dataOne), disabled);

				}else if(ActionWindowState.SCRIPT_STATE_LABEL.equals(actionType)){

					script.addAction(new ActionWindowState(script, dataOne), disabled);

				}else if(actionType.startsWith(ActionWindow.SCRIPT_LABEL)){

					//-----------------------
					// Window action
					//-----------------------

					script.addAction(new ActionWindowSwitch(script, Utils.string2Int(dataOne)), disabled);

					if(ActionWindowState.SCRIPT_CLOSE_LABEL.equals(actionType)){
						script.addAction(new ActionWindowState(script, ActionWindowState.CLOSE), disabled);
					}

				}else if(actionType.equals(ActionScripting.SCRIPT_LABEL) || actionType.equals(ActionScripting.JAVASCRIPT_LABEL)){

					//-----------------------
					// Javascript action
					//-----------------------

					final String[] jsDataArray = dataOne.split(ScriptParser.ATS_ASSIGN_SEPARATOR);

					if(jsDataArray.length > 0) {

						Variable variable = null;
						final String jsCode = jsDataArray[0].trim();

						if(jsDataArray.length > 1) {
							variable = script.getVariable(jsDataArray[1].trim(), true);
						}

						script.addAction(new ActionScripting(script, stopExec, options, jsCode, variable, dataArray), disabled);
					}
					
				}else if(ActionProperty.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Property action
					//-----------------------

					final String[] propertyArray = dataOne.split(ScriptParser.ATS_ASSIGN_SEPARATOR);

					if(propertyArray.length > 1){
						final String propertyName = propertyArray[0].trim();
						final Variable variable = script.getVariable(propertyArray[1].trim(), true);
						script.addAction(new ActionProperty(script, stopExec, options, propertyName, variable, dataArray), disabled);
					}

				}else if(actionType.contains(ActionSelect.SCRIPT_LABEL_SELECT)){

					//-----------------------
					// Select action
					//-----------------------

					script.addAction(new ActionSelect(script, dataOne, stopExec, options, dataArray), disabled);

				}else if(ActionCallscript.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Callscript action
					//-----------------------

					String[] parameters = null;
					String[] returnValues = null;
					String csvFilePath = null;

					if(dataOne.contains(ScriptParser.ATS_ASSIGN_SEPARATOR)) {
						final String[] callscriptData = dataOne.split(ScriptParser.ATS_ASSIGN_SEPARATOR);
						dataOne = callscriptData[0].trim();
						returnValues = callscriptData[1].split(",");
					}

					if(dataArray.size() > 0) {
						csvFilePath = dataArray.remove(0).trim();
					}else {

						matcher = ACTION_PATTERN.matcher(dataOne);
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
								final EnvironmentValue sp = new EnvironmentValue(mv);
								parametersData = parametersData.replace(sp.getReplace(), sp.getNoComma());
							}

							parameters = parametersData.split(",");
						}	
					}

					script.addAction(new ActionCallscript(script, dataOne, parameters, returnValues, csvFilePath), disabled);

				}else if(ActionComment.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Comment action
					//-----------------------

					script.addAction(new ActionComment(script, dataOne, dataArray), disabled);

				}else if(ActionGotoUrl.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Goto url action
					//-----------------------

					script.addAction(new ActionGotoUrl(script, stopExec, new CalculatedValue(script, dataOne)), disabled);

				}else if(ActionMouseScroll.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Mouse scroll action
					//-----------------------

					script.addAction(new ActionMouseScroll(script, dataOne, stopExec, options, dataArray), disabled);

				}else if(ActionMouseSwipe.SCRIPT_LABEL.equals(actionType)){

					script.addAction(new ActionMouseSwipe(script, actionType, dataOne, stopExec, options, dataArray), disabled);

				}else if(ActionAssertCount.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Assert count action
					//-----------------------

					script.addAction(new ActionAssertCount(script, stopExec, options, dataOne, dataArray), disabled);

				}else if(ActionAssertProperty.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Assert property action
					//-----------------------

					script.addAction(new ActionAssertProperty(script, stopExec, options, dataOne, dataArray), disabled);

				}else if(ActionAssertValue.SCRIPT_LABEL.equals(actionType)){

					//-----------------------
					// Assert value action
					//-----------------------

					script.addAction(new ActionAssertValue(script, stopExec, dataOne), disabled);

				}else if(ActionNeoloadContainer.SCRIPT_LABEL.equals(actionType)){

					//--------------------------
					// Neoload container action
					//--------------------------

					if(dataOne.length() > 0){
						script.addAction(new ActionNeoloadContainer(script, dataOne), disabled);
					}

				}else if(ActionNeoloadRecord.SCRIPT_LABEL.equals(actionType)){

					//--------------------------
					// Neoload record actions
					//--------------------------

					script.addAction(new ActionNeoloadRecord(script, dataOne), disabled);

				}
			}
		}
	}
}