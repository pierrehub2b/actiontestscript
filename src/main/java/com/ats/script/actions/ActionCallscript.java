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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.ConditionalValue;
import com.ats.generator.variables.Variable;
import com.ats.script.ProjectData;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.AtsClassLoader;
import com.ats.tools.Utils;
import com.ats.tools.logger.MessageCode;

public class ActionCallscript extends Action {

	public static final String SCRIPT_LABEL = "subscript";

	private static final String SCRIPT_LOOP = "loop";
	public static final Pattern LOOP_REGEXP = Pattern.compile(SCRIPT_LOOP + " ?\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
	private static final String ASSETS_PROTOCOLE = "assets:///";
	private static final String FILE_PROTOCOLE = "file:///";
	private static final String HTTP_PROTOCOLE = "http://";
	private static final String HTTPS_PROTOCOLE = "https://";

	private final AtsClassLoader classLoader = new AtsClassLoader();

	private CalculatedValue name;

	private List<Variable> variables;
	private List<CalculatedValue> parameters;

	private int loop = 1;
	private CalculatedValue csvFilePath = null;

	private ConditionalValue condition;

	//---------------------------------------------------------------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------------------------------------------------------------

	public ActionCallscript() {}

	public ActionCallscript(ScriptLoader script, ArrayList<String> options, String name, String[] parameters, String[] returnValue, String csvFilePath) {

		super(script);
		this.setName(new CalculatedValue(script, name));

		if(!setCsvFilePathData(csvFilePath)) {
			if(parameters.length > 0) {
				final String firstParam = parameters[0].trim();
				if(!setCsvFilePathData(firstParam)) {
					final ArrayList<CalculatedValue> paramsValues = new ArrayList<CalculatedValue>();
					for(String param : parameters){

						param = param.replaceAll("\n", ",");

						final Matcher match = LOOP_REGEXP.matcher(param);
						if(match.find()){
							this.loop = Utils.string2Int(match.group(1), 1);
						}else {
							paramsValues.add(new CalculatedValue(script, param.trim()));
						}
					}
					this.setParameters(paramsValues);
				}
			}
		}

		if(returnValue.length > 0 && this.loop == 1){
			final ArrayList<Variable> variableValues = new ArrayList<Variable>();
			for (String varName : returnValue ){
				variableValues.add(script.getVariable(varName.trim(), true));
			}
			this.setVariables(variableValues);
		}

		if(options.size() > 0) {
			final String option = options.get(0);
			final int operatorIndex = option.indexOf("=");
			if(operatorIndex > 1) {
				condition = new ConditionalValue(script, option.substring(0, operatorIndex).trim(), option.substring(operatorIndex+1).trim());
			}
		}
	}

	public ActionCallscript(Script script, CalculatedValue name) {
		super(script);
		this.setName(name);
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

	public ActionCallscript(Script script, CalculatedValue name, CalculatedValue csvFilePath) {
		this(script, name);
		this.setCsvFilePath(csvFilePath);
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
	//---------------------------------------------------------------------------------------------------------------------------------

	private boolean setCsvFilePathData(String value) {
		if(value != null) {
			if(value.startsWith(ASSETS_PROTOCOLE) || value.startsWith(FILE_PROTOCOLE) || value.startsWith(HTTP_PROTOCOLE) || value.startsWith(HTTPS_PROTOCOLE)) {
				this.setCsvFilePath(new CalculatedValue(script, value));
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

		if(csvFilePath != null) {
			codeBuilder.append(", ")
			.append(csvFilePath.getJavaCode());
		}else {
			if(parameters != null){
				final StringJoiner joiner = new StringJoiner(", ");
				for (CalculatedValue value : parameters){
					joiner.add(value.getJavaCode());
				}
				codeBuilder.append(", ")
				.append(ActionTestScript.JAVA_PARAM_FUNCTION_NAME)
				.append("(")
				.append(joiner.toString())
				.append(")");
			}

			if(loop > 1) {
				codeBuilder.append(", ")
				.append(loop);
			}else if(variables != null){
				final StringJoiner joiner = new StringJoiner(", ");
				for (Variable variable : variables){
					joiner.add(variable.getName());
				}
				codeBuilder.append(", ")
				.append(joiner.toString());
			}
		}

		codeBuilder.append(")");

		if(condition != null) {
			return condition.getJavaCode(codeBuilder);
		}

		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {

		super.execute(ts.getCurrentChannel());
		final String scriptName = name.getCalculated();

		if(condition != null && !condition.isPassed()) {
			ts.getTopScript().sendConditionExecLog(scriptName, condition);
			status.endDuration();
			return;
		}

		//Class<ActionTestScript> clazz = (Class<ActionTestScript>) Class.forName(name.getCalculated()); // old way still working
		Class<ActionTestScript> clazz = classLoader.findClass(scriptName);

		if(clazz == null) {
			status.setError(MessageCode.SCRIPT_NOT_FOUND, "ATS script not found : '" + scriptName + "' (maybe a letter case issue ?)\n");
		}else {

			try {

				final ActionTestScript ats = clazz.getDeclaredConstructor().newInstance();
				final ActionTestScript topScript = ts.getTopScript();

				if(csvFilePath != null) {

					final String csvPath = csvFilePath.getCalculated();
					URL csvUrl = null;

					if(csvPath.startsWith(ASSETS_PROTOCOLE)) {

						final String csvFilePath = csvPath.replace(ASSETS_PROTOCOLE, ProjectData.ASSETS_FOLDER + File.separator);
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
						return;
					}

					File csvFile = null;
					try {
						csvFile = new File(csvUrl.toURI());
					} catch (URISyntaxException e) {}

					try {

						final Method testMain = clazz.getDeclaredMethod(ActionTestScript.MAIN_TEST_FUNCTION, new Class[]{});
						final List<String[]> data = Utils.loadCsvData(csvUrl);
						int iteration = 0;

						for (String[] params : data) {
							ats.initCalledScript(topScript, getCalculatedParameters(ats, params), null, iteration, scriptName, "csv " + Arrays.toString(params), csvFile);
							testMain.invoke(ats);
							iteration++;
						}

					} catch (IOException e) {
						status.setError(ActionStatus.FILE_NOT_FOUND, "CSV file IO error : " + csvPath + " -> " + e.getMessage());
					}

				}else {

					final Method testMain = clazz.getDeclaredMethod(ActionTestScript.MAIN_TEST_FUNCTION, new Class[]{});
					final String[] parameters = getCalculatedParameters();

					for (int iteration=0; iteration<loop; iteration++) {
						ats.initCalledScript(topScript, parameters, variables, iteration, scriptName, "loop " + iteration + " on " + loop, null);
						testMain.invoke(ats);
					}

					status.setData(ats.getReturnValues());
				}

			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (InvocationTargetException e) {
				if(e.getTargetException() instanceof AssertionError) {
					fail(e.getCause().getMessage());
				}
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			}
		}

		status.endDuration();
	}

	private String[] getCalculatedParameters() {
		if(parameters != null) {
			int index = 0;
			final String[] calculatedParameters = new String[parameters.size()];
			for(CalculatedValue calc : parameters) {
				calculatedParameters[index] = calc.getCalculated();
				index++;
			}
			return calculatedParameters;
		}
		return null;
	}

	private String[] getCalculatedParameters(ActionTestScript ats, String[] params) {
		int index = 0;
		final String[] calculatedParameters = new String[params.length];
		for(String data : params) {
			final CalculatedValue calc = new CalculatedValue(ats, data);
			calculatedParameters[index] = calc.getCalculated(); 
			index++;
		}
		return calculatedParameters;
	}

	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, StringBuilder data) {
		if(csvFilePath != null) {
			data.append("\"csv\":\"").append(csvFilePath.getCalculated()).append("\"");
		}else if(parameters != null) {
			data.append("\"parameters\":").append(parameters.size());
		}
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

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> value) {
		this.variables = value;
		if(value != null) {
			this.csvFilePath = null;
			this.loop = 1;
		}
	}

	public List<CalculatedValue> getParameters() {
		return parameters;
	}

	public void setParameters(List<CalculatedValue> value) {
		this.parameters = value;
		if(value != null) {
			this.csvFilePath = null;
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
			this.csvFilePath = null;
			this.variables = null;
		}
		this.loop = loop;
	}

	public CalculatedValue getCsvFilePath() {
		return csvFilePath;
	}

	public void setCsvFilePath(CalculatedValue value) {
		this.csvFilePath = value;
		if(value != null) {
			this.parameters = null;
			this.variables = null;
			this.loop = 1;
		}
	}	

	public ConditionalValue getCondition() {
		return condition;
	}

	public void setCondition(ConditionalValue condition) {
		this.condition = condition;
	}
}