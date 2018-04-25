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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
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

	private String charset = ScriptLoader.DEFAULT_CHARSET;

	public static void main(String[] args) {

		log(Level.INFO, "Java version : " + System.getProperty("java.version"));
		log(Level.INFO, StringUtils.repeat("-", 72));

		ATS arguments = new ATS(args);
		arguments.parse();

		ProjectData projectData = ProjectData.getProjectData(arguments.getProjectFolder(), arguments.getDestinationFolder(), arguments.getReportFolder());

		Generator generator = new Generator(projectData);
		GeneratorReport report = generator.launch();

		log(Level.INFO, StringUtils.repeat("-", 72));
		log(Level.INFO, "ATS Generator finished :");
		log(Level.INFO, "- Java files generated -> " + report.getGeneratedScriptsCount());
		log(Level.INFO, "- Ellapsed time -> " + report.getGenerationEllapsedTime() + " ms");
		log(Level.INFO, StringUtils.repeat("-", 72));

		if(arguments.isCompile()) {

			String targetFolderPath = projectData.getTargetFolderPath().toFile().getAbsolutePath();
			
			log(Level.INFO, "Compile generated java files into folder -> " + targetFolderPath + "/classes");
			
			StringBuilder xmlBuilder = new StringBuilder();
			xmlBuilder.append("<project basedir=\"");
			xmlBuilder.append(targetFolderPath);
			xmlBuilder.append("\" default=\"compile\">");
			xmlBuilder.append("<copy todir=\"classes\"><fileset dir=\"../src/assets\" includes=\"**\"/></copy>");
			xmlBuilder.append("<property name=\"lib.dir\" value=\"lib\"/>");
			xmlBuilder.append("<target name=\"compile\"><mkdir dir=\"classes\"/><javac includeantruntime=\"true\" srcdir=\"generated\" destdir=\"classes\"/></target></project>");

			try {
				File tempXml = File.createTempFile("ant_", ".xml");
				tempXml.deleteOnExit();

				Files.write(tempXml.toPath(), xmlBuilder.toString().getBytes());

				new AntCompiler(tempXml);

			} catch (IOException e) {}
		}
	}

	public static void log(Level lvl, String mess) {
		if(lvl.equals(Level.SEVERE)) {
			System.err.println(mess);
		}else {
			System.out.println("[INFO] " + mess);
		}
	}

	public Generator(File atsFile){
		this(new ProjectData(atsFile));
	}

	public Generator(ProjectData project){

		projectData = project;
		projectData.initFolders();

		genReport = new GeneratorReport();

		File atsSourceFolder = projectData.getAtsSourceFolder().toFile();

		if(atsSourceFolder.exists()){

			filesList = new ArrayList<File>();

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

			if(remainingScripts > 0){

				projectData.initFolders();
				genReport.startGenerator(remainingScripts);

				lexer = new Lexer(projectData, genReport, charset);

			}else{
				log(Level.INFO, "Nothing to be done (no ATS files found !)");
			}
		}
	}

	private void addAtsFile(File f) {
		if(f.getName().toLowerCase().endsWith(ScriptLoader.ATS_FILE_EXTENSION) && f.getName().length() > ScriptLoader.ATS_FILE_EXTENSION.length() + 1) {
			filesList.add(f);
		}
	}

	public GeneratorReport launch(){

		Utils.copyDir(projectData.getJavaSourceFolder().toString(), projectData.getJavaDestinationFolder().toString(), true);

		Stream<File> stream = filesList.parallelStream();
		stream.forEach(f -> loadScript(f));
		stream.close();

		genReport.endGenerator();

		filesList.clear();
		lexer = null;

		return genReport;
	}

	private void loadScript(File f){
		ScriptLoader sc = lexer.loadScript(f, new ScriptProcessedNotifier(this));
		sc.generateJavaFile();
	}

	@Override
	public void scriptProcessed() {
		remainingScripts--;

		//int percent = (int)(10000-(double)remainingScripts/(double)totalScript*10000)/100;
		//log.info("Generator in progress : " + percent + " % done");
	}
}