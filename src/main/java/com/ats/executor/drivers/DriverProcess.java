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

import com.ats.executor.ActionStatus;
import com.ats.executor.StreamGobbler;

public class DriverProcess {

	private String name;

	private int port = 4444;
	private Process process;
	private DriverManager manager;

	private boolean keepRunning = false;

	public DriverProcess(ActionStatus status, String name, DriverManager manager, Path driverFolderPath, String driverName, String[] args) {

		this.name = name;
		this.manager = manager;

		final File driverFile = driverFolderPath.resolve(driverName).toFile();

		if(driverFile.exists()){

			port = findFreePort();

			String[] arguments = {driverFile.getAbsolutePath(), "--port=" + port};

			if(args != null) {
				arguments = Stream.of(arguments, args).flatMap(Stream::of).toArray(String[]::new);
			}

			final ProcessBuilder builder = new ProcessBuilder(arguments);
			builder.redirectErrorStream(true);
			builder.redirectInput(Redirect.INHERIT);

			try {

				process = builder.start();

				final StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");            
				final StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");

				errorGobbler.start();
				outputGobbler.start();

			} catch (IOException e1) {
				status.setError(ActionStatus.CHANNEL_START_ERROR, e1.getMessage());
				return;
			}

		}else{
			status.setError(ActionStatus.CHANNEL_START_ERROR, "unable to launch driver process, driver file is missing : " + driverFile.getAbsolutePath());
			return;
		}

		Runtime.getRuntime().addShutdownHook(new CloseProcess(this));
		status.setNoError();
	}

	public void quit() {
		if(process != null && process.isAlive()) {
			if(!keepRunning) {
				process.descendants().forEach(p -> p.destroy());
				process.destroy();

				try {
					process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			process = null;
		}
	}

	static class CloseProcess extends Thread {

		private DriverProcess driver;
		public CloseProcess(DriverProcess driver) {
			this.driver = driver;
		}

		@Override
		public void run() {
			driver.quit();
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public String getName() {
		return name;
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

	/*private static int getPortUsedByDriver(String driverName) {

		final Stream<ProcessHandle> procs = ProcessHandle
				.allProcesses()
				.parallel()
				.filter(p -> p.info().command().isPresent())
				.filter(p -> p.info().command().get().contains(driverName));

		final Optional<ProcessHandle> firstProc = procs.findFirst();
		if(firstProc.isPresent()) {
			return getPortUsedByProcess(firstProc.get().pid());
		}
		return 0;
	}*/

	/*private static int getPortUsedByProcess(long pid) {

		try {
			final Process p = Runtime.getRuntime().exec("cmd /c netstat -ano | findstr " + pid);
			final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;

			while ((line = input.readLine()) != null) {    
				final String[] data = line.split("\\s+");  
				if(data.length > 1) {
					final String[] ipPort = data[2].split(":");
					if(ipPort.length == 2) {
						try {
							return Integer.parseInt(ipPort[1]);
						}catch (NumberFormatException e) {}
					}
				}
			}  

		} catch (IOException e1) {}

		return 0;
	}*/

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public void close(boolean keepRunning){

		//this.keepRunning = keepRunning;

		quit();
		manager.processTerminated(this);
	}
}