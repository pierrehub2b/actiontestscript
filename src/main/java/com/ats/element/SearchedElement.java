package com.ats.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.Script;

public class SearchedElement {

	private static final Pattern OBJECT_PATTERN = Pattern.compile("(.*)\\[(.*)\\]", Pattern.CASE_INSENSITIVE);
	private static final Pattern OBJECT_INDEX_PATTERN = Pattern.compile("\\s*?index\\((\\d+)\\)\\s*?");

	private String tag = "*";
	
	private SearchedElement parent;
	
	private int index = 0;

	private List<CalculatedProperty> criterias;

	public SearchedElement() {

	}

	public SearchedElement(Script script, ArrayList<String> elements) {

		String value = elements.remove(0);

		setCriterias(new ArrayList<CalculatedProperty>());
		
		Matcher objectMatcher = OBJECT_PATTERN.matcher(value);

		if (objectMatcher.find()) {

			if(objectMatcher.groupCount() >= 1){

				setTag(objectMatcher.group(1).trim());

				if(objectMatcher.groupCount() >= 2){
					Stream<String> stream1 = Arrays.stream(objectMatcher.group(2).split(","));
					stream1.forEach(s -> addCriteria(script, s));
					//stream1.forEach(s -> addCriteria(script, s.replace("%2C", ",").replace("%3D", "=").replace("%5B", "[").replace("%5D", "]").replace("%20", " ")));
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

		StringBuilder codeBuilder = new StringBuilder(ActionTestScript.JAVA_ELEMENT_FUNCTION_NAME + "(");

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