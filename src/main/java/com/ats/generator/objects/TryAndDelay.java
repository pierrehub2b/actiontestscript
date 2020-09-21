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

import com.ats.tools.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class TryAndDelay {

	private static final String TRY_LABEL = "try";
	private static final String[] LABEL_REPLACE = new String[]{TRY_LABEL, "=", " ", "(", ")"};
	private static final String[] LABEL_REPLACEMENT = new String[]{"", "", "", "", ""};

	private static final String DELAY_LABEL = "delay";
	private static final String[] DELAY_REPLACE = new String[]{DELAY_LABEL, "=", " "};
	private static final String[] DELAY_REPLACEMENT = new String[]{"", "", ""};
	
	private int maxTry = 0;
	private int delay = 0;
	
	public TryAndDelay() {}
	
	public TryAndDelay(int maxTry, int delay) {
		setMaxTry(maxTry);
		setDelay(delay);
	}
	
	public TryAndDelay(ArrayList<String> options) {
		final int[] data = getTryAndDelay(options);
		setMaxTry(data[0]);
		setDelay(data[1]);
	}
	
	public StringBuilder getJavaCode() {
		return new StringBuilder(Integer.toString(maxTry)).append(", ").append(delay).append(", ");
	}
	
	//--------------------------------------------------------
	// utils
	//--------------------------------------------------------
	
	public static int[] getTryAndDelay(ArrayList<String> options) {
		final int[] result = new int[] {0, 0};
		final Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			final String opt = itr.next().toLowerCase();
			if(opt.contains(TRY_LABEL)) {
				result[0] = Utils.string2Int(StringUtils.replaceEach(opt, LABEL_REPLACE, LABEL_REPLACEMENT));
				itr.remove();
			}else if(opt.contains(DELAY_LABEL)) {
				result[1] = Utils.string2Int(StringUtils.replaceEach(opt, DELAY_REPLACE, DELAY_REPLACEMENT));
				itr.remove();
			}
		}
		return result;
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getMaxTry() {
		return maxTry;
	}
	
	public void setMaxTry(int maxTry) {
		this.maxTry = Math.max(maxTry, -10);
	}
	
	public int getDelay() {
		return delay;
	}
	
	public void setDelay(int delay) {
		this.delay = Math.max(delay, 0);
	}
}