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

package com.ats.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CampaignReportGenerator {

	public static String patternDOCTYPE = "<!DOCTYPE[^<>]*(?:<![^<>]*>[^<>]*)*>";
	public static String patternXML = "\\<\\?xml[^<>]*(?:<![^<>]*>[^<>]*)*>";

	public static final String ATS_REPORT = "ats-report";

	public static void main(String[] args) throws ParserConfigurationException, SAXException, TransformerException, IOException, InterruptedException {

		String output = null;
		String details = null;
		String fop = null;
		String html = null;
		String pdf = null;
		String suitesFile = null;

		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if(string.startsWith("--") && i+1 < args.length) {
				switch (string.substring(2)) {
				case "outputFolder":
				case "output":
				case "reportFolder":
					output = args[i+1].replaceAll("\"", "");
					break;
				case "suites":
					suitesFile = args[i+1].replaceAll("\"", "");
					break;
				case "fop":
					fop = args[i+1].replaceAll("\"", "");
					break;
				case "details":
					details = args[i+1];
					break;
				}
			}
		}

		if(output == null) {
			System.out.println("Error, output folder not defined !");
			return;
		}

		final Path outputFolderPath = Paths.get(output).toAbsolutePath();
		if(!outputFolderPath.toFile().exists()) {
			System.out.println("Error, output folder path not found : " + output);
			return;
		}

		if(suitesFile == null) {
			System.out.println("No suites file defined !");
			return;
		}

		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		final Path suiteFilesPath = Paths.get(suitesFile);
		if(suiteFilesPath.toFile().exists()) {

			final int detailsValue = Utils.string2Int(details, 1);

			final File xmlReport = outputFolderPath.resolve(ATS_REPORT + ".xml").toFile();
			final FileWriter fw = new FileWriter(xmlReport);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report actions=\"" +  (detailsValue > 1) + "\" details=\"" + (detailsValue > 2) + "\">");

			fw.write("<pics>");  
			final String[] defaultImages = {"logo.png","true.png","false.png","warning.png"};
			for (String img : defaultImages) {
				fw.write("<pic name='"+ img.replace(".png",  "")  +"'>data:image/png;base64," +  getBase64DefaultImages(ResourceContent.class.getResourceAsStream("/reports/images/" + img).readAllBytes())  + "</pic>");
			}
			fw.write("</pics>");

			final Document doc = builder.parse(
					new InputSource(
							new StringReader(
									new String(
											Files.readAllBytes(
													suiteFilesPath
													), StandardCharsets.UTF_8))));

			final NodeList suiteList = doc.getElementsByTagName("suite-file");

			for (int i=0; i<suiteList.getLength(); i++) {

				final String actionsFilePath = ((Element)suiteList.item(i)).getAttribute("path");
				final Path sp = Paths.get(actionsFilePath);
				final File spFile = sp.toFile();

				if(spFile.exists()) {

					final String suiteName = spFile.getName().replace(".xml", "");
					fw.write("<suite name=\"" + suiteName + "\">");

					final Document suiteDoc = builder.parse(
							new InputSource(
									new StringReader(
											new String(Files.readAllBytes(sp), StandardCharsets.UTF_8))));

					final NodeList classesList = suiteDoc.getElementsByTagName("class");
					final NodeList parametersList = suiteDoc.getElementsByTagName("parameter");

					for (int j=0; j<parametersList.getLength(); j++) {
						final String parameterName = ((Element)parametersList.item(j)).getAttribute("name");
						final String parameterValue = ((Element)parametersList.item(j)).getAttribute("value");
						//TODO
					}

					fw.write("<tests>");

					for (int j=0; j<classesList.getLength(); j++) {
						final String className = ((Element)classesList.item(j)).getAttribute("name");

						final Path xmlDataPath = outputFolderPath.resolve(suiteName).resolve(className + "_xml").resolve("actions.xml");
						final File xmlDataFile = xmlDataPath.toFile();

						if(xmlDataFile.exists()) {
							fw.write(new String(Files.readAllBytes(xmlDataPath), StandardCharsets.UTF_8).replaceAll(patternXML, ""));
						}
					}

					fw.write("</tests></suite>");
				}
			}

			fw.write("</report>");
			fw.close();

			if (fop == null || !(new File(fop).exists())) {
				Map<String, String> map = System.getenv();
				for (Map.Entry<String, String> entry : map.entrySet()) {
					Pattern pattern = Pattern.compile("fop-[\\d].[\\d]");
					Matcher matcher = pattern.matcher(entry.getValue().toLowerCase());
					if (entry.getKey().toLowerCase().contains("fop") && matcher.find()) {
						fop = entry.getValue();
					}
				}
			}

			if(fop != null) {
				final Path fopPath = Paths.get(fop);
				final File fopFile = fopPath.toFile();
				
				if(fopFile.exists()) {
					final StringJoiner fopLibsJoin = new StringJoiner(File.pathSeparator);
					fopLibsJoin.add(fopPath.resolve("build").resolve("fop.jar").toFile().getAbsolutePath());
					
					final File[] fopLibs = fopPath.resolve("lib").toFile().listFiles();
					
					for (File libs : fopLibs) {
						if(libs.getName().contains(".jar")) {
							fopLibsJoin.add(libs.getAbsolutePath());
						}
					}
					
					fop = fopLibsJoin.toString();
				}
			}

			final File xsltFolder = Paths.get("").resolve("src/assets/resources/xslt").toFile();
			if(xsltFolder != null && xsltFolder.exists()) {
				for (File xslt : xsltFolder.listFiles()) {
					if(xslt.getName().equalsIgnoreCase("campaign")) {
						for (File stylesheets : xslt.listFiles()) {
							if(stylesheets.getName().contains("_pdf_")) {
								pdf = stylesheets.getAbsolutePath();
							}
							if(stylesheets.getName().contains("_html_")) {
								html = stylesheets.getAbsolutePath();
							}
						}
					}

					if(xslt.getName().equalsIgnoreCase("images")) {
						if(xslt.listFiles().length > 0) {
							for (File images : xslt.listFiles()) {
								InputStream initialStream = new FileInputStream(images);
								byte[] buffer = new byte[initialStream.available()];
								initialStream.read(buffer);

								OutputStream outStream = new FileOutputStream(outputFolderPath.resolve(images.getName()).toFile());
								outStream.write(buffer);
								outStream.close();
								initialStream.close();
							}
						} else {
							//copyDefaultImagesToFolder(targetFile);
						}
					}
				}
			}

			if (pdf == null || (!new File(pdf).exists())) {
				try {
					pdf = outputFolderPath.resolve("campaign_pdf_stylesheet.xml").toFile().getAbsolutePath();
					copyResource(ResourceContent.class.getResourceAsStream("/reports/campaign/campaign_pdf_stylesheet.xml"),pdf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (html == null || (!new File(html).exists())) {
				try {
					html = outputFolderPath.resolve("campaign_html_stylesheet.xml").toFile().getAbsolutePath();
					copyResource(ResourceContent.class.getResourceAsStream("/reports/campaign/campaign_html_stylesheet.xml"),html);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fop == null || pdf == null || html == null) { return; }		

			//HTML reports

			final MinifyWriter filteredWriter = new MinifyWriter(
					new FileWriter(outputFolderPath.resolve(ATS_REPORT + ".html").toFile()));

			final Transformer htmlTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(html));
			htmlTransformer.transform(new StreamSource(xmlReport), new StreamResult(filteredWriter));

			filteredWriter.close();

			try {
				Runtime.getRuntime().exec("java -cp \"" + fop + "\" org.apache.fop.cli.Main -xml \"" + xmlReport.getAbsolutePath() + "\" -xsl " + pdf + " \"" + outputFolderPath.resolve(ATS_REPORT + ".pdf").toFile().getAbsolutePath() +"\"");
			} catch (Throwable t)
			{
				t.printStackTrace();
			}

		}else {
			System.out.println("Suite files not found : " + suitesFile);
		}
	}

	public static void appendSuiteData(DocumentBuilder builder, StringJoiner suitesJoiner, File suiteFile) throws SAXException, IOException {

		if(suiteFile.exists()) {
			final StringJoiner joiner = new StringJoiner(",");
			joiner.add(suiteFile.getName().replace(".xml", ""));
			final Document doc = builder.parse(
					new InputSource(
							new StringReader(
									new String(
											Files.readAllBytes(
													suiteFile.toPath()
													), StandardCharsets.UTF_8))));

			final NodeList classList = doc.getElementsByTagName("class");

			for (int i=0; i<classList.getLength(); i++) {
				joiner.add(((Element)classList.item(i)).getAttribute("name"));
			}

			suitesJoiner.add(joiner.toString());
		}
	}

	public static void copyResource(InputStream res, String dest) throws IOException {
		byte[] buffer = new byte[res.available()];
		res.read(buffer);

		File targetFile = new File(dest);
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
		outStream.close();
	}

	public static String getBase64DefaultImages(byte[] b) throws IOException {
		return Base64.getEncoder().encodeToString(b);
	}	

	/*public static String generateReportXml(String xmlPath, String outputFolder, File projectFolder, String details, String fop) throws IOException, ParserConfigurationException, SAXException {



		int det = Integer.parseInt(details);
		f.setWritable(true);
		f.setReadable(true);


		final String[] paths = xmlPath.split(";");
		String currentSuite = "";

		for (int j = 0; j<paths.length;j++) {
			final String[] scripts = paths[j].split(",");
			for (int i = 0; i < scripts.length; i++) {
				File currentFile = null;
				if(i == 0) {
					currentSuite = scripts[i];
					currentFile = new File(projectFolder.getAbsolutePath() + "/src/exec/" + scripts[i] + ".xml");

					final String content = new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8);
					fw.write(content.replaceAll(patternDOCTYPE, "").replace("</suite>", ""));

				} else {
					currentFile = new File(outputFolder + "/" + currentSuite + "/" + scripts[i] + "/actions.xml");
					if(currentFile.exists()) {
						final String content = new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8);
						final StringBuilder builder = new StringBuilder("<tests>");

						builder.append(content.replaceAll(patternXML, ""));
						builder.append("</tests>");

						fw.write(builder.toString());

						ScriptGeneratorThread t = new ScriptGeneratorThread(currentFile.getAbsolutePath(), fop);
						t.start();
					}
				}
			}
			fw.write("</suite>");
		} 

		fw.write("</report>");
		fw.close();

		return f.getAbsolutePath();
	}*/
}