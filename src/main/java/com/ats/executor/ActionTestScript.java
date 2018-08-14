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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.TestRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.ats.driver.AtsManager;
import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.objects.mouse.MouseKey;
import com.ats.generator.objects.mouse.MouseScroll;
import com.ats.generator.objects.mouse.MouseSwipe;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
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
import com.ats.script.actions.ActionExecute;
import com.ats.script.actions.ActionExecuteElement;
import com.ats.tools.logger.ExecutionLogger;
import com.ats.tools.logger.MessageCode;

public class ActionTestScript extends Script implements ITest{

	public static final String MAIN_TEST_FUNCTION = "testMain";

	protected ActionTestScript topScript;
	private ChannelManager channelManager;

	private ProjectData projectData;

	private String[] returnValues;

	private String testName;
	protected ScriptHeader getHeader() {return null;}

	public ActionTestScript() {
		init();
	}

	public ActionTestScript(ExecutionLogger logger) {
		super(logger);
		init();
	}

	private void init() {

		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);

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
		System.out.println("----------------------------------------------");
		System.out.println("    ATS script started (version " + AtsManager.getVersion() + ")");
		System.out.println("----------------------------------------------\n");
	}

	@BeforeClass(alwaysRun=true)
	public void beforeAtsTest(ITestContext ctx) {

		final TestRunner runner = (TestRunner) ctx;
		setTestName(this.getClass().getName());

		String checkMode = runner.getTest().getParameter("check.mode");
		if("true".equals(checkMode)) {

			throw new SkipException("check mode : " + testName);

		}else {

			setTestParameters(runner.getTest().getAllParameters());

			//-----------------------------------------------------------
			// check report output specified
			//-----------------------------------------------------------

			int visualQuality = 0;
			try {
				visualQuality = Integer.parseInt(getEnvironmentValue("visual.report", "0"));
			}catch(NumberFormatException e) {};

			boolean xml = "true".equals(getEnvironmentValue("xml.report", "").toLowerCase());

			if(visualQuality > 0 || xml) {

				final File output = new File(runner.getOutputDirectory());
				if(!output.exists()) {
					output.mkdirs();
				}

				final ScriptHeader header = getHeader();
				header.setName(getTestName());
				header.setAtsVersion(AtsManager.getVersion());
				setRecorder(new VisualRecorder(output, header, visualQuality, xml));
			}

			//-----------------------------------------------------------
			//-----------------------------------------------------------

			setLogger(new ExecutionLogger(System.out, ctx.getSuite().getXmlSuite().getVerbose()));
			sendInfo("Starting script", " '" + testName + "'");

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
		sendInfo("Script terminated", " '" + testName + "'");
	}

	@AfterTest(alwaysRun=true)
	public void testFinished() {
		stopRecorder();
		tearDown();
	}

	/*@AfterMethod(alwaysRun=true)
	public void cleanup(){
		tearDown();
	}*/

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

	public void initCalledScript(ActionTestScript script, String[] parameters, Variable[] variables) {
		this.topScript = script;
		this.channelManager = script.getChannelManager();

		if(parameters != null) {
			setParameters(parameters);
		}

		if(variables != null) {
			setVariables(variables);
		}
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
		sendInfo("Closing drivers ...", "");
		getChannelManager().tearDown();
	}

	//----------------------------------------------------------------------------------------------------------
	// Script's test object
	//----------------------------------------------------------------------------------------------------------

	public TestElement findObject(TestElement parent, String tag, String operator, int expectedCount, CalculatedProperty...criterias) {

		final List<CalculatedProperty> list = new ArrayList<CalculatedProperty>();
		for (CalculatedProperty criteria : criterias) {
			list.add(criteria);
		}

		if(TestElementDialog.DIALOG_TAG.equals(tag)) {
			return new TestElementDialog(
					getCurrentChannel(), 
					channelManager.getMaxTry(),
					list);
		}else {
			return new TestElement(
					getCurrentChannel(), 
					channelManager.getMaxTry(),
					operator,
					expectedCount,
					parent, 
					tag,
					list);
		}
	}

	public TestElement findObject() {
		return new TestElement(getCurrentChannel());
	}

	public TestElement findObject(int maxTryExecution, SearchedElement searchElement, String operator, int expectedCount) {
		if(TestElementDialog.DIALOG_TAG.equals(searchElement.getTag().toLowerCase())) {
			return new TestElementDialog(getCurrentChannel(), channelManager.getMaxTry() + maxTryExecution,	searchElement);
		}else {
			return new TestElement(	getCurrentChannel(), channelManager.getMaxTry() + maxTryExecution, operator, expectedCount,	searchElement);
		}
	}

	//----------------------------------------------------------------------------------------------------------
	// Generated methods
	//----------------------------------------------------------------------------------------------------------

	public static final String JAVA_VAR_FUNCTION_NAME = "va";
	public Variable va(String name, CalculatedValue value){
		return createVariable(name, value, null);
	}

	public Variable va(String name){
		return createVariable(name, new CalculatedValue(""), null);
	}

	public Variable va(String name, Transformer transformer){
		return createVariable(name, new CalculatedValue(""), transformer);
	}

	public Variable va(String name, CalculatedValue value, Transformer transformer){
		return createVariable(name, value, transformer);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_VALUE_FUNCTION_NAME = "cv";
	public CalculatedValue cv(Object ... data) {
		return new CalculatedValue(this, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PARAM_FUNCTION_NAME = "pm";
	public String pm(int index) {
		return getParameterValue(index, "");
	}

	public String pm(int index, String defaultValue) {
		return getParameterValue(index, defaultValue);
	}

	public CalculatedValue[] pm(CalculatedValue ... values) {
		return values;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_RETURNS_FUNCTION_NAME = "rt";
	public void rt(CalculatedValue ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(CalculatedValue calc : values) {
			returnValues[i] = calc.getCalculated();
			i++;
		}

		updateVariables();
	}

	public void rt(String ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(String value : values) {
			returnValues[i] = value;
			i++;
		}

		updateVariables();
	}

	private void updateVariables() {
		Variable[] variables = getVariables();

		int index = 0;
		for(String value : returnValues) {
			if(variables.length < index + 1) {
				break;
			}

			variables[index].updateValue(value);
			index++;
		}
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ENV_FUNCTION_NAME = "sv";
	public String sv(String name) {
		return getEnvironmentValue(name, "");
	}

	public String sv(String name, String defaultValue) {
		return getEnvironmentValue(name, defaultValue);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PROPERTY_FUNCTION_NAME = "pf";
	public CalculatedProperty pf(boolean isRegexp, String name, CalculatedValue value){
		return new CalculatedProperty(isRegexp, name, value);
	}

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

	public static final String JAVA_ELEMENT_FUNCTION_NAME = "el";
	public SearchedElement el(SearchedElement parent, int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(parent, index, tagName, properties);
	}

	public SearchedElement el(int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(null, index, tagName, properties);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ROOT_FUNCTION_NAME = "rt";
	public SearchedElement rt() {
		return null;
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
	public NumericTransformer nm(int dp, String ... data) {
		return new NumericTransformer(dp, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_POS_FUNCTION_NAME = "md";
	public MouseDirectionData md(Cartesian cart, int value) {
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

	public MouseSwipe ms(int hdir, int vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseSwipe(hdir, vdir, hpos, vpos);
	}

	public MouseSwipe ms(int hdir, int vdir) {
		return new MouseSwipe(hdir, vdir);
	}

	//---------------------------------------------------------------------------------------------

	//----------------------------------------------------------------------------------------------------------
	// Actions
	//----------------------------------------------------------------------------------------------------------

	private int atsCodeLine = -1;

	public static final String JAVA_EXECUTE_FUNCTION_NAME = "exec";
	public void exec(Action action){
		action.execute(this);
	}

	public void exec(int line, Action action){
		atsCodeLine = line;
		action.execute(this);
		execFinished(action.getStatus(), true);
	}

	public void exec(int line, ActionExecute action){
		atsCodeLine = line;
		action.execute(this);
		execFinished(action.getStatus(), action.isStop());
	}

	public void exec(int line, ActionExecuteElement action){
		atsCodeLine = line;
		try {
			action.execute(this);
			execFinished(action.getStatus(), action.isStop());
		}catch (StaleElementReferenceException ex) {
			sleep(200);
			action.reinit();
			exec(line, action);
		}catch (Exception ex) {
			sleep(200);
		}
	}

	private void execFinished(ActionStatus status, boolean stop) {
		if(!status.isPassed()) {
			final String atsScriptLine = "(" + getTestName() + "." + ATS_EXTENSION + ":" + atsCodeLine + ")";

			if(status.getCode() == ActionStatus.CHANNEL_NOT_FOUND) {
				fail("[ATS-ERROR] -> No running channel, please check that 'start channel action' has been added to the script " + atsScriptLine);
			}else {
				if(stop) {
					fail("[ATS-ERROR] -> " + status.getFailMessage() + " " + atsScriptLine + "\n" + status.getChannelInfo());
				}else {
					getTopScript().sendLog(MessageCode.NON_BLOCKING_FAILED, "[ATS-INFO] -> Not stoppable action failed", status.getMessage() + atsScriptLine);
				}
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Animation recorder
	//-----------------------------------------------------------------------------------------------------------

	private IVisualRecorder recorder = new VisualRecorderNull();

	public void updateRecorderChannel(Channel channel) {
		getRecorder().setChannel(channel);
	}

	public IVisualRecorder getRecorder() {
		return topScript.recorder;
	}

	public void setRecorder(IVisualRecorder value) {
		if((value instanceof VisualRecorderNull && this.recorder instanceof VisualRecorder) || (value instanceof VisualRecorder && this.recorder instanceof VisualRecorderNull)) {
			this.recorder.terminate();
			this.recorder = value;
		}
	}

	public void startRecorder(ScriptHeader info, int quality, boolean xml) {
		topScript.setRecorder(new VisualRecorder(info, projectData, quality, xml));
	}

	public void stopRecorder() {
		topScript.setRecorder(new VisualRecorderNull());
	}

	public void createVisual(Action action) {
		getRecorder().createVisualAction(action);
	}
	
	public void createVisual(Action action, Channel channel, long duration, String name, String app) {
		getRecorder().setChannel(channel);
		createVisual(action);
		updateVisual(0, duration, name, app);
	}

	public void updateVisual(String value) {
		getRecorder().updateVisualValue(value);
	}

	public void updateVisualWithImage(int error, long duration) {
		updateVisual(error, duration);
		getRecorder().updateVisualImage();
	}
	
	public void updateVisualWithImage(int error, long duration, String value) {
		updateVisualWithImage(error, duration);
		updateVisual(value);
	}

	public void updateVisual(String value, String data) {
		getRecorder().updateVisualValue(value, data);
	}

	public void updateVisual(String type, MouseDirection position) {
		getRecorder().updateVisualValue(type, position);
	}

	public void updateVisual(int error, long duration) {
		getRecorder().updateVisualStatus(error, duration);
	}	

	public void updateVisual(int error, long duration, String value) {
		getRecorder().updateVisualStatus(error, duration, value);
	}
	
	public void updateVisual(int error, long duration, String value, String data) {
		getRecorder().updateVisualStatus(error, duration, value, data);
	}
	
	public void updateVisual(int error, long duration, String value, String data, TestElement element) {
		updateVisual(error, duration, value, data);
		getRecorder().updateVisualElement(element);
	}	

	public void updateVisual(int error, long duration, TestElement element) {
		updateVisual(error, duration);
		getRecorder().updateVisualElement(element);
	}
}