package com.ats.tools.logger;

import java.io.PrintStream;

import com.ats.executor.channels.Channel;

public class Logger {

	private final static String NO_CHANNEL = "NO_CHANNEL";
	
	private PrintStream printOut;
	private String channelName = NO_CHANNEL;

	public Logger() {
		this(new NullPrintStream());
	}
	
	public Logger(PrintStream out) {
		this.printOut = new PrintStream(out);
	}
	
	public void setChannel(Channel channel) {
		if(channel == null) {
			this.channelName = NO_CHANNEL;
		}else {
			this.channelName = channel.getName();
		}
	}
	
	public void setChannelName(String name) {
		this.channelName = name;
	}
	
	public void sendLog(int code, String message, Object value) {
		
		String data = value.toString();
		if(data.length() > 0) {
			data = " -> " + data;
		}
		
		if(code < 100 ) {
			sendInfo(message, data);
		}else if (code < 399){
			sendWarning(message, data);
		}else {
			sendError(message, data);
		}
	}
	
	public void sendInfo(String message, String value) {
		print("INFO", message + value);
	}
	
	public void sendWarning(String message, String value) {
		print("WARNING", message + value);
	}
	
	public void sendError(String message, String value) {
		print("ERROR",  message + value);
	}
	
	private void print(String type, String data) {
		printOut.println("[ATS-" + type + "] | " + "channel(" + channelName + ") | " + data);
	}
}