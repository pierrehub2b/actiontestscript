package com.ats.executor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.testng.IExecutionListener;
import org.testng.TestNG;
import org.xml.sax.SAXException;

import com.ats.generator.ATS;
import com.ats.tools.Utils;
import com.ats.tools.report.CampaignReportGenerator;

public class ExecutionListener implements IExecutionListener {

	private static final String TESTNG_FILE_NAME = "testng-results.xml";
	private static final String[] JASPER_PROPERTY_NAME = {"jasper", "jasper.home"};
	private static final String JASPER_HOME_ENVIRONMENT = "JASPER_HOME";

	@SuppressWarnings("deprecation")
	private Path getOutputFolderPath() {
		final String outputFolder = System.getProperty("output-folder");
		if(outputFolder != null) {
			return Paths.get(outputFolder);
		}
		return Paths.get(TestNG.getDefault().getOutputDirectory());
	}

	private File getJsonSuitesFile() {
		final Path p = getOutputFolderPath();
		if(p != null) {
			return p.resolve(CampaignReportGenerator.ATS_JSON_SUITES).toFile();
		}
		return null;
	}

	@Override
	public void onExecutionStart() {
		IExecutionListener.super.onExecutionStart();

		final Path output = getOutputFolderPath();
		try {
			Utils.deleteRecursive(output.toFile());
		} catch (FileNotFoundException e) {
		}

		System.out.println("[INFO] ------------------------------------");
		System.out.println("[INFO]   ATS " + ATS.VERSION + " execution start");
		System.out.println("[INFO] ------------------------------------");
	}

	@Override
	public void onExecutionFinish() {
		IExecutionListener.super.onExecutionFinish();

		System.out.println("[INFO] ------------------------------------");
		System.out.println("[INFO]   ATS execution complete");
		System.out.println("[INFO] ------------------------------------");

		final String atsReport = System.getProperty("ats-report");
		if(atsReport != null) {

			System.out.println("[INFO] Generate ATS report");

			final File jsonSuiteFile = getJsonSuitesFile();
			if(jsonSuiteFile != null && jsonSuiteFile.exists()) {
				
				String jasperHome = null;
				for (String s : JASPER_PROPERTY_NAME) {
					jasperHome = System.getProperty(s);
					if(jasperHome != null) {
						break;
					}
				}
				
				if(jasperHome == null) {
					jasperHome = System.getenv(JASPER_HOME_ENVIRONMENT);
				}
				System.out.println("[INFO] Jasper folder -> " + jasperHome != null ? jasperHome:"no folder found");
				
				try {
					new CampaignReportGenerator(getOutputFolderPath(), jsonSuiteFile, atsReport, jasperHome);
				} catch (IOException | TransformerException | ParserConfigurationException | SAXException e) {
					e.printStackTrace();
				}
			}
		}

		final Path testNGResultsPath = Paths.get("target", "surefire-reports", TESTNG_FILE_NAME);
		final Path destResultsPath = getOutputFolderPath().resolve(TESTNG_FILE_NAME);

		try {
			if(testNGResultsPath.toFile().exists()) {
				Files.copy(testNGResultsPath, destResultsPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}