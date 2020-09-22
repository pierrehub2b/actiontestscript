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

package com.ats.generator.variables.parameter;

import com.ats.executor.ActionTestScript;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ParameterList {

	private int iteration;
	private List<Parameter> list;
	
	public ParameterList() {}
	
	public ParameterList(int iteration) {
		this.iteration = iteration;
		this.list = new ArrayList<Parameter>();
	}
	
	public ParameterList(int iteration, List<Parameter> list) {
		this.iteration = iteration;
		this.list = list;
	}
	
	public void updateCalculated(ActionTestScript ts) {
		list.forEach(p -> p.updateCalculated(ts));
	}

	public void getJavaCode(StringBuilder codeBuilder) {
		final StringJoiner joiner = new StringJoiner(", ");
		
		for (Parameter param : list){
			joiner.add(param.getJavaCode());
		}
		codeBuilder.append(", ")
		.append(ActionTestScript.JAVA_PARAM_FUNCTION_NAME)
		.append("(")
		.append(joiner.toString())
		.append(")");
	}
	
	public void addParameter(Parameter param) {
		list.add(param);
	}
	
	public String getParameterValue(String name, String defaultValue) {
		for(Parameter item : list) {
			if(name.equals(item.getName())){
				return item.getCalculated();
			}
		}
		return defaultValue;
	}
	
	public String getParameterValue(int index, String defaultValue) {
		if(list.size() > index) {
			return list.get(index).getCalculated();
		}
		return defaultValue;
	}
	
	public String[] getParameters() {
		final String[] result = new String[list.size()];
		int loop = 0;
		for(Parameter item : list) {
			result[loop] = item.getValue().getCalculated();
		}
		return result;
	}
	
	public int getParametersSize() {
		if(list != null) {
			return list.size();
		}
		return 0;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public List<Parameter> getList() {
		return list;
	}

	public void setList(List<Parameter> list) {
		this.list = list;
	}
}