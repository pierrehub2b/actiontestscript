package com.ats.generator;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.ats.script.ProjectData;

public class GeneratorAnt extends Task{

	String destinationFolder;
	public void setDestinationFolder(String dest) {
		destinationFolder = dest;
	}

	String sourceFolder;
	public void setSourceFolder(String src) {
		sourceFolder = src;
	}
	
	String reportFolder;
	public void setReportFolder(String report) {
		reportFolder = report;
	}

	public void execute() {
		if(destinationFolder != null && sourceFolder != null){

			File destination = new File(destinationFolder);
			File source = new File(sourceFolder);
			File report = null;
			
			if(reportFolder != null) {
				report = new File(reportFolder);
			}

			if(source.exists()){
				
				log("Start Script Generator");
				log(" - Source folder -> " + sourceFolder);
				log(" - Destination folder -> " + destination);
				
				ProjectData projectData = ProjectData.getProjectData(source, destination, report);
				
				Generator generator = new Generator(projectData);
				GeneratorReport generatorReport = generator.launch();
				
				log("Script Generator executed in " + generatorReport.getGenerationEllapsedTime() + " ms");
				log(" - Main scripts -> " + generatorReport.getGeneratedScriptsCount());
				
			}else{
				throw new BuildException("Source folder [" + sourceFolder + "] does not exists !");
			}
			
		}else{
			throw new BuildException("'destinationFolder' and 'sourceFolder' cannot be null or empty !");
		}
	}
}