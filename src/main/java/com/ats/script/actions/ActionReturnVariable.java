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

import com.ats.element.SearchedElement;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;

public class ActionReturnVariable extends ActionExecuteElement {
	
	private Variable variable;
	
	public ActionReturnVariable() {}
	
	public ActionReturnVariable(Script script, boolean stop, ArrayList<String> options, ArrayList<String> objectArray, Variable variable) {
		super(script, stop, options, objectArray);
		setVariable(variable);
	}

	public ActionReturnVariable(Script script, boolean stop, int maxTry, SearchedElement element, Variable variable) {
		super(script, stop, maxTry, element);
		setVariable(variable);
	}
	
	protected void updateVariableValue(String value) {
		variable.setData(value);
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
}
