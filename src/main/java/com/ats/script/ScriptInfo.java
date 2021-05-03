package com.ats.script;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.ats.generator.parsers.Lexer;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionCallscript;

public class ScriptInfo {
	
	private int actions = 0;
	private int callscripts = 0;
	private String author;
	private String description ;
	private String id;
	private List<String> groups;
	private String prerequisite;
	private Date createdAt;
	private Date modifiedAt;
	private String name;
	private String packageName;
	private String nativePath;
	private long size;
	
	public ScriptInfo(ScriptHeader header, File f) {
		author = header.getAuthor();
		description = header.getDescription();
		id = header.getId();
		groups = header.getGroups();
		prerequisite = header.getPrerequisite();
		createdAt = header.getCreatedAt();
		packageName = header.getPackageName();
		name = header.getName();
		
		size = f.length();
		modifiedAt = new Date(f.lastModified());
		nativePath = f.getAbsolutePath();
	}
	
	public ScriptInfo(ScriptHeader header) {
		this(header, new File(header.getPath()));
	}
	
	public ScriptInfo(Lexer lexer, File f) {
		this(lexer.loadScript(f), f);
	}
	
	public ScriptInfo(ScriptLoader sc, File f) {
		this(sc.getHeader(), f);
		Arrays.asList(sc.getActions()).parallelStream().forEach(this::count);
	}

	private void count(Action a) {
		actions++;
		if(a instanceof ActionCallscript) {
			callscripts++;
		}
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------
	
	public int getActions() {
		return actions;
	}

	public void setActions(int actions) {
		this.actions = actions;
	}

	public int getCallscripts() {
		return callscripts;
	}

	public void setCallscripts(int callscripts) {
		this.callscripts = callscripts;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public String getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(String prerequisite) {
		this.prerequisite = prerequisite;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getNativePath() {
		return nativePath;
	}

	public void setNativePath(String nativePath) {
		this.nativePath = nativePath;
	}
}