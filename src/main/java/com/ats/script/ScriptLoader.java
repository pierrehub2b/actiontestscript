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

package com.ats.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import com.ats.executor.ActionTestScript;
import com.ats.generator.GeneratorReport;
import com.ats.generator.parsers.Lexer;
import com.ats.generator.parsers.ScriptParser;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.actions.Action;

public class ScriptLoader extends Script {

	private ScriptParser parser;
	private ScriptHeader header;
	private String javaCode = null;
	private Charset charset;
	private ArrayList<Action> actions;
	
	private String projectGav = "";

	public ScriptLoader(){}

	public ScriptLoader(String type, Lexer lexer, File file, ProjectData projectData){
		this(type, lexer, file, projectData, DEFAULT_CHARSET);
	}

	public ScriptLoader(String type, Lexer lexer, File file, ProjectData projectData, String charset){

		this.setHeader(new ScriptHeader(projectData, file));
		this.setAtsFolder(projectData.getAtsSourceFolder().toFile());
		this.setCharset(charset);
		
		this.projectGav = projectData.getGav();

		if(ATS_EXTENSION.equals(type)){

			this.setParameters(new String[0]);
			this.setVariables(new ArrayList<Variable>());

			this.actions = new ArrayList<Action>();
			this.parser = new ScriptParser(lexer);
			this.parser.addScript();

			try {
				Stream <String> lines = Files.lines(file.toPath(), this.charset);
				lines
				.map(String::trim)
				.filter(a -> !a.isEmpty())
				.filter(a -> !a.startsWith("["))
				.forEach(a -> parser.parse(this, a));
				lines.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}else if("java".equals(type)) {
			try {
				this.javaCode = new String(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {}
		}
	}

	private void setCharset(String value) {
		try {
			this.charset = Charset.forName(value);
		} catch (Exception e) {
			this.charset = Charset.forName(DEFAULT_CHARSET);
		}
	}

	public void addAction(Action data, boolean disabled){
		data.setDisabled(disabled);
		data.setLine(actions.size());
		actions.add(data);
	}

	public void parseGroups(String data) {
		header.parseGroups(data);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Java Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode(String projectGav){

		if(javaCode != null) {

			return javaCode;

		}else {

			final StringBuilder code = new StringBuilder(header.getJavaCode(projectGav));

			//-------------------------------------------------------------------------------------------------
			// variables 
			//-------------------------------------------------------------------------------------------------
			
			code.append("\r\n\r\n\t\t//--------------\r\n\t\t// Variables ...\r\n\t\t//--------------\r\n");
			
			final List<Variable> variables = getVariables();
			Collections.sort(variables);

			for(Variable variable : variables){
				code.append("\r\n\t\t");
				code.append(variable.getJavaCode());
				code.append(";");
			}

			//-------------------------------------------------------------------------------------------------
			// actions 
			//-------------------------------------------------------------------------------------------------

			code.append("\r\n\r\n\t\t//--------------\r\n\t\t// Actions   ...\r\n\t\t//--------------\r\n");

			for(Action action : actions){
				final String lineCode = action.getJavaCode();
				if(lineCode != null && !action.isDisabled()){
					code.append("\r\n\t\t");
					code.append(ActionTestScript.JAVA_EXECUTE_FUNCTION_NAME);
					code.append("(");code.append(action.getLine());code.append(",");
					code.append(lineCode);
					code.append(");");
				}
			}
			
			//-------------------------------------------------------------------------------------------------
			// returns 
			//-------------------------------------------------------------------------------------------------
			
			final CalculatedValue[] returnValues = getReturns();
			if(returnValues != null) {
				
				code.append("\r\n\r\n\t\t//--------------\r\n\t\t// Returns   ...\r\n\t\t//--------------\r\n\r\n\t\t");
				code.append(ActionTestScript.JAVA_RETURNS_FUNCTION_NAME);
				code.append("(");

				final ArrayList<String> returnValuesCode = new ArrayList<String>();
				for(CalculatedValue ret : returnValues){
					returnValuesCode.add(ret.getJavaCode());
				}

				code.append(String.join(", ", returnValuesCode));
				code.append(");");
			}
			
			code.append("\r\n\t}\r\n}");

			return code.toString();
		}
	}

	public final static byte[] UTF8_BOM = {(byte)0xEF, (byte)0xBB, (byte)0xBF};

	public void generateJavaFile(String projectGav){
		if(header.getJavaDestinationFolder() != null){

			final File javaFile = header.getJavaFile();
			try {
				javaFile.getParentFile().mkdirs();

				final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(javaFile, false), charset));
				writer.write(getJavaCode(projectGav));
				writer.close();

			} catch (IOException e) {
			}
		}
	}

	public void setId(String data) {
		header.setId(data);
	}

	public void setDescription(String data) {
		header.setDescription(data);
	}

	public void setAuthor(String author) {
		header.setAuthor(author);
	}

	public void setCreatedDate(Date date) {
		header.setCreatedAt(date);
	}

	public void setPrerequisite(String prerequisite) {
		header.setPrerequisite(prerequisite);
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

	public ScriptHeader getHeader() {
		return header;
	}

	public void setHeader(ScriptHeader header) {
		this.header = header;
	}

	public Action[] getActions() {
		return actions.toArray(new Action[actions.size()]);
	}

	public void setActions(Action[] data) {
		this.actions = new ArrayList<Action>(Arrays.asList(data));
	}	
	
	public String getProjectGav() {
		return projectGav;
	}
	public void setProjectGav(String value) {} // read only

	//-------------------------------------------------------------------------------------------------
	//  transient getters and setters
	//-------------------------------------------------------------------------------------------------

	public Lexer getLexer() {
		return parser.getLexer();
	}	

	//----------------------------------------------------------------------------------------------------
	// Script content java
	//----------------------------------------------------------------------------------------------------

	public static void main(String[] args) {
		if(args.length == 1){

			final File scriptFile = new File(args[0]);
			if(scriptFile.exists() && scriptFile.isFile() && scriptFile.getName().toLowerCase().endsWith(ATS_EXTENSION)){

				final GeneratorReport report = new GeneratorReport();
				final Lexer lexer = new Lexer(report);
				lexer.loadScript(scriptFile);
			}
		}
	}
}