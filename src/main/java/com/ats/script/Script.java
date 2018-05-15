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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.generator.variables.transform.Transformer;
import com.ats.tools.logger.ExecutionLogger;

public class Script {

	public final static String DEFAULT_CHARSET = "UTF-8";

	public final static String ATS_EXTENSION = "ats";
	public final static String ATS_FILE_EXTENSION = "." + ATS_EXTENSION;
	public final static String ATS_VISUAL_EXTENSION = "atsv";

	public final static String ATS_VISUAL_FOLDER = "visual";
	
	private ArrayList<String> parameters = new ArrayList<String>();
	private ArrayList<Variable> variables = new ArrayList<Variable>();
	private ArrayList<CalculatedValue> returns;
	
	private Map<String, String> testParameters;
	
	private File projectAtsFolder;
		
	private ExecutionLogger logger = new ExecutionLogger();

	public Script() {}
	
	public Script(ExecutionLogger logger) {
		if(logger != null) {
			setLogger(logger);
		}
	}

	public File getProjectAtsFolder() {
		return projectAtsFolder;
	}

	protected void setAtsFolder(File projectAtsFolder) {
		this.projectAtsFolder = projectAtsFolder;
	}
	
	public void setCurrentChannel(Channel channel) {
		logger.setChannel(channel);
	}
	
	public void sendLog(int code, String message) {
		logger.sendLog(code, message, "");
	}

	public void sendLog(int code, String message, String value) {
		logger.sendLog(code, message, value);
	}

	public void sendLog(int code, String message, Object value) {
		logger.sendLog(code, message, value);
	}
	
	public void sendInfo(String message, String value) {
		logger.sendInfo(message, value); 
	}
	
	public void setLogger(ExecutionLogger logger) {
		this.logger = logger;
	}
	
	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}
	
	protected void setTestParameters(Map<String, String> params) {
		this.testParameters = params;
	}
	
	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

	public Variable[] getVariables() {
		return variables.toArray(new Variable[variables.size()]);
	}

	public void setVariables(Variable[] data) {
		this.variables = new ArrayList<Variable>(Arrays.asList(data));
	}

	public String[] getParameters() {
		return parameters.toArray(new String[parameters.size()]);
	}

	public void setParameters(String[] data) {
		this.parameters = new ArrayList<String>(Arrays.asList(data));
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
	
	public String getParameterValue(int index, String defaultValue) {
		if(parameters.size() > index) {
			return parameters.get(index);
		}
		return defaultValue;
	}
	
	public String getEnvironmentValue(String name, String defaultValue) {
	
		String value = null;
		if(testParameters != null) {
			value = testParameters.get(name);
		}

		if(value == null) {
			value = System.getenv(name);
		}
		
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

	public void setReturnValues(String ... values) {

	}
}
