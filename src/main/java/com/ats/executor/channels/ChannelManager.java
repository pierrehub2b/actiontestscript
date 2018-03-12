package com.ats.executor.channels;

import java.util.ArrayList;

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
		return driverManager.getMaxTry();
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

	public void startChannel(String name, String app){
		if(getChannel(name) == null){
			setCurrentChannel(new Channel(mainScript, driverManager, name, app));
			channelsList.add(getCurrentChannel());
		}
	}

	public boolean switchChannel(String name){

		boolean foundChannel = false;

		if(channelsList != null && channelsList.size() > 0){
			if(channelsList.size() == 1){
				channelsList.get(0).toFront();
				foundChannel = channelsList.get(0).getName().equals(name);
			}else{
				for(Channel cnl : channelsList){
					if(cnl.getName().equals(name)){
						setCurrentChannel(cnl);
						foundChannel = true;
						break;
					}
				}

				if(foundChannel) {
					for(Channel cnl : channelsList){
						if(!cnl.getName().equals(name)){
							cnl.hide();
						}
					}
				}
			}
		}

		return foundChannel;

	}

	public boolean closeChannel(String name){

		boolean foundChannel = false;

		if(name.length() > 0) {
			for(Channel channel : channelsList){
				if(channel.getName().equals(name)){
					channel.close();
					channelsList.remove(channel);
					foundChannel = true;

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

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void tearDown() {
		closeAllChannels();
		driverManager.tearDown();
	}
}