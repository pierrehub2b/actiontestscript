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

import static org.testng.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.element.SearchedElement;
import com.ats.element.TestElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.ConditionalValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.parameter.Parameter;
import com.ats.generator.variables.parameter.ParameterDataFile;
import com.ats.generator.variables.parameter.ParameterList;
import com.ats.script.Project;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Utils;
import com.ats.tools.logger.MessageCode;
import com.ats.tools.logger.levels.AtsFailError;
import com.google.gson.JsonObject;

public class ActionCallscript extends ActionReturnVariableArray {

	public static final String SCRIPT_LABEL = "subscript";
	private static final int RND_INDEX_VALUE = -1;
	private static final int NO_INDEX_VALUE = -2;

	private static final String SCRIPT_LOOP = "loop";
	public static final Pattern LOOP_REGEXP = Pattern.compile(SCRIPT_LOOP + " ?\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
	public static final String ASSETS_PROTOCOLE = "assets:///";
	public static final String FILE_PROTOCOLE = "file:///";
	public static final String HTTP_PROTOCOLE = "http://";
	public static final String HTTPS_PROTOCOLE = "https://";

	private CalculatedValue name;
	private int type = -1;
	
	private SearchedElement searchElement;

	private ArrayList<Variable> scriptVariables;

	private ParameterList parameters;

	private int loop = 1;
	private int parameterFileIndex = NO_INDEX_VALUE;
	private CalculatedValue parameterFilePath;

	private ConditionalValue condition;

	//---------------------------------------------------------------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------------------------------------------------------------

	public ActionCallscript() {}

	public ActionCallscript(ScriptLoader script, ArrayList<String> options, String name, String[] parameters, String[] returnValue, String csvFilePath, ArrayList<String> dataArray) {

		super(script);
		setName(new CalculatedValue(script, name));

		if(csvFilePath != null) {
			setParameterFilePath(new CalculatedValue(script, csvFilePath));
			if(dataArray != null && dataArray.size() > 0) {
				final String l = dataArray.remove(0);
				if(l.contains("rnd") || l.contains("random")) {
					parameterFileIndex = RND_INDEX_VALUE;
				}else {
					parameterFileIndex = Utils.string2Int(l, NO_INDEX_VALUE);
				}
			}
		}else if(dataArray != null && dataArray.size() > 0) {
			
			searchElement = new SearchedElement(script, dataArray);
	
		}else {
			if(parameters.length > 0) {
				final String firstParam = parameters[0].trim();
				if(!setParameterFilePathData(firstParam)) {
					final ArrayList<CalculatedValue> paramsValues = new ArrayList<CalculatedValue>();
					for(String param : parameters){

						param = param.replaceAll("\n", ",");

						final Matcher match = LOOP_REGEXP.matcher(param);
						if(match.find()){
							loop = Utils.string2Int(match.group(1), 1);
						}else {
							paramsValues.add(new CalculatedValue(script, param.trim()));
						}
					}
					this.setParameters(paramsValues);
				}
			}
		}

		if(returnValue.length > 0 && loop == 1){
			final ArrayList<Variable> variableValues = new ArrayList<Variable>();
			for (String varName : returnValue ){
				variableValues.add(script.getVariable(varName.trim(), true));
			}
			setVariables(variableValues);
		}

		if(options.size() > 0) {
			final String option = options.get(0);
			int operatorIndex = option.indexOf(ConditionalValue.EQUALS);
			if(operatorIndex > 1) {
				condition = new ConditionalValue(script, option.substring(0, operatorIndex).trim(), option.substring(operatorIndex+1).trim());
			}else {
				operatorIndex = option.indexOf(ConditionalValue.DIFFERENT);
				if(operatorIndex > 1) {
					condition = new ConditionalValue(script, option.substring(0, operatorIndex).trim(), option.substring(operatorIndex+2).trim(), ConditionalValue.DIFFERENT);
				}
			}
		}
	}

	public ActionCallscript(Script script, CalculatedValue name) {
		super(script);
		this.setName(name);
	}
	
	public ActionCallscript(Script script, CalculatedValue name, SearchedElement element) {
		this(script, name);
		this.setSearchElement(element);
	}

	public ActionCallscript(Script script, CalculatedValue name, CalculatedValue[] parameters) {
		this(script, name);
		this.setParameters(new ArrayList<CalculatedValue>(Arrays.asList(parameters)));
	}

	public ActionCallscript(Script script, CalculatedValue name, Variable ... variables) {
		this(script, name);
		this.setVariables(new ArrayList<Variable>(Arrays.asList(variables)));
	}

	public ActionCallscript(Script script, CalculatedValue name, CalculatedValue[] parameters, Variable ... variables) {
		this(script, name);
		this.setParameters(new ArrayList<CalculatedValue>(Arrays.asList(parameters)));
		this.setVariables(new ArrayList<Variable>(Arrays.asList(variables)));
	}

	public ActionCallscript(Script script, CalculatedValue name, CalculatedValue csvFilePath, int index) {
		this(script, name);
		this.setParameterFilePath(csvFilePath);
		this.setParameterFileIndex(index);
	}

	public ActionCallscript(Script script, CalculatedValue name, CalculatedValue[] parameters, int loop) {
		this(script, name, parameters);
		this.setLoop(loop);
	}

	public ActionCallscript(Script script, CalculatedValue name, int loop) {
		this(script, name);
		this.setLoop(loop);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Logs
	//---------------------------------------------------------------------------------------------------------------------------------

	public static String getScriptLog(String testName, int line, JsonObject log) {
		final StringBuilder sb = new StringBuilder("Subscript init (")
				.append(testName).append(":").append(line).append(") -> ").append(log.toString());
		return sb.toString();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	public boolean isSubscriptCalled(String scriptName) {
		return name.getCalculated().equals(scriptName);
	}

	private boolean setParameterFilePathData(String value) {
		if(value != null) {
			if(value.startsWith(ASSETS_PROTOCOLE) || value.startsWith(FILE_PROTOCOLE) || value.startsWith(HTTP_PROTOCOLE) || value.startsWith(HTTPS_PROTOCOLE)) {
				setParameterFilePath(new CalculatedValue(script, value));
				return true;
			}
		}
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {

		final StringBuilder codeBuilder = super.getJavaCode();

		codeBuilder.append(name.getJavaCode());

		if(parameterFilePath != null) {
			codeBuilder.append(", ")
			.append(parameterFilePath.getJavaCode())
			.append(", ")
			.append(parameterFileIndex);
		}else if(searchElement != null) {
			codeBuilder.append(", ")
			.append(searchElement.getJavaCode());
		}else {
			if(parameters != null){
				parameters.getJavaCode(codeBuilder);
			}

			if(loop > 1) {
				codeBuilder.append(", ")
				.append(loop);
			}else if(getVariables() != null){
				final StringJoiner joiner = new StringJoiner(", ");
				for (Variable variable : getVariables()){
					joiner.add(variable.getName());
				}
				codeBuilder.append(", ")
				.append(joiner.toString());
			}
		}

		codeBuilder.append(")");

		if(condition != null) {
			return condition.getJavaCode(codeBuilder, getLine());
		}

		return codeBuilder;
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		final ArrayList<String> keywords = super.getKeywords();
		keywords.add(name.getData());
		if(parameterFilePath != null) {
			keywords.add(parameterFilePath.getKeywords());
		}
		if(searchElement != null) {
			keywords.addAll(searchElement.getKeywords());
		}
		return keywords;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	private ActionTestScript getNewAtsInstance(Class<ActionTestScript> clazz, ActionTestScript topScript, String scriptName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		final ActionTestScript ats = clazz.getDeclaredConstructor().newInstance();
		ats.setTopScript(topScript, scriptName);
		return ats;
	}
	
	@Override
	public boolean execute(ActionTestScript ts, String testName, int line) {

		final Channel currentChannel = ts.getCurrentChannel();
		setStatus(currentChannel.newActionStatus(testName, line));

		final String scriptName = name.getCalculated();
		final Class<ActionTestScript> clazz = currentChannel.loadTestScriptClass(scriptName);

		if(clazz == null) {
			status.setError(MessageCode.SCRIPT_NOT_FOUND, "ATS script not found : '" + scriptName + "' (maybe a letter case issue ?)\n");
		}else {

			final ActionTestScript topScript = ts.getTopScript();
			
			try {

				if(parameterFilePath != null) {

					final String csvPath = parameterFilePath.getCalculated();
					URL csvUrl = null;

					if(csvPath.startsWith(ASSETS_PROTOCOLE)) {

						final String csvFilePath = csvPath.replace(ASSETS_PROTOCOLE, Project.ASSETS_FOLDER + File.separator);
						csvUrl = getClass().getClassLoader().getResource(csvFilePath);
						if(csvUrl == null) {
							csvUrl = getClass().getClassLoader().getResource(csvFilePath + ".csv");
						}

					}else {
						try {
							csvUrl = new URL(csvPath);
						} catch (MalformedURLException e) {}
					}

					if(csvUrl == null) {
						status.setError(ActionStatus.FILE_NOT_FOUND, "CSV file not found : " + csvPath);
						return true;
					}

					final ParameterDataFile data = Utils.loadData(csvUrl);

					if(data.noError() && data.getSize() > 0) {

						String csvAbsoluteFilePath = null;
						try {
							if(csvPath.startsWith(HTTP_PROTOCOLE) || csvPath.startsWith(HTTPS_PROTOCOLE)) {
								csvAbsoluteFilePath = csvUrl.toString();
							}else {
								csvAbsoluteFilePath = new File(csvUrl.toURI()).getAbsolutePath();
							}
						} catch (URISyntaxException e) {}

						final Method testMain = clazz.getDeclaredMethod(ActionTestScript.MAIN_TEST_FUNCTION, new Class[]{});
						final int iterationMax = data.getSize();
						int iteration = 0;
						
						if(parameterFileIndex > NO_INDEX_VALUE ) {

							int selectedIndex = parameterFileIndex;
							if(selectedIndex == RND_INDEX_VALUE) {
								selectedIndex = (int)(Math.random() * iterationMax);
							}

							final ParameterList row = data.getData(selectedIndex);
							if(row != null) {
								callScriptWithParametersFile(testMain, getNewAtsInstance(clazz, topScript, scriptName), ts, testName, line, row, 0, 1, scriptName, csvAbsoluteFilePath);
							}
							
						}else {
							for (ParameterList row : data.getData()) {
								callScriptWithParametersFile(testMain, getNewAtsInstance(clazz, topScript, scriptName), ts, testName, line, row, iteration, iterationMax, scriptName, csvAbsoluteFilePath);
								iteration++;
							}
						}

					}else {
						status.setError(ActionStatus.JAVA_EXCEPTION, "Data load file error : " + csvPath + " -> " + data.getError());
					}

				}else {

					final ActionTestScript ats = getNewAtsInstance(clazz, topScript, scriptName);
					final Method testMain = clazz.getDeclaredMethod(ActionTestScript.MAIN_TEST_FUNCTION, new Class[]{});
					
					if(searchElement != null) {
						
						final List<ParameterList> data = getElementTextData(ts.getCurrentChannel(), searchElement);
						final int iterationMax = data.size();
						
						for (int iteration=0; iteration<iterationMax; iteration++) {
							ats.initCalledScript(ts, testName, line, data.get(iteration), getVariables(), iteration, iterationMax, scriptName, SCRIPT_LOOP, null);
							testMain.invoke(ats);
						}
						
					}else {
						for (int iteration=0; iteration<loop; iteration++) {
							ats.initCalledScript(ts, testName, line, parameters, getVariables(), iteration, loop, scriptName, SCRIPT_LOOP, null);
							testMain.invoke(ats);
						}
					}

					status.setData(ats.getReturnValues());
				}

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
				fail(e.getMessage());
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof AtsFailError) {
					final AtsFailError target = (AtsFailError) e.getTargetException();
					ts.getTopScript().addErrorStack(target.getInfo());
					ts.getTopScript().addErrorStack(testName + ":" + line);
					fail(target.getFullMessage());
				} else {
					ts.getTopScript().addErrorStack(testName + ":" + line);
					fail(e.getTargetException().getMessage());
				}
			}
		}

		condition = null;
		status.endDuration();

		return true;
	}
	
	public static List<ParameterList> getElementTextData(Channel channel, SearchedElement element) {

		int max = 10;

		TestElement foundElement = new TestElement(channel, 10, p -> p > 0, element);
		while(max > 0 && foundElement.getCount() == 0) {
			channel.sleep(500);
			foundElement = new TestElement(channel, 10, p -> p > 0, element);
			max--;
		}

		if(foundElement.getCount() > 0) {
			return foundElement.getTextData();
		}
		
		return Collections.emptyList();
	}

	private void callScriptWithParametersFile(
			Method testMain,
			ActionTestScript ats,
			ActionTestScript atsCaller,
			String testName,
			int line,
			ParameterList row,
			int iteration,
			int iterationMax,
			String scriptName,
			String csvAbsoluteFilePath)

			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		row.updateCalculated(atsCaller);

		ats.initCalledScript(atsCaller, testName, line, row, null, iteration, iterationMax, scriptName, "dataFile", csvAbsoluteFilePath);
		
		testMain.invoke(ats);
	}

	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("status", "terminated");
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getName() {
		return name;
	}

	public void setName(CalculatedValue name) {
		this.name = name;
	}

	public void setVariables(ArrayList<Variable> value) {
		if (value != null && value.size() > 0) {
			super.setVariables(value);
			this.parameterFilePath = null;
			this.loop = 1;
		}
	}

	public ParameterList getParameters() {
		return parameters;
	}

	public void setParameters(ParameterList value) {
		if(value != null && value.getParametersSize() > 0) {
			this.parameters = value;
			this.parameterFilePath = null;
		}
	}

	public void setParameters(ArrayList<CalculatedValue> calcs) {
		parameters = new ParameterList(0);
		int i = 0;
		for(CalculatedValue calc : calcs) {
			parameters.addParameter(new Parameter(i, calc));
			i++;
		}
	}

	public int getLoop() {
		return loop;
	}

	public void setLoop(int loop) {

		if(loop <= 0) {
			loop = 1;
		}

		if(loop > 1) {
			this.parameterFilePath = null;
			setVariables(null);
		}
		this.loop = loop;
	}
	
	public SearchedElement getSearchElement() {
		return searchElement;
	}

	public void setSearchElement(SearchedElement value) {
		this.searchElement = value;
	}

	public int getParameterFileIndex() {
		return parameterFileIndex;
	}

	public void setParameterFileIndex(int parameterFileIndex) {
		this.parameterFileIndex = parameterFileIndex;
	}

	public CalculatedValue getParameterFilePath() {
		return parameterFilePath;
	}

	public void setParameterFilePath(CalculatedValue value) {
		this.parameterFilePath = value;
		if(value != null) {
			this.parameters = null;
			setVariables(null);
			this.loop = 1;
		}
	}	

	public ConditionalValue getCondition() {
		return condition;
	}

	public void setCondition(ConditionalValue condition) {
		this.condition = condition;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public ArrayList<Variable> getScriptVariables() {
		return scriptVariables;
	}

	public void setScriptVariables(ArrayList<Variable> scriptVariable) {
		this.scriptVariables = scriptVariable;
	}
}