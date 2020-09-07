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

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionPropertySet extends Action {
	
	public static final String SCRIPT_LABEL = "system-set";
	
	private String name;
	private String value;
	
	public ActionPropertySet() { }
	
	public ActionPropertySet(Script script, String name, String value) {
		super(script);
		setName(name);
		setValue(value);
	}
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder builder = super.getJavaCode();
		builder.append("\"" + name + "\"").append(", ").append("\"" + value + "\"").append(")");
		return builder;
	}
	
	@Override
	public void execute(ActionTestScript ts, String testName, int line) {
		super.execute(ts, testName, line);
		ts.getCurrentChannel().setSysProperty(getName(), getValue());
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
