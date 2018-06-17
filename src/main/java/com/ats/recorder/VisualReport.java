package com.ats.recorder;

public class VisualReport extends VisualAction{
	
	private String id;
	private String name;
	private String description;
	private String author;
	private String prerequisite;
	private String executed;
	private String groups;
	private int quality;
	
	public VisualReport() {
		super();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getPrerequisite() {
		return prerequisite;
	}
	public void setPrerequisite(String prerequisite) {
		this.prerequisite = prerequisite;
	}
	public String getExecuted() {
		return executed;
	}
	public void setExecuted(String executed) {
		this.executed = executed;
	}
	public String getGroups() {
		return groups;
	}
	public void setGroups(String groups) {
		this.groups = groups;
	}
	public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
}
