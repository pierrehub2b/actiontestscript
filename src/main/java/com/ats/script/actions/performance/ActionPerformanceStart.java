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

package com.ats.script.actions.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.script.Script;
import com.ats.tools.Utils;

public class ActionPerformanceStart extends ActionPerformance {

	public static final String SCRIPT_LABEL = ActionPerformance.SCRIPT_PERFORMANCE_LABEL + "-start";
	
	public static final String SEND_LABEL = "send";
	public static final String RECEIVE_LABEL = "receive";
	
	private static final String[] LABEL_REPLACE = new String[]{SEND_LABEL, RECEIVE_LABEL, "=", " "};
	private static final String[] LABEL_REPLACEMENT = new String[]{"", "", "", ""};
	
	private long sendBandWidth = 0;
	private long receiveBandWidth = 0;
	private List<String> whiteList = null;
	
	public ActionPerformanceStart() {}

	public ActionPerformanceStart(Script script, ArrayList<String> options, ArrayList<String> dataArray) {
		super(script);
		setWhiteList(dataArray.stream().map(String :: trim).collect(Collectors.toList()));
		loadBandwidthValues(options);
	}
	
	public ActionPerformanceStart(Script script, long sbw, long rbw, String[] whiteList) {
		super(script);
		setSendBandWidth(sbw);
		setReceiveBandWidth(rbw);
		setWhiteList(Arrays.asList(whiteList));
	}
	
	private void loadBandwidthValues(ArrayList<String> options) {
		final Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			final String opt = itr.next().toLowerCase();
			if(opt.contains(SEND_LABEL)) {
				sendBandWidth = Utils.string2Long(StringUtils.replaceEach(opt, LABEL_REPLACE, LABEL_REPLACEMENT));
				itr.remove();
			}else if(opt.contains(RECEIVE_LABEL)) {
				receiveBandWidth = Utils.string2Long(StringUtils.replaceEach(opt, LABEL_REPLACE, LABEL_REPLACEMENT));
				itr.remove();
			}
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		final ArrayList<String> list = new ArrayList<String>();
		Arrays.asList(whiteList).forEach(s -> list.add("\"" + s + "\""));
		return super.getJavaCode().append(sendBandWidth).append("L, ").append(receiveBandWidth).append("L, new String[]{").append(String.join(", ", list)).append("})");
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Execution
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void execute(ActionTestScript ts, String testName, int testLine) {
		final Channel channel = ts.getCurrentChannel();
		setStatus(channel.newActionStatus(testName, testLine));
		channel.startHarServer(status, whiteList, sendBandWidth, receiveBandWidth);
		status.endDuration();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	
	
	public long getSendBandWidth() {
		return sendBandWidth;
	}

	public void setSendBandWidth(long value) {
		this.sendBandWidth = value;
	}

	public long getReceiveBandWidth() {
		return receiveBandWidth;
	}

	public void setReceiveBandWidth(long value) {
		this.receiveBandWidth = value;
	}
	
	public List<String> getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(List<String> data) {
		this.whiteList = data;
	}	
}