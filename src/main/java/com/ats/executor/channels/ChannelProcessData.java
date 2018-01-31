package com.ats.executor.channels;

import java.util.ArrayList;

public class ChannelProcessData {

	private ArrayList<String> windowHandle = new ArrayList<String>();
	private Long processId = -1L;
	
	public ChannelProcessData() {}
	
	public ChannelProcessData(Long pid) {
		this.setPid(pid);
	}

	public void setPid(Long value) {
		this.processId = value;
	}

	public void addWindowHandle(String value){
		windowHandle.add(value);
	}

	public void addWindowHandle(int value) {
		windowHandle.add("native@0x" + Integer.toHexString(value));
	}
	
	public Long getPid(){
		return processId;
	}
	
	public ArrayList<String> getWindowHandle(){
		return windowHandle;
	}
}