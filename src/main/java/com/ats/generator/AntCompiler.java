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

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.ats.driver.AtsManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AntCompiler {
	
	private static final String XML_SUITE_FILES = "-suiteXmlFiles=";
	
	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			final File buildFile = new File(args[0]);
			if(!buildFile.exists()) {
			
				buildFile.getParentFile().mkdirs();
				
				final FileWriter fw = new FileWriter(buildFile);
				fw.write("<project basedir=\".\" default=\"compile\">");
				fw.write("<copy todir=\"classes\"> ");
				fw.write("<fileset dir=\"..\\src\" includes='assets/**'/>");
				fw.write("</copy>");
				fw.write("<property name=\"lib.dir\" value=\"lib\"/>");
				fw.write("<path id=\"classpath\">");
				fw.write("<fileset dir=\"" + AtsManager.getAtsHomeFolder() + "\\libs\" includes=\"**/*.jar\"/>");
				fw.write("</path>");
				fw.write("<target name=\"compile\">");
				fw.write("<mkdir dir=\"classes\"/>");
				fw.write("<javac srcdir=\"generated\" destdir=\"classes\" classpathref=\"classpath\"/>");
				fw.write("</target>");
				fw.write("</project>");

				fw.close();
			}
			
			if(args.length > 1 && args[1].startsWith(XML_SUITE_FILES)) {
				
				final String[] suites = args[1].replace(XML_SUITE_FILES, "").split(",");
				final Path projectFolderPath = buildFile.getParentFile().toPath();
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
				
				System.out.println("[ATS] " + suites.length + " suite(s) added to job execution");
			}
			
			new AntCompiler(buildFile);
		}
	}
	
	private static String findSuiteFile(String s) {
		if(Paths.get(s).toFile().exists()) {
			return s;
		}else {
			s = "src/exec/" + s;
			if(Paths.get(s).toFile().exists()) {
				return s;
			}
		}
		return null;
	}

	public AntCompiler(File buildFile) {
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		p.init();

		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);

		p.addBuildListener(consoleLogger);
		p.executeTarget(p.getDefaultTarget());
	}
}
