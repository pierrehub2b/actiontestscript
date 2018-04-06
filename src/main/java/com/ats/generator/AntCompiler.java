package com.ats.generator;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class AntCompiler {
	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			File buildFile = new File(args[0]);
			if(buildFile.exists()) {
				new AntCompiler(buildFile);
			}
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
