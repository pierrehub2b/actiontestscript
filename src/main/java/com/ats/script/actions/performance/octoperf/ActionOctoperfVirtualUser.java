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

package com.ats.script.actions.performance.octoperf;

import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.actions.Action;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

public class ActionOctoperfVirtualUser extends Action {

	public static final String SCRIPT_OCTOPERF_LABEL = "octoperf-vu";
	private static final String APPEND_OPTION = "append";

	private CalculatedValue user;
	private CalculatedValue comment;
	private String tags;
	private boolean append = false;
	
	public ActionOctoperfVirtualUser() {}
	
	public ActionOctoperfVirtualUser(Script script, ArrayList<String> options, ArrayList<String> dataArray) {
		super(script);
		
		String usr = "vuser";
		String cmt = "";
		String tgs = "";
		
		if(dataArray.size() > 0) {
			usr = dataArray.remove(0).trim();
			if(dataArray.size() > 0) {
				cmt = dataArray.remove(0).trim();
			}
			if(dataArray.size() > 0) {
				tgs = dataArray.remove(0).trim();
			}
		}
		
		setAppend(options.contains(APPEND_OPTION));
		setUser(new CalculatedValue(script, usr));
		setComment(new CalculatedValue(script, cmt));
		setTags(tgs);
	}
	
	public ActionOctoperfVirtualUser(Script script, boolean append, CalculatedValue user, CalculatedValue comment, String tags) {
		super(script);
		setAppend(append);
		setUser(user);
		setComment(comment);
		setTags(tags);
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append(append).append(", ").append(user.getJavaCode()).append(", ").append(comment.getJavaCode()).append(", \"").append(StringEscapeUtils.escapeJava(tags)).append("\")");
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		final Channel channel = ts.getCurrentChannel();
		setStatus(channel.newActionStatus(testName, testLine));
		channel.sendToOctoperfServer(this);
		status.endDuration();
		return true;
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

	public CalculatedValue getComment() {
		return comment;
	}

	public void setComment(CalculatedValue comment) {
		this.comment = comment;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String value) {
		this.tags = value;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
}