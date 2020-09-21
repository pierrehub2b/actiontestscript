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

import org.apache.commons.cli.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ATS {

	public static void logError(String msg) {
		print("ERROR", msg);
	}

	public static void logInfo(String msg) {
		print("INFO", msg);
	}

	public static void logWarn(String msg) {
		print("WARN", msg);
	}
	
	private static void print(String type, String msg) {
		System.out.println("[" + type + "] " + msg);
	}
	
	private static String getAtsVersion() {
		InputStream resourceAsStream = ATS.class.getResourceAsStream("/version.properties");
		Properties prop = new Properties();
		try{
			prop.load( resourceAsStream );
			return prop.getProperty("version");
		}catch(Exception e) {}

		return "";
	}
	
	public static String VERSION = getAtsVersion();

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

		final CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			ATS.logError("Cannot parse command line : " + e.getMessage());
		}

		if (cmd.hasOption("h")) {
			help();
		}else {

			final boolean force = cmd.hasOption("f");
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
				ATS.logInfo("Using ATS project folder -> " + projectFolder.getAbsolutePath());
			}else {
				ATS.logError("Project folder does not exists -> " + projectFolder.getAbsolutePath());
			}

			if (cmd.hasOption("dest")) {
				destinationFolder = new File(cmd.getOptionValue("dest"));
				if(destinationFolder.exists()) {
					if(force) {
						ATS.logWarn("Destination folder exists ! (java files will be deleted)");
					}else {
						ATS.logError("Destination folder exists, please delete folder or use '-force' option");
					}
				}
				ATS.logInfo("Using destination folder -> " + destinationFolder.getAbsolutePath());
			}

			if (cmd.hasOption("rep")) {
				reportFolder = new File(cmd.getOptionValue("rep"));
				if(reportFolder.exists()) {
					if(force) {
						ATS.logWarn("Execution report folder found, it will be deleted");
					}else {
						ATS.logError("Execution report folder exists, please delete folder or use '-force' option");
					}
				}
				ATS.logInfo("Using report folder : " + reportFolder.getAbsolutePath());
			}
		}
	}

	private void help() {
		final HelpFormatter formater = new HelpFormatter();
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