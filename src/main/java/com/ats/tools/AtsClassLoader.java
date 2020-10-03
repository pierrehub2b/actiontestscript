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

import com.ats.executor.ActionTestScript;
import com.ats.tools.wait.IWaitGuiReady;
import com.ats.tools.wait.WaitGuiReady;
import com.ats.tools.wait.WaitGuiReadyInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AtsClassLoader extends ClassLoader{

	@SuppressWarnings("unchecked")
	public Class<ActionTestScript> loadTestScriptClass(String name) {
		
		Class<ActionTestScript> testScriptClass;
		
		try {
			testScriptClass = (Class<ActionTestScript>) loadClass(name);
		} catch (ClassNotFoundException e) {
			testScriptClass = (Class<ActionTestScript>) findClass(name);
		}
		
		return testScriptClass;
	}
	
	@Override
	public Class<?> findClass(String name) {
		byte[] bt = loadAtsScriptClass(name);
		if(bt != null) {
			try {
				return defineClass(name, bt, 0, bt.length);
			}catch(NoClassDefFoundError e) {}
		}
		return null;
	}

	private byte[] loadAtsScriptClass(String className) {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(className.replace(".", "/") + ".class");
		if(is != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len = 0;
			try {
				while((len = is.read())!=-1){
					bos.write(len);
				}
			} catch (IOException e) {}

			final byte[] data = bos.toByteArray();
			
			try {
				bos.close();
				is.close();
			} catch (IOException e) {}
			
			return data;
		}
		
		return null;
	}
	
	//------------------------------------------------------------------------------------------------------
	// Instance management
	//------------------------------------------------------------------------------------------------------
		
	private IWaitGuiReady waitGuiReady;
	public IWaitGuiReady getWaitGuiReady() {
		return waitGuiReady;
	}

	public AtsClassLoader() {
		final Class<IWaitGuiReady> wait = findCustomWaitClass();
		if(wait != null) {
			try {
				waitGuiReady = wait.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException	| InvocationTargetException | NoSuchMethodException | SecurityException e) {}
		}
		
		if(waitGuiReady == null) {
			waitGuiReady = new WaitGuiReady();
		}
	}

	private Class<IWaitGuiReady> findCustomWaitClass() {

		final String classpath = System.getProperty("java.class.path");
		final String[] files = classpath.split(System.getProperty("path.separator"));

		for (String path : files) {
			final Class<IWaitGuiReady> customWaitClass = findWaitGuiClass(path);
			if(customWaitClass != null) {
				return customWaitClass;
			}
		}
		return null;
	}

	private Class<IWaitGuiReady> findWaitGuiClass(String filePath){
		File f = new File(filePath);
		if(f.exists()) {
			return findWaitGuiClass(f);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Class<IWaitGuiReady> findWaitGuiClass(File file) {
		if(file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					return findWaitGuiClass(child);
				}
			} else if (file.getName().toLowerCase().endsWith(".jar")) {

				JarFile jar = null;

				try {
					jar = new JarFile(file);
				} catch (Exception ex) {}

				if (jar != null) {

					try {
						final Manifest man = jar.getManifest();
						if(man == null) {
							return null;
						}else {
							Attributes attr = man.getMainAttributes();
							final String atsType = attr.getValue("Ats-Type");
							if(atsType == null || !"WaitGuiReady".equals(atsType)) {
								return null;
							}
						}
					} catch (IOException e1) {
						return null;
					}
					
					final Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {

						final String entryName = entries.nextElement().getName();

						if (entryName.endsWith(".class")) {
							final String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
							try
							{
								final Class<?> c = loadClass(className);
								if(c.isAnnotationPresent(WaitGuiReadyInfo.class)) {
									if(IWaitGuiReady.class.isAssignableFrom(c)) {
										return (Class<IWaitGuiReady>) c;
									}
								}
							}catch(ClassNotFoundException | NoClassDefFoundError e) {}
						}
					}
				}
			}
		}
		return null;
	}
}