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

package com.ats.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;

public class SearchedElement {

	public static final String WILD_CHAR = "*";
	
	private static final String DIALOG = "DIALOG";
	private static final String SYSCOMP = "SYSCOMP";
	private static final String SYSBUTTON = "SYSBUTTON";
	
	private static final Pattern OBJECT_INDEX_PATTERN = Pattern.compile("\\s*?index\\((\\d+)\\)\\s*?");

	private String tag = WILD_CHAR;
	private SearchedElement parent;
	private int index = 0;
	private List<CalculatedProperty> criterias;

	public SearchedElement() {} // default constructor

	public SearchedElement(Script script, ArrayList<String> elements) {

		final String value = elements.remove(0);
		final Matcher objectMatcher = Script.OBJECT_PATTERN.matcher(value);
		
		setCriterias(new ArrayList<CalculatedProperty>());

		if (objectMatcher.find()) {

			if(objectMatcher.groupCount() >= 1){

				setTag(objectMatcher.group(1).trim());

				if(objectMatcher.groupCount() >= 2){
					Arrays.stream(objectMatcher.group(2).split(",")).forEach(s -> addCriteria(script, s));
				}
				
			}else{
				setTag(value.trim());
			}
			
		}else if(value != null){
			setTag(value.trim());
		}
				
		if(elements.size() > 0){
			this.setParent(new SearchedElement(script, elements));
		}
	}

	public SearchedElement(int index, String tag, CalculatedProperty[] properties) {
		this(null, index, tag, properties);
	}
	
	public SearchedElement(SearchedElement parent, int index, String tag, CalculatedProperty[] properties) {
		setParent(parent);
		setIndex(index);
		setTag(tag);
		setCriterias(Arrays.asList(properties));
	}
	
	public void dispose() {
		if(parent != null) {
			parent.dispose();
		}
		
		if(criterias != null) {
			while(criterias.size() > 0) {
				criterias.remove(0).dispose();
			}
		}
	}
	
	public boolean isDialog() {
		return DIALOG.equals(tag.toUpperCase());
	}
	
	public boolean isSysButton() {
		return SYSBUTTON.equals(tag.toUpperCase());
	}
	
	public boolean isSysComp() {
		if(parent != null) {
			return parent.isSysComp();
		}
		return SYSCOMP.equals(tag.toUpperCase());
	}

	private void addCriteria(Script script, String data){
		Matcher match = OBJECT_INDEX_PATTERN.matcher(data);
		if(match.find()){
			try{
				this.index = Integer.parseInt(match.group(1));
			}catch(NumberFormatException ex){}
		}else{
			this.criterias.add(new CalculatedProperty(script, data));
		}
	}

	public String getJavaCode() {

		StringBuilder codeBuilder = new StringBuilder(ActionTestScript.JAVA_ELEMENT_FUNCTION_NAME);
		codeBuilder.append("(");
		
		if(parent != null){
			codeBuilder.append(parent.getJavaCode());
			codeBuilder.append(", ");
		}

		codeBuilder.append(index);
		codeBuilder.append(", \"");
		codeBuilder.append(getTag());
		
		if(criterias != null && criterias.size() > 0){

			codeBuilder.append("\", ");

			StringJoiner joiner = new StringJoiner(", ");
			for (CalculatedProperty criteria : criterias){
				joiner.add(criteria.getJavaCode());
			}
			
			codeBuilder.append(joiner.toString());

		}else{
			codeBuilder.append("\"");
		}
		
		codeBuilder.append(")");

		return codeBuilder.toString();
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public SearchedElement getParent() {
		return parent;
	}

	public void setParent(SearchedElement value) {
		this.parent = value;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int value) {
		this.index = value;
	}

	public List<CalculatedProperty> getCriterias() {
		return criterias;
	}

	public void setCriterias(List<CalculatedProperty> value) {
		this.criterias = value;
	}
}