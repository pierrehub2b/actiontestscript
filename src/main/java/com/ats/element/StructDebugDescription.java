package com.ats.element;

import java.util.UUID;

public class StructDebugDescription {
	protected Integer level;
	protected UUID uuid;
	protected String content;
	
	public StructDebugDescription(Integer l, String c) {
		this.level = l;
		this.uuid = UUID.randomUUID();
		this.content = c;
	}
	
	public Integer getLevel() {
		return this.level;
	} 
	
	public UUID getUuid() {
		return this.uuid;
	} 
	
	public String getContent() {
		return this.content;
	} 
}
