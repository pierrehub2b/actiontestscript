package com.ats.generator.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;

public class MouseDirection {

	public static final Pattern POSITION_REGEXP = Pattern.compile("(" + Cartesian.RIGHT + "|" + Cartesian.TOP + "|" + Cartesian.LEFT + "|" + Cartesian.BOTTOM + "|" + Cartesian.MIDDLE + "|" + Cartesian.CENTER + ")\\s*?\\((-?\\d+)\\)", Pattern.CASE_INSENSITIVE);

	private MouseDirectionData horizontalPos;
	private MouseDirectionData verticalPos;

	public MouseDirection() {}
	
	public MouseDirection(ArrayList<String> options) {
		Iterator<String> itr = options.iterator();
		while (itr.hasNext()){
			if(addPosition(itr.next())){
				itr.remove();
			}
		}
	}

	public MouseDirection(MouseDirectionData hpos, MouseDirectionData vpos) {
		setHorizontalPos(hpos);
		setVerticalPos(vpos);
	}

	public boolean addPosition(String value) {
	  
		Matcher match = MouseDirection.POSITION_REGEXP.matcher(value);
		if(match.find()){
			
			String name = match.group(1);
			String pos = match.group(2);
			
			if(Cartesian.RIGHT.equals(name) || Cartesian.LEFT.equals(name) || Cartesian.CENTER.equals(name)){
				setHorizontalPos(new MouseDirectionData(name, pos));
			}else if(Cartesian.TOP.equals(name) || Cartesian.BOTTOM.equals(name) || Cartesian.MIDDLE.equals(name)){
				setVerticalPos(new MouseDirectionData(name, pos));
			}
			return true;
		}
		
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode() {
		
		if(horizontalPos != null || verticalPos != null) {
			
			ArrayList<String> codeData = new ArrayList<String>();

			if(horizontalPos != null){
				codeData.add(ActionTestScript.JAVA_POS_FUNCTION_NAME + "(" + horizontalPos.getJavaCode() + ")");
			}else {
				codeData.add("null");
			}
			
			if(verticalPos != null){
				codeData.add(ActionTestScript.JAVA_POS_FUNCTION_NAME + "(" + verticalPos.getJavaCode() + ")");
			}else {
				codeData.add("null");
			}

			return ", " + String.join(", ", codeData);
		}
		
		return "";
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public MouseDirectionData getHorizontalPos() {
		return horizontalPos;
	}

	public void setHorizontalPos(MouseDirectionData horizontalPos) {
		this.horizontalPos = horizontalPos;
	}

	public MouseDirectionData getVerticalPos() {
		return verticalPos;
	}

	public void setVerticalPos(MouseDirectionData verticalPos) {
		this.verticalPos = verticalPos;
	}
}