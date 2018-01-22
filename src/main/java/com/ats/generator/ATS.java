package com.ats.generator;

import java.io.File;
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

	private File sourceFolder = null;
	private File destinationFolder = null;
	private File reportFolder = null;
	private File outputFolder = null;

	public ATS(String[] args) {

		this.args = args;

		options.addOption("h", "help", false, "Show help");
		options.addOption("f", "force", false, "Force Java files generation if files or folder exists");
		options.addOption("src", "source", true, "ATS source folder");
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

			if (cmd.hasOption("src")) {

				sourceFolder = new File(cmd.getOptionValue("src"));
				if(sourceFolder.exists()) {
										
					if(sourceFolder.isDirectory()) {
						Generator.log(Level.INFO, "Using ATS source folder : " + sourceFolder.getAbsolutePath());
					}else if(sourceFolder.isFile()) {
						Generator.log(Level.INFO, "Using ATS file : " + sourceFolder.getAbsolutePath());
					}

				}else {
					Generator.log(Level.SEVERE, "Source folder does not exists !");
				}

			} else {
				Generator.log(Level.SEVERE, "Source folder is mandatory !");
				help();
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

	public File getSourceFolder() {
		return sourceFolder;
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