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

package com.ats.executor.drivers.engines.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.executor.drivers.DriverManager;
import com.ats.executor.drivers.desktop.DesktopDriver;
import com.ats.executor.drivers.engines.DesktopDriverEngine;

public class ExplorerDriverEngine extends DesktopDriverEngine {

	private final static int DEFAULT_WAIT = 100;

	private Desktop desktop;

	public ExplorerDriverEngine(Channel channel, ActionStatus status, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, desktopDriver, props, DEFAULT_WAIT);

		String folderName = "";

		try {
			File folder = Files.createTempDirectory("ats_").toFile();
			folder.deleteOnExit();

			folderName = folder.getName();

			desktop = Desktop.getDesktop();
			desktop.open(folder);

		} catch (IOException e) {
			e.printStackTrace();
		}

		int maxTry = 10;
		while(maxTry > 0) {
			
			window = desktopDriver.getWindowByTitle(folderName);
			
			if(window != null) {
				
				channel.setApplicationData(DriverManager.DESKTOP_EXPLORER, window.getHandle());
				
				desktopDriver.moveWindow(channel, channel.getDimension().getPoint());
				desktopDriver.resizeWindow(channel, channel.getDimension().getSize());
								
				maxTry = 0;
				
			}else {
				channel.sleep(300);
				maxTry--;
			}
		}
	}

	@Override
	public void close(boolean keepRunning) {
		getDesktopDriver().closeWindow(window.getHandle());
	}
}