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

public class AntCompiler {
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
			
			if(args.length > 1) {
				
				final FileWriter fw = new FileWriter(buildFile.getParentFile().toPath().resolve("suites.xml").toFile());

				fw.write("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">");
				fw.write("<suite name=\"allSuites\">");
				fw.write("<suite-files>");

				for (int i = 1; i < args.length; i++) {
					fw.write("<suite-file path=\"" + args[i] + "\"/>");
				}

				fw.write("</suite-files>");
				fw.write("</suite>");

				fw.close();
			}
			
			new AntCompiler(buildFile);
		}
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
