package com.ats.executor.channels;

import java.util.ArrayList;

import com.ats.executor.ActionTestScript;
import com.ats.executor.TestBound;
import com.ats.executor.drivers.DriverManager;
import com.ats.tools.logger.MessageCode;

public class ChannelManager {

	private static final Double APPLICATION_WIDTH = 1280.00;
	private static final Double APPLICATION_HEIGHT = 960.00;

	private Channel currentChannel;
	private ArrayList<Channel> channelsList;
	//private Logger logger;
	
	private ActionTestScript mainScript;

	private DriverManager driverManager;

	public ChannelManager(ActionTestScript script) {

		this.mainScript = script;
		this.channelsList = new ArrayList<Channel>();
		this.driverManager = new DriverManager();
		
		script.sendInfo("ATS drivers folder -> ", this.driverManager.getDriverFolderPath());
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
			channelsList.get(0).close();
			channelsList.remove(0);
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
			setCurrentChannel(new Channel(mainScript, driverManager, name, app, getFreeLocation()));
			channelsList.add(getCurrentChannel());
			mainScript.sendLog(MessageCode.ACTION_IN_PROGRESS, "start channel");
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
					}else{
						cnl.hide();
					}
				}
			}
		}
		
		if(foundChannel) {
			mainScript.sendLog(MessageCode.ACTION_IN_PROGRESS, "switch channel");
			return true;
		}else {
			mainScript.sendLog(MessageCode.CHANNEL_NOT_FOUND, "channel not found : " + name);
			return false;
		}

	}

	public boolean closeChannel(String name){

		boolean foundChannel = false;

		for(Channel channel : channelsList){
			if(channel.getName().equals(name)){
				channel.close();
				channelsList.remove(channel);
				foundChannel = true;
				
				mainScript.sendLog(MessageCode.ACTION_IN_PROGRESS, "close channel");
				
				break;
			}
		}

		if(channelsList.size() > 0){
			setCurrentChannel(channelsList.get(0));
			
			if(foundChannel) {
				mainScript.sendLog(MessageCode.ACTION_IN_PROGRESS, "switch channel");
			}else {
				mainScript.sendLog(MessageCode.CHANNEL_NOT_FOUND, "channel not found -> " + name);
				return false;
			}
			
		}else{
			setCurrentChannel(null);
		}

		return true;
	}

	//----------------------------------------------------------------------------------------------------------------------
	// logs
	//----------------------------------------------------------------------------------------------------------------------

	/*public void sendLog(int code, String message) {
		mainScript.sendLog(code, message, "");
	}
	
	public void sendLog(int code, String message, Object value) {
		mainScript.sendLog(code, message, value);
	}*/

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	private TestBound getFreeLocation(){
		return new TestBound(10.0, 10.0, APPLICATION_WIDTH, APPLICATION_HEIGHT);
	}

	public void tearDown() {
		closeAllChannels();
		driverManager.tearDown();
	}


}