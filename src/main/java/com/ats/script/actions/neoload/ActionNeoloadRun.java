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

package com.ats.script.actions.neoload;

import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;

public class ActionNeoloadRun extends ActionNeoload {

	private CalculatedValue user;

	private String options;

	public ActionNeoloadRun() {}

	public ActionNeoloadRun(Script script, String options, String user) {
		this(script, options, getCalculatedValue(script, user));
	}

	public ActionNeoloadRun(Script script, String options, CalculatedValue user) {
		super(script);
		setUser(user);
		setOptions(options);
	}

	private static CalculatedValue getCalculatedValue(Script script, String user) {
		if(user != null && user.length() > 0) {
			return new CalculatedValue(script, user);
		}
		return null;
	}

	public void setOptions(String value) {
		this.options = value;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {

		StringBuilder codeBuilder = new StringBuilder(super.getJavaCode());
		codeBuilder.append("\"");
		codeBuilder.append(options);
		codeBuilder.append("\", ");

		if(user != null) {
			codeBuilder.append(user.getJavaCode());
		}else {
			codeBuilder.append("null");
		}

		return codeBuilder.toString();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public CalculatedValue getUser() {
		return user;
	}

	public void setUser(CalculatedValue user) {
		this.user = user;
	}
}
