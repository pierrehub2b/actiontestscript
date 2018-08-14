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
import com.ats.script.actions.ActionChannelStart;

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

	public void startChannel(ActionStatus status, ActionChannelStart action, String name, String app){
		if(getChannel(name) == null){
			final Channel newChannel = new Channel(status, mainScript, driverManager, name, app);

			channelsList.add(newChannel);
			setCurrentChannel(newChannel);
			sendInfo("Start channel with application", app);

			status.setData(getChannelsList());
			status.setChannel(newChannel);

			status.endDuration();
			mainScript.createVisual(action, newChannel, status.getDuration(), name, app);
		}
	}

	public void switchChannel(ActionStatus status, String name){

		boolean found = false;

		status.startDuration();
		if(channelsList != null){
			for(Channel cnl : channelsList){
				if(cnl.getName().equals(name)){

					found = true;

					if(!cnl.isCurrent()) {
						setCurrentChannel(cnl);
						cnl.toFront();

						sendInfo("Switch to channel", name);

						status.setData(getChannelsList());

						mainScript.getRecorder().setChannel(cnl);
					}
				}
			}
		}

		status.setPassed(found);
		status.endDuration();
	}

	public void closeChannel(ActionStatus status, String name){

		status.startDuration();

		if(name.length() > 0) {

			Channel closeChannel = null;

			for(Channel channel : channelsList){
				if(channel.getName().equals(name)){
					closeChannel = channel;
					break;
				}
			}

			if(closeChannel != null) {

				channelsList.remove(closeChannel);
				closeChannel.close();

				sendInfo("Close channel");

				if(channelsList.size() > 0){
					final Channel current = channelsList.get(0);
					setCurrentChannel(current);
					mainScript.getRecorder().setChannel(current);
				}else{
					setCurrentChannel(null);
				}

				status.setPassed(true);
				status.setData(getChannelsList());

			}else {
				status.setPassed(false);
				status.setMessage("Channel named '" + name + "' has not be found !");
			}

		}else {
			status.setPassed(false);
			status.setMessage("Channel name cannot be empty !");
		}

		status.endDuration();
	}

	private void sendInfo(String type) {
		mainScript.sendInfo(type, "");
	}
	
	private void sendInfo(String type, String message) {
		mainScript.sendInfo(type, " -> " + message);
	}
	
	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void tearDown() {
		closeAllChannels();
		driverManager.tearDown();
	}
}