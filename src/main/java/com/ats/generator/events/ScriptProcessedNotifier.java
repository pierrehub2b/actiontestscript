package com.ats.generator.events;

public class ScriptProcessedNotifier {

	private ScriptProcessedEvent spe;

	public ScriptProcessedNotifier (ScriptProcessedEvent event){
		spe = event; 
	} 

	public void scriptProcessed (){
		spe.scriptProcessed();
	} 
}