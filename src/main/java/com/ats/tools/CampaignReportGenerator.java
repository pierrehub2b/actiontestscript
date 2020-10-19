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

import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampaignReportGenerator {
	
    public static String patternDOCTYPE = "<!DOCTYPE[^<>]*(?:<![^<>]*>[^<>]*)*>";
    public static String patternXML = "\\<\\?xml[^<>]*(?:<![^<>]*>[^<>]*)*>";
    
    public static final String ATS_REPORT = "ats-report";
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, TransformerException, IOException, InterruptedException {
		String targetFiles = null;
		String outputFolder = null;
		String details = null;
		String fop = null;
		String html = null;
		String pdf = null;
		
		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if(string.startsWith("--") && i+1 < args.length) {
				switch (string.substring(2)) {
					case "outputFolder":
						outputFolder = args[i+1];
						break;
					case "targetFiles":
						targetFiles = args[i+1];
						break;
					case "fop":
						fop = args[i+1];
						break;
					case "details":
						details = args[i+1];
						break;
				}
			}
		}
		
		File projectFolder = null;
		File currentFolder = new File(outputFolder);
		
		while(projectFolder == null) {
			currentFolder = currentFolder.getParentFile();
			File[] tmpFiles = currentFolder.listFiles();
			for (File f : tmpFiles) {
				if(f.getName().equalsIgnoreCase("src")) {
					projectFolder = currentFolder;
				}
			}
		}
		
		//TODO construct the output report
		String target = generateReportXml(targetFiles, outputFolder, projectFolder, details);
		
		if(target == null) return;
		File targetFile = new File(target);
		
		File xsltFolder = new File(projectFolder.getAbsolutePath() + "/src/assets/resources/xslt");

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
			String fopLibsString = "";
			File[] fopLibs = new File(fop + "/lib").listFiles();
			for (File libs : fopLibs) {
				if(libs.getName().contains(".jar")) {
					fopLibsString += ";" + libs.getAbsolutePath();
				}
			}
			
			fop = fop + "\\build\\fop.jar;" + fopLibsString;
		}
		
		for (File xslt : xsltFolder.listFiles()) {
			if(xslt.getName().equalsIgnoreCase("campaign")) {
				for (File stylesheets : xslt.listFiles()) {
					if(stylesheets.getName().contains("_pdf_")) {
						pdf = stylesheets.getAbsolutePath();
					}
					if(stylesheets.getName().contains("_html_")) {
						html = stylesheets.getAbsolutePath();
					}
					if(stylesheets.getName().contains(".css") || stylesheets.getName().contains(".js")) {
						InputStream initialStream = new FileInputStream(stylesheets);
					    byte[] buffer = new byte[initialStream.available()];
					    initialStream.read(buffer);
					 
					    File styleFile = new File(targetFile.getParentFile().getAbsolutePath() + File.separator + stylesheets.getName());
					    OutputStream outStream = new FileOutputStream(styleFile);
					    outStream.write(buffer);
					    outStream.close();
					    initialStream.close();
					}
				}
			}
			
			if(xslt.getName().equalsIgnoreCase("images")) {
				if(xslt.listFiles().length > 0) {
					for (File images : xslt.listFiles()) {
						InputStream initialStream = new FileInputStream(images);
					    byte[] buffer = new byte[initialStream.available()];
					    initialStream.read(buffer);
					 
					    File styleFile = new File(targetFile.getParentFile().getAbsolutePath() + File.separator + images.getName());
					    OutputStream outStream = new FileOutputStream(styleFile);
					    outStream.write(buffer);
					    outStream.close();
					    initialStream.close();
					}
				} else {
					///TODO copy default files
					copyDefaultImagesToFolder(targetFile);
				}
			}
		}
		
		if (pdf == null || (!new File(pdf).exists())) {
			try {
				pdf = targetFile.getParent() +"/campaign_pdf_stylesheet.xml";
				copyResource(ResourceContent.class.getResourceAsStream("/reports/campaign/campaign_pdf_stylesheet.xml"),pdf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (html == null || (!new File(html).exists())) {
			try {
				html = targetFile.getParent() +"/campaign_html_stylesheet.xml";
				copyResource(ResourceContent.class.getResourceAsStream("/reports/campaign/campaign_html_stylesheet.xml"),html);
				copyResource(ResourceContent.class.getResourceAsStream("/reports/campaign/report.css"),targetFile.getParent() +"/report.css");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (fop == null || target == null || pdf == null || html == null) { return; }		
		
		//HTML reports
		String path = targetFile.getParentFile().getAbsolutePath();
		Transformer transformer = null;
		TransformerFactory tFactory = TransformerFactory.newInstance();
		transformer = tFactory.newTransformer(new StreamSource(html));
	    transformer.transform(new StreamSource(target), new StreamResult(path + File.separator + ATS_REPORT + ".html"));
		
		try {
			ProcessBuilder ps= new ProcessBuilder("java","-cp",fop,"org.apache.fop.cli.Main","-xml",target,"-xsl",pdf,"-pdf",targetFile.getParentFile().getAbsolutePath() + File.separator + ATS_REPORT + ".pdf");
			ps.redirectErrorStream(true);

			Process pr = ps.start();  

			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
			    System.out.println(line);
			}
			pr.waitFor();
			System.out.println("ok!");

			in.close();
			System.exit(0);
		} catch (Throwable t)
          {
            t.printStackTrace();
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
	
	public static void copyDefaultImagesToFolder(File targetFile) throws IOException {
		HashMap<String, BufferedImage> map = new HashMap<String, BufferedImage>();
		
		BufferedImage logoImg = ImageIO.read(ResourceContent.class.getResource("/reports/images/logo.png"));
		BufferedImage trueImg = ImageIO.read(ResourceContent.class.getResource("/reports/images/true.png"));
		BufferedImage falseImg = ImageIO.read(ResourceContent.class.getResource("/reports/images/false.png"));
		BufferedImage warningImg = ImageIO.read(ResourceContent.class.getResource("/reports/images/warning.png"));
		
		map.put("logo.png", logoImg);
		map.put("true.png", trueImg);
		map.put("false.png", falseImg);
		map.put("warning.png", warningImg);
		
		for(Map.Entry<String, BufferedImage> entry : map.entrySet()) {
		    String key = entry.getKey();
		    BufferedImage value = entry.getValue();

		    String path = targetFile.getParentFile().getAbsolutePath() + File.separator + key;
			File tmpFile = new File(path);
			ImageIO.write(value, "png", tmpFile);
		}
	}	
	
	public static String generateReportXml(String xmlPath, String outputFolder, File projectFolder, String details) throws IOException, ParserConfigurationException, SAXException{
		
		final File f = new File(outputFolder + File.separator + ATS_REPORT + ".xml");
		
		int det = Integer.parseInt(details);
        f.setWritable(true);
        f.setReadable(true);
        
        final FileWriter fw = new FileWriter(f);
        fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report actions=\"" +  (det > 1) + "\" details=\"" + (det > 2) + "\">");
        
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
        			final String content = new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8);
        			final StringBuilder builder = new StringBuilder("<tests>");
        			
        			builder.append(content.replaceAll(patternXML, ""));
        			builder.append("</tests>");
        			
        			fw.write(builder.toString());
        		}
    		}
        	fw.write("</suite>");
        } 
        
        fw.write("</report>");
	    fw.close();
	    
        return f.getAbsolutePath();
	}
}