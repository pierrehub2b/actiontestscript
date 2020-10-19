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

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptReportGenerator {	
	
	public static void main(String[] args) throws TransformerException, InterruptedException, IOException {
		
		String target = null;
		String fop = null;
		String html = null;
		String pdf = null;
		
		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if(string.startsWith("--") && i+1 < args.length) {
				switch (string.substring(2)) {
					case "target":
						target = args[i+1];
						break;
					case "fop":
						fop = args[i+1];
						break;
				}
			}
		}
			
		if(target == null) return;
		File targetFile = new File(target);
		
		File projectFolder = null;
		File currentFolder = targetFile;
		
		while(projectFolder == null) {
			currentFolder = currentFolder.getParentFile();
			File[] tmpFiles = currentFolder.listFiles();
			for (File f : tmpFiles) {
				if(f.getName().equalsIgnoreCase("src")) {
					projectFolder = currentFolder;
				}
			}
		}
		File xsltFolder = new File(projectFolder.getAbsolutePath() + "/src/assets/resources/xslt");
		
		for (File xslt : xsltFolder.listFiles()) {
			if(xslt.getName().equalsIgnoreCase("script")) {
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
					//copyDefaultImagesToFolder(targetFile);
				}
			}
		}

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
		
		if (pdf == null || (!new File(pdf).exists())) {
			try {
				pdf = targetFile.getParentFile().getParent() +"\\script_pdf_stylesheet.xml";
				copyResource(ResourceContent.class.getResourceAsStream("/reports/script/script_pdf_stylesheet.xml"),pdf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (html == null || (!new File(html).exists())) {
			try {
				html = targetFile.getParentFile().getParent() +"\\script_html_stylesheet.xml";
				copyResource(ResourceContent.class.getResourceAsStream("/reports/script/script_html_stylesheet.xml"),html);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (fop == null || target == null || pdf == null || html == null) { return; }		
		
		//HTML reports
		String path = targetFile.getParentFile().getParentFile().getAbsolutePath();
		Transformer transformer = null;
		TransformerFactory tFactory = TransformerFactory.newInstance();
		transformer = tFactory.newTransformer(new StreamSource(html));
	    transformer.transform(new StreamSource(target), new StreamResult(path + File.separator + targetFile.getParentFile().getName().replace("_xml", "") + ".html"));
		
		try {
			ProcessBuilder ps= new ProcessBuilder("java","-cp",fop,"org.apache.fop.cli.Main","-xml",target,"-xsl",pdf,"-pdf",targetFile.getParentFile().getParentFile().getAbsolutePath() + File.separator + targetFile.getParentFile().getName().replace("_xml", "") + ".pdf");
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
}