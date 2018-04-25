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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ATS {
	private String[] args = null;
	private Options options = new Options();

	private File projectFolder = null;
	private File destinationFolder = null;
	private File reportFolder = null;
	private File outputFolder = null;
	
	private boolean compile = false;

	public ATS(String[] args) {

		this.args = args;

		options.addOption("h", "help", false, "Show help");
		options.addOption("f", "force", false, "Force Java files generation if files or folder exists");
		options.addOption("comp", "compile", false, "Compile generated java files");
		options.addOption("prj", "project", true, "ATS project folder");
		options.addOption("dest", "destination", true, "Generated Java files destination folder");
		options.addOption("rep", "report", true, "Execution report Java files destination folder");
	}

	public void parse() {

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			Generator.log(Level.SEVERE, "Cannot parse command line : " + e.getMessage());
		}

		if (cmd.hasOption("h")) {
			help();
		}else {

			boolean force = cmd.hasOption("f");
			compile = cmd.hasOption("comp");
			
			File file;

			String prjFolder = ".";
			if (cmd.hasOption("prj")) {
				prjFolder = cmd.getOptionValue("prj");
			}
			
			file = new File(prjFolder);
			
			if(file.exists()) {
				projectFolder = new File(file.getAbsolutePath());
			}else {
				Path projectPath = Paths.get(prjFolder);
				projectFolder = projectPath.toFile();
			}
			
			if(projectFolder.exists()) {
				Generator.log(Level.INFO, "Using ATS project folder -> " + projectFolder.getAbsolutePath());
			}else {
				Generator.log(Level.SEVERE, "Project folder does not exists -> " + projectFolder.getAbsolutePath());
			}
			
			if (cmd.hasOption("dest")) {
				destinationFolder = new File(cmd.getOptionValue("dest"));
				if(destinationFolder.exists()) {
					if(force) {
						Generator.log(Level.INFO, "Destination folder exists ! (java files will be deleted)");
					}else {
						Generator.log(Level.SEVERE, "Destination folder exists, please delete folder or use '-force' option");
					}
				}
				Generator.log(Level.INFO, "Using destination folder : " + destinationFolder.getAbsolutePath());
			}

			if (cmd.hasOption("rep")) {
				reportFolder = new File(cmd.getOptionValue("rep"));
				if(reportFolder.exists()) {
					if(force) {
						Generator.log(Level.INFO, "Execution report folder found, it will be deleted");
					}else {
						Generator.log(Level.SEVERE, "Execution report folder exists, please delete folder or use '-force' option");
					}
				}
				Generator.log(Level.INFO, "Using report folder : " + reportFolder.getAbsolutePath());
			}
		}
	}

	private void help() {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("ATS Java Code Generator", options);
	}

	//--------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean isCompile() {
		return compile;
	}
	
	public File getProjectFolder() {
		return projectFolder;
	}

	public File getDestinationFolder() {
		return destinationFolder;
	}

	public File getReportFolder() {
		return reportFolder;
	}

	public File getOutputFolder() {
		return outputFolder;
	}
}