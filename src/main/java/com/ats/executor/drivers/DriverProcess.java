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

package com.ats.executor.drivers;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DriverProcess {

	private int port = 4444;
	private Process process;
	private DriverManager manager;
	
	private boolean started = true;

	public DriverProcess(DriverManager manager, Path driverFolderPath, String driverFileName, String[] args) {

		this.manager = manager;

		File driverFile = driverFolderPath.resolve(driverFileName).toFile();

		if(driverFile.exists()){
			
			port = findFreePort();

			String[] arguments = {driverFile.getAbsolutePath(), "--port=" + port};

			if(args != null) {
				arguments = Stream.of(arguments, args).flatMap(Stream::of).toArray(String[]::new);
			}

			ProcessBuilder builder = new ProcessBuilder(arguments);
			builder.redirectErrorStream(true);
			builder.redirectInput(Redirect.INHERIT);

			try {
				process = builder.start();
				Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
			} catch (IOException e1) {
				e1.printStackTrace();
				started = false;
			}
		}else{
			started = false;
		}
	}
	
	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public boolean isStarted() {
		return started;
	}
	
	public int getPort() {
		return port;
	}

	public URL getDriverServerUrl(){
		try {
			return new URL("http://localhost:" + port);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	private static Integer findFreePort() {
		try (ServerSocket socket = new ServerSocket(0);) {
			return socket.getLocalPort();
		} catch (IOException e) {
			return 2106;
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public void close(){
		if(process != null){

			process.descendants().forEach(p -> p.destroy());
			process.destroy();

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			process = null;
		}

		manager.processTerminated(this);
	}
}