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
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;

import com.ats.executor.ActionTestScript;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.variables.Variable;
import com.ats.script.actions.Action;
import com.ats.tools.Operators;

public class ScriptHeader {

	private ProjectData projectData;

	private String path = "";
	private String projectPath = "";
	private String packageName = "";
	private String name = "";

	private List<String> groups = null;
	private String description = "";
	private String author = "";
	private String prerequisite = "";
	private Date createdAt = new Date();

	private String atsVersion = "N/A";

	public ScriptHeader(){} // needed for serialization

	public ScriptHeader(ProjectData projectData, File file){

		this.projectData = projectData;
		this.atsVersion = loadAtsVersion();

		this.setProjectPath(projectData.getFolderPath());
		this.setPath(file.getAbsolutePath());
		this.setName(FilenameUtils.removeExtension(file.getName()));
		this.setPackageName(file.getParent().substring(projectData.getAtsSourceFolder().toFile().getAbsolutePath().length()).replace(File.separator, "."));
	}
	
	private String loadAtsVersion() {
		InputStream resourceAsStream = this.getClass().getResourceAsStream("/version.properties");
		Properties prop = new Properties();
		try{
			prop.load( resourceAsStream );
			return prop.getProperty("version");
		}catch(Exception e) {}
		
		return "";
	}

	public File getReportFolder() {
		return projectData.getReportFolder().toFile();
	}

	public File getJavaDestinationFolder() {
		return projectData.getJavaDestinationFolder().toFile();
	}

	public File getJavaFile() {
		return projectData.getJavaFile(getPackagePath() + name + ".java");
	}

	public String getPackagePath() {
		String path = packageName.replace(".", File.separator);
		if(path.length() > 0) {
			path += File.separator;
		}
		return path;
	}

	public void parseGroups(String data) {
		groups = new ArrayList<String>();
		String[] list = data.split(",");
		for(String grp : list) {
			groups.add(grp.trim());
		}
	}

	private String groupCode() {
		StringBuilder code = new StringBuilder("");

		if(groups != null){
			StringJoiner joiner = new StringJoiner(", ");
			for(int i = 0; i < groups.size(); i++){
				if(groups.get(i).length() > 0) {
					joiner.add("\"" + groups.get(i) + "\"");
				}
			}
			if(joiner.length() > 0) {
				code.append("(groups={" + joiner.toString() + "})");
			}
		}

		return code.toString();
	}

	public String getQualifiedName() {
		if(packageName.length() > 0) {
			return packageName + "." + name;
		}else {
			return name;
		}
	}

	public void setAtsVersion(String version) {
		this.atsVersion = version;
	}

	//private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private static final String javaCode = String.join(
			System.getProperty("line.separator")
			, ""
			, "import " + Test.class.getName() + ";"
			, "import " + Keys.class.getName() + ";"
			, ""
			, "import " + ActionTestScript.class.getName() + ";"
			, "import " + Action.class.getPackageName() + ".*;"
			, ""
			, "import " + Cartesian.class.getName() + ";"
			, "import " + Mouse.class.getName() + ";"
			, "import " + Operators.class.getName() + ";"
			, "import " + Variable.class.getName() + ";"
			, ""
			, "public class #CLASS_NAME# extends " + ActionTestScript.class.getSimpleName() + "{"
			, ""
			, "\t//------------------------------------------------------------------------"
			, "\t//     _  _____ ____     ____                           _             "
			, "\t//    / \\|_   _/ ___|   / ___| ___ _ __   ___ _ __ __ _| |_ ___  _ __ "
			, "\t//   / _ \\ | | \\___ \\  | |  _ / _ \\ '_ \\ / _ \\ '__/ _` | __/ _ \\| '__|"
			, "\t//  / ___ \\| |  ___) | | |_| |  __/ | | |  __/ | | (_| | || (_) | |   "
			, "\t// /_/   \\_\\_| |____/   \\____|\\___|_| |_|\\___|_|  \\__,_|\\__\\___/|_|   "
			, "\t//"
			, "\t//------------------------------------------------------------------------"
			, "\t//	!! Warning !!"
			, "\t//	Class automatically generated by ATS Script Generator (ver. #ATS_VERSION#)"
			, "\t//	You may loose modifications if you edit this file manually !"
			, "\t//------------------------------------------------------------------------"
			, ""
			, "\t/**"
			, "\t* Test Name : <b>#CLASS_NAME#</b>"
			, "\t* Test Author : <b>#AUTHOR_NAME#</b>"
			, "\t* Test Description : <i>#DESCRIPTION#</i>"
			, "\t* Test Prerequisites : <i>#PREREQUISITES#</i>"
			, "\t* Generated at : <b>" + DateFormat.getDateTimeInstance().format(new Date()) + "</b>"
			, "\t*/"
			, ""
			, "\t@Test#GROUP_DATA#"
			, "\tpublic void " + ActionTestScript.MAIN_TEST_FUNCTION + "(){"
			);


	public String getJavaCode() {

		String code = javaCode.replaceAll("#CLASS_NAME#", name);
		code = code.replace("#DESCRIPTION#", description);
		code = code.replace("#PREREQUISITES#", prerequisite);
		code = code.replace("#AUTHOR_NAME#", author);
		code = code.replace("#GROUP_DATA#", groupCode());
		code = code.replace("#ATS_VERSION#", atsVersion);

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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String value) {
		if(value != null && value.startsWith(".")) {
			value = value.substring(1);
		}
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
}