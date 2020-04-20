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

package com.ats.tools.performance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openqa.selenium.Proxy;

import com.ats.executor.ActionStatus;
import com.ats.generator.ATS;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.actions.Action;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarNameVersion;
import net.lightbody.bmp.proxy.BlacklistEntry;
import net.lightbody.bmp.proxy.CaptureType;

public class AtsProxy implements IAtsProxy {

	private final static int BUFFER = 2048;;
	
	private ProxyThread proxyThread;

	private String fileName;
	private ArrayList<String> sessionFiles = new ArrayList<String>();

	private Path folderPath;

	private int recording = 0;

	public AtsProxy(String channelName, String application, ArrayList<BlacklistEntry> blacklist) {

		this.proxyThread = new ProxyThread(blacklist, application, channelName);

		final File folder = new File("target/performance/" + channelName).getAbsoluteFile();
		if(!folder.exists()) {
			folder.mkdirs();
		}
		
		folderPath = Paths.get(folder.toURI());
	}

	private class ProxyThread implements Runnable {

		private HarNameVersion harNameVersion = new HarNameVersion("ats", ATS.VERSION);
		private BrowserMobProxyServer server;
		private Proxy proxy;

		private boolean running = true;

		public ProxyThread(ArrayList<BlacklistEntry> blacklist, String application, String channelName) {
			this.harNameVersion.setComment("application=" + application + ", channel=" + channelName);
			this.server = new BrowserMobProxyServer();
			this.server.setBlacklist(blacklist);
			this.server.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_CONTENT);
		}

		@Override
		public void run() {
			
			server.start(0);
			proxy = ClientUtil.createSeleniumProxy(server);

			while(running) {
				Thread.yield();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}

			server.abort();
			Thread.currentThread().interrupt();
		}

		public Proxy getProxy() {
			return proxy;
		}

		public void stop() {
			running = false;
		}

		public void record(List<String> whiteList, long sendBandWidth, long receiveBandWidth) {
			if(whiteList != null && whiteList.size() > 0) {
				server.whitelistRequests(whiteList, 200);
			}
			
			if(sendBandWidth > 0) {
				server.setWriteBandwidthLimit(sendBandWidth);
			}

			if(receiveBandWidth > 0) {
				server.setReadBandwidthLimit(receiveBandWidth);
			}

			server.newHar("ats-channel-start", "ATS channel start");
		}

		public boolean pause(CalculatedValue comment, Path harPath) {
			final Har har = server.getHar();
			if(har != null) {

				server.endHar();

				har.getLog().setCreator(harNameVersion);
				if(comment != null) {
					har.getLog().setComment(comment.getCalculated());
				}else {
					har.getLog().setComment("ats-stop-record");
				}

				try {
					har.writeTo(harPath.toFile());
					return true;
				} catch (Exception e) {}
			}
			return false;
		}

		public void resume(CalculatedValue comment) {
			if(comment != null) {
				server.newHar("ats-resume-record", comment.getCalculated());
			}else {
				server.newHar("ats-resume-record", "Resume ATS record");
			}
		}

		public void startAction(String testLine, String simpleName) {
			server.newPage(testLine, simpleName);
			server.newHar();
			server.getCurrentHarPage().setComment(testLine);
			
			server.getHar().
			

		}

		public void endAction() {
			server.endPage();
		}
	}

	@Override
	public Proxy startProxy() {

		fileName = new Timestamp(System.currentTimeMillis()).getTime() + ".har";

		int maxTry = 30;
		new Thread(proxyThread, "atsProxyThread").start();

		while(proxyThread.getProxy() == null && maxTry > 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			maxTry--;
		}
		
		return proxyThread.getProxy();
	}

	@Override
	public void startRecord(ActionStatus status, List<String> whiteList, long sendBandWidth, long receiveBandWidth) {

		fileName = new Timestamp(System.currentTimeMillis()).getTime() + ".har";
		proxyThread.record(whiteList, sendBandWidth, receiveBandWidth);

		recording = 2;
	}

	@Override
	public void pauseRecord(CalculatedValue comment) {
		if(recording == 2) {
			if(fileName != null) {

				if(proxyThread.pause(comment, folderPath.resolve(fileName))) {
					sessionFiles.add(fileName);
				}

				recording = 1;
				fileName = null;
			}
		}
	}

	@Override
	public void resumeRecord(CalculatedValue comment) {
		if(recording == 1) {
			if(fileName == null) {
				this.fileName = new Timestamp(System.currentTimeMillis()).getTime() + ".har";
				proxyThread.resume(comment);
				recording = 2;
			}
		}
	}

	@Override
	public void startAction(Action action, String testLine) {
		proxyThread.startAction(testLine, action.getClass().getSimpleName());
	}	

	@Override
	public void endAction() {
		proxyThread.endAction();
	}

	@Override
	public void terminate(String channelName) {
		
		if(recording > 0) {
			pauseRecord(null);
		}
		
		proxyThread.stop();
		proxyThread = null;
		
		try {
			
	        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(folderPath.resolve("vu_" + channelName + ".zip").toFile()));
	        for (String f : sessionFiles) {
	        	addToZipFile(f, folderPath.resolve(f).toFile(), out);
	        }

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void addToZipFile(String harFileName, File harFile, ZipOutputStream out) throws IOException {
	
		final FileInputStream fi = new FileInputStream(harFile);
		final BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
        final ZipEntry entry = new ZipEntry(harFileName);
        
        final byte data[] = new byte[BUFFER];
        
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
            out.flush();
        }
        
        origin.close();
        out.flush();
	}
}