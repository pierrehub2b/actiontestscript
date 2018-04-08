package com.ats.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class Utils {

	public static String escapeAts(String data) {
		return data.replaceAll("&sp;", " ").replaceAll("&co;", ",").replaceAll("&eq;", "=").replaceAll("&rb;", "]").replaceAll("&lb;", "[");
	}

	public static int string2Int(String value){

		int result = 0;

		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {}

		return result;
	}

	public static JsonArray string2JsonArray(String value){
		JsonArray array = new JsonArray();
		char[] letters = value.toCharArray();
		for (char ch : letters) {
			array.add(new JsonPrimitive((int)ch));
		}
		return array;
	}

	public static boolean deleteRecursive(File path) throws FileNotFoundException{
		if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()){
			for (File f : path.listFiles()){
				ret = ret && Utils.deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}

	public static void deleteRecursiveJavaFiles(File f) throws FileNotFoundException{
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
		if (f.isDirectory()){
			for (File f0 : f.listFiles()){
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

		if(responseCode == HttpsURLConnection.HTTP_OK) {
			return true;
		}else {
			status.setPassed(false);
			status.setCode(ActionStatus.UNREACHABLE_GOTO_URL);
			status.setData(urlPath);
			return false;
		}
	}

	public static byte[] iconToImage(Icon icon) {

		BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
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
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = output.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0 && !("BuildNumber".equals(line))){
					buildNumber = line;
					break;
				}
			}
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
}