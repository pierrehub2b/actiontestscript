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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ats.executor.ActionTestScript;
import com.ats.generator.parsers.ScriptParser;
import com.ats.tools.Utils;

public class ProjectData {

	private static final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

	public static final String TARGET_FOLDER = "target";
	public static final String LOGS_FOLDER = "logs";
	private static final String TARGET_FOLDER_REPORT = "report";

	public static final String TARGET_FOLDER_GENERATED = "generated";
	public static final String TARGET_FOLDER_CLASSES = "classes";

	public static final String SRC_FOLDER = "src";
	public static final String ASSETS_FOLDER = "assets";
	public static final String CERTS_FOLDER = "certs";
	public static final String RESOURCES_FOLDER = "resources";
	public static final String IMAGES_FOLDER = "images";

	private static final String SRC_FOLDER_MAIN = "main";
	private static final String SRC_FOLDER_ATS = "ats";
	private static final String SRC_FOLDER_JAVA = "java";

	private String name = "";
	private String domain = "";
	private String description = "";
	private String version = "";
	private String folderPath = "";

	private File folder;

	private Path javaDestinationFolderPath;
	private Path reportDestinationFolderPath;

	public static ProjectData getProjectData(File sourceFolder, File destinationFolder, File reportFolder) {
		final File xmlDataFile = checkAtsProjectFolder(sourceFolder);
		if(xmlDataFile != null) {
			return new ProjectData(xmlDataFile, destinationFolder, reportFolder);
		}else {
			return new ProjectData(sourceFolder);
		}
	}

	public static String getAssetsJavaCode(String path) {
		final StringBuilder pathBuilder = new StringBuilder("\", ");
		pathBuilder.append(ActionTestScript.JAVA_EMBEDED_FUNCTION_NAME).append("(\"").append(ASSETS_FOLDER).append("/").append(path).append("\"), \"");
		return pathBuilder.toString();
	}

	public static String getAssetsImageJavaCode(String path) {
		final StringBuilder pathBuilder = new StringBuilder(RESOURCES_FOLDER);
		pathBuilder.append("/").append(IMAGES_FOLDER).append("/").append(path);
		return getAssetsJavaCode(pathBuilder.toString());
	}

	private static File checkAtsProjectFolder(File f){
		if(f != null){
			final File xmlPropertiesFile = f.toPath().resolve(ScriptParser.ATS_PROPERTIES_FILE).toFile();
			if(xmlPropertiesFile.exists()){
				return xmlPropertiesFile;
			}else{
				return checkAtsProjectFolder(f.getParentFile());
			}
		}
		return null;
	}

	public ProjectData() {}

	//create project from current source folder
	public ProjectData(File f) {
		if(f.exists()) {
			if(f.isDirectory()) {
				this.folder = f;
			}else if(f.isFile()) {
				this.folder = f.getParentFile();
			}
			this.folderPath = folder.getPath();

			setJavaDestinationFolderPath(getTargetFolderPath().resolve(TARGET_FOLDER_GENERATED));
			setReportDestinationFolderPath(getTargetFolderPath().resolve(TARGET_FOLDER_REPORT));
		}
	}

	public ProjectData(File xmlPropertiesFile, File generatedJavaFolder, File reportFolder) {

		this.folder = xmlPropertiesFile.getParentFile();
		this.folderPath = xmlPropertiesFile.getParent();

		parseXmlFile(xmlPropertiesFile);

		if(generatedJavaFolder != null) {
			setJavaDestinationFolderPath(generatedJavaFolder.toPath());
		}else {
			setJavaDestinationFolderPath(getTargetFolderPath().resolve(TARGET_FOLDER_GENERATED));
		}

		if(reportFolder != null) {
			setReportDestinationFolderPath(reportFolder.toPath());
		}else {
			setReportDestinationFolderPath(getTargetFolderPath().resolve(TARGET_FOLDER_REPORT));
		}
	}
	
	public boolean isValidated() {
		return folder != null;
	}

	private void setJavaDestinationFolderPath(Path p) {
		this.javaDestinationFolderPath = p;
		this.javaDestinationFolderPath.toFile().mkdirs();
	}

	private void setReportDestinationFolderPath(Path p) {
		this.reportDestinationFolderPath = p;
		this.reportDestinationFolderPath.toFile().mkdirs();
	}

	public void synchronize() {

		folder = new File(folderPath);

		final Path targetFolderPath = getTargetFolderPath();

		setJavaDestinationFolderPath(targetFolderPath.resolve(TARGET_FOLDER_GENERATED));
		setReportDestinationFolderPath(targetFolderPath.resolve(TARGET_FOLDER_REPORT));
	}

	public void initFolders() {
		final File javaFolder = javaDestinationFolderPath.toFile();
		try {
			Utils.deleteRecursiveJavaFiles(javaFolder);
			javaFolder.mkdirs();
		} catch (FileNotFoundException e1) {}
	}

	private void parseXmlFile(File xmlPropertiesFile) {
		try {
			final DocumentBuilder dBuilder = docFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(xmlPropertiesFile);

			Node xmlNode = doc.getElementsByTagName("domain").item(0);
			if(xmlNode != null){
				setDomain(xmlNode.getTextContent());
			}

			xmlNode = doc.getElementsByTagName("name").item(0);
			if(xmlNode != null){
				setName(xmlNode.getTextContent());
			}

			xmlNode = doc.getElementsByTagName("description").item(0);
			if(xmlNode != null){
				setDescription(xmlNode.getTextContent());
			}

			xmlNode = doc.getElementsByTagName("version").item(0);
			if(xmlNode != null){
				setVersion(xmlNode.getTextContent());
			}

			//TODO env + group + libs


		} catch (ParserConfigurationException e) {
			System.err.println(e.getMessage());
		} catch (SAXException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public Path getTargetFolderPath() {
		return folder.toPath().resolve(TARGET_FOLDER);
	}
	
	public Path getAssetsFolderPath() {
		return getSourceFolderPath().resolve(ASSETS_FOLDER);
	}

	private Path getSourceFolderPath() {
		return folder.toPath().resolve(SRC_FOLDER);
	}

	private Path getSourceMainFolderPath() {
		return getSourceFolderPath().resolve(SRC_FOLDER_MAIN);
	}

	public Path getAtsSourceFolder() {
		return getSourceMainFolderPath().resolve(SRC_FOLDER_ATS);
	}

	public Path getJavaSourceFolder() {
		return getSourceMainFolderPath().resolve(SRC_FOLDER_JAVA);
	}

	public Path getJavaDestinationFolder() {
		return javaDestinationFolderPath;
	}

	public Path getReportFolder() {
		return reportDestinationFolderPath;
	}

	public File getJavaFile(String qualifiedPath) {
		return getJavaDestinationFolder().resolve(qualifiedPath).toFile();
	}

	public String getGav() {
		return domain + "." + name + "(" + version + ")";
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

	public String getDomain() {
		return domain;
	}
	public void setDomain(String value) {
		this.domain = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String value) {
		this.name = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String value) {
		this.description = value;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String value) {
		this.version = value;
	}
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String value) {
		this.folderPath = value;
	}
}
