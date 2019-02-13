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
import com.ats.executor.drivers.desktop.DesktopWindow;

public class ExplorerDriverEngine extends DesktopDriverEngine {

	private final static int DEFAULT_WAIT = 100;

	private Desktop desktop;
	private DesktopWindow window;

	public ExplorerDriverEngine(Channel channel, ActionStatus status, DesktopDriver desktopDriver, ApplicationProperties props) {
		super(channel, DriverManager.DESKTOP_EXPLORER, desktopDriver, props, DEFAULT_WAIT);

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
				
				channel.setApplicationData(window.handle);
				
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
	public void goToUrl(ActionStatus status, String url) {
		getDesktopDriver().gotoUrl(status, window.handle, url);
	}

	@Override
	public void close() {
		getDesktopDriver().closeWindow(window.handle);
	}
}