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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;

import com.ats.generator.events.ScriptProcessedEvent;
import com.ats.generator.events.ScriptProcessedNotifier;
import com.ats.generator.parsers.Lexer;
import com.ats.script.Project;
import com.ats.script.ScriptLoader;
import com.ats.tools.Utils;

public class Generator implements ScriptProcessedEvent{

	private GeneratorReport genReport;
	private Lexer lexer;
	private ArrayList<File> filesList;

	private Project project;

	private int remainingScripts = 0;

	public static void main(String[] args) throws IOException {

		ATS.logInfo("Java version : " + System.getProperty("java.version"));
		ATS.logInfo(StringUtils.repeat("-", 72));

		final ATS arguments = new ATS(args);
		arguments.parse();

		final Project projectData = Project.getProjectData(arguments.getProjectFolder(), arguments.getDestinationFolder(), arguments.getReportFolder());

		if(projectData.isValidated()) {

			final Generator generator = new Generator(projectData);
			final GeneratorReport report = generator.launch();

			ATS.logInfo(StringUtils.repeat("-", 72));
			ATS.logInfo("ATS Generator finished :");
			ATS.logInfo("- Java files generated -> " + report.getGeneratedScriptsCount());
			ATS.logInfo("- Ellapsed time -> " + report.getGenerationEllapsedTime() + " ms");
			ATS.logInfo(StringUtils.repeat("-", 72));

			if(arguments.isCompile()) {

				final String targetFolderPath = projectData.getTargetFolderPath().toString();

				ATS.logInfo("Compile generated java files into folder -> " + targetFolderPath + "/" + Project.TARGET_FOLDER_CLASSES);

				StringBuilder xmlBuilder = new StringBuilder();
				xmlBuilder.append("<project basedir=\"");
				xmlBuilder.append(targetFolderPath);
				xmlBuilder.append("\" default=\"compile\">");
				xmlBuilder.append("<copy todir=\"");
				xmlBuilder.append(Project.TARGET_FOLDER_CLASSES);
				xmlBuilder.append("\"><fileset dir=\"../");
				xmlBuilder.append(Project.SRC_FOLDER);
				xmlBuilder.append("\" includes=\"");
				xmlBuilder.append(Project.ASSETS_FOLDER);
				xmlBuilder.append("/**\"/></copy>");
				xmlBuilder.append("<property name=\"lib.dir\" value=\"lib\"/>");
				xmlBuilder.append("<target name=\"compile\"><mkdir dir=\"");
				xmlBuilder.append(Project.TARGET_FOLDER_CLASSES);
				xmlBuilder.append("\"/><javac includeantruntime=\"true\" srcdir=\"");
				xmlBuilder.append(Project.TARGET_FOLDER_GENERATED);
				xmlBuilder.append("\" destdir=\"");
				xmlBuilder.append(Project.TARGET_FOLDER_CLASSES);
				xmlBuilder.append("\"/></target></project>");

				try {
					File tempXml = File.createTempFile("ant_", ".xml");
					tempXml.deleteOnExit();

					Files.write(tempXml.toPath(), xmlBuilder.toString().getBytes());

					new AntCompiler(tempXml);

				} catch (IOException e) {}
			}

			if(arguments.getSuites() != null) {

				final String[] suites = arguments.getSuites();
				if(suites.length > 0) {
					final Path projectFolderPath = projectData.getTargetFolderPath();
					final FileWriter fw = new FileWriter(projectFolderPath.resolve("suites.xml").toFile());

					fw.write("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">");
					fw.write("<suite name=\"allSuites\">");
					fw.write("<suite-files>");

					for (int i = 0; i < suites.length; i++) {
						final String suitePath = findSuiteFile(suites[i]);
						if(suitePath != null) {
							fw.write("<suite-file path=\"" + suitePath + "\"/>");
						}
					}

					fw.write("</suite-files>");
					fw.write("</suite>");
					fw.close();
				}
			}

		}else {
			ATS.logInfo("No valid Ats project found at -> " + arguments.getProjectFolder());
		}
	}

	public static String findSuiteFile(String s) {
		if(Paths.get(s).toFile().exists()) {
			return s;
		}else {
			if(Paths.get(s + ".xml").toFile().exists()) {
				return s + ".xml";
			}else {
				s = "src/exec/" + s;
				if(Paths.get(s).toFile().exists()) {
					return s;
				}else {
					if(Paths.get(s + ".xml").toFile().exists()) {
						return s + ".xml";
					}
				}
			}
		}
		return null;
	}

	public Generator(String projectPath){
		this(new File(projectPath));
	}

	public Generator(File atsFile){
		this(Project.getProjectData(atsFile, null, null));
	}

	public Generator(Project project){

		if(init(project) && remainingScripts > 0){

			project.initFolders();
			genReport.startGenerator(remainingScripts);

			lexer = new Lexer(project, genReport, StandardCharsets.UTF_8);

		}else{
			ATS.logInfo("Nothing to be done (no ATS files found !)");
		}
	}

	private boolean init(Project p) {
		if(p.isValidated()) {

			genReport = new GeneratorReport();
			project = p;

			filesList = project.getAtsScripts();
			remainingScripts = filesList.size();

			return true;
		}
		return false;
	}

	private void addDataFile(ArrayList<File> list, File f) {
		if(f.getName().toLowerCase().endsWith(".csv") || f.getName().toLowerCase().endsWith(".json")) {
			list.add(f);
		}
	}

	public GeneratorReport launch(){

		if(project.getJavaSourceFolder().toFile().exists()){
			Utils.copyDir(project.getJavaSourceFolder().toString(), project.getJavaDestinationFolder().toString(), true);
		}

		final Stream<File> stream = filesList.parallelStream();
		stream.forEach(f -> loadScript(f));
		stream.close();

		genReport.endGenerator();

		filesList.clear();
		lexer = null;

		return genReport;
	}

	private void loadScript(File f){
		final ScriptLoader sc = lexer.loadScript(f, new ScriptProcessedNotifier(this));
		sc.generateJavaFile(project);
	}

	public ArrayList<String> findSubscriptRef(String calledScript){
		final ArrayList<String> result = new ArrayList<String>();
		for(File f : filesList) {
			final ScriptLoader sc = lexer.loadScript(f, new ScriptProcessedNotifier(this));
			if(sc.isSubscriptCalled(calledScript)) {
				result.add(sc.getHeader().getQualifiedName());
			}
		}

		final ArrayList<File> dataFiles = new ArrayList<File>();
		try {
			Files.find(project.getAssetsFolderPath(), 99999, (p, bfa) -> bfa.isRegularFile()).forEach(p -> addDataFile(dataFiles, p.toFile()));

			for(File f : dataFiles) {
				String[] stringArray = f.getName().split("\\.");
				String fileExtension = stringArray[stringArray.length-1];
				if (fileExtension.equalsIgnoreCase("csv")) {
					try (CSVReader csvReader = new CSVReader(new FileReader(f.getAbsolutePath()))) {
						String[] values;
						while ((values = csvReader.readNext()) != null) {
							List<String> list = Arrays.asList(values);
							if (list.contains(calledScript)) {
								result.add(f.getCanonicalPath());
								break;
							}
						}
					}
				} else {
					Scanner scanner = new Scanner(f);
					while (scanner.hasNext()) {
						final String line = scanner.next();
						if(line.equals(calledScript) || line.contains("\"" + calledScript + "\"")) {
							result.add(f.getCanonicalPath());
							break;
						}
					}
					scanner.close();
				}
			}

		} catch (IOException | CsvValidationException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public void scriptProcessed() {
		remainingScripts--;

		//int percent = (int)(10000-(double)remainingScripts/(double)totalScript*10000)/100;
		//log.info("Generator in progress : " + percent + " % done");
	}


}