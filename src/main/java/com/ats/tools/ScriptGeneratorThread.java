package com.ats.tools;

import com.ats.tools.report.ScriptReportGenerator;

public class ScriptGeneratorThread extends Thread {
	public String path;
	public String fop;

	public ScriptGeneratorThread(String path, String fop) {   // constructor
		this.path = path;
		this.fop = fop;
	}

	@Override
	public void run() {
		String[] scriptArgs = {"--target",path, "--fop", fop};
		try {
			ScriptReportGenerator.main(scriptArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
