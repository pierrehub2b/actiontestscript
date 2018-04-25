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

package com.ats.recorder.stream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.ats.recorder.VisualAction;
import com.ats.script.ScriptHeader;
import com.ats.script.ScriptLoader;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Serializer;

public class VisualStream {
	
	private ByteArrayOutputStream serializedObjectInBytes;
	private AMF3Serializer amfSerializer;
	private FileOutputStream outputStream;

	public VisualStream(Path videoFolderPath, ScriptHeader header) {
		
		this.serializedObjectInBytes = new ByteArrayOutputStream();
		
		File videoFile = videoFolderPath.resolve(header.getQualifiedName() + "." + ScriptLoader.ATS_VISUAL_EXTENSION).toFile();

		try{
			videoFile.createNewFile();
			this.outputStream = new FileOutputStream(videoFile);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		writeObject(header);
	}
	
	public void terminate() {
		try {
			outputStream.close();
			serializedObjectInBytes.close();
			amfSerializer.close();
		} catch (IOException e) {}
		
		outputStream = null;
		serializedObjectInBytes = null;
		amfSerializer = null;
		
	}

	public void flush(VisualAction currentVisual) {
		writeObject(currentVisual);
	}
	
	private void writeObject(Object obj) {
		serializedObjectInBytes.reset();
		amfSerializer = new AMF3Serializer(serializedObjectInBytes);

		try {
			amfSerializer.writeObject(obj);
			serializedObjectInBytes.writeTo(outputStream);
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
		}finally{
			try {
				serializedObjectInBytes.flush();
				serializedObjectInBytes.close();
				amfSerializer.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
