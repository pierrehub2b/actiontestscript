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

package com.ats.executor;

import static org.testng.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.TestRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.generator.ATS;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.objects.mouse.MouseKey;
import com.ats.generator.objects.mouse.MouseScroll;
import com.ats.generator.objects.mouse.MouseSwipe;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.ConditionalValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.NumericTransformer;
import com.ats.generator.variables.transform.RegexpTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.generator.variables.transform.Transformer;
import com.ats.recorder.IVisualRecorder;
import com.ats.recorder.VisualRecorder;
import com.ats.recorder.VisualRecorderNull;
import com.ats.script.ProjectData;
import com.ats.script.Script;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionCallscript;
import com.ats.script.actions.ActionExecute;
import com.ats.tools.Utils;
import com.ats.tools.logger.ExecutionLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;

@Listeners({TestListener.class})
public class ActionTestScript extends Script implements ITest{

	public static final String MAIN_TEST_FUNCTION = "testMain";

	protected ActionTestScript topScript;
	private ChannelManager channelManager;

	private ProjectData projectData;

	private String[] returnValues;

	private String testName;
	protected ScriptHeader getHeader() {return new ScriptHeader();}

	private ScriptStatus status;

	public ActionTestScript() {
		init();
	}

	public ActionTestScript(ExecutionLogger logger) {
		super(logger);
		init();
	}

	private void init() {
		status = new ScriptStatus();
		topScript = this;
		channelManager = new ChannelManager(this);
	}

	public String[] getReturnValues() {
		return returnValues;
	}

	private void setTestName(String name) {
		this.testName = name;
	}

	//----------------------------------------------------------------------------------------------------------
	// TestNG management
	//----------------------------------------------------------------------------------------------------------

	@BeforeSuite(alwaysRun=true)
	public void beforeSuite() {
		System.out.println("-------------------------[ ATS " + ATS.VERSION + " execution start ]-------------------------\n");
	}

	@BeforeClass(alwaysRun=true)
	public void beforeAtsTest(ITestContext ctx) {

		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger(Actions.class.getName()).setLevel(Level.OFF);
		java.util.logging.Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);

		final TestRunner runner = (TestRunner) ctx;
		setTestName(this.getClass().getName());

		final String suiteName = ctx.getSuite().getName();
		status = new ScriptStatus(testName, suiteName);

		if("true".equalsIgnoreCase(runner.getTest().getParameter("check.mode"))) {
			throw new SkipException("check mode : " + testName);
		}else {

			final Map<String, String> params = runner.getTest().getAllParameters();
			setTestExecutionVariables(params);

			final ExecutionLogger mainLogger = new ExecutionLogger(System.out, getEnvironmentValue("ats.log.level", ""));
			setLogger(mainLogger);

			//-----------------------------------------------------------
			// check report output specified
			//-----------------------------------------------------------

			final int visualQuality = Utils.string2Int(getEnvironmentValue("visual.report", "0"));
			final boolean xml = "true".equalsIgnoreCase(getEnvironmentValue("xml.report", ""));

			if(visualQuality > 0 || xml) {

				final ScriptHeader header = getHeader();

				header.setName(getTestName());

				final File output = new File(runner.getOutputDirectory());
				if(!output.exists()) {
					output.mkdirs();
				}

				setRecorder(new VisualRecorder(output, header, xml, visualQuality, mainLogger));
			}

			final JsonObject logs = new JsonObject();
			logs.addProperty("suite", suiteName);
			logs.addProperty("xmlReport", xml);
			logs.addProperty("visualQuality", visualQuality);

			final JsonArray parameters = new JsonArray();
			for(Entry<String, String>param : params.entrySet()) {
				final JsonObject elem = new JsonObject();
				elem.addProperty(param.getKey(), param.getValue());
				parameters.add(elem);
			}
			logs.add("parameters", parameters);

			sendScriptInfo("Starting script (" + testName + ") -> " + logs.toString());

			//-----------------------------------------------------------
			//-----------------------------------------------------------

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					tearDown();
				}
			});

			new StopExecutionThread(System.in).start();
		}
	}

	@AfterClass(alwaysRun=true)
	public void afterClass() {
		sendScriptInfo("Script terminated -> " + status.endLogs());
	}

	@AfterTest(alwaysRun=true)
	public void testFinished() {
		stopRecorder();
		tearDown();
	}

	@AfterMethod(alwaysRun=true)
	public void cleanup(){
		stopRecorder();
		tearDown();
	}

	@Override
	public String getTestName() {
		return this.getClass().getName();
	}

	//----------------------------------------------------------------------------------------------------------
	// Channels management
	//----------------------------------------------------------------------------------------------------------

	public Channel getCurrentChannel(){
		return getChannelManager().getCurrentChannel();
	}

	public Channel getChannel(String name){
		return getChannelManager().getChannel(name);
	}

	//----------------------------------------------------------------------------------------------------------
	// Call script
	//----------------------------------------------------------------------------------------------------------

	public ActionTestScript getTopScript() {
		return topScript;
	}

	public void initCalledScript(ActionTestScript testScript, String testName, int line, ActionTestScript script, String[] parameters, List<Variable> variables, int iteration, int iterationMax, String scriptName, String type, File csvFile) {

		this.topScript = script;
		this.channelManager = script.getChannelManager();
		this.iteration = iteration;
		this.csvFile = csvFile;

		final JsonObject log = testScript.getConditionLogs();

		log.addProperty("called", scriptName);
		log.addProperty("iteration", iteration+1 + "/" + iterationMax);
		log.addProperty("type", type);

		if(csvFile != null) {
			log.addProperty("url", csvFile.getAbsolutePath());
		}

		if(parameters != null) {
			setParameters(parameters);
			if(parameters.length > 0) {
				final JsonArray parametersArray = new JsonArray();
				for (String s : parameters) {
					parametersArray.add(s);
				}
				log.add("parameters", parametersArray);
			}
		}

		if(variables != null) {
			setVariables(variables);
		}
		setTestExecutionVariables(script.getTestExecutionVariables());

		script.sendScriptInfo(ActionCallscript.getScriptLog(testName, line, log));
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}

	//----------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------

	public void setProjectData(ProjectData value) {
		projectData = value;
		projectData.synchronize();
	}

	public void tearDown(){
		sendInfoLog("Drivers", "closing ...");
		getChannelManager().tearDown();
	}

	//----------------------------------------------------------------------------------------------------------
	// Generated methods
	//----------------------------------------------------------------------------------------------------------

	public static final String JAVA_VAR_FUNCTION_NAME = "var";
	public Variable var(String name, CalculatedValue value){
		return createVariable(name, value, null);
	}

	public Variable var(String name){
		return createVariable(name, new CalculatedValue(""), null);
	}

	public Variable var(String name, Transformer transformer){
		return createVariable(name, new CalculatedValue(""), transformer);
	}

	public Variable var(String name, CalculatedValue value, Transformer transformer){
		return createVariable(name, value, transformer);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_VALUE_FUNCTION_NAME = "clv";
	public CalculatedValue clv(Object ... data) {
		return new CalculatedValue(this, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PARAM_FUNCTION_NAME = "prm";
	public String prm(int index) {
		return getParameterValue(index);
	}

	public String prm(int index, String defaultValue) {
		return getParameterValue(index, defaultValue);
	}

	public CalculatedValue[] prm(CalculatedValue ... values) {
		return values;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_RETURNS_FUNCTION_NAME = "rtn";
	public void rtn(CalculatedValue ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(CalculatedValue calc : values) {
			returnValues[i] = calc.getCalculated();
			i++;
		}

		updateVariables();
	}

	//---------------------------------------------------------------------------------------------

	public void rtn(String ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(String value : values) {
			returnValues[i] = value;
			i++;
		}

		updateVariables();
	}

	private void updateVariables() {
		List<Variable> variables = getVariables();

		int index = 0;
		for(String value : returnValues) {
			if(variables.size() < index + 1) {
				break;
			}
			variables.get(index).setData(value);
			index++;
		}
	}

	//---------------------------------------------------------------------------------------------

	public void returnValues(String... values) {
		returnValues = values;
		updateVariables();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_RNDSTRING_FUNCTION_NAME = "rds";
	public String rds(int len) {
		return getRandomStringValue(len, null);
	}

	public String rds(int len, String upp) {
		return getRandomStringValue(len, upp);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ENV_FUNCTION_NAME = "env";
	public String env(String name) {
		return getEnvironmentValue(name, "");
	}

	public String env(String name, String defaultValue) {
		return getEnvironmentValue(name, defaultValue);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PROPERTY_FUNCTION_NAME = "prp";
	public CalculatedProperty prp(boolean isRegexp, String name, CalculatedValue value){
		return new CalculatedProperty(isRegexp, name, value);
	}

	//---------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------

	public static final String JAVA_UUID_FUNCTION_NAME = "uid";
	public String uid() {
		return getUuidValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TODAY_FUNCTION_NAME = "td";
	public String td() {
		return getTodayValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_NOW_FUNCTION_NAME = "nw";
	public String nw() {
		return getNowValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ITERATION_FUNCTION_NAME = "itr";
	public int itr() {
		return getIteration();
	}

	//---------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ELEMENT_FUNCTION_NAME = "el";
	public SearchedElement el(SearchedElement parent, int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(parent, index, tagName, properties);
	}

	public SearchedElement el(int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(null, index, tagName, properties);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_REGEX_FUNCTION_NAME = "rx";
	public RegexpTransformer rx(String patt, int group) {
		return new RegexpTransformer(patt, group);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_DATE_FUNCTION_NAME = "dt";
	public DateTransformer dt(String ... data) {
		return new DateTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TIME_FUNCTION_NAME = "tm";
	public TimeTransformer tm(String ... data) {
		return new TimeTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_NUMERIC_FUNCTION_NAME = "nm";
	public NumericTransformer nm(int dp, boolean comma) {
		return new NumericTransformer(dp, comma);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_POS_FUNCTION_NAME = "ps";
	public MouseDirectionData ps(Cartesian cart, CalculatedValue value) {
		return new MouseDirectionData(cart, value);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_MOUSE_FUNCTION_NAME = "ms";
	public Mouse ms(String type) {
		return new Mouse(type);
	}

	public Mouse ms(String type, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new Mouse(type, hpos, vpos);
	}

	public MouseKey ms(String type, Keys key, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseKey(type, key, hpos, vpos);
	}

	public MouseKey ms(String type, Keys key) {
		return new MouseKey(type, key);
	}

	public MouseScroll ms(int scroll, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseScroll(scroll, hpos, vpos);
	}

	public MouseScroll ms(int scroll) {
		return new MouseScroll(scroll);
	}

	public MouseSwipe ms(MouseDirectionData hdir, MouseDirectionData vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseSwipe(hdir, vdir, hpos, vpos);
	}

	public MouseSwipe ms(MouseDirectionData hdir, MouseDirectionData vdir) {
		return new MouseSwipe(hdir, vdir);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_EMBEDED_FUNCTION_NAME = "emb";
	public String emb(String relativePath) {
		return getAssetsUrl(relativePath);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_GAV_FUNCTION_NAME = "gav";
	public String gav() {
		return "";
	}

	//----------------------------------------------------------------------------------------------------------
	// Actions
	//----------------------------------------------------------------------------------------------------------

	public static final String JAVA_CONDITION_FUNCTION = "condition";
	public boolean condition(String type, int line, Variable variable, CalculatedValue calculatedValue) {

		condition = new ConditionalValue(type, variable, calculatedValue);

		if(condition.isExec()) {
			return true;
		}else {
			getTopScript().sendScriptInfo(ActionCallscript.getScriptLog(getTestName(), line, condition.getLog()));
			condition = null;
			return false;
		}
	}

	private ConditionalValue condition;
	public JsonObject getConditionLogs() {
		JsonObject result = new JsonObject();
		if(condition != null) {
			result = condition.getLog(result);
			condition = null;
		}
		return result;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_EXECUTE_FUNCTION_NAME = "exec";

	public void exec(int line, Action action){
		action.execute(this);
		execFinished(line, action, true);
	}

	public void exec(int line, ActionCallscript action){
		action.execute(getTestName(), line, this);
		execFinished(line, action, true);
	}

	public void exec(int line, ActionExecute action){
		action.execute(this);
		execFinished(line, action, action.isStop());
	}

	private void execFinished(int line, Action action, boolean stop) {
		getTopScript().actionFinished(line, action, stop);
	}

	private void failedAt(String actionClass, String script, int line, String app, int errorCode, String errorMessage) {
		status.failed();
		
		final StringBuilder scriptLine = new StringBuilder(script).append(".").append(ATS_EXTENSION).append(":").append(line);

		final JsonObject logs = new JsonObject();
		logs.addProperty("app", app);
		logs.addProperty("errorCode", errorCode);
		logs.addProperty("errorMessage", errorMessage);

		sendErrorLog(actionClass + " (" + scriptLine.toString() + ")", logs.toString());
		
		if(status.isSuiteExecution()) {
			fail("ATS " + actionClass + " execution error -> " + errorMessage + " at " + scriptLine.toString());
		}
	}
	
	public void actionFinished(int line, Action action, boolean stop) {
		status.addAction();
		
		final ActionStatus actionStatus = action.getStatus();
		
		if(actionStatus.isPassed()) {
			sendActionLog(action, getTestName(), line);
		}else {
			if(stop) {
				failedAt(action.getClass().getSimpleName(), getTestName(), line, actionStatus.getChannelApplication(), actionStatus.getCode(), actionStatus.getFailMessage());
			}else {
				sendActionLog(action, getTestName(), line);
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Drag drop management
	//-----------------------------------------------------------------------------------------------------------

	private boolean dragWithDesktop = false;

	public void startDrag() {
		topScript.dragWithDesktop = getCurrentChannel().isDesktop();
	}

	public boolean isDesktopDragDrop() {
		return topScript.dragWithDesktop;
	}

	public void endDrag() {
		topScript.dragWithDesktop = false;
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Animation recorder
	//-----------------------------------------------------------------------------------------------------------

	private IVisualRecorder recorder = new VisualRecorderNull();

	public IVisualRecorder getRecorder() {
		return topScript.recorder;
	}

	public void setRecorder(IVisualRecorder value) {
		if((value instanceof VisualRecorderNull && this.recorder instanceof VisualRecorder) || (value instanceof VisualRecorder && this.recorder instanceof VisualRecorderNull)) {
			this.topScript.recorder.terminate();
			this.topScript.recorder = value;
		}
	}

	public void startRecorder(ScriptHeader info, int quality, boolean xml) {
		topScript.setRecorder(new VisualRecorder(info, projectData, xml, quality));
	}

	public void stopRecorder() {
		topScript.setRecorder(new VisualRecorderNull());
	}
}