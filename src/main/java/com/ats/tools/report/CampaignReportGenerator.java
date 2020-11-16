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

package com.ats.tools.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ats.tools.ResourceContent;
import com.ats.tools.Utils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class CampaignReportGenerator {

	public static String ATS_JSON_SUITES = "ats-suites.json";
	public static final String ATS_REPORT = "ats-report";

	public static void main(String[] args) {

		String output = null;
		String details = null;
		String jasper = null;

		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if (string.startsWith("--") && i + 1 < args.length) {
				switch (string.substring(2)) {
				case "outputFolder":
				case "output":
				case "reportFolder":
					output = args[i + 1].replaceAll("\"", "");
					break;
				case "jasper":
					jasper = args[i + 1].replaceAll("\"", "");
					break;
				case "details":
					details = args[i + 1];
					break;
				}
			}
		}

		if (output == null) {
			System.out.println("Error, output folder not defined !");
			return;
		}

		final Path outputFolderPath = Paths.get(output).toAbsolutePath();
		if (!outputFolderPath.toFile().exists()) {
			System.out.println("Error, output folder path not found : " + output);
			return;
		}

		final File jsonSuiteFilesFile = outputFolderPath.resolve(ATS_JSON_SUITES).toFile();
		if (jsonSuiteFilesFile.exists()) {

			try {
				new CampaignReportGenerator(outputFolderPath, jsonSuiteFilesFile, details, jasper);
			} catch (IOException | TransformerException | ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Suites file not found : " + ATS_JSON_SUITES);
		}
	}

	public CampaignReportGenerator(Path outputFolderPath, File jsonSuiteFilesFile, String details, String jasper)
			throws IOException, TransformerException, ParserConfigurationException, SAXException {

		final int detailsValue = Utils.string2Int(details, 1);

		SuitesReport sr = null;
		try {

			final JsonReader reader = new JsonReader(new FileReader(jsonSuiteFilesFile));
			sr = new Gson().fromJson(reader, SuitesReport.class);
			reader.close();

		} catch (IOException e) {
		}

		if (sr == null) {
			System.out.println("No suites found, nothing to do !");
			return;
		}

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document writeXmlDocument = builder.newDocument();

		final Element report = writeXmlDocument.createElement("ats-report");
		report.setAttribute("details", String.valueOf(detailsValue));
		report.setAttribute("projectId", sr.projectId);
		writeXmlDocument.appendChild(report);

		final Element picsList = writeXmlDocument.createElement("pics");

		final String[] defaultImages = new String[] { "logo.png", "true.png", "false.png", "warning.png", "noStop.png",
				"pdf.png" };
		for (String img : defaultImages) {
			final Element pic = writeXmlDocument.createElement("pic");
			pic.setAttribute("name", img.replace(".png", ""));
			pic.setTextContent("data:image/png;base64," + getBase64DefaultImages(
					ResourceContent.class.getResourceAsStream("/reports/images/" + img).readAllBytes()));
			picsList.appendChild(pic);
		}
		report.appendChild(picsList);

		report.setAttribute("suitesCount", String.valueOf(sr.suites.length));
		int totalTests = 0;
		int totalTestsPassed = 0;
		int totalSuitesPassed = 0;
		int totalActions = 0;
		int totalDuration = 0;

		for (SuitesReportItem info : sr.suites) {

			boolean suitePassed = true;
			final Element suite = writeXmlDocument.createElement("suite");

			suite.setAttribute("name", info.name);

			final Element parameters = writeXmlDocument.createElement("parameters");
			suite.appendChild(parameters);

			for (Map.Entry<String, String> entry : info.parameters.entrySet()) {
				final Element parameter = writeXmlDocument.createElement("parameter");
				parameter.setAttribute("name", entry.getKey());
				parameter.setAttribute("value", entry.getValue());

				parameters.appendChild(parameter);
			}

			final Element tests = writeXmlDocument.createElement("tests");
			suite.appendChild(tests);

			int testsPassed = 0;
			int actionsExecuted = 0;
			int suiteDuration = 0;

			suite.setAttribute("testsCount", String.valueOf(info.tests.length));

			for (String className : info.tests) {

				final File xmlDataFile = outputFolderPath.resolve(info.name).resolve(className + "_xml")
						.resolve(XmlReport.REPORT_FILE).toFile();
				if (xmlDataFile.exists()) {

					totalTests++;
					int testDuration = 0;

					final Element atsTest = builder.parse(xmlDataFile).getDocumentElement();
					final NodeList summary = atsTest.getElementsByTagName("summary");

					if (summary.getLength() > 0) {
						if ("1".equals(summary.item(0).getAttributes().getNamedItem("status").getNodeValue())) {
							testsPassed++;
							totalTestsPassed++;
						} else {
							suitePassed = false;
						}
					}

					final NodeList actionsList = atsTest.getElementsByTagName("action");
					actionsExecuted += actionsList.getLength();
					totalActions += actionsExecuted;

					for (int i = 0; i < actionsList.getLength(); i++) {
						final Node action = actionsList.item(i);

						for (int j = 0; j < action.getChildNodes().getLength(); j++) {
							final Node actionNode = action.getChildNodes().item(j);
							if ("duration".equals(actionNode.getNodeName())) {
								testDuration += Utils.string2Int(actionNode.getTextContent());
								break;
							}
						}
					}

					atsTest.setAttribute("duration", String.valueOf(testDuration));

					suiteDuration += testDuration;
					totalDuration += suiteDuration;

					tests.appendChild(writeXmlDocument.importNode(atsTest, true));
				}
			}

			if (suitePassed) {
				totalSuitesPassed++;
			}
			suite.setAttribute("passed", String.valueOf(suitePassed));
			suite.setAttribute("duration", String.valueOf(suiteDuration));
			suite.setAttribute("actions", String.valueOf(actionsExecuted));
			suite.setAttribute("testsPassed", String.valueOf(testsPassed));
			report.appendChild(suite);
		}

		report.setAttribute("duration", String.valueOf(totalDuration));
		report.setAttribute("tests", String.valueOf(totalTests));
		report.setAttribute("testsPassed", String.valueOf(totalTestsPassed));
		report.setAttribute("suitesPassed", String.valueOf(totalSuitesPassed));
		report.setAttribute("actions", String.valueOf(totalActions));

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(writeXmlDocument),
				new StreamResult(new OutputStreamWriter(
						new FileOutputStream(outputFolderPath.resolve(ATS_REPORT + ".xml").toFile()),
						StandardCharsets.UTF_8)));

		String html = null;

		final File xsltFolder = Paths.get("").resolve("src/assets/resources/xslt").toFile();
		if (xsltFolder != null && xsltFolder.exists()) {
			for (File xslt : xsltFolder.listFiles()) {
				if (xslt.getName().equalsIgnoreCase("campaign")) {
					for (File stylesheets : xslt.listFiles()) {
						if (stylesheets.getName().contains("_html_")) {
							html = stylesheets.getAbsolutePath();
						}
					}
				}

				if (xslt.getName().equalsIgnoreCase("images")) {
					if (xslt.listFiles().length > 0) {
						for (File images : xslt.listFiles()) {
							InputStream initialStream = new FileInputStream(images);
							byte[] buffer = new byte[initialStream.available()];
							initialStream.read(buffer);

							OutputStream outStream = new FileOutputStream(
									outputFolderPath.resolve(images.getName()).toFile());
							outStream.write(buffer);
							outStream.close();
							initialStream.close();
						}
					}
				}
			}
		}

		if (html == null || (!new File(html).exists())) {
			try {
				html = outputFolderPath.resolve("campaign_html_stylesheet.xml").toFile().getAbsolutePath();
				copyResource(
						ResourceContent.class.getResourceAsStream("/reports/campaign/campaign_html_stylesheet.xml"),
						html);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (html == null) {
			return;
		}

		// HTML reports

		final Path atsXmlDataPath = outputFolderPath.resolve(ATS_REPORT + ".xml");
		final MinifyWriter filteredWriter = new MinifyWriter(
				Files.newBufferedWriter(outputFolderPath.resolve(ATS_REPORT + ".html"), StandardCharsets.UTF_8));
		final Transformer htmlTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(html));

		htmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		htmlTransformer.transform(
				new DOMSource(builder.parse(new InputSource(
						new InputStreamReader(Files.newInputStream(atsXmlDataPath), StandardCharsets.UTF_8)))),
				new StreamResult(filteredWriter));

		filteredWriter.close();

		if (jasper != null) {

			final File jasperFolder = new File(jasper);
			if (jasperFolder.exists()) {
				
				final String jasperFolderPath = jasperFolder.getAbsolutePath();
				System.out.println("[ATS-SCRIPT] Jasper folder -> " + jasperFolderPath);
				
				copyResource("summary.jrxml", outputFolderPath);
				copyResource("suite.jrxml", outputFolderPath);
				copyResource("test.jrxml", outputFolderPath);

				final String outputPath = outputFolderPath.toAbsolutePath().toString();
				
				//-------------------------------------------------------------------------------------------------
				//-------------------------------------------------------------------------------------------------
				
				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				writeXmlDocument = builder.newDocument();

				final Element project = writeXmlDocument.createElement("project");
				project.setAttribute("default", "run");
				project.setAttribute("basedir", outputPath);
				
				final Element path = writeXmlDocument.createElement("path");
				path.setAttribute("id", "classpath");
				
				final Element fileset = writeXmlDocument.createElement("fileset");
				fileset.setAttribute("dir", jasperFolderPath);
				
				final Element include = writeXmlDocument.createElement("include");
				include.setAttribute("name", "**/*.jar");
				
				fileset.appendChild(include);
				path.appendChild(fileset);
				project.appendChild(path);
				
				
				final Element taskdef = writeXmlDocument.createElement("taskdef");
				taskdef.setAttribute("name", "jrc");
				taskdef.setAttribute("classname", "net.sf.jasperreports.ant.JRAntCompileTask");
				
				final Element classpath = writeXmlDocument.createElement("classpath");
				classpath.setAttribute("refid", "classpath");
				taskdef.appendChild(classpath);
				project.appendChild(taskdef);
								
				final Element target = writeXmlDocument.createElement("target");
				target.setAttribute("name", "run");
				
				final Element echo = writeXmlDocument.createElement("echo");
				echo.setAttribute("message", "Clean generated files");
				target.appendChild(echo);
				
				final Element delete = writeXmlDocument.createElement("delete");
				
				final Element fileset2 = writeXmlDocument.createElement("fileset");
				fileset2.setAttribute("dir", ".");
				fileset2.setAttribute("includes", "**/*.jasper");
				delete.appendChild(fileset2);
				
				final Element fileset3 = writeXmlDocument.createElement("fileset");
				fileset3.setAttribute("dir", ".");
				fileset3.setAttribute("includes", "**/*.jrprint");
				delete.appendChild(fileset3);
				
				final Element fileset4 = writeXmlDocument.createElement("fileset");
				fileset4.setAttribute("file", "summary.pdf");
				delete.appendChild(fileset4);
				
				final Element fileset5 = writeXmlDocument.createElement("fileset");
				fileset5.setAttribute("file", "summary.html");
				delete.appendChild(fileset5);
				
				final Element fileset6 = writeXmlDocument.createElement("fileset");
				fileset6.setAttribute("file", "summary.xls");
				delete.appendChild(fileset6);
				
				target.appendChild(delete);
				
				final Element echo3 = writeXmlDocument.createElement("echo");
				echo3.setAttribute("message", "Generate and build Jasper files");
				target.appendChild(echo3);
				
				final Element jrc = writeXmlDocument.createElement("jrc");
				jrc.setAttribute("destdir", outputPath);
				
				final Element src = writeXmlDocument.createElement("src");
				final Element fileset7 = writeXmlDocument.createElement("fileset");
				fileset7.setAttribute("dir", outputPath);
				
				final Element include2 = writeXmlDocument.createElement("include");
				include2.setAttribute("name", "**/*.jrxml");
				fileset7.appendChild(include2);
				src.appendChild(fileset7);
				
				jrc.appendChild(src);
				
				final Element classpath2 = writeXmlDocument.createElement("classpath");
				classpath2.setAttribute("refid", "classpath");
				
				jrc.appendChild(classpath2);
				target.appendChild(jrc);
								
				final Element echo2 = writeXmlDocument.createElement("echo");
				echo2.setAttribute("message", "Export Jasper report to file");
				target.appendChild(echo2);
								
				final Element java = writeXmlDocument.createElement("java");
				java.setAttribute("classname", "ats.reports.ExportToFile");
				
				final Element classpath3 = writeXmlDocument.createElement("classpath");
				final Element path2 = writeXmlDocument.createElement("path");
				path2.setAttribute("refid", "classpath");
				
				classpath3.appendChild(path2);
				java.appendChild(classpath3);
				
				final Element arg1 = writeXmlDocument.createElement("arg");
				arg1.setAttribute("value", outputPath);
				java.appendChild(arg1);
				
				final Element arg2 = writeXmlDocument.createElement("arg");
				arg2.setAttribute("value", "summary");
				java.appendChild(arg2);
				
				final Element arg3 = writeXmlDocument.createElement("arg");
				arg3.setAttribute("value", "pdf");
				java.appendChild(arg3);
				
				target.appendChild(java);
				project.appendChild(target);
				
				final File buildFile = outputFolderPath.resolve("build-report.xml").toFile();
				
				transformer = TransformerFactory.newInstance().newTransformer();
				transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(new DOMSource(project),
						new StreamResult(new OutputStreamWriter(
								new FileOutputStream(buildFile),
								StandardCharsets.UTF_8)));
				
											
				//-------------------------------------------------------------------------------------------
				//-------------------------------------------------------------------------------------------
				
				final Project p = new Project();
				p.setUserProperty("ant.file", buildFile.getAbsolutePath());
				p.init();

				final ProjectHelper helper = ProjectHelper.getProjectHelper();
				p.addReference("ant.projectHelper", helper);
				helper.parse(p, buildFile);

				final DefaultLogger consoleLogger = new DefaultLogger();
				consoleLogger.setErrorPrintStream(System.err);
				consoleLogger.setOutputPrintStream(System.out);
				consoleLogger.setMessageOutputLevel(Project.MSG_INFO);

				p.addBuildListener(consoleLogger);
				p.executeTarget(p.getDefaultTarget());
			}
		}
	}

	private static void copyResource(String resName, Path dest) throws IOException {
		InputStream is = ResourceContent.class.getResourceAsStream("/reports/campaign/" + resName);

		byte[] buffer = new byte[is.available()];
		is.read(buffer);

		File targetFile = dest.resolve(resName).toFile();
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
		outStream.close();
	}

	private static void copyResource(InputStream res, String dest) throws IOException {
		byte[] buffer = new byte[res.available()];
		res.read(buffer);

		File targetFile = new File(dest);
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
		outStream.close();
	}

	private static String getBase64DefaultImages(byte[] b) throws IOException {
		return Base64.getEncoder().encodeToString(b);
	}
}