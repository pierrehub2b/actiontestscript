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

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Project;
import com.ats.script.Script;
import com.ats.tools.Utils;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;

public class SearchedElement {

	public static final String WILD_CHAR = "*";

	private static final String DIALOG = "DIALOG";
	private static final String SYSCOMP = "SYSCOMP";
	public static final String IMAGE_TAG = "@IMAGE";
	
	public static final String INDEX_PROPERTY = "@index";

	private String tag = WILD_CHAR;
	private SearchedElement parent;

	private int index = 0;
	private List<CalculatedProperty> criterias;
	
	private byte[] image;

	public SearchedElement() {} // default constructor
	
	public SearchedElement(String tag) {
		this.tag = tag;
		this.criterias = new ArrayList<CalculatedProperty>();
	}

	public SearchedElement(Script script, ArrayList<String> elements) {

		final String value = elements.remove(0);
		final Matcher objectMatcher = Script.OBJECT_PATTERN.matcher(value);

		setCriterias(new ArrayList<CalculatedProperty>());

		if (objectMatcher.find()) {

			if(objectMatcher.groupCount() >= 1){

				setTag(objectMatcher.group(1).trim());

				if(objectMatcher.groupCount() >= 2){
					Arrays.stream(objectMatcher.group(2).split(",")).map(String::trim).forEach(s -> addCriteria(script, s));
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

	private void addCriteria(Script script, String data){
		if(data.startsWith(INDEX_PROPERTY)) {
			this.index = Utils.string2Int(StringUtils.getDigits(data));
		}else{
			this.criterias.add(new CalculatedProperty(script, data));
		}
	}

	//----------------------------------------------------------------------------------------------------------------
	// image management
	//----------------------------------------------------------------------------------------------------------------
	
	public void setImage(byte[] value) {
		this.image = value;
	}

	public byte[] getImage() {
		if(image == null) {
			final CalculatedProperty prop = criterias.stream().filter(c -> "source".equals(c.getName())).findFirst().orElse(null);
			if(prop != null) {

				final String imagePath = prop.getValue().getCalculated();
				
				URL imageUrl = null;
				if(imagePath.startsWith("http://") || imagePath.startsWith("https://") || imagePath.startsWith("file://")) {
					try {
						imageUrl = new URL(imagePath);
					} catch (MalformedURLException e) {}
				}else {
					final String relativePath = Project.ASSETS_FOLDER + "/" + Project.RESOURCES_FOLDER + "/" + Project.IMAGES_FOLDER + "/" + imagePath;
					imageUrl = getClass().getClassLoader().getResource(relativePath);
				}

				if(imageUrl != null) {
					image = Utils.loadImage(imageUrl);
				}
			}
		}
		return image;
	}

	//----------------------------------------------------------------------------------------------------------------
	// types
	//----------------------------------------------------------------------------------------------------------------

	public boolean isImageSearch() {
		return IMAGE_TAG.equals(tag.toUpperCase());
	}

	public boolean isDialog() {
		return DIALOG.equals(tag.toUpperCase());
	}
	
	public boolean isScrollable() {
		return tag.equals("RecyclerView") || tag.equals("Table") || tag.equals("CollectionView");
	}
	
	public boolean isSysComp() {
		if(parent != null) {
			return parent.isSysComp();
		}
		return SYSCOMP.equals(tag.toUpperCase());
	}

	//----------------------------------------------------------------------------------------------------------------
	//
	//----------------------------------------------------------------------------------------------------------------

	public String getJavaCode() {

		final StringBuilder codeBuilder = new StringBuilder(ActionTestScript.JAVA_ELEMENT_FUNCTION_NAME);
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

			final StringJoiner joiner = new StringJoiner(", ");
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
	
	public ArrayList<String> getKeywords() {
		final ArrayList<String> keywords = new ArrayList<String>();
		keywords.add(tag);
		
		for (CalculatedProperty crit : criterias) {
			keywords.addAll(crit.getKeywords());
		}
		
		if(parent != null) {
			keywords.addAll(parent.getKeywords());
		}
		
		return keywords;
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