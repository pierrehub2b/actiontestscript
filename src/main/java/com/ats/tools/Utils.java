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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ats.executor.ActionStatus;
import com.ats.recorder.VisualAction;
import com.ats.recorder.VisualImage;
import com.ats.recorder.VisualReport;
import com.ats.tools.logger.IExecutionLogger;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class Utils {

	public static String unescapeAts(String data) {
		return data.replaceAll("&sp;", " ").replaceAll("&co;", ",").replaceAll("&eq;", "=").replaceAll("&rb;", "]").replaceAll("&lb;", "[");
	}

	public static int string2Int(String value){
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static JsonArray string2JsonArray(String value){
		final char[] letters = value.toCharArray();
		JsonArray array = new JsonArray();
		for (final char ch : letters) {
			array.add(new JsonPrimitive((int)ch));
		}
		return array;
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  Files utils
	//-------------------------------------------------------------------------------------------------------------------------------------------

	private static final ExecutorService DELETE_SERVICE = Executors.newSingleThreadExecutor();
	private static final String ATS_DRIVER_FOLDER = ".AtsDrivers";

	public static File createDriverFolder(String name) {

		final File worksDirectory = new File(
				(new StringBuilder(System.getProperty("user.home")).
						append(File.separator).
						append(ATS_DRIVER_FOLDER).
						append(File.separator).
						append(name).
						append(File.separator).
						append(UUID.randomUUID().toString())).toString());

		worksDirectory.mkdirs();

		return worksDirectory;
	}

	public static void clearDriverFolder(String name) {

		final File driverProfileDir = new File(
				new StringBuilder(System.getProperty("user.home")).
				append(File.separator).
				append(ATS_DRIVER_FOLDER).
				append(File.separator).
				append(name).toString());

		if(driverProfileDir.exists()) {
			Arrays.stream(driverProfileDir.listFiles(File::isDirectory)).parallel().forEach(f -> deleteFile(f));
		}
	}

	private static void deleteFile(final File file) {
		if (file != null) {
			DELETE_SERVICE.submit(new Runnable() {
				@Override
				public void run() {
					try {
						deleteRecursive(file);
					} catch (FileNotFoundException e) {}
				}
			});
		}
	}

	public static boolean deleteRecursive(File path) throws FileNotFoundException{
		if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()){
			for (final File f : path.listFiles()){
				ret = ret && Utils.deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}

	public static void deleteRecursiveFiles(File f) throws FileNotFoundException{
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		if (f.isDirectory()){
			for (final File f0 : f.listFiles()){
				if(f0.isDirectory()) {
					deleteRecursiveFiles(f0);
					if(f0.listFiles().length == 0) {
						f0.delete();
					}
				}else if(f0.isFile()) {
					f0.delete();
				}
			}
		}
	}	

	public static void deleteRecursiveJavaFiles(File f) throws FileNotFoundException{
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		if (f.isDirectory()){
			for (final File f0 : f.listFiles()){
				if(f0.isDirectory()) {
					deleteRecursiveJavaFiles(f0);
					if(f0.listFiles().length == 0) {
						f0.delete();
					}
				}else if(f0.isFile() && f0.getName().toLowerCase().endsWith(".java")) {
					f0.delete();
				}
			}
		}
	}	

	public static void copyDir(String src, String dest, boolean overwrite) {
		try {
			Files.walk(Paths.get(src)).forEach(a -> {
				Path b = Paths.get(dest, a.toString().substring(src.length()));
				try {
					if (!a.toString().equals(src))
						Files.copy(a, b, overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{});
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  Files utils
	//-------------------------------------------------------------------------------------------------------------------------------------------

	public static boolean checkUrl(ActionStatus status, String urlPath){

		URL url = null;

		try {
			url = new URL(urlPath);
		}catch(MalformedURLException e) {
			status.setPassed(false);
			status.setCode(ActionStatus.MALFORMED_GOTO_URL);
			status.setData(urlPath);
			return false;
		}

		int responseCode;
		try {
			if(urlPath.startsWith("https")) {
				HttpsURLConnection con =	(HttpsURLConnection)url.openConnection();
				con.setRequestMethod("HEAD");
				responseCode = con.getResponseCode();
			}else {
				HttpURLConnection con =	(HttpURLConnection)url.openConnection();
				con.setRequestMethod("HEAD");
				responseCode = con.getResponseCode();
			}
		}catch (IOException e) {
			status.setPassed(false);
			status.setCode(ActionStatus.UNKNOWN_HOST_GOTO_URL);
			status.setData(e.getMessage());
			return false;
		}

		if(responseCode == HttpURLConnection.HTTP_OK) {
			return true;
		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.UNREACHABLE_GOTO_URL);
			status.setData(urlPath);
			return false;
		}
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  Image utils
	//-------------------------------------------------------------------------------------------------------------------------------------------

	public static byte[] iconToImage(Icon icon) {

		final BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();

		icon.paintIcon(null, g2d, 0, 0);
		g2d.dispose();

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			try {
				ImageIO.write(img, "png", ios);
			} finally {
				ios.close();
			}
			return baos.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  CSV utils
	//-------------------------------------------------------------------------------------------------------------------------------------------

	public static ArrayList<String[]> loadCsvData(String url) throws MalformedURLException, IOException{
		return loadCsvData(new URL(url));
	}

	public static ArrayList<String[]> loadCsvData(URL url) throws IOException{

		ArrayList<String[]> result = new ArrayList<String[]>();
		try {
			final Reader reader = new InputStreamReader(new BOMInputStream(url.openStream()), "UTF-8");
			final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withAllowMissingColumnNames());

			for (final CSVRecord record : parser) {
				String[] lineData = new String[record.size()];
				for (int i=0; i < record.size(); i++) {
					lineData[i] = record.get(i);
				}
				result.add(lineData);
			}
			parser.close();
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  XML report
	//-------------------------------------------------------------------------------------------------------------------------------------------

	public static void createXmlReport(Path output, String qualifiedName, IExecutionLogger logger) {

		final File atsvFile = output.resolve(qualifiedName + ".atsv").toFile();

		if(atsvFile.exists()) {

			final File xmlFolder = output.resolve(qualifiedName + "_xml").toFile();
			logger.sendInfo("Create XML report -> ", xmlFolder.getAbsolutePath());

			final ArrayList<VisualImage> imagesList = new ArrayList<VisualImage>();
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			try {
				deleteRecursive(xmlFolder);
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
					transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(xmlFolder.toPath().resolve("actions.xml").toFile())));

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