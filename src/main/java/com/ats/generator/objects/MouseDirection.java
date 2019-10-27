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

package com.ats.generator.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.ats.executor.ActionTestScript;

public class MouseDirection {

	private MouseDirectionData horizontalPos;
	private MouseDirectionData verticalPos;

	public MouseDirection() {}

	public MouseDirection(ArrayList<String> options, boolean canBeEmpty) {
		Iterator<String> itr = options.iterator();
		while (itr.hasNext()){
			if(addPosition(itr.next())){
				itr.remove();
			}
		}

		if(!canBeEmpty && this.horizontalPos == null && this.verticalPos == null) {
			this.setHorizontalPos(new MouseDirectionData(Cartesian.RIGHT, 20));
		}
	}

	public MouseDirection(MouseDirectionData hpos, MouseDirectionData vpos) {
		setHorizontalPos(hpos);
		setVerticalPos(vpos);
	}

	public boolean addPosition(String value) {

		String data = Cartesian.RIGHT.extractData(value);
		if(data != null) {
			setHorizontalPos(new MouseDirectionData(Cartesian.RIGHT.toString(), data));
			return true;
		}

		data = Cartesian.RIGHT.extractData(value);
		if(data != null) {
			setHorizontalPos(new MouseDirectionData(Cartesian.RIGHT.toString(), data));
			return true;
		}

		data = Cartesian.CENTER.extractData(value);
		if(data != null) {
			setHorizontalPos(new MouseDirectionData(Cartesian.CENTER.toString(), data));
			return true;
		}

		data = Cartesian.TOP.extractData(value);
		if(data != null) {
			setVerticalPos(new MouseDirectionData(Cartesian.TOP.toString(), data));
			return true;
		}

		data = Cartesian.BOTTOM.extractData(value);
		if(data != null) {
			setVerticalPos(new MouseDirectionData(Cartesian.BOTTOM.toString(), data));
			return true;
		}

		data = Cartesian.MIDDLE.extractData(value);
		if(data != null) {
			setVerticalPos(new MouseDirectionData(Cartesian.MIDDLE.toString(), data));
			return true;
		}

		return false;
	}

	public int getHorizontalDirection() {
		if(horizontalPos != null) {
			return horizontalPos.getHorizontalDirection();
		}
		return 0;
	}

	public int getVerticalDirection() {
		if(verticalPos != null) {
			return verticalPos.getVerticalDirection();
		}
		return 0;
	}

	public void updateForDrag() {
		setHorizontalPos(null);
		setVerticalPos(new MouseDirectionData(Cartesian.BOTTOM, -20));
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	private String getJavaCode(ArrayList<String> codeData) {
		if(horizontalPos != null || verticalPos != null) {
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
			return String.join(", ", codeData);
		}
		return "";
	}

	public String getPositionJavaCode() {
		return getJavaCode(new ArrayList<String>(Arrays.asList(new String[]{""})));
	}

	public String getDirectionJavaCode() {
		return getJavaCode(new ArrayList<String>());
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