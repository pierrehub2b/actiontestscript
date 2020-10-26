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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;


public class CampaignReportGenerator {

	public static String ATS_JSON_SUITES = "ats-suites.json";
	public static final String ATS_REPORT = "ats-report";

	public static void main(String[] args) {

		String output = null;
		String details = null;
		String fop = null;

		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if(string.startsWith("--") && i+1 < args.length) {
				switch (string.substring(2)) {
				case "outputFolder":
				case "output":
				case "reportFolder":
					output = args[i+1].replaceAll("\"", "");
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

		final File jsonSuiteFilesFile = outputFolderPath.resolve(ATS_JSON_SUITES).toFile();
		if(jsonSuiteFilesFile.exists()) {

			try {
				new CampaignReportGenerator(outputFolderPath, jsonSuiteFilesFile, details, fop);
			} catch (IOException | TransformerException | ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}

		}else {
			System.out.println("Suites file not found : " + ATS_JSON_SUITES);
		}
	}

	public CampaignReportGenerator(Path outputFolderPath, File jsonSuiteFilesFile, String details, String fop) throws IOException, TransformerException, ParserConfigurationException, SAXException {

		final int detailsValue = Utils.string2Int(details, 1);

		SuiteReportInfo[] suitesList = null;
		try{

			final JsonReader reader = new JsonReader(new FileReader(jsonSuiteFilesFile));
			suitesList = new Gson().fromJson(reader, SuiteReportInfo[].class);
			reader.close();

		}catch (IOException e) {}

		if(suitesList == null) {
			System.out.println("No suites found, nothing to do !");
			return;
		}

		final Transformer xmlSerializer = TransformerFactory.newInstance().newTransformer();
		xmlSerializer.setOutputProperty("omit-xml-declaration", "yes");

		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final BufferedWriter writer = Files.newBufferedWriter(outputFolderPath.resolve(ATS_REPORT + ".xml"), StandardCharsets.UTF_8);

		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report actions=\"" +  (detailsValue > 1) + "\" details=\"" + (detailsValue > 2) + "\"><pics>");

		final String[] defaultImages = new String[]{"logo.png","true.png","false.png","warning.png"};
		for (String img : defaultImages) {
			writer.write("<pic name='"+ img.replace(".png",  "")  +"'>data:image/png;base64," +  getBase64DefaultImages(ResourceContent.class.getResourceAsStream("/reports/images/" + img).readAllBytes())  + "</pic>");
		}
		
		writer.write("</pics>");

		for (int i=0; i<suitesList.length; i++) {

			final String suiteName = suitesList[i].name;

			writer.write("<suite name=\"" + suiteName + "\"><parameters>");

			for (Map.Entry<String, String> entry : suitesList[i].parameters.entrySet()) {
				writer.write("<parameter name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>");
			}

			writer.write("</parameters><tests>");

			final String[] tests = suitesList[i].tests;
			for (int j=0; j<tests.length; j++) {
				final String className = tests[j];
				final Path xmlDataPath = outputFolderPath.resolve(suiteName).resolve(className + "_xml").resolve(XmlReport.REPORT_FILE);

				if(xmlDataPath.toFile().exists()) {
					xmlSerializer.transform(
							new DOMSource(
									builder.parse(
											new InputSource(
													new InputStreamReader(
															new FileInputStream(xmlDataPath.toAbsolutePath().toString()), 
															StandardCharsets.UTF_8)))), new StreamResult(writer));
				}
			}

			writer.write("</tests></suite>");
		}

		writer.write("</report>");
		writer.close();

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

		String html = null;
		String pdf = null;

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

		if (pdf == null || html == null) { return; }		

		//HTML reports

		final Path atsXmlDataPath = outputFolderPath.resolve(ATS_REPORT + ".xml");
		
		final MinifyWriter filteredWriter = new MinifyWriter(Files.newBufferedWriter(outputFolderPath.resolve(ATS_REPORT + ".html"), StandardCharsets.UTF_8));
		final Transformer htmlTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(html));
		
		htmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		htmlTransformer.transform(
				new DOMSource(
						builder.parse(
								new InputSource(
										new InputStreamReader(
												Files.newInputStream(atsXmlDataPath), StandardCharsets.UTF_8)))), new StreamResult(filteredWriter));
		
		filteredWriter.close();

		if(fop != null) {
			try {
				final String cmd = "java -cp \"" + fop + "\" org.apache.fop.cli.Main -xml \"" + atsXmlDataPath.toFile().getAbsolutePath() + "\" -xsl " + pdf + " \"" + outputFolderPath.resolve(ATS_REPORT + ".pdf").toFile().getAbsolutePath() + "\"";
				Runtime.getRuntime().exec(cmd);
			} catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
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