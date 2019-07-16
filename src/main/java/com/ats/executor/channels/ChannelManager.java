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
import java.util.Optional;

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

		script.sendActionLog("ATS drivers folder", this.driverManager.getDriverFolderPath());
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
		channel.setCurrent(true);
	}

	private void noChannel() {
		currentChannel = null;
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

	public void startChannel(ActionStatus status, ActionChannelStart action){
		
		final String name = action.getName();

		if(getChannel(name) == null){
			
			final String app = action.getApplication().getCalculated();
			final Channel newChannel = new Channel(status, mainScript, driverManager, action);

			if(status.isPassed()) {
				
				for(Channel cn : channelsList) {
					cn.clearData();
				}
				
				channelsList.add(newChannel);
				setCurrentChannel(newChannel);
				
				mainScript.sendActionLog("Start channel : " + newChannel.getName(), "application : " + app);

				status.setChannel(newChannel);
				status.endDuration();

				mainScript.getRecorder().createVisualAction(action, status.getDuration(), name, app);
			}
			
			status.setData(getChannelsList());
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

						mainScript.sendActionLog("Switch to channel", name);

						status.setData(getChannelsList());
						status.setChannel(cnl);
					}
				}
			}
		}

		status.setPassed(found);
		status.endDuration();
	}
	
	public void closeChannel(ActionStatus status, String channelName){
		Optional<Channel> cn = channelsList.stream().filter(c -> c.getName().equals(channelName)).findFirst();
		if(cn.isPresent()) {
			cn.get().close(status);
		}
	}
	
	public void channelClosed(ActionStatus status, Channel channel){
		status.startDuration();
		if(channelsList.remove(channel)) {
			mainScript.sendActionLog("Close channel", channel.getName());
			if(channelsList.size() > 0){
				final Channel current = channelsList.get(0);
				setCurrentChannel(current);
				status.setChannel(current);
			}else{
				noChannel();
			}

			status.setPassed(true);
			status.setData(getChannelsList());
		}else {
			status.setPassed(false);
			status.setMessage("Channel '" + channel.getName() + "' not found !");
		}
		status.endDuration();
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void tearDown() {
		closeAllChannels();
		driverManager.tearDown();
	}
}