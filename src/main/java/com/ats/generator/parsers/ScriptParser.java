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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.transform.Transformer;
import com.ats.script.ScriptLoader;

public class ScriptParser {

	public static final String ATS_SEPARATOR = "->";
	public static final String ATS_ASSIGN_SEPARATOR = "=>";
	public static final String ATS_PROPERTIES_FILE = ".atsProjectProperties";

	public static final String SCRIPT_ID = "id";
	public static final String SCRIPT_GROUPS_LABEL = "groups";
	public static final String SCRIPT_DESCRIPTION_LABEL = "description";
	public static final String SCRIPT_DATE_CREATED_LABEL = "created";
	public static final String SCRIPT_RETURN_LABEL = "return";

	public static final String SCRIPT_AUTHOR_LABEL = "author";
	public static final String SCRIPT_PREREQUISITE_LABEL = "prerequisite";

	private static final Pattern ID_PATTERN = Pattern.compile("^" + SCRIPT_ID + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^" + SCRIPT_DESCRIPTION_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern CREATED_DATE_PATTERN = Pattern.compile("^" + SCRIPT_DATE_CREATED_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern RETURN_PATTERN = Pattern.compile("^" + SCRIPT_RETURN_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern GROUP_PATTERN = Pattern.compile("^" + SCRIPT_GROUPS_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern AUTHOR_PATTERN = Pattern.compile("^" + SCRIPT_AUTHOR_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PREREQUISITE_PATTERN = Pattern.compile("^" + SCRIPT_PREREQUISITE_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("^" + Variable.SCRIPT_LABEL + " *?\\" + ATS_SEPARATOR + "(.*)", Pattern.CASE_INSENSITIVE);

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

	public void parse(ScriptLoader script, String data){

		Matcher m = null;

		if((m = GROUP_PATTERN.matcher(data)) != null && m.find()){

			script.parseGroups(getDataGroup(m, 1));

		}else if((m = DESCRIPTION_PATTERN.matcher(data)) != null && m.find()){

			script.setDescription(getDataGroup(m, 1));

		}else if((m = ID_PATTERN.matcher(data)) != null && m.find()){

			script.setId(getDataGroup(m, 1));
			
		}else if((m = CREATED_DATE_PATTERN.matcher(data)) != null && m.find()){

			try {
				script.setCreatedDate(dateFormat.parse(getDataGroup(m, 1)));
			}catch (ParseException | NumberFormatException e) {}

		}else if((m = AUTHOR_PATTERN.matcher(data)) != null && m.find()){

			script.setAuthor(getDataGroup(m, 1));

		}else if((m = PREREQUISITE_PATTERN.matcher(data)) != null && m.find()){

			script.setPrerequisite(getDataGroup(m, 1));

		}else if((m = VARIABLE_PATTERN.matcher(data)) != null && m.find()){

			final ArrayList<String> dataArray = new ArrayList<String>(Arrays.asList(getDataGroup(m, 1).split(ScriptParser.ATS_SEPARATOR)));

			if(dataArray.size() > 0){

				String name = dataArray.remove(0).trim();
				
				String value = "";
				Transformer transformer = null;

				if(dataArray.size() > 0) {

					String nextData = dataArray.remove(0).trim();
					if((m = Transformer.TRANSFORM_PATTERN.matcher(nextData)) != null && m.find()){

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

		}else if((m = RETURN_PATTERN.matcher(data)) != null && m.find()){

			String[] returnsData = getDataGroup(m, 1).split(ATS_SEPARATOR);
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