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

package com.ats.script;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.testng.annotations.Test;

import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.SystemValues;
import com.ats.generator.ATS;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.variables.ConditionalValue;
import com.ats.generator.variables.Variable;
import com.ats.script.actions.Action;
import com.ats.script.actions.neoload.ActionNeoload;
import com.ats.script.actions.performance.ActionPerformance;
import com.ats.script.actions.performance.octoperf.ActionOctoperfVirtualUser;
import com.ats.tools.Operators;
import com.ats.tools.Utils;

public class ScriptHeader {

	private Project project;

	private String path = "";
	private String projectPath = "";
	private String packageName = "";
	private String name = "";
	private String id = "";
	private String squashId = "";
	private String jiraId = "";

	private List<String> groups = null;
	private String description = "";
	private String author = "";
	private String prerequisite = "";
	private Date createdAt = new Date();

	public ScriptHeader(){} // needed for serialization

	public ScriptHeader(String id, String author, String description, String prerequisites, String groups){
		this.id = id;
		this.author = author;
		this.description = description;
		this.prerequisite = prerequisites;
		this.groups = Arrays.asList(groups.split(","));
	}

	public ScriptHeader(Project prj, File file){

		this.project = prj;

		this.setProjectPath(project.getFolderPath());
		this.setPath(file.getAbsolutePath());
		this.setName(Utils.removeExtension(file.getName()));

		try {
			final Path relative = project.getAtsSourceFolder().relativize(file.getParentFile().toPath());
			this.setPackageName(relative.toString().replace(File.separator, "."));
		}catch (IllegalArgumentException e) {}
	}

	public File getTestReportFolder(Path path) {
		return project.getReportFolder().toFile();
	}

	public File getReportFolder() {
		return project.getReportFolder().toFile();
	}

	public File getJavaDestinationFolder() {
		return project.getJavaDestinationFolder().toFile();
	}

	public File getJavaFile() {
		return project.getJavaFile(getPackagePath() + name + ".java");
	}

	public String getPackagePath() {
		String path = packageName.replace(".", File.separator);
		if(path.length() > 0) {
			path += File.separator;
		}
		return path;
	}

	public void parseGroups(String data) {
		groups = Arrays.stream(data.split(",")).map(String :: trim).filter(s -> (s.length() > 0)).collect(Collectors.toList());
	}

	public String getDataGroups() {
		if(groups != null){
			return String.join(",", groups);
		}
		return "";
	}

	public String getJoinedGroups() {
		if(groups != null){
			StringJoiner joiner = new StringJoiner(", ");
			for(int i = 0; i < groups.size(); i++){
				if(groups.get(i).length() > 0) {
					joiner.add("\"" + groups.get(i) + "\"");
				}
			}
			return joiner.toString();
		}
		return "";
	}

	private String getGroupCode() {
		String code = getJoinedGroups();
		if(code.length() > 0){
			return "(groups={" + code + "})";
		}
		return "";
	}

	public String getQualifiedName() {
		if(packageName.length() > 0) {
			return packageName + "." + name;
		}else {
			return name;
		}
	}

	private static final String javaCode = String.join(
			System.getProperty("line.separator")
			, ""
			, "//---------------------------------------------------------------------------------------"
			, "//\t    _  _____ ____     ____                           _             "
			, "//\t   / \\|_   _/ ___|   / ___| ___ _ __   ___ _ __ __ _| |_ ___  _ __ "
			, "//\t  / _ \\ | | \\___ \\  | |  _ / _ \\ '_ \\ / _ \\ '__/ _` | __/ _ \\| '__|"
			, "//\t / ___ \\| |  ___) | | |_| |  __/ | | |  __/ | | (_| | || (_) | |   "
			, "//\t/_/   \\_\\_| |____/   \\____|\\___|_| |_|\\___|_|  \\__,_|\\__\\___/|_|   "
			, "//"
			, "//---------------------------------------------------------------------------------------"
			, "//\t/!\\ Warning /!\\"
			, "//\tThis class has been automatically generated by ATS Script Generator (ver. #ATS_VERSION#)"
			, "//\tYou may loose modifications if you edit this file manually !"
			, "//---------------------------------------------------------------------------------------"
			, ""
			, "import " + Test.class.getName() + ";"
			, "import " + ScriptHeader.class.getPackageName() + ".*;"
			, "import " + Action.class.getPackageName() + ".*;"
			, "import " + ActionNeoload.class.getPackageName() + ".*;"
			, "import " + ActionPerformance.class.getPackageName() + ".*;"
			, "import " + ActionOctoperfVirtualUser.class.getPackageName() + ".*;"
			, "import " + ActionTestScript.class.getName() + ";"
			, "import " + Cartesian.class.getName() + ";"
			, "import " + Mouse.class.getName() + ";"
			, "import " + Variable.class.getName() + ";"
			, "import " + ConditionalValue.class.getName() + ";"
			, "import " + Operators.class.getName() + ";"
			, ""
			, "public class #CLASS_NAME# extends " + ActionTestScript.class.getSimpleName() + "{"
			, ""
			, "\t/**"
			, "\t* Test Name : <b>#TEST_NAME#</b>"
			, "\t* Generated at : <b>" + DateFormat.getDateTimeInstance().format(new Date()) + "</b>"
			, "\t*/"
			, ""
			, "\tpublic #CLASS_NAME#(){super();}"
			, "\tpublic #CLASS_NAME#(" + ActionTestScript.class.getName() + " sc){super(sc);}"
			, ""
			, "\t@Override"
			, "\tpublic final String " + ActionTestScript.JAVA_GAV_FUNCTION_NAME + "(){return \"#PROJECT_GAV#\";}"
			, ""
			, "\t@Override"
			, "\tprotected " + ScriptHeader.class.getSimpleName() + " getHeader(){"
			, "\t\treturn new ScriptHeader("
			, "\t\t\t\"#SCRIPT_ID#\","
			, "\t\t\t\"#AUTHOR_NAME#\","
			, "\t\t\t\"#DESCRIPTION#\","
			, "\t\t\t\"#PREREQUISITES#\","
			, "\t\t\t\"#GROUP_DESCRIPTION#\");"
			, "\t}"
			, ""
			, "\t@Test#GROUP_DATA#"
			, "\tpublic void " + ActionTestScript.MAIN_TEST_FUNCTION + "(){"
			);

	public String getJavaCode(Project project) {

		String code = javaCode.replace("#CLASS_NAME#", name)
				.replace("#TEST_NAME#", getQualifiedName())
				.replace("#SCRIPT_ID#", StringEscapeUtils.escapeJava(id))
				.replace("#DESCRIPTION#", StringEscapeUtils.escapeJava(description))
				.replace("#PREREQUISITES#", StringEscapeUtils.escapeJava(prerequisite))
				.replace("#AUTHOR_NAME#", StringEscapeUtils.escapeJava(author))
				.replace("#GROUP_DATA#", getGroupCode())
				.replace("#ATS_VERSION#", ATS.VERSION)
				.replace("#GROUP_DESCRIPTION#", getDataGroups())
				.replace("#PROJECT_GAV#", StringEscapeUtils.escapeJava(project.getGav()));

		if(packageName.length() > 0) {
			code = "package " + packageName + ";\r\n" + code;
		}

		return code;
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> value) {
		this.groups = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String value) {
		this.description = value;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String value) {
		this.author = value;
	}
	public String getPrerequisite() {
		return prerequisite;
	}
	public void setPrerequisite(String value) {
		this.prerequisite = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String value) {
		this.id = value;
	}

	public String getJiraId() {
		return jiraId;
	}

	public void setJiraId(String value) {
		this.jiraId = value;
	}

	public String getSquashId() {
		return squashId;
	}

	public void setSquashId(String value) {
		this.squashId = value;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String value) {
		this.packageName = value;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String value) {
		this.projectPath = value;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public void setSystemProperties(String value) {}
	
	public String getSystemProperties() {
		return String.join(",", SystemValues.VALUES_NAME);
	}
}