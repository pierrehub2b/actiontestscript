package com.ats.executor.drivers.engines.mobiles;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.ats.element.*;
import com.ats.executor.ActionStatus;
import com.ats.executor.drivers.engines.MobileDriverEngine;
import com.ats.generator.objects.MouseDirection;
import com.google.gson.JsonObject;

public class IosRootElement extends RootElement {

	private final String regexSpaces = "^\\s+";
	private final String regexBraces = "[{},]+";
	
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
		String coordinates = element.getX() + ";" + element.getY() + ";" + element.getWidth() + ";" + element.getHeight() + ";" + getRatioWidth() + ";" + getRatioHeight();
		return new MobileTestElement(element.getId(), (driver.getOffsetX(rect, position)), (driver.getOffsetY(rect, position)), coordinates);
	}

	@Override
	public void tap(ActionStatus status, FoundElement element, MouseDirection position) {
		final Rectangle rect = element.getRectangle();
		String coordinates = element.getX() + ";" + element.getY() + ";" + element.getWidth() + ";" + element.getHeight() + ";" + getRatioWidth() + ";" + getRatioHeight();
		driver.executeRequest(
				MobileDriverEngine.ELEMENT,
				element.getId(),
				MobileDriverEngine.TAP,
				(driver.getOffsetX(rect, position)) + "",
				(driver.getOffsetY(rect, position)) + "",
				coordinates
		);
	}
	
	@Override
	public void tap(FoundElement element, int count) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.TAP, String.valueOf(count));
	}
	
	@Override
	public void press(FoundElement element, ArrayList<String> paths, int duration) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.TAP, String.valueOf(paths), String.valueOf(duration));
	}
	
	@Override
	public void swipe(MobileTestElement testElement, int hDirection, int vDirection) {
		driver.executeRequest(MobileDriverEngine.ELEMENT, testElement.getId(), MobileDriverEngine.SWIPE, testElement.getOffsetX() + "", testElement.getOffsetY() + "", hDirection + "", +vDirection + "", testElement.getCoordinates());
	}

	@Override
	public Object scripting(String script, FoundElement element) {
		String coordinates = element.getX() + ";" + element.getY() + ";" + element.getWidth() + ";" + element.getHeight() + ";" + getRatioWidth() + ";" + getRatioHeight();
		return driver.executeRequest(MobileDriverEngine.ELEMENT, element.getId(), MobileDriverEngine.SCRIPTING, "0", "0", coordinates, script);
	}

	@Override
	public Object scripting(String script) {
		return scripting(script, getValue().getFoundElement());
	}

	@Override
	public void refresh(JsonObject jsonObject) {

		if (jsonObject == null || jsonObject.get("root") == null) {
			return;
		}

		this.deviceHeight = jsonObject.get("deviceHeight").getAsDouble();
		this.deviceWidth = jsonObject.get("deviceWidth").getAsDouble();

		final String debugDescription = jsonObject.get("root").getAsString();
		final String[] debugDescriptionArray = debugDescription.split("\n");

		Double width = 0.0;
		Double height = 0.0;

		final ArrayList<StructDebugDescription> structDebug = new ArrayList<>();
		
		for (String item : debugDescriptionArray) {
			int level = countSpaces(item);
			if (level >= 4 && !item.contains("Application, pid:")) {
				String trimmedLine = item.replaceAll(regexSpaces, "");
				if (trimmedLine.startsWith("Window (Main)")) {
					String regexBracesAndSpaces = "[\\s{},]+";
					final String[] arraySize = trimmedLine.split(regexBracesAndSpaces);
					width = Double.parseDouble(arraySize[arraySize.length - 2]);
					height = Double.parseDouble(arraySize[arraySize.length - 1]);
				}
				structDebug.add(new StructDebugDescription((level / 2) - 1, trimmedLine));
			}
		}

		// calculate ratio
		if (!height.equals(deviceHeight) || !width.equals(deviceWidth)) {
			ratioHeight = deviceHeight / height;
			ratioWidth = deviceWidth / width;
			width = width * ratioWidth;
			height = height * ratioHeight;
		}

		final AtsMobileElement domStructure = new AtsMobileElement(UUID.randomUUID().toString(), "root", width, height,
				0.0, 0.0, false, new HashMap<>());

		int currentIndex = 0;
		for (StructDebugDescription structDebugDescription : structDebug) {

			final String tag = structDebugDescription.getContent().split(regexBraces)[0].replaceAll(regexSpaces, "");
			final String[] arraySize = structDebugDescription.getContent().split(regexBraces);

			final ArrayList<String> stringArray = new ArrayList<>();
			
			for (String s : arraySize) {
				if (!s.replaceAll(regexSpaces, "").equals("")) {
					stringArray.add(s);
				}
			}

			int firstSizeIndex = 2;
			if (structDebugDescription.getContent().contains("pid:")) {
				firstSizeIndex++;
			}

			final double currentX = Double.parseDouble(stringArray.get(firstSizeIndex)) * ratioWidth;
			final double currentY = Double.parseDouble(stringArray.get(firstSizeIndex + 1)) * ratioHeight;
			final double currentWidth = Double.parseDouble(stringArray.get(firstSizeIndex + 2)) * ratioWidth;
			final double currentHeight = Double.parseDouble(stringArray.get(firstSizeIndex + 3)) * ratioHeight;

			if (currentX < 0.0 || currentX > height) {
				continue;
			}

			final AtsMobileElement currentAtsMobileElement = new AtsMobileElement(
					structDebugDescription.getUuid().toString(), tag, currentWidth, currentHeight, currentX, currentY,
					true, getAttributes(structDebugDescription.getContent()));

			if (structDebugDescription.getLevel() == 1) {
				domStructure.addChildren(currentAtsMobileElement);
			} else {
				// get parentLevel
				int i = currentIndex;
				UUID uuidParent = null;
				while ((i - 1) > -1) {
					if (structDebug.get(i - 1).getLevel() + 1 == structDebugDescription.getLevel()) {
						uuidParent = structDebug.get(i - 1).getUuid();
						break;
					}
					i--;
				}

				if (uuidParent != null) {
					searchAndAdd(uuidParent.toString(), domStructure, currentAtsMobileElement);
				}

			}
			currentIndex++;
		}

		final AtsMobileElement[] firstChilds = domStructure.getChildren();
		if (firstChilds != null) {
			final AtsMobileElement[] frontElements = firstChilds[0].getChildren();
			if (frontElements != null && frontElements.length > 1) {

				final AtsMobileElement child = frontElements[frontElements.length - 1];
				final boolean containsElements = checkConsistency(child.getChildren(), false);

				if (containsElements) {
					domStructure.getChildren()[0].setChildren(child.getChildren());
				} else {
					domStructure.getChildren()[0].setChildren(frontElements[0].getChildren());
				}

			}
		}
		// domStructure.getChildren()[0].setChildren(fixErrorsInDOM(domStructure.getChildren()[0]));
		this.value = domStructure;
	}

	public boolean checkConsistency(AtsMobileElement[] child, boolean val) {
		for (AtsMobileElement atsMobileElement : child) {
			if (atsMobileElement != null && atsMobileElement.getChildren().length > 0) {
				val = checkConsistency(atsMobileElement.getChildren(), val);
			} else {
				if (atsMobileElement != null && !atsMobileElement.getTag().equalsIgnoreCase("other")) {
					val = true;
				}
			}
		}
		return val;
	}

	public AtsMobileElement[] fixErrorsInDOM(AtsMobileElement domStructure) {

		AtsMobileElement[] childrens = domStructure.getChildren();

		for (int i = 0; i < childrens.length; i++) {
			if (childrens[i] != null && childrens[i].getChildren().length > 0) {
				childrens[i].setChildren(fixErrorsInDOM(childrens[i]));
				if (childrens[i].getChildren().length == 0 && childrens[i].getTag().equalsIgnoreCase("other")) {
					childrens = removeTheElement(childrens, i);
					i--;
				}
			} else {
				if (childrens[i] != null && childrens[i].getTag().equalsIgnoreCase("other")
						&& childrens[i].getChildren().length == 0) {
					childrens = removeTheElement(childrens, i);
					i--;
				}
			}
		}

		if (childrens.length == 0) {
			return null;
		}
		return childrens;
	}

	// Function to remove the element
	public static AtsMobileElement[] removeTheElement(AtsMobileElement[] arr, int index) {

		// If the array is empty
		// or the index is not in array range
		// return the original array
		if (arr == null || index < 0 || index >= arr.length) {

			return arr;
		}

		// Create another array of size one less
		AtsMobileElement[] anotherArray = new AtsMobileElement[arr.length - 1];

		// Copy the elements except the index
		// from original array to the other array
		for (int i = 0, k = 0; i < arr.length; i++) {

			// if the index is
			// the removal element index
			if (i == index) {
				continue;
			}

			// if the index is not
			// the removal element index
			anotherArray[k++] = arr[i];
		}

		// return the resultant array
		return anotherArray;
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

	private Map<String, String> getAttributes(String str) {
		TreeMap<String, String> result = new TreeMap<>();
		String[] arraySize = str.split(regexBraces);

		if (arraySize[0].contains("checkbox")) {
			result.put("checkable", "true");
		} else {
			result.put("checkable", "false");
		}

		for (String s : arraySize) {
			final String cleanString = cleanAttribute(s);
			if (s.contains("label")) {
				result.put("text", cleanString);
			}
			if (s.contains("placeholderValue")) {
				result.put("description", cleanString);
			}
			if (s.contains("identifier")) {
				result.put("identifier", cleanString);
			}
			if (s.contains("value")) {
				result.put("value", cleanString);
			}
			if (s.contains("Disabled")) {
				result.put("enabled", "false");
				result.put("editable", "false");
			}
			if (s.contains("Selected")) {
				result.put("selected", "true");
			}
		}

		if (!result.containsKey("text")) {
			result.put("text", "");
		}
		if (!result.containsKey("description")) {
			result.put("description", "");
		}
		if (!result.containsKey("identifier")) {
			result.put("identifier", "");
		}
		if (!result.containsKey("value")) {
			result.put("value", "");
		}
		if (!result.containsKey("enabled")) {
			result.put("enabled", "true");
			result.put("editable", "true");
		}
		if (!result.containsKey("selected")) {
			result.put("selected", "false");
		}
		if (!result.containsKey("numeric")) {
			result.put("numeric", "false");
		}

		return result;
	}

	private String cleanAttribute(String str) {
		return str.split(":")[str.split(":").length - 1].replace("'", "").replaceAll(regexSpaces, "");
	}

	private int countSpaces(String str) {
		int count = 0;

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') {
				count++;
			} else {
				break;
			}
		}

		return count;
	}

	private void searchAndAdd(String uuid, AtsMobileElement node, AtsMobileElement elementToAdd) {
		if (node.getId().equals(uuid)) {
			node.addChildren(elementToAdd);
		} else {
			final AtsMobileElement[] childs = node.getChildren();
			if (childs != null) {
				for (int i = 0; i < node.getChildren().length; i++) {
					searchAndAdd(uuid, node.getChildren()[i], elementToAdd);
				}
			}
		}
	}
}