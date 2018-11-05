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

public class MouseSwipe extends Mouse {

	private MouseDirection direction;
	
	public MouseSwipe(MouseDirectionData dir1, MouseDirectionData dir2) {
		setDirection(new MouseDirection(dir1, dir2));
	}
	
	public MouseSwipe(MouseDirectionData dir1, MouseDirectionData dir2, MouseDirectionData hpos, MouseDirectionData vpos) {
		super(hpos, vpos);
		setDirection(new MouseDirection(dir1, dir2));
	}
	
	public MouseDirection getDirection() {
		return direction;
	}

	public void setDirection(MouseDirection value) {
		this.direction = value;
	}
}
