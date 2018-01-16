package com.ats.generator;

import java.util.ArrayList;
import java.util.Date;

public class GeneratorReport {

	private int generatedScriptsCount = -1;
	private long generationEllapsedTime = -1;
	
	private ArrayList<String> errorLogs;
		
	public GeneratorReport() {
		errorLogs = new ArrayList<String>();
	}
	
	public void addError(String message){
		errorLogs.add(message);
	}
	
	public void startGenerator(int count){
		generatedScriptsCount = count;
		generationEllapsedTime = new Date().getTime();
	}
	
	public void endGenerator(){
		generationEllapsedTime = new Date().getTime() - generationEllapsedTime;
	}

	public int getGeneratedScriptsCount() {
		return generatedScriptsCount;
	}

	public long getGenerationEllapsedTime() {
		return generationEllapsedTime;
	}
}