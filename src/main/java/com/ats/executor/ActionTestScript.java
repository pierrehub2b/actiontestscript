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
import java.net.URL;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.ats.driver.AtsManager;
import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.generator.objects.BoundData;
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
import com.ats.recorder.RecorderThread;
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

		TestRunner runner = (TestRunner) ctx;
		setTestName(this.getClass().getName());
		
		String checkMode = runner.getTest().getParameter("check.mode");
		if("true".equals(checkMode)) {
			
			throw new SkipException("check mode : " + testName);

		}else {
			
			setTestParameters(runner.getTest().getAllParameters());
			String visualReport = getEnvironmentValue("visual.report", "");
			
			if("true".equals(visualReport.trim().toLowerCase())) {

				String outputDirectory = runner.getOutputDirectory();

				File output = new File(outputDirectory);
				if(!output.exists()) {
					output.mkdirs();
				}
				setRecorder(new RecorderThread(output, testName, true, false, false));
			}

			setLogger(new ExecutionLogger(System.out, (int)ctx.getSuite().getXmlSuite().getVerbose()));
			sendInfo("starting script", " '" + testName + "'");

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					tearDown();
				}
			});

			new StopExecutionThread(System.in).start();
		}
	}

	@AfterClass(alwaysRun=true)
	public void afterClass() {
		sendInfo("script terminated", " '" + testName + "'");
	}

	@AfterTest(alwaysRun=true)
	public void testFinished() {
		tearDown();
	}

	@AfterMethod(alwaysRun=true)
	public void cleanup(){
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
		sendInfo("script's execution terminated", ", closing drivers ...");
		if(isRecord()) {
			setRecorder(null);
		}
		getChannelManager().tearDown();
	}

	//----------------------------------------------------------------------------------------------------------
	// Script's test object
	//----------------------------------------------------------------------------------------------------------

	public TestElement findObject(TestElement parent, String tag, String operator, int expectedCount, CalculatedProperty...criterias) {

		List<CalculatedProperty> list = new ArrayList<CalculatedProperty>();
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
			String atsScriptError = "(" + getTestName() + "." + ATS_EXTENSION + ":" + atsCodeLine + ")";

			if(status.getCode() == ActionStatus.CHANNEL_NOT_FOUND) {
				fail("ATS error -> No running channel, please check that 'start channel action' has been added to the script " + atsScriptError);
			}else {
				if(stop) {
					fail("ATS error -> " + status.getFailMessage() + " " + atsScriptError + "\n" + status.getChannelInfo());
				}else {
					getTopScript().sendLog(MessageCode.NON_BLOCKING_FAILED, "ATS script info -> Not stoppable action failed", status.getMessage() + atsScriptError);
				}
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Channel action
	//-----------------------------------------------------------------------------------------------------------

	public void startChannel(ActionStatus status, String name, String app){
		getChannelManager().startChannel(status, name, app);
		updateStatus(status);
	}

	public void switchChannel(ActionStatus status, String name){
		updateStatus(status, getChannelManager().switchChannel(name));
	}

	public void closeChannel(ActionStatus status, String name){
		updateStatus(status, getChannelManager().closeChannel(name));
	}

	private void updateStatus(ActionStatus status, boolean foundChannel){
		if(!foundChannel) {
			status.setCode(ActionStatus.CHANNEL_NOT_FOUND);
		}
		updateStatus(status);
	}

	private void updateStatus(ActionStatus status){
		status.setData(getChannelManager().getChannelsList());
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Window action
	//-----------------------------------------------------------------------------------------------------------

	public void setWindowBound(ActionStatus status, BoundData x, BoundData y, BoundData width, BoundData height){
		if(getCurrentChannel() != null){
			getCurrentChannel().setWindowBound(x, y, width, height);
		}
	}

	public void switchWindow(ActionStatus status, int index){
		if(getCurrentChannel() != null){
			getCurrentChannel().switchWindow(index);
			status.setPassed(true);
		}
	}

	public void closeWindow(ActionStatus status, int index){
		if(getCurrentChannel() != null){
			getCurrentChannel().closeWindow(status, index);
			status.setPassed(true);
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Goto Url Action
	//-----------------------------------------------------------------------------------------------------------

	public void navigate(ActionStatus status, URL url, boolean newWindow) {
		if(getCurrentChannel() != null){

			sendInfo("goto url", " '" + url.toString() + "'");

			getCurrentChannel().navigate(url, newWindow);

			//TODO check url with browser
			//if(Utils.checkUrl(status, url)) {
			//	status.setMessage(getCurrentChannel().goToUrl(url).toString());
			//}
		}
		status.setMessage(getCurrentChannel().getCurrentUrl());
		status.setPassed(true);
		status.updateDuration();
	}

	public void navigate(ActionStatus status, String type){
		if(getCurrentChannel() != null){
			getCurrentChannel().navigate(type);
		}
		status.setMessage(getCurrentChannel().getCurrentUrl());
		status.setPassed(true);
		status.updateDuration();
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Animation recorder
	//-----------------------------------------------------------------------------------------------------------

	private RecorderThread recorder;
	private boolean record = false;
	
	public boolean isRecord() {
		return topScript.record;
	}
	
	public RecorderThread getRecorder() {
		return topScript.recorder;
	}

	public void setRecorder(RecorderThread value) {
		if(value == null) {
			if(this.recorder != null) {
				this.recorder.terminate();
				this.recorder = null;
			}
			this.record = false;
		}else {
			this.recorder = value;
			this.record = true;
		}
	}

	public void startRecorder(ScriptHeader info, boolean visual, boolean pdf, boolean xml) {
		if(!isRecord()) {
			topScript.setRecorder(new RecorderThread(info, projectData, visual, pdf, xml));
		}
	}

	public void stopRecorder() {
		if(isRecord()) {
			topScript.setRecorder(null);
		}
	}

	public void pauseRecorder(boolean value) {
		if(isRecord()) {
			getRecorder().setPause(value);
		}
	}

	public void updateVisualImage() {
		if(isRecord() && getCurrentChannel() != null) {
			getRecorder().updateVisualImage(getCurrentChannel().getScreenShot());
		}
	}

	public void updateVisualElement(TestElement to) {
		if(isRecord()) {
			getRecorder().updateVisualElement(to);
		}
	}

	public void updateVisualValue(String value) {
		if(isRecord()) {
			getRecorder().updateVisualValue(value);
		}
	}

	public void updateVisualValue(String value, String data) {
		if(isRecord()) {
			getRecorder().updateVisualValue(value, data);
		}
	}
	
	public void updateVisualValue(String type, MouseDirection position) {
		if(isRecord()) {
			getRecorder().updateVisualValue(type, position);
		}
	}

	public void updateVisualStatus(boolean value) {
		if(isRecord()) {
			getRecorder().updateVisualStatus(value);
		}
	}	

	public void newVisual(Action action) {
		if(isRecord() && getCurrentChannel() != null) {
			getRecorder().addVisual(getCurrentChannel(), action);
		}
	}
}