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

package com.ats.script.actions;

import com.ats.generator.objects.TryAndDelay;
import com.ats.script.Script;

import java.util.ArrayList;

public abstract class ActionExecuteElementAbstract extends ActionExecute {
	
	private TryAndDelay tryAndDelay;
	
	public ActionExecuteElementAbstract() {}
	
	public ActionExecuteElementAbstract(Script script, int stopPolicy, ArrayList<String> options) {
		super(script, stopPolicy);
		setTryAndDelay(new TryAndDelay(options));
	}
	
	public ActionExecuteElementAbstract(Script script, int stopPolicy, int maxTry, int delay) {
		super(script, stopPolicy);
		setTryAndDelay(new TryAndDelay(maxTry, delay));
	}
	
	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append(tryAndDelay.getJavaCode());
	}
		
	public int getMaxTry() {
		return tryAndDelay.getMaxTry();
	}
	
	public int getDelay() {
		return tryAndDelay.getDelay();
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public TryAndDelay getTryAndDelay() {
		return tryAndDelay;
	}

	public void setTryAndDelay(TryAndDelay value) {
		this.tryAndDelay = value;
	}
}