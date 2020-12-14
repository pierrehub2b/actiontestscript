package com.ats.tools.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SuitesReport {
	public String projectId;
	public String projectDescription;
	public SuitesReportItem[] suites;
	
	public SuitesReport(String id, SuitesReportItem suite) {
		this.projectId = id;
		this.suites = new SuitesReportItem[] {suite};
				
		final Path projectProperties = Paths.get(".atsProjectProperties");
		if(Files.exists(projectProperties)) {
		    try {
				final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			    final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				final Document doc = dBuilder.parse(projectProperties.toFile());
				
				final Element root = doc.getDocumentElement();
				final NodeList desc = root.getElementsByTagName("description");
				if(desc != null && desc.getLength() > 0) {
					this.projectDescription = desc.item(0).getTextContent();
				}
			} catch (SAXException | IOException | ParserConfigurationException e) {}
		}
	}
	
	public void add(SuitesReportItem suite) {
		this.suites = Stream.concat(Arrays.stream(this.suites), Arrays.stream(new SuitesReportItem[] {suite})).toArray(SuitesReportItem[]::new);
	}
}
