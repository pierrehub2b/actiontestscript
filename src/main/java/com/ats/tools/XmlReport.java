package com.ats.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ats.recorder.VisualAction;
import com.ats.recorder.VisualImage;
import com.ats.recorder.VisualReport;
import com.ats.tools.logger.IExecutionLogger;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

public class XmlReport {

	public static String REPORT_FILE = "actions.xml";
	
	public static void createReport(Path output, String qualifiedName, IExecutionLogger logger) {

		final File atsvFile = output.resolve(qualifiedName + ".atsv").toFile();

		if(atsvFile.exists()) {

			final File xmlFolder = output.resolve(qualifiedName + "_xml").toFile();
			logger.sendInfo("Create XML report -> ", xmlFolder.getAbsolutePath());

			final ArrayList<VisualImage> imagesList = new ArrayList<VisualImage>();
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			try {
				Utils.deleteRecursive(xmlFolder);
			} catch (FileNotFoundException e) {}

			xmlFolder.mkdirs();
			final Path xmlFolerPath = xmlFolder.toPath();

			try {

				final DocumentBuilder builder = factory.newDocumentBuilder();
				final Document document= builder.newDocument();

				FileInputStream fis = null;
				AMF3Deserializer amf3 = null;

				try {

					fis = new FileInputStream(atsvFile);
					amf3 = new AMF3Deserializer(fis);

					final Element atsRoot = document.createElement("ats");
					document.appendChild(atsRoot);

					//------------------------------------------------------------------------------------------------------
					// script header
					//------------------------------------------------------------------------------------------------------

					final VisualReport report = (VisualReport) amf3.readObject();
					final Element script = document.createElement("script");

					atsRoot.appendChild(script);

					script.setAttribute("testId", report.getId());					
					script.setAttribute("testName", report.getName());
					
					script.setAttribute("cpuSpeed", report.getCpuSpeed() + "");
					script.setAttribute("cpuCount", report.getCpuCount() + "");
					script.setAttribute("totalMemory", report.getTotalMemory() + "");		
					script.setAttribute("osInfo", report.getOsInfo());	

					Element description = document.createElement("description");
					description.setTextContent(report.getDescription());
					script.appendChild(description);

					Element author = document.createElement("author");
					author.setTextContent(report.getAuthor());
					script.appendChild(author);

					Element prerequisite = document.createElement("prerequisite");
					prerequisite.setTextContent(report.getPrerequisite());
					script.appendChild(prerequisite);

					Element started = document.createElement("started");
					started.setTextContent(report.getStarted());
					script.appendChild(started);

					Element groups = document.createElement("groups");
					groups.setTextContent(report.getGroups());
					script.appendChild(groups);

					Element quality = document.createElement("quality");
					quality.setTextContent(report.getQuality() + "");
					script.appendChild(quality);

					//------------------------------------------------------------------------------------------------------
					//------------------------------------------------------------------------------------------------------

					Element actions = document.createElement("actions");
					atsRoot.appendChild(actions);

					while(amf3.available() > 0) {

						final VisualAction va = (VisualAction) amf3.readObject();

						final Element action = document.createElement("action");
						action.setAttribute("index", va.getIndex() + "");
						action.setAttribute("type", va.getType());
						
						action.appendChild(document.createElement("line")).setTextContent(va.getLine() + "");
						action.appendChild(document.createElement("timeLine")).setTextContent(va.getTimeLine() + "");
						action.appendChild(document.createElement("cpu")).setTextContent(va.getCpu() + "");
						action.appendChild(document.createElement("ram")).setTextContent(va.getRam() + "");
						action.appendChild(document.createElement("netReceived")).setTextContent(va.getNetReceived() + "");
						action.appendChild(document.createElement("netSent")).setTextContent(va.getNetSent() + "");
						action.appendChild(document.createElement("error")).setTextContent(va.getError() + "");
						action.appendChild(document.createElement("duration")).setTextContent(va.getDuration() + "");
						action.appendChild(document.createElement("passed")).setTextContent((va.getError() == 0) + "");
						action.appendChild(document.createElement("value")).setTextContent(va.getValue());
						action.appendChild(document.createElement("data")).setTextContent(va.getData());

						Element elem = document.createElement("img");
						elem.setAttribute("src", va.getImageFileName());
						elem.setAttribute("width", va.getChannelBound().getWidth().intValue() + "");
						elem.setAttribute("height", va.getChannelBound().getHeight().intValue() + "");
						action.appendChild(elem);

						Element channel = document.createElement("channel");
						channel.setAttribute("name", va.getChannelName());

						Element channelBound = document.createElement("bound");
						Element channelX = document.createElement("x");
						channelX.setTextContent(va.getChannelBound().getX().intValue() + "");
						channelBound.appendChild(channelX);

						Element channelY = document.createElement("y");
						channelY.setTextContent(va.getChannelBound().getY().intValue() + "");
						channelBound.appendChild(channelY);

						Element channelWidth = document.createElement("width");
						channelWidth.setTextContent(va.getChannelBound().getWidth().intValue() + "");
						channelBound.appendChild(channelWidth);

						Element channelHeight = document.createElement("height");
						channelHeight.setTextContent(va.getChannelBound().getHeight().intValue() + "");
						channelBound.appendChild(channelHeight);

						channel.appendChild(channelBound);
						action.appendChild(channel);

						if(va.getElement() != null) {

							Element element = document.createElement("element");
							element.setAttribute("tag", va.getElement().getTag());

							Element criterias = document.createElement("criterias");
							criterias.setTextContent(va.getElement().getCriterias());
							element.appendChild(criterias);

							Element foundElements = document.createElement("foundElements");
							foundElements.setTextContent(va.getElement().getFoundElements() + "");
							element.appendChild(foundElements);

							Element searchDuration = document.createElement("searchDuration");
							searchDuration.setTextContent(va.getElement().getSearchDuration() + "");
							element.appendChild(searchDuration);

							Element elementBound = document.createElement("bound");
							Element elementX = document.createElement("x");
							elementX.setTextContent(va.getElement().getBound().getX().intValue() + "");
							elementBound.appendChild(elementX);

							Element elementY = document.createElement("y");
							elementY.setTextContent(va.getElement().getBound().getY().intValue() + "");
							elementBound.appendChild(elementY);

							Element elementWidth = document.createElement("width");
							elementWidth.setTextContent(va.getElement().getBound().getWidth().intValue() + "");
							elementBound.appendChild(elementWidth);

							Element elementHeight = document.createElement("height");
							elementHeight.setTextContent(va.getElement().getBound().getHeight().intValue() + "");
							elementBound.appendChild(elementHeight);

							element.appendChild(elementBound);
							action.appendChild(element);
						}
						
						actions.appendChild(action);

						va.addImage(xmlFolerPath, imagesList);
					}

				} catch (FileNotFoundException e0) {
					logger.sendError("XML report stream error ->", e0.getMessage());
				} catch (IOException e1) {
					logger.sendError("XML report file error ->", e1.getMessage());
				} catch (Exception e2) {
					logger.sendError("XML report exception ->", e2.getMessage());
				}finally {
					try {
						if(fis != null) {
							fis.close();
						}
					} catch (IOException e) {
						logger.sendError("XML report close stream error ->", e.getMessage());
					}
				}

				imagesList.parallelStream().forEach(im -> im.save());

				try {

					final Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(xmlFolder.toPath().resolve(REPORT_FILE).toFile())));

				} catch (TransformerConfigurationException e2) {
					logger.sendError("XML report config error ->", e2.getMessage());
				} catch (TransformerException e3) {
					logger.sendError("XML report transform error ->", e3.getMessage());
				} catch (FileNotFoundException e4) {
					logger.sendError("XML report write file error ->", e4.getMessage());
				}

			} catch (ParserConfigurationException e4) {
				logger.sendError("XML report parser error ->", e4.getMessage());
			}
			
			logger.sendInfo("XML report generated in -> ", xmlFolder.getAbsolutePath());
		}
	}
}