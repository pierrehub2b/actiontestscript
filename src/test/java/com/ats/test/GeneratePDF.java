package com.ats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.tools.TestPdfGenerator;
import com.ats.tools.SuitePdfGenerator;

public class GeneratePDF {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	public String suiteClass = "com.ats.tools.SuitePdfGenerator";
	public String testClass = "com.ats.tools.TestPdfGenerator";
	
	/*
	 * @Before public void setUp() { }
	 * 
	 * @After public void tearDown() { }
	 */

	@Test
	public void sendPdfCreationRequest() throws IOException, TransformerException {

		final String[] xmlKeys = new String[] {"exist", "notExist", "empty", "null"};
		final String[] xmlValues = new String[] {"C:\\Users\\Agilitest\\.agilitest\\work\\jenkins\\workspace\\test_azerty\\target\\surefire-reports\\icagilitest\\MyFirstScript_xml\\MyFirstScript_xml.xml", "C:\\", "", null};
		
		final String[] xslKeys = new String[] {"exist", "notExist", "empty", "null"};
		final String[] xslValues = new String[] {"C:\\Users\\Agilitest\\.agilitest\\work\\jenkins\\workspace\\test_azerty\\src\\assets\\resources\\xslt\\stylesheet.xml", "C:\\", "", null};
		
		final String[] pdfKeys = new String[] {"exist", "notExist", "empty", "null"};
		final String[] pdfValues = new String[] {"C:\\Users\\Agilitest\\.agilitest\\work\\jenkins\\workspace\\test_azerty\\target\\surefire-reports\\icagilitest\\MyFirstScript_xml\\\\MyFirstScript_xml.pdf", "azerty", "", null};

		final String[] nameKeys = new String[] {"exist", "notExist", "empty", "null"};
		final String[] nameValues = new String[] {"icagilitest", "azerty", "", null};
		
		final File folder = tempFolder.newFolder();
		for (int i = 0; i < xmlKeys.length; i++) {
			for (int j = 0; j < xslKeys.length; j++) {
				for (int j2 = 0; j2 < pdfKeys.length; j2++) {
					for (int k = 0; k < nameKeys.length; k++) {
						ArrayList<String> args = new ArrayList<String>();
						args.add("-Dxml="+xmlValues[i]);
						args.add("-Dxsl="+xslValues[j]);
						args.add("-Dpdf="+pdfValues[j2]);
						args.add("-Dname="+nameValues[k]);

						try 
						{
							TestPdfGenerator.main(args.toArray(new String[args.size()]));
							Assert.assertEquals("true", "true");
						}
						catch (Exception ex)
						{
						    Assert.fail(args.toString());
						}
					}
				}
			}
		}
		folder.deleteOnExit();
	}
	
	@Test
	public void testResportExist() throws IOException {
	}
}
