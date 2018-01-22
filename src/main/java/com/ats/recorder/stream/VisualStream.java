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
