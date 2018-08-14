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

package com.ats.generator.objects.mouse;

import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;

public class Mouse {

	public static final String OVER = "over";
	public static final String CLICK = "click";
	public static final String DOUBLE_CLICK = CLICK + "-double";
	public static final String RIGHT_CLICK = CLICK + "-right";
	public static final String WHEEL_CLICK = CLICK + "-wheel";

	public static final String DRAG = "drag";
	public static final String DROP = "drop";

	private String type = "undefined";
	private MouseDirection position;

	public Mouse() {
		setPosition(new MouseDirection());
	}
	
	public Mouse(String type) {
		setType(type);
		setPosition(new MouseDirection());
	}

	public Mouse(MouseDirectionData hpos, MouseDirectionData vpos) {
		setPosition(new MouseDirection(hpos, vpos));
	}
	
	public Mouse(String type, MouseDirectionData hpos, MouseDirectionData vpos) {
		setType(type);
		setPosition(new MouseDirection(hpos, vpos));
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MouseDirection getPosition() {
		return position;
	}

	public void setPosition(MouseDirection position) {
		this.position = position;
	}
}