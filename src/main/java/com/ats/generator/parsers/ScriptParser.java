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

package com.ats.generator.parsers;

import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.transform.Transformer;
import com.ats.script.ScriptHeader;
import com.ats.script.ScriptLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

public class ScriptParser {

	public static final String ATS_SEPARATOR = "->";
	public static final int ATS_SEPARATOR_SIZE = ATS_SEPARATOR.length();
	public static final String ATS_ASSIGN_SEPARATOR = "=>";
	public static final String ATS_PROPERTIES_FILE = ".atsProjectProperties";

	public static final String SCRIPT_ID = "id";
	public static final String SCRIPT_GROUPS_LABEL = "groups";
	public static final String SCRIPT_DESCRIPTION_LABEL = "description";
	public static final String SCRIPT_DATE_CREATED_LABEL = "created";
	public static final String SCRIPT_RETURN_LABEL = "return";

	public static final String SCRIPT_AUTHOR_LABEL = "author";
	public static final String SCRIPT_PREREQUISITE_LABEL = "prerequisite";
	
	public static final String SCRIPT_ID_JIRA = "jira";
	public static final String SCRIPT_ID_SQUASH = "squash";
	
	public static final int SCRIPT_ID_LENGTH = SCRIPT_ID.length();
	public static final int SCRIPT_GROUPS_LABEL_LENGTH = SCRIPT_GROUPS_LABEL.length();
	public static final int SCRIPT_DESCRIPTION_LABEL_LENGTH = SCRIPT_DESCRIPTION_LABEL.length();
	public static final int SCRIPT_DATE_CREATED_LABEL_LENGTH = SCRIPT_DATE_CREATED_LABEL.length();
	public static final int SCRIPT_RETURN_LABEL_LENGTH = SCRIPT_RETURN_LABEL.length();

	public static final int SCRIPT_AUTHOR_LABEL_LENGTH = SCRIPT_AUTHOR_LABEL.length();
	public static final int SCRIPT_PREREQUISITE_LABEL_LENGTH = SCRIPT_PREREQUISITE_LABEL.length();
	
	public static final int SCRIPT_ID_JIRA_LENGTH = SCRIPT_ID_JIRA.length();
	public static final int SCRIPT_ID_SQUASH_LENGTH = SCRIPT_ID_SQUASH.length();

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");

	private Lexer lexer;

	public ScriptParser(Lexer lexer) {
		this.lexer = lexer;
	}

	public Lexer getLexer() {
		return lexer;
	}	

	public boolean isGenerator() {
		return lexer.isGenerator();
	}

	public void addScript(){
		lexer.addScript();
	}

	private String getDataGroup(Matcher m, int index){
		if(m.groupCount() > index -1 && m.group(index) != null){
			return m.group(index).trim();
		}
		return "";
	}
	
	private String getHeaderData(String data) {
		return data.substring(data.indexOf(ATS_SEPARATOR) + ATS_SEPARATOR_SIZE).trim();
	}

	public void parse(ScriptLoader script, ScriptHeader header, String data){

		if(data.regionMatches(true, 0, SCRIPT_GROUPS_LABEL, 0, SCRIPT_GROUPS_LABEL_LENGTH)){

			header.parseGroups(getHeaderData(data));

		}else if(data.regionMatches(true, 0, SCRIPT_DESCRIPTION_LABEL, 0, SCRIPT_DESCRIPTION_LABEL_LENGTH)){

			header.setDescription(getHeaderData(data));

		}else if(data.regionMatches(true, 0, SCRIPT_ID, 0, SCRIPT_ID_LENGTH)){

			header.setId(getHeaderData(data));
			
		}else if(data.regionMatches(true, 0, SCRIPT_ID_SQUASH, 0, SCRIPT_ID_SQUASH_LENGTH)){

			header.setSquashId(getHeaderData(data));
			
		}else if(data.regionMatches(true, 0, SCRIPT_ID_JIRA, 0, SCRIPT_ID_JIRA_LENGTH)){

			header.setJiraId(getHeaderData(data));
			
		}else if(data.regionMatches(true, 0, SCRIPT_DATE_CREATED_LABEL, 0, SCRIPT_DATE_CREATED_LABEL_LENGTH)){

			final String dateString = getHeaderData(data);
			try {
				header.setCreatedAt(dateFormat.parse(dateString));
			}catch (Exception e) {}

		}else if(data.regionMatches(true, 0, SCRIPT_AUTHOR_LABEL, 0, SCRIPT_AUTHOR_LABEL_LENGTH)){

			header.setAuthor(getHeaderData(data));

		}else if(data.regionMatches(true, 0, SCRIPT_PREREQUISITE_LABEL, 0, SCRIPT_PREREQUISITE_LABEL_LENGTH)){

			header.setPrerequisite(getHeaderData(data));

		}else if(data.regionMatches(true, 0, Variable.SCRIPT_LABEL, 0, Variable.SCRIPT_LABEL_LENGTH)){

			final ArrayList<String> dataArray = new ArrayList<String>(Arrays.asList(getHeaderData(data).split(ScriptParser.ATS_SEPARATOR)));

			if(dataArray.size() > 0){

				final String name = dataArray.remove(0).trim();
				
				String value = "";
				Transformer transformer = null;

				if(dataArray.size() > 0) {

					final String nextData = dataArray.remove(0).trim();
					final Matcher m = Transformer.TRANSFORM_PATTERN.matcher(nextData);
					
					if(m != null && m.find()){

						transformer = Transformer.createTransformer(getDataGroup(m, 1), getDataGroup(m, 2));

						if(dataArray.size() > 0){
							value = dataArray.remove(0).trim();
						}

					}else {
						value = nextData;
					}
				}

				script.addVariable(name, new CalculatedValue(script, value), transformer);
			}

		}else if(data.regionMatches(true, 0, SCRIPT_RETURN_LABEL, 0, SCRIPT_RETURN_LABEL.length())){

			final String[] returnsData = getHeaderData(data).split(ATS_SEPARATOR);
			CalculatedValue[] returns = new CalculatedValue[returnsData.length];

			for(int i=0; i < returnsData.length; i++){
				returns[i] = new CalculatedValue(script, returnsData[i].trim());
			}
			script.setReturns(returns);

		}else{

			boolean actionDisabled = false;
			if(data.startsWith("//")){
				data = data.substring(2);
				actionDisabled = true;
			}
			lexer.createAction(script, data, actionDisabled);
		}
	}
}