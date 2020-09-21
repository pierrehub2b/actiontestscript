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

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.filters.RequestFilterAdapter;
import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.filters.ResponseFilterAdapter;
import com.browserup.bup.proxy.CaptureType;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AtsBrowserMobProxyServer extends BrowserUpProxyServer {

	private static final int TRAFFIC_IDLE_DEFAULT = 3000;
	private static final int TRAFFIC_IDLE_WAIT = 1000;
	
	private int trafficIddle = TRAFFIC_IDLE_DEFAULT;

	private boolean trafficStarted = false;
	private long timestamp = 0;
	
	private int waitTraffic = TRAFFIC_IDLE_WAIT;

	public AtsBrowserMobProxyServer(int trafficIddle) {
		super();

		setTrafficIddle(trafficIddle);
		setIdleConnectionTimeout(30, TimeUnit.SECONDS);
		
		setTrustAllServers(true);
		enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_CONTENT);
	}
		
	//--------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------
	
	public void setTrafficIddle(int value) {
		if(value > 0) {
			this.trafficIddle = value * 1000;
		}
	}
	
    public void setLatency(int value) {
    	waitTraffic = TRAFFIC_IDLE_WAIT + (value * 1000);
    	setLatency(value, TimeUnit.SECONDS);
    }
    
    public void setWhiteListRequests(List<String> value) {
    	if(value != null && value.size() > 0) {
    		whitelistRequests(value, 200);
    	}else {
    		disableWhitelist();
    	}
    }
    
    public void setMaxUpload(long value) {
    	if(value > 0) {
        	setWriteBandwidthLimit(value);
    	}else {
    		setWriteBandwidthLimit(Long.MAX_VALUE);
    	}
    }
    
    public void setMaxDownload(long value) {
    	if(value > 0) {
        	setReadBandwidthLimit(value);
    	}else {
    		setReadBandwidthLimit(Long.MAX_VALUE);
    	}
    }
    
	//--------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------    

	public void startTrafficListener() {
		trafficStarted = false;
		timestamp = System.currentTimeMillis();
	}

	public void waitTrafficFinished() {
		if(trafficStarted) {
			int maxWait = 20;
			while(!isQuiet() && maxWait > 0) {
				sleep(waitTraffic);
				maxWait--;
			}
		}
	}

	private boolean isQuiet() {
		final long diff = System.currentTimeMillis() - timestamp;
		return diff > trafficIddle;
	}

	private void sleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {}
	}

	@Override
	public void addResponseFilter(ResponseFilter filter) {
		addLastHttpFilterFactory(new ResponseFilterAdapter.FilterSource(filter, Integer.MAX_VALUE));
	}

	@Override
	public void addRequestFilter(RequestFilter filter) {
		addFirstHttpFilterFactory(new RequestFilterAdapter.FilterSource(filter, Integer.MAX_VALUE));
	}

	@Override
	public void start(int port) {
		super.start(port);
		
		addResponseFilter((response, contents, messageInfo) -> {
			timestamp = System.currentTimeMillis();
		});

		addRequestFilter((response, contents, messageInfo) -> {
			trafficStarted = true;
			timestamp = System.currentTimeMillis();
			return null;
		});
	}
}