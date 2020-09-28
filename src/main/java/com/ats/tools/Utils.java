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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.Icon;

import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.generator.ATS;
import com.ats.generator.variables.parameter.ParameterDataFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class Utils {

	public static String unescapeAts(String data) {
		return data.replaceAll("&sp;", " ").replaceAll("&co;", ",").replaceAll("&eq;", "=").replaceAll("&rb;", "]").replaceAll("&lb;", "[");
	}

	public static int string2Int(String value){
		return string2Int(value, 0);
	}

	public static int string2Int(String value, int defaultValue){
		try {
			return Integer.parseInt(value.replaceAll("\\s", ""));
		} catch (NullPointerException | NumberFormatException e) {
			return defaultValue;
		}
	}

	public static long string2Long(String value){
		try {
			return Long.parseLong(value.replaceAll("\\s", ""));
		} catch (NullPointerException | NumberFormatException e) {
			return 0L;
		}
	}

	public static double string2Double(String value){
		try {
			return Double.parseDouble(value.replaceAll("\\s", ""));
		} catch (NullPointerException | NumberFormatException e) {
			return 0D;
		}
	}

	public static String truncateString(String value, int length)
	{
		if (value != null && value.length() > length)
			value = value.substring(0, length);
		return value;
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

	public static String removeExtension(String s) {

		String separator = System.getProperty("file.separator");
		String filename;

		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}

		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;

		return filename.substring(0, extensionIndex);
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

	public static byte[] loadImage(URL url) {

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try (InputStream inputStream = url.openStream()) {

			int n = 0;
			byte [] buffer = new byte[ 1024 ];

			while (-1 != (n = inputStream.read(buffer))) {
				output.write(buffer, 0, n);
			}

			return output.toByteArray();

		} catch (IOException e) {

		}

		return null;
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  JSON CSV utils
	//-------------------------------------------------------------------------------------------------------------------------------------------

	public static ParameterDataFile loadData(String url) throws MalformedURLException{
		return loadData(new URL(url));
	}

	public static ParameterDataFile loadData(URL url){
		return new ParameterDataFile(url);
	}

	//-------------------------------------------------------------------------------------------------------------------------------------------
	//  browser start page
	//-------------------------------------------------------------------------------------------------------------------------------------------

	public static byte[] getAtsBrowserContent(
			String titleUid,
			String browserName,
			String browserPath,
			String browserVersion, 
			String driverVersion, 
			TestBound testBound,
			int actionWait,
			int propertyWait,
			int maxtry,
			int maxTryProperty,
			int scriptTimeout,
			int pageLoadTimeout,
			int watchdog, 
			DesktopDriver desktopDriver, 
			String profilePath) {

		if(driverVersion == null) {
			driverVersion = "--";
		}

		if(browserVersion == null) {
			browserVersion = "--";
		}

		final StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");

		htmlContent.append(ResourceContent.getPageStyle());

		htmlContent.append("<title>");
		htmlContent.append(titleUid);
		htmlContent.append("</title></head><body bgcolor=\"#f2f2f2\"><div><div id=\"header\"><div class=\"clearfix\">");
		htmlContent.append("ActionTestScript (ver. ");
		htmlContent.append(ATS.VERSION);
		htmlContent.append(")");
		htmlContent.append("</div></div><div id=\"content-wrapper\"><div class=\"site\"><div class=\"article js-hide-during-search\"><a href=\"https://www.actiontestscript.com\"><img src=\"data:image/png;base64, ");

		htmlContent.append(ResourceContent.getAtsLogo());

		htmlContent.append("\" alt=\"ActionTestScript\"/></a><div class=\"article-body content-body wikistyle markdown-format\"><div class=\"intro\" style=\"margin-left:30px\">");

		htmlContent.append("<p><strong>ActionTestScript version : </strong>");
		htmlContent.append(ATS.VERSION);

		htmlContent.append("<br><strong>Browser driver version : </strong>");
		htmlContent.append(driverVersion);

		htmlContent.append("<br><strong>Desktop driver version : </strong>");
		htmlContent.append(desktopDriver.getDriverVersion());

		htmlContent.append("<br><strong>Search element max try : </strong>");
		htmlContent.append(maxtry);

		htmlContent.append("<br><strong>Get property max try : </strong>");
		htmlContent.append(maxTryProperty);

		htmlContent.append("<br>JavaScript execution time out : ");
		htmlContent.append(scriptTimeout);
		htmlContent.append(" s");

		htmlContent.append("<br>Page load time out : ");
		htmlContent.append(pageLoadTimeout);
		htmlContent.append(" s");

		htmlContent.append("<br>Action execution watchdog : ");
		htmlContent.append(watchdog);
		htmlContent.append(" s");

		htmlContent.append("</p></div><div class=\"alert note\" style=\"margin-left:30px;min-width: 360px;display: inline-block\"><p>");
		htmlContent.append("<strong>Browser : </strong>");
		htmlContent.append("<br><strong>  - Name : </strong>");
		htmlContent.append(browserName);
		htmlContent.append("<br><strong>  - Version : </strong>");
		htmlContent.append(browserVersion);

		if(browserPath != null) {
			htmlContent.append("<br><strong>  - Binary path : </strong>");
			htmlContent.append(browserPath);
		}

		if(profilePath != null) {
			htmlContent.append("<br><strong>  - Profile path : </strong>");
			htmlContent.append(profilePath);
		}

		htmlContent.append("<br><strong>  - Start position : </strong>");
		htmlContent.append(testBound.getX().intValue());
		htmlContent.append(" x ");
		htmlContent.append(testBound.getY().intValue());

		htmlContent.append("<br><strong>  - Start size : </strong>");
		htmlContent.append(testBound.getWidth().intValue());
		htmlContent.append(" x ");
		htmlContent.append(testBound.getHeight().intValue());

		htmlContent.append("<br><strong>  - Wait after action : </strong>");
		htmlContent.append(actionWait);
		htmlContent.append(" ms");

		htmlContent.append("<br><strong>  - Double check property : </strong>");
		htmlContent.append(propertyWait);
		htmlContent.append(" ms");		

		htmlContent.append("</p></div><div class=\"alert warning\" style=\"margin-left:30px;min-width: 360px;display: inline-block\"><p>");
		htmlContent.append("<strong>Operating System : </strong>");
		htmlContent.append("<br><strong>  - Machine name : </strong>");
		htmlContent.append(desktopDriver.getMachineName());
		htmlContent.append("<br><strong>  - System name : </strong>");
		htmlContent.append(desktopDriver.getOsName());
		htmlContent.append("<br><strong>  - System version : </strong>");
		htmlContent.append(desktopDriver.getOsVersion());
		htmlContent.append("<br><strong>  - Build version : </strong>");
		htmlContent.append(desktopDriver.getOsBuildVersion());
		htmlContent.append("<br><strong>  - Country code : </strong>");
		htmlContent.append(desktopDriver.getCountryCode());
		htmlContent.append("<br><strong>  - DotNET version : </strong>");
		htmlContent.append(desktopDriver.getDotNetVersion());

		htmlContent.append("<br><strong>  - Resolution : </strong>");
		htmlContent.append(desktopDriver.getScreenResolution());

		htmlContent.append("<br><strong>  - Processor name : </strong>");
		htmlContent.append(desktopDriver.getCpuName());
		htmlContent.append("<br><strong>  - Processor socket : </strong>");
		htmlContent.append(desktopDriver.getCpuSocket());
		htmlContent.append("<br><strong>  - Processor architecture : </strong>");
		htmlContent.append(desktopDriver.getCpuArchitecture());
		htmlContent.append("<br><strong>  - Processor max speed : </strong>");
		htmlContent.append(desktopDriver.getCpuMaxClock());
		htmlContent.append("<br><strong>  - Processor cores : </strong>");
		htmlContent.append(desktopDriver.getCpuCores());

		htmlContent.append("<br><strong>  - Current drive letter : </strong>");
		htmlContent.append(desktopDriver.getDriveLetter());

		htmlContent.append("<br><strong>  - Disk total size : </strong>");
		htmlContent.append(desktopDriver.getDiskTotalSize());

		htmlContent.append("<br><strong>  - Disk free space : </strong>");
		htmlContent.append(desktopDriver.getDiskFreeSpace());		

		htmlContent.append("</p></div></div></body></html>");

		return htmlContent.toString().getBytes();
	}
}