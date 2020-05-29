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

package com.ats.script;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ats.crypto.Passwords;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.RandomStringValue;
import com.ats.generator.variables.ScriptValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.parameter.ParameterList;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.generator.variables.transform.Transformer;
import com.ats.script.actions.Action;
import com.ats.tools.logger.ExecutionLogger;
import com.ats.tools.logger.levels.AtsLogger;

public class Script {

	public static final Pattern OBJECT_PATTERN = Pattern.compile("(.*)\\[(.*)\\]", Pattern.CASE_INSENSITIVE);
	public final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	public final static String ATS_EXTENSION = "ats";
	public final static String ATS_FILE_EXTENSION = "." + ATS_EXTENSION;
	public final static String ATS_VISUAL_EXTENSION = "atsv";
	public final static String ATS_VISUAL_FILE_EXTENSION = "." + ATS_VISUAL_EXTENSION;

	public final static String ATS_VISUAL_FOLDER = "visual";

	public final static String SCRIPT_LOG = "SCRIPT";
	public final static String COMMENT_LOG = "COMMENT";

	private ParameterList parameterList;
	private List<Variable> variables = new ArrayList<Variable>();
	private ArrayList<CalculatedValue> returns;

	protected File csvFile;
	protected int iteration = 0;

	private Map<String, String> testExecutionVariables;
	private ExecutionLogger logger = new ExecutionLogger();
	protected Passwords passwords;

	public Script() {}

	public Script(ExecutionLogger logger) {
		if(logger != null) {
			setLogger(logger);
		}
	}

	public String getPassword(String name) {
		return passwords.getPassword(name);
	}

	//-------------------------------------------------------------------------------------

	public void sendLog(int code, String message) {
		logger.sendLog(code, message, "");
	}

	public void sendLog(int code, String message, String value) {
		logger.sendLog(code, message, value);
	}

	public void sendLog(int code, String message, Object value) {
		logger.sendLog(code, message, value);
	}

	public void sendInfoLog(String message, String value) {
		logger.sendInfo(message, value);
	}

	public void sendActionLog(Action action, String testName, int line) {
		logger.sendAction(action, testName, line); 
	}

	public void sendWarningLog(String message, String value) {
		logger.sendWarning(message, value); 
	}

	public void sendErrorLog(String message, String value) {
		logger.sendError(message, value); 
	}

	public void sendCommentLog(String calculated) {
		logger.sendExecLog(COMMENT_LOG, calculated);
	}

	public void sendScriptInfo(String value) {
		logger.sendExecLog(SCRIPT_LOG, value); 
	}

	public void sendScriptFail(String value) {
		if(value != null) {
			logger.sendExecLog(AtsLogger.FAILED, value); 
		}
	}

	//---------------------------------------------------------------------------------------------------

	public void setLogger(ExecutionLogger logger) {
		this.logger = logger;
	}

	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}

	protected void setTestExecutionVariables(Map<String, String> params) {
		this.testExecutionVariables = params;
	}

	protected Map<String, String> getTestExecutionVariables() {
		return testExecutionVariables;
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> data) {
		this.variables = data;
	}

	public ParameterList getParameterList() {
		return parameterList;
	}

	public void setParameterList(ParameterList data) {
		this.parameterList = data;
	}

	public CalculatedValue[] getReturns() {
		if(returns != null) {
			return returns.toArray(new CalculatedValue[returns.size()]);
		}
		return null;
	}

	public void setReturns(CalculatedValue[] data) {
		this.returns = new ArrayList<CalculatedValue>(Arrays.asList(data));
	}

	//-------------------------------------------------------------------------------------------------
	// variables
	//-------------------------------------------------------------------------------------------------

	public boolean checkVariableExists(String name){
		for(Variable variable : getVariables()){
			if(variable.getName().equals(name)){
				return true;
			}
		}
		return false;
	}

	public Variable getVariable(String name, boolean noCalculation){

		Variable foundVar = getVariable(name);

		if(foundVar == null) {
			foundVar = createVariable(name, new CalculatedValue(this, ""), null);
		}

		if(noCalculation) {
			foundVar.getValue().setData("");
			foundVar.setCalculation(false);
		}

		return foundVar;
	}

	private Variable getVariable(String name) {
		Variable found = null;

		Optional<Variable> opt = (variables.stream().filter(p -> p.getName().equals(name))).findFirst();
		if(opt != null && opt.isPresent()){
			found = opt.get();
		}
		return found;
	}

	public Variable addVariable(String name, CalculatedValue value, Transformer transformer){
		Variable foundVar = getVariable(name);
		if(foundVar == null) {
			foundVar = createVariable(name, value, transformer);
		}else {
			foundVar.setValue(value);
			foundVar.setTransformation(transformer);
		}
		return foundVar;
	}

	public Variable createVariable(String name, CalculatedValue value, Transformer transformer){
		Variable newVar = new Variable(name, value, transformer);
		variables.add(newVar);
		return newVar;
	}

	public String getVariableValue(String variableName) {
		return getVariable(variableName, false).getValue().getCalculated();
	}

	//-------------------------------------------------------------------------------------------------
	// variable calculation
	//-------------------------------------------------------------------------------------------------

	public String getRandomStringValue(int len, String type) {

		String baseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		if(!RandomStringValue.UPP_KEY.equals(type)){
			if(RandomStringValue.NUM_KEY.equals(type)){
				baseChars = "0123456789";
			}else if(RandomStringValue.LOW_KEY.equals(type)){
				baseChars = baseChars.toLowerCase();
			}else if(type != null && type.length() > 0){
				baseChars = type;
			}else{
				baseChars += baseChars.toLowerCase();
			}
		}

		List<Character> temp = baseChars.chars()
				.mapToObj(i -> (char)i)
				.collect(Collectors.toList());

		Collections.shuffle(temp, new SecureRandom());
		return temp.stream()
				.map(Object::toString)
				.limit(len)
				.collect(Collectors.joining());
	}

	//-------------------------------------------------------------------------------------------------
	// parameters
	//-------------------------------------------------------------------------------------------------

	public String[] getParameters() {
		if(parameterList == null) {
			return new String[0];
		}
		return parameterList.getParameters();
	}

	public ScriptValue getParameter(String name) {
		return new ScriptValue(getParameterValue(name, ""));
	}

	public ScriptValue getParameter(int index) {
		return new ScriptValue(getParameterValue(index, ""));
	}

	public String getParameterValue(String name) {
		return getParameterValue(name, "");
	}

	public String getParameterValue(String name, String defaultValue) {

		if(parameterList == null) {
			return "";
		}

		try {
			int index = Integer.parseInt(name);
			return getParameterValue(index, defaultValue);
		}catch (NumberFormatException e) {}

		return parameterList.getParameterValue(name, defaultValue);
	}

	public String getParameterValue(int index) {
		return getParameterValue(index, "");
	}

	public String getParameterValue(int index, String defaultValue) {
		if(parameterList == null) {
			return defaultValue;
		}
		return parameterList.getParameterValue(index, defaultValue);
	}

	//-------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------

	public String getEnvironmentValue(String name, String defaultValue) {

		String value = null;

		value = System.getProperty(name);
		if(value != null) {
			return value;
		}

		if(testExecutionVariables != null) {
			value = testExecutionVariables.get(name);
			if(value != null) {
				return value;
			}
		}

		value = System.getenv(name);
		if(value != null) {
			return value;
		}

		return defaultValue;
	}

	public String getUuidValue() {
		return UUID.randomUUID().toString();
	}	

	public String getTodayValue() {
		return DateTransformer.getTodayValue();
	}	

	public String getNowValue() {
		return TimeTransformer.getNowValue();
	}

	public int getIteration() {
		return iteration;
	}

	public String getCsvFilePath() {
		if(csvFile == null) {
			return "";
		}
		return csvFile.getAbsolutePath();
	}

	public File getCsvFile() {
		return csvFile;
	}

	public File getAssetsFile(String relativePath) {
		if(!relativePath.startsWith("/")) {
			relativePath = "/" + relativePath;
		}
		relativePath = ProjectData.ASSETS_FOLDER + relativePath;

		final URL url = getClass().getClassLoader().getResource(relativePath);
		if(url != null) {
			try {
				return Paths.get(url.toURI()).toFile();
			} catch (URISyntaxException e) {}
		}
		return null;
	}

	public String getAssetsUrl(String relativePath) {
		final URL url = getClass().getClassLoader().getResource(relativePath);
		if(url != null) {
			return "file://" + url.getPath();
		}
		return "";
	}
}