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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.Icon;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

import com.ats.executor.ActionStatus;
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

	public static String getWindowsBuildVersion(){

		String buildNumber = "";
		try {
			final Process p = Runtime.getRuntime().exec("wmic os get BuildNumber");
			final BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = output.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0 && !("BuildNumber".equals(line))){
					buildNumber = line;
					break;
				}
			}
			output.close();
			p.destroy();
			
		} catch (IOException e) {
		}

		return buildNumber;
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
}