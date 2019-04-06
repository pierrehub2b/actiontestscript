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
