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

import com.ats.generator.objects.MouseDirectionData;

public class MouseSwipe extends Mouse {

	private int hdir;
	private int vdir;
	
	public MouseSwipe(int hdir, int vdir) {
		setHdir(hdir);
		setVdir(vdir);
	}

	public MouseSwipe(int hdir, int vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		super(hpos, vpos);
		setHdir(hdir);
		setVdir(vdir);
	}
	
	public int getHdir() {
		return hdir;
	}

	public void setHdir(int hdir) {
		this.hdir = hdir;
	}

	public int getVdir() {
		return vdir;
	}

	public void setVdir(int vdir) {
		this.vdir = vdir;
	}
}
