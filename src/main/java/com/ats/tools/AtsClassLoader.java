package com.ats.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ats.executor.ActionTestScript;

public class AtsClassLoader extends ClassLoader{

	@SuppressWarnings("unchecked")
	@Override
	public Class<ActionTestScript> findClass(String name) {
		byte[] bt = loadClassData(name);
		if(bt != null) {
			try {
				return (Class<ActionTestScript>)defineClass(name, bt, 0, bt.length);
			}catch(NoClassDefFoundError e) {}
		}
		return null;
	}

	private byte[] loadClassData(String className) {

		InputStream is = getClass().getClassLoader().getResourceAsStream(className.replace(".", "/")+".class");
		if(is != null) {
			ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
			int len = 0;
			try {
				while((len = is.read())!=-1){
					byteSt.write(len);
				}
			} catch (IOException e) {}

			return byteSt.toByteArray();
		}
		return null;
	}
}
