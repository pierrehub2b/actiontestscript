package com.ats.recorder;

import com.ats.executor.TestBound;

public class VisualImage {

	private String name;
	private byte[] data;
	private TestBound bound;

	public VisualImage(String name, byte[] data) {
		setName(name);
		setData(data);
	}
	
	public VisualImage(String name, byte[] data, TestBound bound) {
		this(name,  data);
		setBound(bound);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
		
	public TestBound getBound() {
		return bound;
	}

	public void setBound(TestBound bound) {
		this.bound = bound;
	}
}