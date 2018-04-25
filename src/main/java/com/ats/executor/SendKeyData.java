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

package com.ats.executor;

import org.openqa.selenium.Keys;

public class SendKeyData {

	private static final String KEY_DOWN_SHIFT = "SHIFT";
	private static final String KEY_DOWN_ALT = "ALT";
	private static final String KEY_DOWN_CONTROL = "CONTROL";

	//private StringBuffer sequence = null;
	//private boolean altDown = false;
	//private boolean shiftDown = false;
	//private boolean controlDown = false;

	private String data;
	private CharSequence chord;

	public SendKeyData(String key, String spare) {

		//this.sequence = new StringBuffer();

		this.data = spare;

		StringBuffer sequence = new StringBuffer();

		if(spare != null && spare.length() > 0) {

			if(KEY_DOWN_SHIFT.equals(key)) {
				sequence.append(Keys.SHIFT);
			}else if(KEY_DOWN_ALT.equals(key)) {
				sequence.append(Keys.ALT);
			}else if(KEY_DOWN_CONTROL.equals(key)) {
				sequence.append(Keys.CONTROL);
			}
			sequence.append(spare.toLowerCase());
			

		}else {
			try {
				sequence.append(Keys.valueOf(key));
			}catch(IllegalArgumentException e) {}
		}

		chord = sequence;

		/*try {
			this.sequence.append(Keys.valueOf(spare));
		}catch(IllegalArgumentException e) {
			this.sequence.append(spare.toLowerCase());
		}*/
	}

	public SendKeyData(String data) {

		this.data = data;

		/*this.sequence = new StringBuffer();

		for (int i = 0, n = data.length(); i < n; i++) {
			char c = data.charAt(i);
			if(Character.isDigit(c)) {
				this.sequence.append(getNumpad(c));
			}else {
				this.sequence.append(c);
			}
	    }*/
	}

	private Keys getNumpad(char d) {

		switch (Character.getNumericValue(d)) {
		case 1 :
			return Keys.NUMPAD1;
		case 2 :
			return Keys.NUMPAD2;
		case 3 :
			return Keys.NUMPAD3;
		case 4 :
			return Keys.NUMPAD4;
		case 5 :
			return Keys.NUMPAD5;
		case 6 :
			return Keys.NUMPAD6;
		case 7 :
			return Keys.NUMPAD7;
		case 8 :
			return Keys.NUMPAD8;
		case 9 :
			return Keys.NUMPAD9;
		}
		return Keys.NUMPAD0;
	}

	public CharSequence getSequence() {

		if(chord != null) {
			return chord;
		}
		/*if(shiftDown) {
			return Keys.chord(Keys.SHIFT, data);
		}else if(altDown) {
			return Keys.chord(Keys.ALT, data);
		}else if(controlDown) {
			return Keys.chord(Keys.CONTROL, data);
		}*/

		StringBuffer sequence = new StringBuffer();

		for (int i = 0, n = data.length(); i < n; i++) {
			char c = data.charAt(i);
			if(Character.isDigit(c)) {
				sequence.append(getNumpad(c));
			}else {
				sequence.append(c);
			}
		}

		return sequence;
	}

	public CharSequence getSequenceFirefox() {

		if(chord != null) {
			return chord;
		}
		
		/*if(shiftDown) {
			return Keys.chord(Keys.SHIFT, data);
		}else if(altDown) {
			return Keys.chord(Keys.ALT, data);
		}else if(controlDown) {
			return Keys.chord(Keys.CONTROL, data);
		}*/

		return data;
	}

}