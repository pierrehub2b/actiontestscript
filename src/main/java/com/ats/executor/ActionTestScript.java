package com.ats.executor;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.TestRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.Cartesian;
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
import com.ats.tools.logger.Logger;
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

	public ActionTestScript(Logger logger) {
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
		InputStream resourceAsStream = this.getClass().getResourceAsStream("/version.properties");
		Properties prop = new Properties();
		try{
			prop.load( resourceAsStream );
			System.out.println("ATS script started (version " + prop.getProperty("version") + ")");
		}catch(Exception e) {}
	}

	@BeforeClass(alwaysRun=true)
	public void beforeTest(ITestContext ctx) {

		TestRunner runner = (TestRunner) ctx;

		setTestName(this.getClass().getName());
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

		setLogger(new Logger(ctx.getSuite().getXmlSuite().getVerbose()));
		sendInfo("starting script", " '" + testName + "'");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tearDown();
			}
		});

		new StopExecutionThread(System.in).start();

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
		getChannelManager().tearDown();
		if(getRecorder() != null) {
			getRecorder().terminate();
			setRecorder(null);
		}
	}

	//----------------------------------------------------------------------------------------------------------
	// Script's test object
	//----------------------------------------------------------------------------------------------------------

	public TestElement findObject(TestElement parent, String tag, int expectedCount, CalculatedProperty...criterias) {

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
					expectedCount,
					parent, 
					tag,
					list);
		}
	}

	public TestElement findObject() {
		return new TestElement(getCurrentChannel());
	}

	public TestElement findObject(int maxTryExecution, SearchedElement searchElement, int expectedCount) {
		if(TestElementDialog.DIALOG_TAG.equals(searchElement.getTag().toLowerCase())) {
			return new TestElementDialog(	getCurrentChannel(), channelManager.getMaxTry() + maxTryExecution,	searchElement);
		}else {
			return new TestElement(	getCurrentChannel(), channelManager.getMaxTry() + maxTryExecution, expectedCount,	searchElement);
		}
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

	public static final String JAVA_VALUE_FUNCTION_NAME = "cvl";
	public CalculatedValue cvl(Object ... data) {
		return new CalculatedValue(this, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PARAM_FUNCTION_NAME = "pmr";
	public String pmr(int index) {
		return getParameterValue(index, "");
	}

	public String pmr(int index, String defaultValue) {
		return getParameterValue(index, defaultValue);
	}

	public CalculatedValue[] pmr(CalculatedValue ... values) {
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

	public static final String JAVA_ENV_FUNCTION_NAME = "env";
	public String env(String name) {
		return getEnvironmentValue(name, "");
	}

	public String env(String name, String defaultValue) {
		return getEnvironmentValue(name, defaultValue);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PROPERTY_FUNCTION_NAME = "ppy";
	public CalculatedProperty ppy(boolean isRegexp, String name, CalculatedValue value){
		return new CalculatedProperty(isRegexp, name, value);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_UUID_FUNCTION_NAME = "uid";
	public String uid() {
		return getUuidValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TODAY_FUNCTION_NAME = "tdy";
	public String tdy() {
		return getTodayValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_NOW_FUNCTION_NAME = "now";
	public String now() {
		return getNowValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ELEMENT_FUNCTION_NAME = "elt";
	public SearchedElement elt(SearchedElement parent, int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(parent, index, tagName, properties);
	}

	public SearchedElement elt(int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(null, index, tagName, properties);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ROOT_FUNCTION_NAME = "root";
	public SearchedElement root() {
		return null;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_REGEX_FUNCTION_NAME = "rxp";
	public RegexpTransformer rxp(String patt, int group) {
		return new RegexpTransformer(patt, group);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_DATE_FUNCTION_NAME = "dte";
	public DateTransformer dte(String ... data) {
		return new DateTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TIME_FUNCTION_NAME = "tme";
	public TimeTransformer tme(String ... data) {
		return new TimeTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_NUMERIC_FUNCTION_NAME = "num";
	public NumericTransformer num(int dp, String ... data) {
		return new NumericTransformer(dp, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_POS_FUNCTION_NAME = "pos";
	public MouseDirectionData pos(Cartesian cart, int value) {
		return new MouseDirectionData(cart, value);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_MOUSE_FUNCTION_NAME = "mse";
	public Mouse mse(String type) {
		return new Mouse(type);
	}

	public Mouse mse(String type, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new Mouse(type, hpos, vpos);
	}

	public MouseKey mse(String type, Keys key, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseKey(type, key, hpos, vpos);
	}

	public MouseKey mse(String type, Keys key) {
		return new MouseKey(type, key);
	}

	public MouseScroll mse(int scroll, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseScroll(scroll, hpos, vpos);
	}

	public MouseScroll mse(int scroll) {
		return new MouseScroll(scroll);
	}

	public MouseSwipe mse(int hdir, int vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseSwipe(hdir, vdir, hpos, vpos);
	}

	public MouseSwipe mse(int hdir, int vdir) {
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
			exec(line, action);
		}catch (Exception ex) {
			sleep(200);
		}
	}

	private void execFinished(ActionStatus status, boolean stop) {
		if(!status.isPassed()) {

			String atsScriptError = "(" + getTestName() + "." + ATS_EXTENSION + ":" + atsCodeLine + ")";

			if(status.getCode() == ActionStatus.CHANNEL_NOT_FOUND) {
				fail("ATS script error -> No running channel, please check that 'start channel action' has been added to the script " + atsScriptError);
			}else {
				if(stop) {
					fail("ATS script error -> " + status.getMessage() + " after " + status.getDuration() + " ms " + atsScriptError);
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
		sendInfo("start channel", " '" + name + "' -> " + app);
		getChannelManager().startChannel(name, app);
		updateStatus(status);
	}

	public void switchChannel(ActionStatus status, String name){
		sendInfo("switch channel", " '" + name + "'");
		updateStatus(status, getChannelManager().switchChannel(name));
	}

	public void closeChannel(ActionStatus status, String name){
		sendInfo("close channel", " '" + name + "'");
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
		status.setMessage(getCurrentChannel().getWebDriver().getCurrentUrl());
		status.setPassed(true);
		status.updateDuration();
	}

	public void navigate(ActionStatus status, String type){
		if(getCurrentChannel() != null){
			getCurrentChannel().navigate(type);
		}
		status.setMessage(getCurrentChannel().getWebDriver().getCurrentUrl());
		status.setPassed(true);
		status.updateDuration();
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Animation recorder
	//-----------------------------------------------------------------------------------------------------------

	private RecorderThread recorder;
	public RecorderThread getRecorder() {
		return topScript.recorder;
	}

	public void setRecorder(RecorderThread value) {
		this.recorder = value;
	}

	public void startRecorder(ScriptHeader info, boolean visual, boolean pdf, boolean xml) {
		if(getRecorder() == null) {
			topScript.setRecorder(new RecorderThread(info, projectData, visual, pdf, xml));
		}
	}

	public void stopRecorder() {
		if(getRecorder() != null) {
			getRecorder().terminate();
			topScript.setRecorder(null);
		}
	}

	public void pauseRecorder(boolean value) {
		if(getRecorder() != null) {
			getRecorder().setPause(value);
		}
	}

	public void updateVisualImage() {
		if(getRecorder() != null) {
			getRecorder().updateVisualImage(getCurrentChannel().getScreenShot());
		}
	}

	public void updateVisualElement(TestElement to) {
		if(getRecorder() != null) {
			getRecorder().updateVisualElement(to);
		}
	}

	public void updateVisualValue(String value) {
		if(getRecorder() != null) {
			getRecorder().updateVisualValue(value);
		}
	}

	public void updateVisualValue(String value, String data) {
		if(getRecorder() != null) {
			getRecorder().updateVisualValue(value, data);
		}
	}

	public void updateVisualStatus(boolean value) {
		if(getRecorder() != null) {
			getRecorder().updateVisualStatus(value);
		}
	}	

	public void newVisual(Action action) {
		if(getRecorder() != null && getCurrentChannel() != null) {
			getRecorder().addVisual(getCurrentChannel(), action);
		}
	}
}