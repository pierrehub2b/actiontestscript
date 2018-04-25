package com.ats.script.actions;

import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.logger.MessageCode;

public class ActionCallscript extends Action {

	public static final String SCRIPT_LABEL = "subscript";

	private static final String SCRIPT_LOOP = "loop";
	public static final Pattern LOOP_REGEXP = Pattern.compile(SCRIPT_LOOP + " ?\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
	private static final String ASSETS_PROTOCOLE = "assets:///";
	private static final String FILE_PROTOCOLE = "file:///";
	private static final String HTTP_PROTOCOLE = "http://";

	private String name;

	private Variable[] variables;
	private CalculatedValue[] parameters;
	private int loop = 1;
	private String csvFilePath = null;

	public ActionCallscript() {}

	public ActionCallscript(ScriptLoader script, String name, String[] parameters, String[] returnValue) {

		super(script);
		setName(name);

		if(parameters != null && parameters.length > 0) {

			String firstParam = parameters[0];
			if(firstParam.startsWith(ASSETS_PROTOCOLE) || firstParam.startsWith(FILE_PROTOCOLE) || firstParam.startsWith(HTTP_PROTOCOLE)) {
				setCsvFilePath(firstParam);
				return;
			}

			ArrayList<CalculatedValue> paramsValues = new ArrayList<CalculatedValue>();
			for(String param : parameters){
				Matcher match = LOOP_REGEXP.matcher(param);
				if(match.find()){
					try{
						this.loop = Integer.parseInt(match.group(1));
					}catch (NumberFormatException e){}
				}else {
					paramsValues.add(new CalculatedValue(script, param.trim()));
				}
			}

			setParameters(paramsValues.toArray(new CalculatedValue[paramsValues.size()]));
		}

		/*if(options != null && options.size() > 0){

			int loopValue = 1;

			Iterator<String> itr = options.iterator();
			while (itr.hasNext())
			{
				String param = itr.next().trim();
				Matcher match = LOOP_REGEXP.matcher(param);
				if(match.find()){
					try{
						loopValue = Integer.parseInt(match.group(1));
						break;
					}catch (NumberFormatException e){}
				}
			}

			this.loop = loopValue;
		}*/

		if(returnValue != null && returnValue.length > 0 && this.loop == 1){
			setVariables(new Variable[returnValue.length]);

			int index = 0;
			for (String varName : returnValue ){
				this.variables[index] = script.getVariable(varName.trim(), true);
				index++;
			}
		}
	}

	public ActionCallscript(Script script, String name) {
		super(script);
		setName(name);
	}

	public ActionCallscript(Script script, String name, CalculatedValue[] parameters) {
		this(script, name);
		setParameters(parameters);
	}

	public ActionCallscript(Script script, String name, Variable ... variables) {
		this(script, name);
		setVariables(variables);
	}

	public ActionCallscript(Script script, String name, CalculatedValue[] parameters, Variable ... variables) {
		this(script, name);
		setParameters(parameters);
		setVariables(variables);
	}

	public ActionCallscript(Script script, String name, String csvFilePath) {
		this(script, name);
		setCsvFilePath(csvFilePath);
	}

	public ActionCallscript(Script script, String name, CalculatedValue[] parameters, int loop) {
		this(script, name, parameters);
		setLoop(loop);
	}

	public ActionCallscript(Script script, String name, int loop) {
		this(script, name);
		setLoop(loop);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getJavaCode() {

		StringBuilder codeBuilder = new StringBuilder(super.getJavaCode());
		codeBuilder.append("\"");
		codeBuilder.append(name);
		codeBuilder.append("\"");

		if(csvFilePath != null) {
			codeBuilder.append(", \"");
			codeBuilder.append(csvFilePath);
			codeBuilder.append("\"");
		}else {
			if(parameters != null){
				StringJoiner joiner = new StringJoiner(", ");
				for (CalculatedValue value : parameters){
					joiner.add(value.getJavaCode());
				}
				codeBuilder.append(", ");
				codeBuilder.append(ActionTestScript.JAVA_PARAM_FUNCTION_NAME);
				codeBuilder.append("(");
				codeBuilder.append(joiner.toString());
				codeBuilder.append(")");
			}

			if(loop > 1) {
				codeBuilder.append(", ");
				codeBuilder.append(loop);
			}else if(variables != null){
				StringJoiner joiner = new StringJoiner(", ");
				for (Variable variable : variables){
					joiner.add(variable.getName());
				}
				codeBuilder.append(", ");
				codeBuilder.append(joiner.toString());
			}
		}

		codeBuilder.append(")");

		return codeBuilder.toString();
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ActionTestScript ts) {
		super.execute(ts);

		try {

			Class<ActionTestScript> clazz = (Class<ActionTestScript>) Class.forName(name);
			ActionTestScript ats = clazz.getDeclaredConstructor().newInstance();

			if(csvFilePath != null) {

				URL csvUrl = null;

				if(csvFilePath.startsWith(ASSETS_PROTOCOLE)) {
					csvUrl = getClass().getClassLoader().getResource(csvFilePath.replace(ASSETS_PROTOCOLE, ""));
				}else {
					try {
						csvUrl = new URL(csvFilePath);
					} catch (MalformedURLException e) {}
				}

				if(csvUrl == null) {

					status.setPassed(false);
					status.setMessage("CSV file url not found : " + csvFilePath);

					return;
				}


				try {
					URLConnection urlConn = csvUrl.openConnection();

					InputStreamReader inputCSV = new InputStreamReader(((URLConnection) urlConn).getInputStream());
					BufferedReader br = new BufferedReader(inputCSV);

					ArrayList<String[]> data = new ArrayList<String[]>();

					String csvLine;
					while ((csvLine = br.readLine()) != null) {
						if(csvLine.length() > 0) {
							data.add(csvLine.split(","));
						}
					}

					for (String[] param : data) {
						ts.getTopScript().sendInfo("Call subscript -> ", name);

						ats.initCalledScript(ts.getTopScript(), param, null);
						Method testMain = clazz.getDeclaredMethod(ActionTestScript.MAIN_TEST_FUNCTION, new Class[]{});
						testMain.invoke(ats);
					}

				} catch (IOException e) {
					status.setPassed(false);
					status.setMessage("CSV file IO error : " + csvFilePath + " -> " + e.getMessage());
				}

			}else {
				ats.initCalledScript(ts.getTopScript(), getCalculatedParameters(), variables);
				Method testMain = clazz.getDeclaredMethod(ActionTestScript.MAIN_TEST_FUNCTION, new Class[]{});
				for (int i=0; i<loop; i++) {
					ts.getTopScript().sendInfo("call subscript -> ", name);
					testMain.invoke(ats);
				}
				
				status.setData(ats.getReturnValues());
			}

		} catch (ClassNotFoundException e) {

			status.setPassed(false);
			status.setCode(MessageCode.SCRIPT_NOT_FOUND);
			status.setMessage("ATS script not found : '" + name + "'");

		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {

			if(e.getTargetException() instanceof AssertionError) {
				fail(e.getCause().getMessage());
			}

		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		}
	}

	private String[] getCalculatedParameters() {
		if(parameters != null) {
			int index = 0;
			String[] calculatedParameters = new String[parameters.length];
			for(CalculatedValue calc : parameters) {
				calculatedParameters[index] = calc.getCalculated();
				index++;
			}
			return calculatedParameters;
		}
		return null;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Variable[] getVariables() {
		return variables;
	}

	public void setVariables(Variable[] value) {
		this.variables = value;
		if(value != null) {
			this.csvFilePath = null;
			this.loop = 1;
		}
	}

	public CalculatedValue[] getParameters() {
		return parameters;
	}

	public void setParameters(CalculatedValue[] value) {
		this.parameters = value;
		if(value != null) {
			this.csvFilePath = null;
		}
	}

	public int getLoop() {
		return loop;
	}

	public void setLoop(int loop) {

		if(loop <= 0) {
			loop = 1;
		}

		if(loop > 1) {
			this.csvFilePath = null;
			this.variables = null;
		}
		this.loop = loop;
	}

	public String getCsvFilePath() {
		return csvFilePath;
	}

	public void setCsvFilePath(String value) {
		this.csvFilePath = value;
		if(value != null) {
			this.parameters = null;
			this.variables = null;
			this.loop = 1;
		}
	}	
}