package com.ats.executor;

import java.io.File;
import java.nio.file.Paths;

import org.testng.IExecutionListener;
import org.testng.TestNG;

import com.ats.generator.ATS;
import com.ats.tools.CampaignReportGenerator;

public class ExecutionListener implements IExecutionListener {
	@Override
	public void onExecutionStart() {
		IExecutionListener.super.onExecutionStart();
		
		System.out.println("----------------------------------------------");
		System.out.println("   ATS " + ATS.VERSION + " execution start");
		System.out.println("----------------------------------------------");
				
		final File jsonSuiteFile = Paths.get(TestNG.getDefault().getOutputDirectory()).resolve(CampaignReportGenerator.ATS_JSON_SUITES).toFile();
		if(jsonSuiteFile.exists()) {
			jsonSuiteFile.delete();
		}
	}
}