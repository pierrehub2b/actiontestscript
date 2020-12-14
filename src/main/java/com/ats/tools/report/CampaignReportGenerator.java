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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
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

	private final static String xmlSourceName = ATS_REPORT + ".xml";
	private final static String xmlSourceRoot = "ats-report";

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

	private void copyReportsTemplate(Path path, Path toPath) {
		if(Files.exists(path)) {
			for(File f : path.toFile().listFiles()) {
				if(f.isFile()) {
					try {
						Files.copy(f.toPath(), toPath.resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {}
				}
			}
		}
	}

	public CampaignReportGenerator(Path outputFolderPath, File jsonSuiteFilesFile, String reportLevel, String jasper)
			throws IOException, TransformerException, ParserConfigurationException, SAXException {

		final int detailsValue = Utils.string2Int(reportLevel, 0);

		if(detailsValue > 0) {

			final Path reportPath = Paths.get("src", "assets", "resources", "reports");
			if(Files.exists(reportPath)) {
				copyReportsTemplate(reportPath.resolve("templates"), outputFolderPath);
				copyReportsTemplate(reportPath.resolve("images"), outputFolderPath);
			}

			SuitesReport suiteReport = null;
			try {

				final JsonReader reader = new JsonReader(new FileReader(jsonSuiteFilesFile));
				suiteReport = new Gson().fromJson(reader, SuitesReport.class);
				reader.close();

			} catch (IOException e) {
			}

			if (suiteReport == null) {
				System.out.println("No suites found, nothing to do !");
				return;
			}

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document writeXmlDocument = builder.newDocument();

			final Element report = writeXmlDocument.createElement("ats-report");
			report.setAttribute("details", String.valueOf(detailsValue));
			report.setAttribute("projectId", suiteReport.projectId);
			report.setAttribute("projectDescription", suiteReport.projectDescription);
			writeXmlDocument.appendChild(report);

			final Element picsList = writeXmlDocument.createElement("pics");

			final String[] defaultImages = new String[] { "logo.png", "true.png", "false.png", "warning.png", "noStop.png", "pdf.png" };
			for (String img : defaultImages) {
				final Element pic = writeXmlDocument.createElement("pic");
				pic.setAttribute("name", img.replace(".png", ""));

				byte[] imgBytes = null;
				if(Files.exists(outputFolderPath.resolve(img))){
					imgBytes = Files.readAllBytes(outputFolderPath.resolve(img));
				}else {
					imgBytes = ResourceContent.class.getResourceAsStream("/reports/images/" + img).readAllBytes();
				}

				pic.setTextContent("data:image/png;base64," + getBase64DefaultImages(imgBytes));
				picsList.appendChild(pic);
			}
			report.appendChild(picsList);

			report.setAttribute("suitesCount", String.valueOf(suiteReport.suites.length));
			int totalTests = 0;
			int totalTestsPassed = 0;
			int totalSuitesPassed = 0;
			int totalActions = 0;
			int totalDuration = 0;

			for (SuitesReportItem info : suiteReport.suites) {

				boolean suitePassed = true;
				final Element suite = writeXmlDocument.createElement("suite");

				suite.setAttribute("name", info.name);
				suite.setAttribute("description", info.description);

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

					final File xmlDataFile = outputFolderPath.resolve(info.name).resolve(className + "_xml").resolve(XmlReport.REPORT_FILE).toFile();

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

						tests.appendChild(writeXmlDocument.importNode(atsTest, true));
					}
				}

				totalActions += actionsExecuted;
				totalDuration += suiteDuration;

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
							new FileOutputStream(outputFolderPath.resolve(xmlSourceName).toFile()),
							StandardCharsets.UTF_8)));


			File htmlTemplateFile = copyResource("suites_html.xml", outputFolderPath);

			// HTML reports

			final Path atsXmlDataPath = outputFolderPath.resolve(xmlSourceName);
			final MinifyWriter filteredWriter = new MinifyWriter(
					Files.newBufferedWriter(outputFolderPath.resolve(ATS_REPORT + ".html"), StandardCharsets.UTF_8));
			final Transformer htmlTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(htmlTemplateFile));

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
					final String reportName = "summary";

					//-----------------------------------------------------------------------------------------------------
					// Clean files
					//-----------------------------------------------------------------------------------------------------	

					File [] filesToDelete = outputFolderPath.toFile().listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".jrprint") || name.endsWith(".jasper") || name.equals(reportName + ".pdf");
						}
					});

					System.out.println("Delete files.");
					for (File f : filesToDelete) {
						try {
							f.delete();
							System.out.println("File : " + f.getAbsolutePath());
						}catch(Exception e) {}
					}

					//-----------------------------------------------------------------------------------------------------
					// Build Jasper reports
					//-----------------------------------------------------------------------------------------------------	

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

					final Element jrc = writeXmlDocument.createElement("jrc");
					jrc.setAttribute("destdir", outputPath);
					jrc.setAttribute("xmlvalidation", "false");

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

					project.appendChild(target);

					final File buildFile = outputFolderPath.resolve("build-report.xml").toFile();

					transformer = TransformerFactory.newInstance().newTransformer();
					transformer = TransformerFactory.newInstance().newTransformer();
					transformer.transform(new DOMSource(project),
							new StreamResult(new OutputStreamWriter(
									new FileOutputStream(buildFile),
									StandardCharsets.UTF_8)));


					//-----------------------------------------------------------------------------------------------------
					// Launch Ant task to build reports
					//-----------------------------------------------------------------------------------------------------	

					final Project p = new Project();

					p.setUserProperty("ant.file", buildFile.getAbsolutePath());


					//				File outputFolder = outputFolderPath.toFile();
					//				p.setDefault("run");
					//				p.setBaseDir(outputFolder);
					//				
					//				Reference ref = new Reference(p, "classpath");
					//				org.apache.tools.ant.types.Path ap = new org.apache.tools.ant.types.Path(p);
					//				ap.setRefid(ref);
					//				
					//				FileSet afs = new FileSet();
					//				afs.setDir(jasperFolder);
					//				FilenameSelector afsSelector = new FilenameSelector();
					//				afsSelector.setRegex(".*\\.jar");
					//				afs.add(afsSelector);
					//				ap.addFileset(afs);
					//				
					//				Taskdef tsk = new Taskdef();
					//				tsk.setClassname("net.sf.jasperreports.ant.JRAntCompileTask");
					//				tsk.setName("jrc");
					//				tsk.setTaskName("jrc");
					//				org.apache.tools.ant.types.Path ap2 = new org.apache.tools.ant.types.Path(p);
					//				ap2.setRefid(ref);
					//				
					//				tsk.setProject(p);
					//				
					//				Target t = new Target();
					//				t.setName("run");
					//				
					//				t.addTask(tsk);
					//				
					//				p.addTarget(t);

					p.init();

					final ProjectHelper helper = ProjectHelper.getProjectHelper();
					p.addReference("ant.projectHelper", helper);
					helper.parse(p, buildFile);

					final DefaultLogger consoleLogger = new DefaultLogger();
					consoleLogger.setErrorPrintStream(System.err);
					consoleLogger.setOutputPrintStream(System.out);
					consoleLogger.setEmacsMode(false);
					consoleLogger.setMessageOutputLevel(Project.MSG_INFO);

					p.addBuildListener(consoleLogger);
					p.executeTarget(p.getDefaultTarget());

					//-----------------------------------------------------------------------------------------------------
					// Generate pdf report
					//-----------------------------------------------------------------------------------------------------		

					System.out.println("Generate reports.");

					try {
						generatePdf(reportName, outputPath, jasperFolder);
					}catch(ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void generatePdf(String reportName, String outputPath, File jasperFolder) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {

		outputPath += File.separator;
		final String reportFullName = outputPath + reportName + ".pdf";
		System.out.print("File : " + reportFullName + " ... ");

		int len = jasperFolder.listFiles().length;
		final URL[] urls = new URL[len];

		for (int i=0; i < len; i++) {
			urls[i] = jasperFolder.listFiles()[i].toURI().toURL();
		}

		final URLClassLoader loader = new URLClassLoader(urls); 

		Class clazz = loader.loadClass("net.sf.jasperreports.engine.JasperFillManager");

		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("workingDir", outputPath);
		parameters.put("xmlSource", xmlSourceName);
		parameters.put("xmlSourceRoot", xmlSourceRoot);

		Method method = clazz.getMethod("fillReportToFile", String.class, Map.class);

		final Object fileName = method.invoke(null, outputPath + reportName + ".jasper", parameters);

		clazz = loader.loadClass("net.sf.jasperreports.engine.JasperExportManager");
		method = clazz.getMethod("exportReportToPdfFile", String.class, String.class);

		method.invoke(null, fileName.toString(), reportFullName);
		System.out.println("OK");
		System.out.flush();

		loader.close();
	}

	private static File copyResource(String resName, Path dest) throws IOException {
		Path filePath = dest.resolve(resName);
		if(!Files.exists(filePath)) {
			InputStream is = ResourceContent.class.getResourceAsStream("/reports/templates/" + resName);

			byte[] buffer = new byte[is.available()];
			is.read(buffer);

			File targetFile = dest.resolve(resName).toFile();
			OutputStream outStream = new FileOutputStream(targetFile);
			outStream.write(buffer);
			outStream.close();
		}
		return filePath.toFile();
	}

	private static String getBase64DefaultImages(byte[] b) throws IOException {
		return Base64.getEncoder().encodeToString(b);
	}
}