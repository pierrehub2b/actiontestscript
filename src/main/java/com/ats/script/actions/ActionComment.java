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

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

public class ActionComment extends Action {

	public static final String SCRIPT_LABEL = "comment";

	private static final String STEP_TYPE = "step";
	private static final String LOG_TYPE = "log";
	private static final String SCRIPT_TYPE = "script";

	private CalculatedValue comment;
	private String type = STEP_TYPE;

	public ActionComment() {}

	public ActionComment(ScriptLoader script, String type, ArrayList<String> dataArray) {
		super(script);
		if(type.equals(LOG_TYPE) || type.equals(SCRIPT_TYPE)){
			setType(type);
		}
		
		String data = "";
		if(dataArray.size() > 0){
			data = dataArray.remove(0).trim();
		}
		
		setComment(new CalculatedValue(script, data));
	}

	public ActionComment(Script script, String type, CalculatedValue value) {
		super(script);
		setType(type);
		setComment(value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {
		if(SCRIPT_TYPE.equals(type)) {
			return null;
		}
		return super.getJavaCode() + "\"" + type + "\", " + comment.getJavaCode() + ")";
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts) {
		status = new ActionStatus(ts.getCurrentChannel());
		if(ts.getCurrentChannel() != null) {
			if(STEP_TYPE.equals(type)) {
				super.execute(ts);
				status.endDuration();
				ts.getRecorder().update(type, comment.getCalculated());
			}else if(LOG_TYPE.equals(type)) {
				status.endDuration();
				ts.getTopScript().sendInfo("Comment log -> ", comment.getCalculated());
			}
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public CalculatedValue getComment() {
		return comment;
	}

	public void setComment(CalculatedValue comment) {
		this.comment = comment;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		this.type = value;
	}
}