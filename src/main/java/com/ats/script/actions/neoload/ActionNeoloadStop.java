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

import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;

import java.util.StringJoiner;

public class ActionNeoloadStop extends ActionNeoloadRun {

	public static final String SCRIPT_LABEL = SCRIPT_NEOLOAD_LABEL + "-stop";

	private static final String FRAMEWORK_PARAM = "framework";
	private static final String GENERIC_PARAM = "generic";

	private static final String SHARED_CONTAINER = "sharedcont";
	private static final String INCLUDE_VARIABLE = "includevar";
	private static final String DELETE_RECORD = "deleterec";
	
	private static final String API_SERVICE_NAME = "StopRecording";
	
	private int threshold = 60;
	private boolean updateSharedContainer = false;
	private boolean includeVariable = false;
	private boolean deleteRecording = false;
	private boolean searchFrameworkParameter = false;
	private boolean searchGenericParameter = false;

	public ActionNeoloadStop() {}

	public ActionNeoloadStop(ScriptLoader script, String options, String userData, String userOptions) {
		super(script, options, userData);
		checkOptions(options, userOptions);
	}

	public ActionNeoloadStop(Script script, String options, CalculatedValue user, String userOptions) {
		super(script, options, user);
		checkOptions(options, userOptions);
	}

	private void checkOptions(String options, String userOptions) {

		searchFrameworkParameter = options.contains(FRAMEWORK_PARAM);
		searchGenericParameter = options.contains(GENERIC_PARAM);

		for (String opt : userOptions.split(",")) {
			try {
				threshold = Integer.parseInt(opt);
			}catch (NumberFormatException e) {
				opt = opt.trim().toLowerCase();
				if(SHARED_CONTAINER.equals(opt)) {
					updateSharedContainer = true;
				}else if(INCLUDE_VARIABLE.equals(opt)) {
					includeVariable = true;
				}else if(DELETE_RECORD.equals(opt)) {
					deleteRecording = true;
				}
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {

		StringJoiner joiner = new StringJoiner(", ");
		if(searchFrameworkParameter) {
			joiner.add(FRAMEWORK_PARAM);
		}
		if(searchGenericParameter) {
			joiner.add(GENERIC_PARAM);
		}
		setOptions(joiner.toString());

		StringBuilder codeBuilder = super.getJavaCode();

		joiner = new StringJoiner(", ");
		if(threshold != 60) {
			joiner.add(threshold + "");
		}
		if(updateSharedContainer) {
			joiner.add(SHARED_CONTAINER);
		}
		if(deleteRecording) {
			joiner.add(DELETE_RECORD);
		}
		if(includeVariable) {
			joiner.add(INCLUDE_VARIABLE);
		}

		codeBuilder.append(", \"").append(joiner.toString()).append("\")");

		return codeBuilder;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void executeRequest(Channel channel, String designApiUrl) {
		super.executeRequest(channel, designApiUrl + API_SERVICE_NAME);
	    postDesignData(new StopUser(this));
	    channel.setStopNeoloadRecord(null);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	public boolean isUpdateSharedContainer() {
		return updateSharedContainer;
	}
	public void setUpdateSharedContainer(boolean updateSharedContainer) {
		this.updateSharedContainer = updateSharedContainer;
	}
	public boolean isIncludeVariable() {
		return includeVariable;
	}
	public void setIncludeVariable(boolean includeVariable) {
		this.includeVariable = includeVariable;
	}
	public boolean isDeleteRecording() {
		return deleteRecording;
	}
	public void setDeleteRecording(boolean deleteRecording) {
		this.deleteRecording = deleteRecording;
	}
	public boolean isSearchFrameworkParameter() {
		return searchFrameworkParameter;
	}
	public void setSearchFrameworkParameter(boolean searchFrameworkParameter) {
		this.searchFrameworkParameter = searchFrameworkParameter;
	}
	public boolean isSearchGenericParameter() {
		return searchGenericParameter;
	}
	public void setSearchGenericParameter(boolean searchGenericParameter) {
		this.searchGenericParameter = searchGenericParameter;
	}
	
	//--------------------------------------------------------
	// Json serialization
	//--------------------------------------------------------
	
	@SuppressWarnings("unused")
	private class StopUser{

		public boolean FrameworkParameterSearch = false;
		public boolean GenericParameterSearch = false;
		public String Name = null;
		public int MatchingThreshold = 60;
		public boolean UpdateSharedContainers = false;
		public boolean IncludeVariables  = false;
		public boolean DeleteRecording  = false;
		
		public StopUser(ActionNeoloadStop action) {
			
			this.FrameworkParameterSearch = action.isSearchFrameworkParameter();
			this.GenericParameterSearch = action.isSearchGenericParameter();
			
			if(action.getUser() != null) {
				this.Name = action.getUser().getCalculated();
				this.MatchingThreshold = action.getThreshold();
				this.UpdateSharedContainers = action.isUpdateSharedContainer();
				this.IncludeVariables = action.isIncludeVariable();
				this.DeleteRecording = action.isDeleteRecording();
			}
		}
	}
}
