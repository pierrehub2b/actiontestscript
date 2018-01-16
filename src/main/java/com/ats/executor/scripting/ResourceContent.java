package com.ats.executor.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.io.ByteStreams;

public class ResourceContent {
	
	private static final ClassLoader classLoader = ResourceContent.class.getClassLoader();
	
	public static String getScript(String ressourceName){
		
		//ClassLoader jsContentLoader = ResourceContent.class.getClassLoader();
		InputStreamReader isr = new InputStreamReader(classLoader.getResourceAsStream("javascript/" + ressourceName + ".js"));
		
		BufferedReader in = new BufferedReader(isr);
		String line = null;

		StringBuilder responseData = new StringBuilder();
		try {
			while((line = in.readLine()) != null) {
			    responseData.append(line);
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		String javaScript = responseData.toString();
		
		return javaScript.replaceAll("[\n\t\r]", "");
	}
	
	public static byte[] getIcon(String iconName, int iconSize){
		//ClassLoader iconContentLoader = ResourceContent.class.getClassLoader();
		
		try {
			return ByteStreams.toByteArray(classLoader.getResourceAsStream("icon/" + iconSize + "/" + iconName + ".png"));
		} catch (IOException e) {
			return new byte[0];
		}
	}
}