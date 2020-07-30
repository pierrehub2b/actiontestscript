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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class TestPdfGenerator {

	public static void main(String[] args) {
		String fopDir = System.getProperty("fop", null);
		String xmlPath = System.getProperty("xml", null);
		String xslPath = System.getProperty("xsl", null);
		String pdfPath = System.getProperty("pdf", null);
		
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

		if (xslPath == null || !(new File(xslPath).exists())) {
			try {
				final String styleSheet = Resources.toString(ResourceContent.class.getResource("/Reports/test_pdf_stylesheet.xml"), Charsets.UTF_8);
				xslPath = createEmptyStylesheet(styleSheet,xmlPath);
				copyImageToTempFolder("false",xmlPath);
				copyImageToTempFolder("true",xmlPath);
				copyImageToTempFolder("warning",xmlPath);
				copyImageToTempFolder("agilitest",xmlPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (fopDir == null || xmlPath == null || xslPath == null || pdfPath == null) { return; }
		String command = String.format("java -cp %s;%s org.apache.fop.cli.Main -xml %s -xsl %s -pdf %s",
				fopDir + "\\build\\fop.jar", fopDir + "\\lib\\*", xmlPath, xslPath, pdfPath);
		try {
			Runtime.getRuntime().exec("cmd /c " + command);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void copyImageToTempFolder(String name, String xmlPath) throws IOException {
		File f = new File(xmlPath);
		String path = f.getParentFile().getAbsolutePath();
		byte[] aByteArray = Resources.toByteArray(ResourceContent.class.getResource("/Reports/" + name + ".png"));
		final File file = new File(path + File.separator + name + ".png");
        final FileOutputStream fileOut = new FileOutputStream(file );
        fileOut.write(aByteArray);
        fileOut.flush();
        fileOut.close();
	}
	
	public static String createEmptyStylesheet(String xmlSource,String xmlPath) throws IOException {
		File f = new File(xmlPath);		
		String path = f.getParentFile().getAbsolutePath() + File.separator + "test_pdf_stylesheet.xml";
		File file = new File(path);
        file.setWritable(true);
        file.setReadable(true);
        FileWriter fw = new FileWriter(file);
        fw.write(xmlSource);
	    fw.close();

        return file.getAbsolutePath();
	}	
}