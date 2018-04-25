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

package com.ats.recorder;

import java.util.ArrayList;
import java.util.Arrays;

import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionComment;

public class VisualAction {

	private int index;
	private Long timeLine;

	private boolean passed;

	private ArrayList<byte[]> images;

	private String channelName;
	private TestBound channelDimension;

	private String type;
	private int line;
	private String scriptName;
	private String value = "";
	private String data = "";
	private MouseDirection position;

	private TestBound elementBound;
	private long totalSearchDuration;
	private int numElements = -1;

	private String criterias;

	public VisualAction(Channel channel, Action action) {

		if(!(action instanceof ActionComment)) {
			this.images = new ArrayList<byte[]>();
			this.images.add(channel.getScreenShot());		
		}

		this.channelName = channel.getName();
		this.channelDimension = channel.getDimension();

		this.type = action.getClass().getSimpleName();
		this.line = action.getLine();
		this.timeLine = System.currentTimeMillis();
	}

	public void addImageFrame(byte[] newScreen) {
		if(!Arrays.equals(images.get(images.size()-1), newScreen)){
			images.add(newScreen);
		}
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public Long getTimeLine() {
		return timeLine;
	}

	public void setTimeLine(Long timeLine) {
		this.timeLine = timeLine;
	}

	public ArrayList<byte[]> getImages() {
		return images;
	}

	public void setImages(ArrayList<byte[]> list) {
		this.images = list;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public TestBound getChannelDimension() {
		return channelDimension;
	}

	public void setChannelDimension(TestBound channelDimension) {
		this.channelDimension = channelDimension;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if(value != null) {
			this.value = value;
		}
	}

	public String getData() {
		return data;
	}

	public void setData(String value) {
		if(value != null) {
			this.data = value;
		}
	}

	public TestBound getElementBound() {
		return elementBound;
	}

	public void setElementBound(TestBound elementBound) {
		this.elementBound = elementBound;
	}

	public long getTotalSearchDuration() {
		return totalSearchDuration;
	}

	public void setTotalSearchDuration(long totalSearchDuration) {
		this.totalSearchDuration = totalSearchDuration;
	}

	public int getNumElements() {
		return numElements;
	}

	public void setNumElements(int numElements) {
		this.numElements = numElements;
	}

	public String getCriterias() {
		return criterias;
	}

	public void setCriterias(String value) {
		this.criterias = value;
	}

	public MouseDirection getPosition() {
		return position;
	}

	public void setPosition(MouseDirection position) {
		this.position = position;
	}
}