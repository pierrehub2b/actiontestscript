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

package com.ats.generator;

import com.ats.script.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;

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

	@Override
	public void execute() {
		if(destinationFolder != null && sourceFolder != null){

			final File destination = new File(destinationFolder);
			final File source = new File(sourceFolder);
			File report = null;

			if(reportFolder != null) {
				report = new File(reportFolder);
			}

			if(source.exists()){

				log("Start Script Generator");
				log(" - Source folder -> " + sourceFolder);
				log(" - Destination folder -> " + destination);

				Project projectData = Project.getProjectData(source, destination, report);
				if(projectData.isValidated()) {
					Generator generator = new Generator(projectData);
					GeneratorReport generatorReport = generator.launch();

					log("Script Generator executed in " + generatorReport.getGenerationEllapsedTime() + " ms");
					log(" - Main scripts -> " + generatorReport.getGeneratedScriptsCount());
				}else {
					throw new BuildException("Source folder [" + sourceFolder + "] does not exists !");
				}

			}else{
				throw new BuildException("Source folder [" + sourceFolder + "] does not exists !");
			}

		}else{
			throw new BuildException("'destinationFolder' and 'sourceFolder' cannot be null or empty !");
		}
	}
}