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

import com.ats.generator.parsers.ScriptParser;
import com.ats.tools.Utils;

public class ProjectData {

	private static final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

	private static final String TARGET_FOLDER = "target";
	private static final String TARGET_FOLDER_GENERATED = "generated";
	private static final String TARGET_FOLDER_REPORT = "report";

	private static final String SRC_FOLDER = "src";
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
		File xmlDataFile = checkLtfProjectFolder(sourceFolder);
		if(xmlDataFile != null) {
			return new ProjectData(xmlDataFile, destinationFolder, reportFolder);
		}else {
			return new ProjectData(sourceFolder);
		}
	}

	private static File checkLtfProjectFolder(File f){
		if(f != null){
			File xmlPropertiesFile = f.toPath().resolve(ScriptParser.ATS_PROPERTIES_FILE).toFile();
			if(xmlPropertiesFile.exists()){
				return xmlPropertiesFile;
			}else{
				return checkLtfProjectFolder(f.getParentFile());
			}
		}
		return null;
	}

	public ProjectData() {}

	//create project from current source folder
	public ProjectData(File f) {
		if(f.isDirectory()) {
			folder = f;
		}else if(f.isFile()) {
			folder = f.getParentFile();
		}
	}

	public ProjectData(File xmlPropertiesFile, File generatedJavaFolder, File reportFolder) {

		this.folder = xmlPropertiesFile.getParentFile();
		this.folderPath = xmlPropertiesFile.getParent();

		parseXmlFile(xmlPropertiesFile);

		if(generatedJavaFolder != null) {
			this.javaDestinationFolderPath = generatedJavaFolder.toPath();
		}else {
			this.javaDestinationFolderPath = getTargetFolderPath().resolve(TARGET_FOLDER_GENERATED);
		}
		this.javaDestinationFolderPath.toFile().mkdirs();

		if(reportFolder != null) {
			this.reportDestinationFolderPath = reportFolder.toPath();
		}else {
			this.reportDestinationFolderPath = getTargetFolderPath().resolve(TARGET_FOLDER_REPORT);
		}
		this.reportDestinationFolderPath.toFile().mkdirs();
	}

	public void synchronize() {

		folder = new File(folderPath);
		
		Path targetFolderPath = getTargetFolderPath();

		javaDestinationFolderPath = targetFolderPath.resolve(TARGET_FOLDER_GENERATED);
		reportDestinationFolderPath = targetFolderPath.resolve(TARGET_FOLDER_REPORT);
	}

	public void initFolders() {
		File javaFolder = javaDestinationFolderPath.toFile();
		try {
			Utils.deleteRecursiveJavaFiles(javaFolder);
			javaFolder.mkdirs();
		} catch (FileNotFoundException e1) {}
	}

	private void parseXmlFile(File xmlPropertiesFile) {
		try {
			DocumentBuilder dBuilder = docFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlPropertiesFile);

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
