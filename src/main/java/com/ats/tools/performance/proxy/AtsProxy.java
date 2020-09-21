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

package com.ats.tools.performance.proxy;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.generator.ATS;
import com.ats.script.actions.Action;
import com.ats.script.actions.performance.octoperf.ActionOctoperfVirtualUser;
import com.ats.tools.performance.external.OctoperfApi;
import com.ats.tools.performance.filters.BlackListFilter;
import com.ats.tools.performance.filters.PausedListFilter;
import com.ats.tools.performance.filters.UrlBaseFilter;
import com.ats.tools.performance.filters.WhiteListFilter;
import com.browserup.bup.client.ClientUtil;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.Proxy;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AtsProxy implements IAtsProxy {

	public final static String CHANNEL_STARTED_PAGEID = "ats-channel-start";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private ProxyThread proxyThread;
	private ArrayList<HarEntry> harEntries = new ArrayList<HarEntry>();

	private Path currentFile = null;
	private Path targetFolder = null;

	private OctoperfApi octoperf;

	public AtsProxy(String channelName, String application, ArrayList<String> blacklist, int trafficIdle, OctoperfApi octoperf) {

		this.octoperf = octoperf;

		final File folder = new File("target/performance/" + channelName).getAbsoluteFile();
		if(!folder.exists()) {
			folder.mkdirs();
		}

		targetFolder = Paths.get(folder.toURI());
		currentFile = getNewHarFile(targetFolder);

		proxyThread = new ProxyThread(blacklist, trafficIdle, application, channelName);
	}

	private static Path getNewHarFile(Path folder) {
		return folder.resolve(new Timestamp(System.currentTimeMillis()).getTime() + ".har");
	}

	private class ProxyThread implements Runnable {

		private List<String> blacklistUrls;
		private List<String> whitelistUrls;

		private UrlBaseFilter currentUrlFilter;

		private boolean running = true;
		private boolean paused = false;

		private AtsBrowserMobProxyServer server;
		private Proxy proxy;

		public ProxyThread(ArrayList<String> blacklist, int trafficIddle, String application, String channelName) {

			server = new AtsBrowserMobProxyServer(trafficIddle);
			blacklistUrls = blacklist;

			selectFilter();
			startNewHar(CHANNEL_STARTED_PAGEID);
		}

		private void selectFilter() {
			if(paused) {
				currentUrlFilter = new PausedListFilter();
			}else {
				if(whitelistUrls != null && whitelistUrls.size() > 0) {
					currentUrlFilter = new WhiteListFilter(whitelistUrls);
				}else if(blacklistUrls != null && blacklistUrls.size() > 0) {
					currentUrlFilter = new BlackListFilter(blacklistUrls);
				}else {
					currentUrlFilter = new UrlBaseFilter();
				}
			}
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

			server.stop();
			terminate();

			Thread.currentThread().interrupt();
		}

		public Proxy getProxy() {
			return proxy;
		}

		public void stop() {
			running = false;
		}

		public void start(List<String> whiteList, int trafficIddle, int latency, long sendBandWidth, long receiveBandWidth) {
			server.setTrafficIddle(trafficIddle);
			server.setLatency(latency);
			server.setMaxUpload(sendBandWidth);
			server.setMaxDownload(receiveBandWidth);

			whitelistUrls = whiteList;
			selectFilter();
		}

		public void startNewHar(String newPageId) {
			server.newHar(newPageId, "ATS channel");
			server.setHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_CONTENT);
			server.getCurrentHarPage().setComment("new har capture");

			harEntries = new ArrayList<HarEntry>();
			currentUrlFilter.filter(server.getHar().getLog().getEntries(), harEntries);
		}

		public void setPaused(boolean value) {
			paused = value;
			selectFilter();
		}

		public boolean terminate() {
			final Har har = server.getHar();

			currentUrlFilter.filter(server.getHar().getLog().getEntries(), harEntries);
			server.endHar();

			har.getLog().getCreator().setName("ats-automation");
			har.getLog().getCreator().setVersion(ATS.VERSION);
			har.getLog().getEntries().addAll(harEntries);

			try {
				OBJECT_MAPPER.writeValue(currentFile.toFile(), har);
				return true;
			} catch (Exception e) {}

			return false;
		}

		public void startAction(String testLine, String simpleName) {
			if(server.getHar() != null) {
				server.startTrafficListener();

				server.endPage();
				currentUrlFilter.filter(server.getHar().getLog().getEntries(), harEntries);
				currentUrlFilter.setPageId(testLine);

				server.newPage(testLine, simpleName);
				server.getCurrentHarPage().setComment(testLine);
			}
		}

		public void endAction() {
			server.waitTrafficFinished();
		}
	}

	@Override
	public Proxy startProxy() {

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
	public void startRecord(ActionStatus status, List<String> whiteList, int trafficIddle, int latency, long sendBandWidth, long receiveBandWidth) {
		proxyThread.start(whiteList, trafficIddle, latency, sendBandWidth, receiveBandWidth);
	}

	@Override
	public void pauseRecord() {
		proxyThread.setPaused(true);
	}

	@Override
	public void resumeRecord() {
		proxyThread.setPaused(false);
	}

	@Override
	public void startAction(Action action, String testLine) {
		proxyThread.startAction(testLine, action.getClass().getSimpleName() + " (" + testLine + ")");
	}	

	@Override
	public void endAction() {
		proxyThread.endAction();
	}

	@Override
	public void terminate(String channelName) {
		proxyThread.stop();
		proxyThread = null;
	}

	@Override
	public void sendToOctoperfServer(Channel channel, ActionOctoperfVirtualUser action) {
		if(proxyThread.terminate()) {
			if(octoperf != null) {
				octoperf.sendHarFileToUser(channel, action, currentFile);
			}
		}
		currentFile = getNewHarFile(targetFolder);
		proxyThread.startNewHar("new-ats-page");
	}
}