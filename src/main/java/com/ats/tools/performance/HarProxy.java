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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

public class HarProxy implements IHarProxy {

	private ProxyThread proxyThread;

	private String fileName;
	private ArrayList<String> sessionFiles = new ArrayList<String>();

	private File folder;

	private int recording = 0;

	public HarProxy(String channelName, String application, ArrayList<BlacklistEntry> blacklist) {
		this.proxyThread = new ProxyThread(blacklist, application, channelName);

		folder = new File("target/performance/" + channelName).getAbsoluteFile();
		if(!folder.exists()) {
			folder.mkdirs();
		}
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
			this.server.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_COOKIES, CaptureType.RESPONSE_CONTENT);
		}

		@Override
		public void run() {
			server.start(0);
			proxy = ClientUtil.createSeleniumProxy(server);
			
			while(running) {}

			server.stop();
		}

		public Proxy getProxy() {
			return proxy;
		}

		public void stop() {
			running = false;
			
		}

		public void record(List<String> whiteList, long sendBandWidth, long receiveBandWidth) {
			if(whiteList.size() > 0) {
				server.whitelistRequests(whiteList, 200);
			}
			server.setWriteBandwidthLimit(sendBandWidth);
			server.setReadBandwidthLimit(receiveBandWidth);
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
		}

		public void endAction() {
			server.endPage();
		}
	}

	@Override
	public Proxy startProxy() {

		fileName = new Timestamp(System.currentTimeMillis()).getTime() + ".har";

		int maxTry = 30;
		new Thread(proxyThread, "harProxyThread").start();
		
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

				if(proxyThread.pause(comment, Paths.get(folder.toURI()).resolve(fileName))) {
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
	public void dispose() {
		if(recording > 0) {
			pauseRecord(null);
		}
		proxyThread.stop();
		proxyThread = null;
	}
}