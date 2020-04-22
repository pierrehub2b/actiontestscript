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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.ats.generator.events.ScriptProcessedEvent;
import com.ats.generator.events.ScriptProcessedNotifier;
import com.ats.generator.parsers.Lexer;
import com.ats.script.ProjectData;
import com.ats.script.ScriptLoader;
import com.ats.tools.Utils;

public class Generator implements ScriptProcessedEvent{

	private GeneratorReport genReport;
	private Lexer lexer;
	private ArrayList<File> filesList;

	private ProjectData projectData;

	private int remainingScripts = 0;

	public static void main(String[] args) {

		ATS.logInfo("Java version : " + System.getProperty("java.version"));
		ATS.logInfo(StringUtils.repeat("-", 72));

		final ATS arguments = new ATS(args);
		arguments.parse();

		final ProjectData projectData = ProjectData.getProjectData(arguments.getProjectFolder(), arguments.getDestinationFolder(), arguments.getReportFolder());

		if(projectData.isValidated()) {

			final Generator generator = new Generator(projectData);
			final GeneratorReport report = generator.launch();

			ATS.logInfo(StringUtils.repeat("-", 72));
			ATS.logInfo("ATS Generator finished :");
			ATS.logInfo("- Java files generated -> " + report.getGeneratedScriptsCount());
			ATS.logInfo("- Ellapsed time -> " + report.getGenerationEllapsedTime() + " ms");
			ATS.logInfo(StringUtils.repeat("-", 72));

			if(arguments.isCompile()) {

				final String targetFolderPath = projectData.getTargetFolderPath().toFile().getAbsolutePath();

				ATS.logInfo("Compile generated java files into folder -> " + targetFolderPath + "/" + ProjectData.TARGET_FOLDER_CLASSES);

				StringBuilder xmlBuilder = new StringBuilder();
				xmlBuilder.append("<project basedir=\"");
				xmlBuilder.append(targetFolderPath);
				xmlBuilder.append("\" default=\"compile\">");
				xmlBuilder.append("<copy todir=\"");
				xmlBuilder.append(ProjectData.TARGET_FOLDER_CLASSES);
				xmlBuilder.append("\"><fileset dir=\"../");
				xmlBuilder.append(ProjectData.SRC_FOLDER);
				xmlBuilder.append("\" includes=\"");
				xmlBuilder.append(ProjectData.ASSETS_FOLDER);
				xmlBuilder.append("/**\"/></copy>");
				xmlBuilder.append("<property name=\"lib.dir\" value=\"lib\"/>");
				xmlBuilder.append("<target name=\"compile\"><mkdir dir=\"");
				xmlBuilder.append(ProjectData.TARGET_FOLDER_CLASSES);
				xmlBuilder.append("\"/><javac includeantruntime=\"true\" srcdir=\"");
				xmlBuilder.append(ProjectData.TARGET_FOLDER_GENERATED);
				xmlBuilder.append("\" destdir=\"");
				xmlBuilder.append(ProjectData.TARGET_FOLDER_CLASSES);
				xmlBuilder.append("\"/></target></project>");

				try {
					File tempXml = File.createTempFile("ant_", ".xml");
					tempXml.deleteOnExit();

					Files.write(tempXml.toPath(), xmlBuilder.toString().getBytes());

					new AntCompiler(tempXml);

				} catch (IOException e) {}
			}
		}else {
			ATS.logInfo("No valid Ats project found at -> " + arguments.getProjectFolder());
		}
	}

	public Generator(String projectPath){
		this(new File(projectPath));
	}

	public Generator(File atsFile){
		this(ProjectData.getProjectData(atsFile, null, null));
	}

	public Generator(ProjectData project){

		if(init(project) && remainingScripts > 0){

			projectData.initFolders();
			genReport.startGenerator(remainingScripts);

			lexer = new Lexer(projectData, genReport, StandardCharsets.UTF_8);

		}else{
			ATS.logInfo("Nothing to be done (no ATS files found !)");
		}
	}

	private boolean init(ProjectData pdata) {

		filesList = new ArrayList<File>();

		if(pdata.isValidated()) {
			genReport = new GeneratorReport();
			projectData = pdata;

			final File atsSourceFolder = projectData.getAtsSourceFolder().toFile();

			if(atsSourceFolder.exists()){
				if(atsSourceFolder.isDirectory()){
					try {
						Files.find(atsSourceFolder.toPath(), 99999, (p, bfa) -> bfa.isRegularFile()).forEach(p -> addAtsFile(p.toFile()));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}else if(atsSourceFolder.isFile()){
					addAtsFile(atsSourceFolder);
				}

				remainingScripts = filesList.size();
			}
			return true;
		}else {
			return false;
		}
	}

	private void addAtsFile(File f) {
		if(f.getName().toLowerCase().endsWith(ScriptLoader.ATS_FILE_EXTENSION) && f.getName().length() > ScriptLoader.ATS_FILE_EXTENSION.length() + 1) {
			filesList.add(f);
		}
	}

	private void addDataFile(ArrayList<File> list, File f) {
		if(f.getName().toLowerCase().endsWith(".csv") || f.getName().toLowerCase().endsWith(".json")) {
			list.add(f);
		}
	}

	public GeneratorReport launch(){

		if(projectData.getJavaSourceFolder().toFile().exists()){
			Utils.copyDir(projectData.getJavaSourceFolder().toString(), projectData.getJavaDestinationFolder().toString(), true);
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
		sc.generateJavaFile(projectData.getGav());
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
			Files.find(projectData.getAssetsFolderPath(), 99999, (p, bfa) -> bfa.isRegularFile()).forEach(p -> addDataFile(dataFiles, p.toFile()));

			for(File f : dataFiles) {
				Scanner scanner = new Scanner(f);
				while (scanner.hasNext()) {
					final String line = scanner.next();
					if(line.contains(calledScript)) {
						result.add(f.getCanonicalPath());
					}
				}
				scanner.close();
			}

		} catch (IOException e) {
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