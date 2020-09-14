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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class CampaignReportGenerator {
	
    public static String patternDOCTYPE = "<!DOCTYPE[^<>]*(?:<![^<>]*>[^<>]*)*>";
    public static String patternXML = "\\<\\?xml[^<>]*(?:<![^<>]*>[^<>]*)*>";
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, TransformerException, IOException, InterruptedException {
		String fopDir = System.getProperty("fop", null);
		String xmlPath = System.getProperty("xml", null);
		String xslPathHtml = System.getProperty("xslHtml", null);
		String xslPathPdf = System.getProperty("xslPdf", null);
		String pdfPath = System.getProperty("pdf", null);
		String name = System.getProperty("name", null);
		String actions = System.getProperty("actions", null);
		String details =  System.getProperty("details", null);
		
		String basePath = new File(pdfPath).getParentFile().getAbsolutePath();
		
		if (fopDir == null || !(new File(fopDir).exists())) {
			Map<String, String> map = System.getenv();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				Pattern pattern = Pattern.compile("fop-[\\d].[\\d]");
				Matcher matcher = pattern.matcher(entry.getValue().toLowerCase());
				if (entry.getKey().toLowerCase().contains("fop") && matcher.find()) {
					fopDir = entry.getValue();
				}
			}
		}

		if (xslPathPdf == null || !(new File(xslPathPdf).exists())) {
			try {
				final String styleSheet = Resources.toString(ResourceContent.class.getResource("/reports/campaign/campaign_pdf_stylesheet.xml"), Charsets.UTF_8);
				xslPathPdf = createEmptyStylesheet(styleSheet,basePath);
				copyImageToTempFolder("false",basePath);
				copyImageToTempFolder("false",basePath);
				copyImageToTempFolder("true",basePath);
				copyImageToTempFolder("warning",basePath);
				copyImageToTempFolder("agilitest",basePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (fopDir == null || xmlPath == null || xslPathPdf == null || pdfPath == null) { return; }
		
		String xmlUrl = generateReportXml(xmlPath, basePath, actions, details);
		
		//HTML reports
		try {
			Transformer transformer = null;
			TransformerFactory tFactory = TransformerFactory.newInstance();
			if(xslPathHtml == null || !(new File(xslPathHtml).exists())) {
				String styleSheetHtmlDetail = Resources.toString(ResourceContent.class.getResource("/reports/campaign/campaign_html_stylesheet.xml"), Charsets.UTF_8);
				String styleSheetHtml = createEmptyStylesheetHtml(styleSheetHtmlDetail,basePath);
				transformer = tFactory.newTransformer(new StreamSource(styleSheetHtml));
			} else {
				transformer = tFactory.newTransformer(new StreamSource(xslPathHtml));
			}
		    transformer.transform(new StreamSource(xmlUrl), new StreamResult(basePath + File.separator + name + ".html"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			String command = String.format("java -cp %s;%s org.apache.fop.cli.Main -xml %s -xsl %s -pdf %s",
					fopDir + "\\build\\fop.jar", fopDir + "\\lib\\*", xmlUrl, xslPathPdf, pdfPath);
			Process p = Runtime.getRuntime().exec("cmd /c " + command);
			/*System.out.println("Waiting for batch file for process " + name + "...");
		    p.waitFor();
		    System.out.println("Batch file done for " + name);*/
		} catch (Throwable t)
          {
            t.printStackTrace();
          }
		
	}
	
	public static void copyImageToTempFolder(String name, String basePath) throws IOException {
		byte[] aByteArray = Resources.toByteArray(ResourceContent.class.getResource("/reports/images/" + name + ".png"));
		final File file = new File(basePath + File.separator + name + ".png");
        final FileOutputStream fileOut = new FileOutputStream(file );
        fileOut.write(aByteArray);
        fileOut.flush();
        fileOut.close();
	}
	
	public static String createEmptyStylesheet(String xmlSource,String basePath) throws IOException {	
		String path = basePath + File.separator + "campaign_pdf_stylesheet.xml";
		File f = new File(path);
        f.setWritable(true);
        f.setReadable(true);
        FileWriter fw = new FileWriter(f);
        fw.write(xmlSource);
	    fw.close();

        return f.getAbsolutePath();
	}
	
	public static String createEmptyStylesheetHtml(String xmlSource,String basePath) throws IOException {	
		String path = basePath + File.separator + "campaign_html_stylesheet.xml";
		File f = new File(path);
        f.setWritable(true);
        f.setReadable(true);
        FileWriter fw = new FileWriter(f);
        fw.write(xmlSource);
	    fw.close();

        return f.getAbsolutePath();
	}
	
	public static String generateReportXml(String xmlPath, String basePath, String actions, String details) throws IOException, ParserConfigurationException, SAXException{
		File f = new File(basePath + File.separator + "report.xml");
        f.setWritable(true);
        f.setReadable(true);
        FileWriter fw = new FileWriter(f);
        fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report actions=\"" +  actions + "\" details=\"" + details + "\">");
        
        String[] paths = xmlPath.split(",");
        String currentScript = "";
        String currentSuite = "";
        for (int j = 0; j<paths.length;j++) {
        	String[] scripts = paths[j].split(";");
        	for (int i = 0; i < scripts.length; i++) {
        		File currentFile = new File(scripts[i]);
            	String content = Files.asCharSource(currentFile, Charsets.UTF_8).read();
				if(i != 0) {
					currentScript = currentFile.getParentFile().getName();
				} else {
					currentSuite = currentFile.getName().replace(".xml", "");
				}
    			fw.write(content.replaceAll(patternDOCTYPE, "").replaceAll(patternXML, "").replaceAll("<action ", "<action scriptName=\"" + currentScript + "\" suiteName=\"" + currentSuite + "\" ").replace("<script ", "<script suiteName=\"" + currentSuite + "\" "));
    			if(i == 0) { fw.write("<tests>"); }
    		}
        	fw.write("</tests>");
        } 
        fw.write("</report>");
	    fw.close();
        return f.getAbsolutePath();
	}
}