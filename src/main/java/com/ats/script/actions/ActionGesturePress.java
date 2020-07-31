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

package com.ats.script.actions;

import com.ats.element.SearchedElement;
import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionGesturePress extends ActionExecuteElement {
	
	/* public class ActionGesturePressPath {
		
		public class ActionGesturePressPathCoordinate {
			public int x;
			public int y;
			
			public ActionGesturePressPathCoordinate(String info) {
				String[] coordinatesInfo = info.split(",");
				this.x = Integer.parseInt(coordinatesInfo[0]);
				this.y = Integer.parseInt(coordinatesInfo[1]);
			}
		}
		
		public ArrayList<ActionGesturePressPathCoordinate> coordinates;
		
		public ActionGesturePressPath(String pathString) {
			String[] coordinatesInfo =  pathString.split(";");
			coordinates = new ArrayList<>();
			
			for (String info:coordinatesInfo) {
				ActionGesturePressPathCoordinate coordinate = new ActionGesturePressPathCoordinate(info);
				coordinates.add(coordinate);
			}
		}
	} */
	
	public static final String SCRIPT_LABEL = "press";
	
	// private ArrayList<ActionGesturePressPath> paths;
	private ArrayList<String> paths;
	private int duration;
	
	public ActionGesturePress() {}
	
	public ActionGesturePress(Script script, boolean stop, ArrayList<String> options, ArrayList<String> paths, ArrayList<String> elements) {
		super(script, stop, options, elements);
		// parsePaths(parameters);
		setPaths(paths);
		parseOptions(options);
	}
	
	public ActionGesturePress(Script script, boolean stop, int maxTry, int delay, SearchedElement element, int duration, String[] paths) {
		super(script, stop, maxTry, delay, element);
		setDuration(duration);
		setPaths(new ArrayList<>(Arrays.asList(paths)));
	}
	
	private void parseOptions(ArrayList<String> options) {
		setDuration(2);
	}
	
	/* private void parsePaths(ArrayList<String> pathsInfo) {
		paths = new ArrayList<>();
		
		for (String info:pathsInfo) {
			ActionGesturePressPath path = new ActionGesturePressPath(StringUtils.trim(info));
			paths.add(path);
		}
	} */
	
	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", ").append(duration).append(", ").append("new String[]{");

		List<String> myFinalList = getPaths().stream().map(path -> "\"" + path.trim() +"\"").collect(Collectors.toList());
		String test = String.join(",", myFinalList);
		codeBuilder.append(test);
		
		codeBuilder.append("}").append(")");
		return codeBuilder;
	}
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		
		if (status.isPassed()) {
			ts.getRecorder().updateScreen(true);
			
			getTestElement().press(getDuration(), getPaths());
			
			status.endAction();
			ts.getRecorder().updateScreen(0, status.getDuration());
		}
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = Math.max(duration, 0);
	}
	
	public ArrayList<String> getPaths() { return paths; }
	public void setPaths(ArrayList<String> paths) { this.paths = paths; }
}
