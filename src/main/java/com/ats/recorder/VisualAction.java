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

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;

import com.ats.executor.TestBound;
import com.ats.executor.channels.Channel;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionComment;

public class VisualAction {
	
	private int index;
	private Long timeLine;

	private int error;

	private ArrayList<byte[]> images;

	private String imageType;

	private String channelName;
	private TestBound channelBound;

	private String type;
	private int line;
	private String value = "";
	private String data = "";

	private VisualElement element;

	public VisualAction() {}

	public VisualAction(Channel channel, Action action) {

		if(!(action instanceof ActionComment)) {
			this.images = new ArrayList<byte[]>();
			this.images.add(channel.getScreenShot());		
		}

		this.type = action.getClass().getSimpleName();
		this.line = action.getLine();
		this.timeLine = System.currentTimeMillis();
	}

	public void addImageFrame(byte[] newScreen) {
		if(!Arrays.equals(images.get(images.size()-1), newScreen)){
			images.add(newScreen);
		}
	}

	public String getImageFileName() {
		return index + "." + imageType;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public TestBound getChannelBound() {
		return channelBound;
	}

	public void setChannelBound(TestBound channelBound) {
		this.channelBound = channelBound;
	}

	public String getData() {
		return data;
	}

	public void setData(String value) {
		if(value != null) {
			this.data = value;
		}
	}

	public VisualElement getElement() {
		return element;
	}

	public void setElement(VisualElement element) {
		this.element = element;
	}

	@Transient
	public ArrayList<byte[]> getImages() {
		return images;
	}

	public void setImages(ArrayList<byte[]> list) {
		this.images = list;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public Long getTimeLine() {
		return timeLine;
	}

	public void setTimeLine(Long timeLine) {
		this.timeLine = timeLine;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if(value != null) {
			this.value = value;
		}
	}

	public void addImage(ArrayList<VisualImage> imagesList) {
		if(images.size() > 0 && element != null && element.getBound() != null) {
			imagesList.add(new VisualImage(getImageFileName(), images.get(images.size()-1), element.getBound()));
		}
	}
}