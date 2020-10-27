package com.ats.element;

import java.util.UUID;

public class StructDebugDescription {
	
	protected Integer level;
	protected UUID uuid;
	protected String content;
	
	public StructDebugDescription(Integer level, String content) {
		this.uuid = UUID.randomUUID();
		this.level = level;
		this.content = content;
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
