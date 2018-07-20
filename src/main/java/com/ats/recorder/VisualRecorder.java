package com.ats.recorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;
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

import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.script.ProjectData;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.Action;
import com.ats.tools.Utils;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;

public class VisualRecorder {

	private Channel channel;
	private String outputPath;
	private ScriptHeader scriptHeader;

	private int visualQuality = 3;
	private boolean xml = false;

	private Path xmlFolderPath;

	public VisualRecorder(ScriptHeader header, ProjectData project, int quality, boolean xml) {

		Path output = project.getReportFolder().resolve(header.getPackagePath());
		output.toFile().mkdirs();

		initAndStart(output, header, quality, xml);
	}

	public VisualRecorder(File outputFolder, ScriptHeader header, int quality, boolean xml) {

		Path output = outputFolder.toPath();
		initAndStart(output, header, quality, xml);
	}

	//--------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------

	private void initAndStart(Path output, ScriptHeader header, int quality, boolean xml) {
		this.outputPath = output.toFile().getAbsolutePath();
		this.scriptHeader = header;
		this.xml = xml;

		if(quality > 0) {
			this.visualQuality = quality;
		}
	}

	private static void saveImageFile(Path folder, String fileName, byte[] data, TestBound bound) {

		InputStream in = new ByteArrayInputStream(data);
		try {
			BufferedImage buffImage = ImageIO.read(in);

			Graphics2D g2d = buffImage.createGraphics();
			g2d.setColor(Color.MAGENTA);
			g2d.setStroke(new BasicStroke(3));
			g2d.drawRect(bound.getX().intValue()-6, bound.getY().intValue()-7, bound.getWidth().intValue(), bound.getHeight().intValue());
			g2d.dispose();

			ImageIO.write(buffImage, "png", folder.resolve(fileName).toFile());

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------

	public void setChannel(Channel channel) {
		if(this.channel == null) {
			channel.startVisualRecord(outputPath, scriptHeader, visualQuality);
		}
		this.channel = channel;
	}

	public void terminate() {
		if(channel != null) {
			channel.stopVisualRecord();

			if(xml) {

				Path output = Paths.get(outputPath);

				File atsvFile = output.resolve(scriptHeader.getQualifiedName() + ".atsv").toFile();
				if(atsvFile.exists()) {

					ArrayList<VisualImage> imagesList = new ArrayList<VisualImage>();

					File xmlFolder = output.resolve(scriptHeader.getQualifiedName() + "_xml").toFile();
					try {
						Utils.deleteRecursive(xmlFolder);
					} catch (FileNotFoundException e) {}

					xmlFolder.mkdirs();
					xmlFolderPath = xmlFolder.toPath();

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = null;
					try {
						builder = factory.newDocumentBuilder();
					} catch (ParserConfigurationException e2) {
					}

					Document document= builder.newDocument();

					FileInputStream fis = null;
					try {

						fis = new FileInputStream(atsvFile);
						AMF3Deserializer amf3 = new AMF3Deserializer(fis);

						Element atsRoot = document.createElement("ats");
						document.appendChild(atsRoot);

						//------------------------------------------------------------------------------------------------------
						// script header
						//------------------------------------------------------------------------------------------------------

						VisualReport report = (VisualReport) amf3.readObject();

						Element script = document.createElement("script");
						atsRoot.appendChild(script);

						script.setAttribute("id", report.getId());					
						script.setAttribute("name", report.getName());

						Element description = document.createElement("description");
						description.setTextContent(report.getDescription());
						script.appendChild(description);

						Element author = document.createElement("author");
						author.setTextContent(report.getAuthor());
						script.appendChild(author);

						Element prerequisite = document.createElement("prerequisite");
						prerequisite.setTextContent(report.getPrerequisite());
						script.appendChild(prerequisite);

						Element executed = document.createElement("executed");
						executed.setTextContent(report.getExecuted());
						script.appendChild(executed);

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

							VisualAction va = (VisualAction) amf3.readObject();

							Element action = document.createElement("action");
							action.setAttribute("index", va.getIndex() + "");
							action.setAttribute("type", va.getType());
							actions.appendChild(action);

							Element line = document.createElement("line");
							line.setTextContent(va.getLine()+"");
							action.appendChild(line);

							Element timeLine = document.createElement("timeLine");
							timeLine.setTextContent(va.getTimeLine() + "");
							action.appendChild(timeLine);

							Element error = document.createElement("error");
							error.setTextContent(va.getError() + "");
							action.appendChild(error);
							
							Element passed = document.createElement("passed");
							passed.setTextContent((va.getError() == 0) + "");
							action.appendChild(passed);

							Element value = document.createElement("value");
							value.setTextContent(va.getValue());
							action.appendChild(value);

							Element data = document.createElement("data");
							data.setTextContent(va.getData());
							action.appendChild(data);

							Element image = document.createElement("img");
							image.setAttribute("src", va.getImageFileName());
							image.setAttribute("width", va.getChannelBound().getWidth().intValue() + "");
							image.setAttribute("height", va.getChannelBound().getHeight().intValue() + "");
							action.appendChild(image);

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

							va.addImage(imagesList);
						}

						amf3.close();
						imagesList.stream().parallel().forEach(vi -> saveImageFile(xmlFolderPath, vi.getName(), vi.getData(), vi.getBound()));

					} catch (IOException e1) {
						//e1.printStackTrace();
					} finally {
						try {
							if (fis != null)
								fis.close();
						} catch (IOException ex) {
							//ex.printStackTrace();
						}
					}


					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					try {
						Transformer transformer = transformerFactory.newTransformer();
						transformer.transform(new DOMSource(document), new StreamResult(xmlFolder.toPath().resolve("actions.xml").toFile()));
					} catch (TransformerConfigurationException e) {
						//e.printStackTrace();
					} catch (TransformerException e) {
						//e.printStackTrace();
					}
				}
			}
		}
	}

	public void createVisualAction(Action action) {
		channel.createVisualAction(action.getClass().getName(), action.getLine());
	}

	public void updateVisualImage() {
		channel.updateVisualImage();
	}

	public void updateVisualValue(String value) {
		channel.updateVisualValue(value);
	}

	public void updateVisualValue(String value, String data) {
		channel.updateVisualData(value, data);
	}

	public void updateVisualValue(String type, MouseDirection position) {
		channel.updateVisualPosition(type, position.getHorizontalPos(), position.getVerticalPos());
	}

	public void updateVisualStatus(int error) {
		channel.updateVisualStatus(error);
	}

	public void updateVisualElement(TestElement element) {
		channel.updateVisualElement(element);
	}

	public void updateVisualStatus(int error, String value, String data) {
		channel.updateVisualStatus(error, value, data);
	}
}