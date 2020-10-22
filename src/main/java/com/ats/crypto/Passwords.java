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

package com.ats.crypto;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

//-----------------------------------------------------------------------------------------------------------------
// Very simple class to encrypt passwords data /!\ DO NOT USE for very strong encryption or security needs /!\
//-----------------------------------------------------------------------------------------------------------------

public class Passwords implements Serializable {

	private static final long serialVersionUID = -2364261599407176796L;

	private static final String FILE_NAME = "passwords.crypto";

	private transient static BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));

	private transient File file;

	private int keyLen = 16;
	private byte[] masterKey;
	private HashMap<String, byte[]> data = new HashMap<String, byte[]>();

	public Passwords(Path assetsPath) {

		file = assetsPath.resolve("secret").resolve(FILE_NAME).toFile();
		if(file.exists()) {
			load();
		}else {
			save();
		}
	}

	public Passwords(File assetsFolder) {
		try {
			file = assetsFolder.toPath().resolve("secret").resolve(FILE_NAME).toFile();
			if(file.exists()) {
				load();
			}else {
				file = null;
			}
		}catch(Exception e) {
			file = null;
		}
	}

	public void setPassword(String key, String value) {
		data.put(key, value.getBytes());
		save();
		load();
	}

	public String getPassword(String key) {
		if(file != null && data.containsKey(key)) {
			return new String(data.get(key)).replaceAll("\0", "");
		}
		return "";
	}

	public PasswordData[] getDataList() {

		final List<String> keySet = data.keySet().stream().collect(Collectors.toList());
		Collections.sort(keySet, (o1, o2) -> o1.compareTo(o2));

		final PasswordData[] result = new PasswordData[data.size()];
		int loop = 0;
		for(String key : keySet) {
			result[loop] = new PasswordData(key, getPassword(key));
			loop++;
		}
		return result;
	}

	public void clear() {
		data.clear();
	}

	//-----------------------------------------------------------------------------------------------------------
	//
	//-----------------------------------------------------------------------------------------------------------

	private void load() {
		try {

			byte[] fileContent = Base64.getDecoder().decode(Files.readAllBytes(file.toPath()));

			ByteArrayInputStream bis = new ByteArrayInputStream(fileContent);

			byte[] key = new byte[keyLen];
			bis.read(key, 0, keyLen);

			byte[] bytes = decrypt(key, bis.readAllBytes());

			final ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			final ObjectInputStream objStream = new ObjectInputStream(byteStream);

			final Passwords pass = (Passwords) objStream.readObject();
			this.data = pass.data;
			this.masterKey = pass.masterKey;

		} catch (IOException | ClassNotFoundException e) {}

		data.replaceAll((k, v) -> decrypt(masterKey, v));
	}

	private void save() {
		try {

			file.getParentFile().mkdirs();			

			final byte[] randomKey = new byte[keyLen];
			masterKey = new byte[keyLen];

			final SecureRandom random = SecureRandom.getInstanceStrong();
			random.nextBytes(randomKey);
			random.nextBytes(masterKey);

			data.replaceAll((k, v) -> encrypt(masterKey, v));

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final ObjectOutput out = new ObjectOutputStream(bos);  
			out.writeObject(this);

			final byte[] dataBytes = encrypt(randomKey, bos.toByteArray());
			out.close();
			bos.close();

			bos = new ByteArrayOutputStream();
			bos.write(randomKey);
			bos.write(dataBytes);

			final FileOutputStream outfile = new FileOutputStream(file);
			outfile.write(Base64.getEncoder().encode(bos.toByteArray()));
			outfile.close();

		} catch (NoSuchAlgorithmException | IOException e) {

			System.out.println(e.getMessage());
		} 
	}

	private static byte[] encrypt(byte[] key, byte[] data) {
		cipher.init(true, new KeyParameter(key));
		byte[] rv = new byte[cipher.getOutputSize(data.length)];
		int tam = cipher.processBytes(data, 0, data.length, rv, 0);
		try {
			cipher.doFinal(rv, tam);
		} catch (Exception ce) {
			ce.printStackTrace();
		}
		return rv;
	}

	private byte[] decrypt(byte[] key, byte[] data) {
		cipher.init(false, new KeyParameter(key));
		byte[] rv = new byte[cipher.getOutputSize(data.length)];
		int tam = cipher.processBytes(data, 0, data.length, rv, 0);
		try {
			cipher.doFinal(rv, tam);
		} catch (Exception ce) {
			ce.printStackTrace();
		}
		return rv;
	}
}