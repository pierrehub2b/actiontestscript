package com.ats.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.ats.executor.ActionTestScript;
import com.ats.generator.GeneratorReport;
import com.ats.generator.parsers.Lexer;
import com.ats.generator.parsers.ScriptParser;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.actions.Action;

public class ScriptLoader extends Script {

	public final static String DEFAULT_CHARSET = "UTF-8";

	public final static String ATS_EXTENSION = "ats";
	public final static String ATS_FILE_EXTENSION = "." + ATS_EXTENSION;
	public final static String ATS_VISUAL_EXTENSION = "atsv";

	public final static String ATS_VISUAL_FOLDER = "visual";
	
	private ScriptParser parser;
	
	private ScriptHeader header;

	private String javaCode = null;

	private ArrayList<Action> actions;

	public ScriptLoader(){}

	public ScriptLoader(String type, Lexer lexer, File file, ProjectData projectData){
		this(type, lexer, file, projectData, DEFAULT_CHARSET);
	}

	public ScriptLoader(String type, Lexer lexer, File file, ProjectData projectData, String charset){

		this.setHeader(new ScriptHeader(projectData, file));
		this.setProjectAtsFolder(projectData.getAtsRootFolder());

		if(ATS_EXTENSION.equals(type)){

			this.setParameters(new String[0]);
			this.setVariables(new Variable[0]);

			this.actions = new ArrayList<Action>();
			
			this.parser = new ScriptParser(lexer);

			this.parser.addScript();

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			}catch(FileNotFoundException e) {
				return;
			}

			String line = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis, Charset.forName(charset)));

			try {
				while((line = reader.readLine()) != null){
					processLine(line);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//simpleName = simpleName.substring(0, simpleName.length() - ATS_EXTENSION.length()) + "java";

		}else if("java".equals(type)) {
			try {
				this.javaCode = FileUtils.readFileToString(file);
			} catch (IOException e) {}
		}
	}

	private void processLine(String line){
		parser.parse(this, line);
	}

	public void addAction(Action data){
		if(data != null){
			data.setLine(actions.size());
			actions.add(data);
		}
	}

	public void parseGroups(String data) {
		header.parseGroups(data);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Java Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	public String getJavaCode(){

		if(javaCode != null) {

			return javaCode;

		}else {

			//-------------------------------------------------------------------------------------------------
			// return values 
			//-------------------------------------------------------------------------------------------------
			
			StringBuilder returnValuesBuilder = new StringBuilder("");
			CalculatedValue[] returnValues = getReturns();
			
			if(returnValues != null) {
				returnValuesBuilder.append("\r\n\r\n\t\t");
				returnValuesBuilder.append(ActionTestScript.JAVA_RETURNS_FUNCTION_NAME);
				returnValuesBuilder.append("(");
				
				ArrayList<String> returnValuesCode = new ArrayList<String>();
				for(CalculatedValue ret : returnValues){
					returnValuesCode.add(ret.getJavaCode());
				}
				
				returnValuesBuilder.append(String.join(", ", returnValuesCode));
				returnValuesBuilder.append(");");
			}
						
			//-------------------------------------------------------------------------------------------------
			// variables 
			//-------------------------------------------------------------------------------------------------
			
			StringBuilder variableCode = new StringBuilder("");
			Variable[] variables = getVariables();
			Arrays.sort(variables);
			
			for(Variable variable : variables){
				variableCode.append(codeLine(variable.getJavaCode()));
			}
				
			
			StringBuilder code = new StringBuilder(header.getJavaCode());
			
			code.append("\r\n\r\n\t\t//--------------------------------------------------------------------------------------\r\n");
			code.append("\t\t// Variables\r\n");
			code.append("\t\t//--------------------------------------------------------------------------------------\r\n");
			
			code.append(variableCode);

			//-------------------------------------------------------------------------------------------------
			// actions 
			//-------------------------------------------------------------------------------------------------
			
			code.append("\r\n\r\n\t\t//--------------------------------------------------------------------------------------\r\n");
			code.append("\t\t// Actions\r\n");
			code.append("\t\t//--------------------------------------------------------------------------------------\r\n");

			StringBuilder actionCode = new StringBuilder("");
			for(Action action : actions){
				String lineCode = action.getJavaCode();
				if(lineCode != null && !action.isDisabled()){
					actionCode.append("\r\n\t\t");
					actionCode.append(ActionTestScript.JAVA_EXECUTE_FUNCTION_NAME);
					actionCode.append("(");actionCode.append(action.getLine());actionCode.append(",");
					actionCode.append(lineCode);
					actionCode.append(");");
				}
			}
			
			code.append(actionCode);
			code.append(returnValuesBuilder.toString());
			code.append("\r\n\t}\r\n}");

			return code.toString();
		}
	}

	private String codeLine(String code){
		return "\r\n\t\t" + code + ";";
	}

	public void generateJavaFile(){
		if(header.getJavaDestinationFolder() != null){
			File javaFile = header.getJavaFile();
			try {
				javaFile.getParentFile().mkdirs();
				FileUtils.writeStringToFile(javaFile, getJavaCode());
			} catch (IOException e) {
			}
		}
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

			File scriptFile = new File(args[0]);
			if(scriptFile.exists() && scriptFile.isFile() && scriptFile.getName().toLowerCase().endsWith(ATS_EXTENSION)){

				GeneratorReport report = new GeneratorReport();
				Lexer lexer = new Lexer(report);
				lexer.loadScript(scriptFile);
			}
		}
	}
}