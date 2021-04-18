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

package com.ats.tools;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import com.ats.element.AtsBaseElement;
import com.ats.generator.variables.CalculatedValue;

public final class Operators {

	public static final String REGEXP = "=~";
	public static final String DIFFERENT = "<>";
	public static final String LOWER_EQUAL = "<=";
	public static final String GREATER_EQUAL = ">=";
	
	public static final String EQUAL = "=";
	public static final String LOWER = "<";
	public static final String GREATER = ">";

	public static final Pattern REGEXP_PATTERN = Pattern.compile("(.*)" + REGEXP + "(.*)");
	public static final Pattern EQUAL_PATTERN = Pattern.compile("(.*)" + EQUAL + "(.*)");
	
	private static final Trie trieOperator = Trie.builder().ignoreOverlaps().addKeywords(REGEXP).addKeyword(GREATER_EQUAL).addKeyword(LOWER_EQUAL).addKeyword(EQUAL).addKeyword(LOWER).addKeyword(GREATER).build();
	
	private String type = EQUAL;
	private Pattern regexpPattern = Pattern.compile(".*");
	
	public Operators() {}
	
	public Operators(String value) {
		setType(value);
	}
	
	public Predicate<AtsBaseElement> getPredicate(Predicate<AtsBaseElement> predicate, String name, CalculatedValue value) {
		if(REGEXP.equals(getType())){
			return predicate.and(p -> regexpMatch(p.getAttribute(name)));
		}else {
			return predicate.and(p -> textEquals(p.getAttribute(name), value.getCalculated()));
		}
	}
	
	public void updatePattern(String calculated) {
		try {
			regexpPattern = Pattern.compile(calculated);
		}catch(PatternSyntaxException e) {
			regexpPattern = Pattern.compile(".*");
		}
	}
	
	public String check(String data, String calculated) {
		if(REGEXP.equals(getType())){
			if(!regexpMatch(data)) {
				return "does not match regex pattern";
			}
		}else if(DIFFERENT.equals(getType())){
			if(textEquals(data, calculated)) {
				return "is not different than";
			}
		}else if(GREATER.equals(getType())) {
			try {
				if(Double.parseDouble(data) <= Double.parseDouble(calculated)) {
					return "is not greater than";
				}
			}catch (NumberFormatException e) {
				return "cannot be compared as number with";
			}
		}else if(LOWER.equals(getType())) {
			try {
				if(Double.parseDouble(data) >= Double.parseDouble(calculated)) {
					return "is not lower than";
				}
			}catch (NumberFormatException e) {
				return "cannot be compared as number with";
			}
		}else if(GREATER_EQUAL.equals(getType())) {
			try {
				if(Double.parseDouble(data) < Double.parseDouble(calculated)) {
					return "is not greater or equals to";
				}
			}catch (NumberFormatException e) {
				return "cannot be compared as number with";
			}
		}else if(LOWER_EQUAL.equals(getType())) {
			try {
				if(Double.parseDouble(data) > Double.parseDouble(calculated)) {
					return "is not lower or equals to";
				}
			}catch (NumberFormatException e) {
				return "cannot be compared as number with";
			}
		}else {
			if(!textEquals(data, calculated)) {
				return "is not equals to";
			}
		}
		
		return null;
	}
	
	public boolean textEquals(String data, String calculated){
		if(data == null) {
			return false;
		}
		return data.equals(calculated);
	}
	
	public boolean regexpMatch(String data){
		if(data == null) {
			return false;
		}
		return regexpPattern.matcher(data).matches();
	}
	
	public static String getJavaCode(String op) {
		final String code = Operators.class.getSimpleName() + ".";

		switch (op) {
		case LOWER:
			return code + "LOWER";
		case GREATER:
			return code + "GREATER";
		case DIFFERENT:
			return code + "DIFFERENT";
		case LOWER_EQUAL:
			return code + "LOWER_EQUAL";
		case GREATER_EQUAL:
			return code + "GREATER_EQUAL";
		case REGEXP:
			return code + "REGEXP";
		default:
			return code + "EQUAL";
		}
	}
	
	public String getJavaCode() {
		return getJavaCode(type);
	}
	
	public boolean isRegexp() {
		return REGEXP.equals(type);
	}
	
	public String[] initData(String data) {
		final Optional<Emit> opt = trieOperator.parseText(data).stream().findFirst();
		if(opt.isPresent()) {
			final Emit emit = opt.get();
			setType(emit.getKeyword());
			return new String[] {data.substring(0, emit.getStart()).trim(), data.substring(emit.getEnd()+1).trim()}; 
		}
		
		return new String[] {data.trim(), ""}; 
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
}