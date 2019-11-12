package com.ats.executor.drivers.engines.mobiles;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.ats.element.AtsMobileElement;
import com.ats.element.FoundElement;
import com.ats.element.MobileTestElement;
import com.ats.element.StructDebugDescription;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.google.gson.JsonObject;

public class IosRootElement extends RootElement {	

	private String regexSpaces = "^\\s+";
	private String regexBraces = "[{},]+";
	private String regexBracesAndSpaces = "[\\s{},]+";
	
	private Double deviceWidth = 0.0;
	private Double deviceHeight = 0.0;
	
	private Double ratioWidth = 1.0;
	private Double ratioHeight = 1.0;
	
	public IosRootElement(MobileDriverEngine drv) {
		super(drv);
	}
		
	@Override
	public MobileTestElement getCurrentElement(FoundElement element, MouseDirection position) {

		final Rectangle rect = element.getRectangle();
		
		final StringBuilder coordinates = new StringBuilder()
		.append(element.getX()).append(";")
		.append(element.getY()).append(";")
		.append(element.getWidth()).append(";")
		.append(element.getHeight()).append(";")
		.append(getRatioWidth()).append(";")
		.append(getRatioHeight());
				
		return new MobileTestElement(
				element.getId(), 
				(int)(driver.getOffsetX(rect, position)), 
				(int)(driver.getOffsetY(rect, position)), 
				coordinates.toString()
			);
	}

	@Override
	public void tap(ActionStatus status, FoundElement element, MouseDirection position) {
		final Rectangle rect = element.getRectangle();

		final StringBuilder coordinates = new StringBuilder()
		.append(element.getX()).append(";")
		.append(element.getY()).append(";")
		.append(element.getWidth()).append(";")
		.append(element.getHeight()).append(";")
		.append(getRatioWidth()).append(";")
		.append(getRatioHeight());
	
		driver.executeRequest(MobileDriverEngine.ELEMENT, 
				element.getId(), 
				MobileDriverEngine.TAP, 
				(int)(driver.getOffsetX(rect, position)) + "", 
				(int)(driver.getOffsetY(rect, position)) + "", 
				coordinates.toString()
			);
	}
		
	@Override
	public void swipe(MobileTestElement testElement, int hDirection, int vDirection) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, testElement.getId(), MobileDriverEngine.SWIPE, testElement.getOffsetX() + "", testElement.getOffsetY() + "", hDirection + "", + vDirection + "",  testElement.getCoordinates());
	}

	@Override
	public void refresh(JsonObject jsonObject) {
		
		if(jsonObject == null || jsonObject.get("root") == null) {
			return;
		}
		
		this.deviceHeight = jsonObject.get("deviceHeight").getAsDouble();
		this.deviceWidth = jsonObject.get("deviceWidth").getAsDouble();
		
		final String debugDescription = jsonObject.get("root").getAsString();
		final String[] debugDescriptionArray = debugDescription.split("\n");
		
		Double width = 0.0;
		Double height = 0.0;
		
		final ArrayList<StructDebugDescription> structDebug = new ArrayList<StructDebugDescription>();

		for (int i = 0; i < debugDescriptionArray.length; i++) {
			int level = countSpaces(debugDescriptionArray[i]);
			if(level >= 4 && !debugDescriptionArray[i].contains("Application, pid:")) {
				String trimmedLine = debugDescriptionArray[i].replaceAll(regexSpaces, "");
				if(trimmedLine.startsWith("Window (Main)")) {
					final String[] arraySize = trimmedLine.split(regexBracesAndSpaces);
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
		
		final AtsMobileElement domStructure = new AtsMobileElement(UUID.randomUUID().toString(),"root",width, height, 0.0,0.0, false, new HashMap<String, String>());
	
		int currentIndex = 0;
		for (StructDebugDescription structDebugDescription : structDebug) {
			
			final String tag = structDebugDescription.getContent().split(regexBraces)[0].replaceAll(regexSpaces, "");
			final String[] arraySize = structDebugDescription.getContent().split(regexBraces);
			
			final ArrayList<String> stringArray = new ArrayList<String>();

			for (int i = 0; i < arraySize.length; i++) {
				if(!arraySize[i].replaceAll(regexSpaces, "").equals("")) {
					stringArray.add(arraySize[i]);
				}
			}
			
			final double currentX = Double.parseDouble(stringArray.get(2)) * ratioWidth;
			final double currentY = Double.parseDouble(stringArray.get(3)) * ratioHeight; 
			final double currentWidth = Double.parseDouble(stringArray.get(4)) * ratioWidth;
			final double currentheight = Double.parseDouble(stringArray.get(5)) * ratioHeight; 
			
			if(currentX < 0.0 || currentX > height) {
				continue;
			}
			
			final AtsMobileElement currentAtsMobileElement = new AtsMobileElement(
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
				int i = currentIndex;
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
		
		final AtsMobileElement[] firstChilds = domStructure.getChildren();
		if(firstChilds != null && firstChilds.length >= 0) {
			final AtsMobileElement[] frontElements = firstChilds[0].getChildren();
			if(frontElements != null && frontElements.length > 0) {
				// at least 2 elements; so get only the last one
				final AtsMobileElement[] child = new AtsMobileElement[1];
				//check consistancy of second element 
				var containsElements = checkConsistancy(child);
				if(containsElements) {
					child[0] = frontElements[frontElements.length-1];
				} else {
					child[0] = frontElements[0];
				}
				domStructure.getChildren()[0].setChildren(child);
			}
		}
		
		this.value = domStructure;
	}
	
	public Boolean checkConsistancy(AtsMobileElement[] child) {
		for (int i = 0; i < child.length; i++) {
			if(child[i] != null && child[i].getChildren().length > 0) {
				return checkConsistancy(child[i].getChildren());
			} else {
				if(child[i] != null && !child[i].getTag().equalsIgnoreCase("other")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Double getDeviceWidth() {
		return this.deviceWidth;
	}
	
	public Double getDeviceHeight() {
		return this.deviceHeight;
	}
	
	public Double getRatioWidth() {
		return this.ratioWidth;
	}
	
	public Double getRatioHeight() {
		return this.ratioHeight;
	}

	private Map<String,String> getAttributes(String str) {
		TreeMap<String, String> result = new TreeMap<String, String>();
		String[] arraySize = str.split(regexBraces);
		
		if(arraySize[0].contains("checkbox")) {
			result.put("checkable", "true");
		} else 
		{
			result.put("checkable", "false");
		}
		
		for (String s : arraySize) {
			final String cleanString = cleanAttribute(s);
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
			result.put("text", "");
		}
		if(!result.containsKey("description")) {
			result.put("description", "");
		}
		if(!result.containsKey("identifier")) {
			result.put("identifier", "");
		}
		if(!result.containsKey("value")) {
			result.put("value", "");
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
	
	private String cleanAttribute(String str) {
		return str.split(":")[str.split(":").length-1].replace("\'", "").replaceAll(regexSpaces, "");
	}
	
	private int countSpaces(String str) {
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
	
	private void searchAndAdd(String uuid, AtsMobileElement node, AtsMobileElement elementToAdd){
		if(node.getId().equals(uuid)) {
			node.addChildren(elementToAdd);
		} else {
			final AtsMobileElement[] childs = node.getChildren();
			if(childs != null ) {
				for (int i = 0; i < node.getChildren().length; i++) {
					searchAndAdd(uuid, node.getChildren()[i], elementToAdd);
				}
			}
		}
	}
}