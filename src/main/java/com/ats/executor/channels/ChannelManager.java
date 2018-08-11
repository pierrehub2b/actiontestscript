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

package com.ats.executor.channels;

import java.util.ArrayList;

import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.executor.drivers.DriverManager;

public class ChannelManager {
	
	private Channel currentChannel;
	private ArrayList<Channel> channelsList;

	private ActionTestScript mainScript;

	private DriverManager driverManager;

	public ChannelManager(ActionTestScript script) {

		this.mainScript = script;
		this.channelsList = new ArrayList<Channel>();
		this.driverManager = new DriverManager();

		script.sendInfo("ATS drivers folder -> ", this.driverManager.getDriverFolderPath());
	}

	public int getMaxTry() {
		return DriverManager.ATS.getMaxTrySearch();
	}

	public Channel getCurrentChannel(){
		return currentChannel;
	}

	public Channel[] getChannelsList(){
		if(channelsList.size() > 0){
			return channelsList.toArray(new Channel[channelsList.size()]);
		}else{
			return new Channel[0];
		}
	}

	private void setCurrentChannel(Channel channel){
		for(Channel cnl : channelsList){
			cnl.setCurrent(cnl == channel);
		}
		currentChannel = channel;
		mainScript.setCurrentChannel(channel);
	}

	public void closeAllChannels(){
		while(channelsList.size() > 0){
			channelsList.remove(0).close();
		}
	}

	public Channel getChannel(String name){
		for(Channel cnl : channelsList){
			if(cnl.getName().equals(name)){
				return cnl;
			}
		}
		return null;// Channel with name : does not exists or has been closed
	}

	public void startChannel(ActionStatus status, String name, String app){
		if(getChannel(name) == null){
			Channel newChannel = new Channel(status, mainScript, driverManager, name, app);
			if(status.isPassed()) {
				channelsList.add(newChannel);
				setCurrentChannel(newChannel);
				sendInfo("Start channel", " -> " + app);
			}else {
				sendError(ActionStatus.CHANNEL_START_ERROR, status.getMessage());
			}
		}
	}

	public boolean switchChannel(String name){

		if(channelsList != null){
			for(Channel cnl : channelsList){
				if(cnl.getName().equals(name)){
					if(!cnl.isCurrent()) {
						setCurrentChannel(cnl);
						cnl.toFront();
						
						sendInfo("switch channel", " '" + name + "'");
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean closeChannel(String name){

		boolean foundChannel = false;

		if(name.length() > 0) {
			for(Channel channel : channelsList){
				if(channel.getName().equals(name)){
					channel.close();
					channelsList.remove(channel);
					foundChannel = true;

					sendInfo("close channel", " '" + name + "'");
					
					break;
				}
			}
		}else {
			getCurrentChannel().close();
			channelsList.remove(getCurrentChannel());
			foundChannel = true;
		}

		if(channelsList.size() > 0){
			setCurrentChannel(channelsList.get(0));

			if(!foundChannel) {
				return false;
			}

		}else{
			setCurrentChannel(null);
		}

		return true;
	}
	
	private void sendInfo(String type, String message) {
		mainScript.sendInfo(type, " '" + message + "'");
	}
	
	private void sendError(int code, String message) {
		mainScript.sendLog(code, message);
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void tearDown() {
		closeAllChannels();
		driverManager.tearDown();
	}
}