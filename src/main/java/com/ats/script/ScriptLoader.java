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

import com.ats.executor.ActionTestScript;
import com.ats.generator.GeneratorReport;
import com.ats.generator.parsers.Lexer;
import com.ats.generator.parsers.ScriptParser;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.parameter.ParameterList;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionCallscript;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.ahocorasick.trie.Trie;

public class ScriptLoader extends Script {

	private ScriptParser parser;
	private ScriptHeader header;
	private String javaCode = null;

	private Charset charset;

	private ArrayList<Action> actions;

	private String projectGav = "";

	public ScriptLoader(){}

	public ScriptLoader(Lexer lexer){
		this.actions = new ArrayList<Action>();
		this.parser = new ScriptParser(lexer);
		this.parser.addScript();
	}

	public ScriptLoader(String type, Lexer lexer, File file, Project projectData){
		this(type, lexer, file, projectData, DEFAULT_CHARSET);
	}

	public ScriptLoader(String type, Lexer lexer, File file, Project prj, Charset charset){

		final ScriptHeader header = new ScriptHeader(prj, file);

		this.setHeader(header);
		this.setCharset(charset);
		this.projectGav = prj.getGav();

		if(ATS_EXTENSION.equals(type)){

			this.setParameterList(new ParameterList(0));
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
				.forEach(a -> parser.parse(this, header, a));
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

	private void setCharset(Charset value) {
		this.charset = value;
	}

	public void addAction(Action data, boolean disabled){
		data.setDisabled(disabled);
		data.setLine(actions.size());
		actions.add(data);
	}

	public boolean isSubscriptCalled(String scriptName) {
		for (Action action : actions) {
			if(action instanceof ActionCallscript) {
				if(((ActionCallscript)action).isSubscriptCalled(scriptName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean getActionsKeywords(Trie trie) {
		for (Action action : actions) {
			final List<String> actionKeywords = action.getKeywords();
			for(String keywords : actionKeywords) {
				if(trie.containsMatch(keywords)) {
					return true;
				}
			}
		}
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Java Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode(Project project){

		if(javaCode != null) {

			return javaCode;

		}else {

			final StringBuilder code = new StringBuilder(header.getJavaCode(project));

			//-------------------------------------------------------------------------------------------------
			// variables 
			//-------------------------------------------------------------------------------------------------

			code.append("\r\n\r\n\t\t//   ---< Variables >---   //\r\n");

			final List<Variable> variables = getVariables();
			Collections.sort(variables);

			for(Variable variable : variables){
				code.append("\r\n\t\t")
				.append(variable.getJavaCode())
				.append(";");
			}

			//-------------------------------------------------------------------------------------------------
			// actions 
			//-------------------------------------------------------------------------------------------------

			code.append("\r\n\r\n\t\t//   ---< Actions >---   //\r\n");

			for(Action action : actions){
				if(!action.isDisabled() && !action.isScriptComment()){
					code.append("\r\n\t\t").append(action.getJavaCode()).append(");");
				}
			}

			//-------------------------------------------------------------------------------------------------
			// returns 
			//-------------------------------------------------------------------------------------------------

			final CalculatedValue[] returnValues = getReturns();
			if(returnValues != null) {

				code.append("\r\n\r\n\t\t//   ---< Return >---   //\r\n\r\n\t\t")
				.append(ActionTestScript.JAVA_RETURNS_FUNCTION_NAME)
				.append("(");

				final ArrayList<String> returnValuesCode = new ArrayList<String>();
				for(CalculatedValue ret : returnValues){
					returnValuesCode.add(ret.getJavaCode());
				}

				code.append(String.join(", ", returnValuesCode)).append(");");
			}

			code.append("\r\n\t}\r\n}");

			return code.toString();
		}
	}

	public void generateJavaFile(Project project){
		if(header.getJavaDestinationFolder() != null){

			final File javaFile = header.getJavaFile();
			try {
				javaFile.getParentFile().mkdirs();

				final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(javaFile, false), charset));
				writer.write(getJavaCode(project));
				writer.close();

			} catch (IOException e) {
			}
		}
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