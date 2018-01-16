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

	private static final String TARGET_FOLDER_GENERATED = "generated";
	private static final String TARGET_FOLDER_REPORT = "report";

	private static final String SRC_FOLDER_ATS = "ats";
	private static final String SRC_FOLDER_JAVA = "java";

	private String name;
	private String description;
	private String version;
	private String folderPath;
	private String srcPath = "src" + File.separator + "main";
	private String assetsPath = "assets";
	private String targetPath = "target";

	private File javaDestinationFolder;
	private File reportDestinationFolder;

	private File atsRootFolder;
	private File javaRootFolder;

	private File folder;

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
			Path targetFolderPath = folder.toPath().resolve(targetPath);

			javaDestinationFolder = targetFolderPath.resolve(TARGET_FOLDER_GENERATED).toFile();
			reportDestinationFolder = targetFolderPath.resolve(TARGET_FOLDER_REPORT).toFile();

		}else if(f.isFile()) {
			folder = f.getParentFile();
			javaDestinationFolder = f.getParentFile();
			reportDestinationFolder = f.getParentFile();
		}
	}

	public ProjectData(File xmlPropertiesFile, File generatedJavaFolder, File reportFolder) {

		this.folder = xmlPropertiesFile.getParentFile();
		this.folderPath = xmlPropertiesFile.getParent();

		parseXmlFile(xmlPropertiesFile);

		Path targetFolderPath = folder.toPath().resolve(targetPath);

		if(generatedJavaFolder == null) {
			this.javaDestinationFolder = targetFolderPath.resolve(TARGET_FOLDER_GENERATED).resolve("java").toFile();
		}else {
			this.javaDestinationFolder = generatedJavaFolder;
		}

		if(reportFolder == null) {
			this.reportDestinationFolder = targetFolderPath.resolve(TARGET_FOLDER_REPORT).toFile();
		}else {
			this.reportDestinationFolder = reportFolder;
		}
	}

	public void synchronize() {

		folder = new File(folderPath);
		Path targetFolderPath = folder.toPath().resolve(targetPath);

		javaDestinationFolder = targetFolderPath.resolve(TARGET_FOLDER_GENERATED).toFile();
		reportDestinationFolder = targetFolderPath.resolve(TARGET_FOLDER_REPORT).toFile();
	}

	public void initFolders() {
		try {
			Utils.deleteRecursiveJavaFiles(javaDestinationFolder);
			javaDestinationFolder.mkdirs();
		} catch (FileNotFoundException e1) {}
	}

	private void parseXmlFile(File xmlPropertiesFile) {
		try {
			DocumentBuilder dBuilder = docFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlPropertiesFile);

			Node xmlNode = doc.getElementsByTagName("name").item(0);
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

			xmlNode = doc.getElementsByTagName("src").item(0);
			if(xmlNode != null){
				setSourceFolder(xmlNode.getTextContent());
			}

			xmlNode = doc.getElementsByTagName("assets").item(0);
			if(xmlNode != null){
				setAssetsPath(xmlNode.getTextContent());
			}

			xmlNode = doc.getElementsByTagName("target").item(0);
			if(xmlNode != null){
				setTargetPath(xmlNode.getTextContent());
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

	public void setSourceFolder(String src) {
		this.srcPath = src;

		Path srcPath = this.folder.toPath().resolve(src);

		this.atsRootFolder = srcPath.resolve(SRC_FOLDER_ATS).toFile();
		this.javaRootFolder = srcPath.resolve(SRC_FOLDER_JAVA).toFile();
	}

	public File getJavaDestinationFolder() {
		return this.javaDestinationFolder;
	}

	public File getAtsRootFolder() {
		return atsRootFolder;
	}

	public File getJavaRootFolder() {
		return javaRootFolder;
	}

	public File getReportFolder() {
		return reportDestinationFolder;
	}

	public File getJavaFile(String qualifiedPath) {
		return javaDestinationFolder.toPath().resolve(qualifiedPath).toFile();
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	public String getSrcPath() {
		return srcPath;
	}
	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}
	public String getAssetsPath() {
		return assetsPath;
	}
	public void setAssetsPath(String assetsPath) {
		this.assetsPath = assetsPath;
	}
	public String getTargetPath() {
		return targetPath;
	}
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
}
