package com.ats.executor.drivers.engines.mobiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.ats.element.AtsMobileElement;
import com.ats.element.StructDebugDescription;
import com.google.gson.JsonObject;

public class IosRootElement extends RootElement {	

	protected String regexSpaces = "^\\s+";
	protected String regexBraces = "[{},]+";
	protected String regexBracesAndSpaces = "[\\s{},]+";
	
	@Override
	public void refresh(JsonObject jsonObject) {
		var debugDescription = jsonObject.get("root").getAsString();
		var deviceHeight = jsonObject.get("deviceHeight").getAsDouble();
		var deviceWidth = jsonObject.get("deviceWidth").getAsDouble();
		var debugDescriptionArray = debugDescription.split("\n");
		var ratioWidth = 1.0;
		var ratioHeight = 1.0;
		
		Double width = 0.0;
		Double height = 0.0;
		
		ArrayList<StructDebugDescription> structDebug = new ArrayList<StructDebugDescription>();

		for (int i = 0; i < debugDescriptionArray.length; i++) {
			var level = countSpaces(debugDescriptionArray[i]);
			if(level >= 4 && !debugDescriptionArray[i].contains("Application, pid:")) {
				
				
				String trimmedLine = debugDescriptionArray[i].replaceAll(regexSpaces, "");
				if(trimmedLine.startsWith("Window (Main)")) {
					var arraySize = trimmedLine.split(regexBracesAndSpaces);
					width = Double.parseDouble(arraySize[arraySize.length-2]);
					height = Double.parseDouble(arraySize[arraySize.length-1]);
				}
				structDebug.add(new StructDebugDescription((level/2)-1, trimmedLine));
			}
		}
		
		
		//calculate ratio
		if(height != deviceHeight || width != deviceWidth) {
			ratioHeight = deviceHeight / height;
			ratioWidth = deviceWidth / width;
			width = width * ratioWidth;
			height = height * ratioHeight;
		}
		
		var domStructure = new AtsMobileElement(UUID.randomUUID().toString(),"root",width, height, 0.0,0.0, false, new HashMap<String, String>());
	
		var currentIndex = 0;
		for (StructDebugDescription structDebugDescription : structDebug) {
			var tag = structDebugDescription.getContent().split(regexBraces)[0].replaceAll(regexSpaces, "");
			var arraySize = structDebugDescription.getContent().split(regexBraces);
			ArrayList<String> stringArray = new ArrayList<String>();

			for (int i = 0; i < arraySize.length; i++) {
				if(!arraySize[i].replaceAll(regexSpaces, "").equals("")) {
					stringArray.add(arraySize[i]);
				}
			}
			
			var currentX = Double.parseDouble(stringArray.get(2)) * ratioWidth;
			var currentY = Double.parseDouble(stringArray.get(3)) * ratioHeight; 
			var currentWidth = Double.parseDouble(stringArray.get(4)) * ratioWidth;
			var currentheight = Double.parseDouble(stringArray.get(5)) * ratioHeight; 
			
			var currentAtsMobileElement = new AtsMobileElement(
					structDebugDescription.getUuid().toString(), 
					tag, 
					currentWidth, 
					currentheight,
					currentX, 
					currentY, 
					true,
					getAttributes(structDebugDescription.getContent())
				);
			
			if(structDebugDescription.getLevel() == 1) {
				domStructure.addChildren(currentAtsMobileElement);
			} else 
			{
				// get parentLevel
				var i = currentIndex;
				UUID uuidParent = null;
				while ((i-1) > -1) {
					if(structDebug.get(i-1).getLevel()+1 == structDebugDescription.getLevel()) {
						uuidParent = structDebug.get(i-1).getUuid();
						break;
					}
					i--;
				}
				
				if(uuidParent != null) {
					searchAndAdd(uuidParent.toString(), domStructure, currentAtsMobileElement);
				}
				
			}
			currentIndex++;
		}
		
		this.value = domStructure;
	}

	protected Map<String,String> getAttributes(String str) {
		var result = new TreeMap<String, String>();
		var arraySize = str.split(regexBraces);
		
		if(arraySize[0].contains("checkbox")) {
			result.put("checkable", "true");
		} else 
		{
			result.put("checkable", "false");
		}
		
		for (String s : arraySize) {
			var cleanString = cleanAttribute(s);
			if(s.contains("label")) {
				result.put("text", cleanString);
			}
			if(s.contains("placeholderValue")) {
				result.put("description", cleanString);
			}
			if(s.contains("identifier")) {
				result.put("identifier", cleanString);
			}
			if(s.contains("value")) {
				result.put("value", cleanString);
			}
			if(s.contains("Disabled")) {
				result.put("enabled", "false");
				result.put("editable", "false");
			}
			if(s.contains("Selected")) {
				result.put("selected", "true");
			}
		}
		
		if(!result.containsKey("text")) {
			result.put("text", null);
		}
		if(!result.containsKey("description")) {
			result.put("description", null);
		}
		if(!result.containsKey("identifier")) {
			result.put("identifier", null);
		}
		if(!result.containsKey("value")) {
			result.put("value", null);
		}
		if(!result.containsKey("enabled")) {
			result.put("enabled", "true");
			result.put("editable", "true");
		}
		if(!result.containsKey("selected")) {
			result.put("selected", "false");
		}
		if(!result.containsKey("numeric")) {
			result.put("numeric", "false");
		}
		
		return result;
	}
	
	protected String cleanAttribute(String str) {
		return str.split(":")[str.split(":").length-1].replace("\'", "").replaceAll(regexSpaces, "");
	}

	protected String[] parseLine(String str) {
		return str.split("\\,");
	}
	
	protected int countSpaces(String str) {
		int count = 0;

	    for(int i=0; i < str.length(); i++)
	    {    
	    	if(str.charAt(i) == ' ') {
	    		count ++;
	    	} else {
	    		break;
	    	}
	    }

	    return count;
	}
	
	protected String trimStart(String str) {
        return str.replaceFirst("^\\s+", "");
    }
	
	protected void searchAndAdd(String uuid, AtsMobileElement node, AtsMobileElement elementToAdd){
		if(node.getId().equals(uuid)) {
			node.addChildren(elementToAdd);
		} else {
			var childs = node.getChildren();
			if(childs != null ) {
				for (int i = 0; i < node.getChildren().length; i++) {
					searchAndAdd(uuid, node.getChildren()[i], elementToAdd);
				}
			}
		}
	}
}
