package com.ats.executor;

import static org.testng.Assert.fail;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.ITest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

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

	public ActionTestScript() {}

	public ActionTestScript(Logger logger) {
		super(logger);
		init();
	}

	private void init() {

		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);

		topScript = this;
		channelManager = new ChannelManager(this);

		if("true".equals(System.getProperty("ats.log"))) {
			setLogger(new Logger(System.out));
		}

		InputStream resourceAsStream = this.getClass().getResourceAsStream("/version.properties");
		Properties prop = new Properties();
		try{
			prop.load( resourceAsStream );
			sendInfo("ATS script started", "(version " + prop.getProperty("version") + ")");
		}catch(Exception e) {}

	}

	public String[] getReturnValues() {
		return returnValues;
	}

	//----------------------------------------------------------------------------------------------------------
	// TestNG management
	//----------------------------------------------------------------------------------------------------------

	@BeforeTest
	public void beforeTest() {
		init();
	}

	@AfterTest
	public void testFinished() {
		tearDown();
	}

	@Override
	public String getTestName() {
		return this.getClass().getName();
	}

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
		getChannelManager().tearDown();
		if(recorder != null) {
			recorder.terminate();
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

	public static final String JAVA_VALUE_FUNCTION_NAME = "cal";
	public CalculatedValue cal(Object ... data) {
		return new CalculatedValue(this, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PARAM_FUNCTION_NAME = "param";
	public String param(int index) {
		return getParameterValue(index, "");
	}

	public String param(int index, String defaultValue) {
		return getParameterValue(index, defaultValue);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PARAMS_FUNCTION_NAME = "param";
	public CalculatedValue[] param(CalculatedValue ... values) {
		return values;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_RETURNS_FUNCTION_NAME = "returns";
	public void returns(CalculatedValue ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(CalculatedValue calc : values) {
			returnValues[i] = calc.getCalculated();
			i++;
		}

		updateVariables();
	}

	public void returns(String ... values) {

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

	public static final String JAVA_PROPERTY_FUNCTION_NAME = "prop";
	public CalculatedProperty prop(boolean isRegexp, String name, CalculatedValue value){
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

	public static final String JAVA_ELEMENT_FUNCTION_NAME = "ele";
	public SearchedElement ele(SearchedElement parent, int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(parent, index, tagName, properties);
	}

	public SearchedElement ele(int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(null, index, tagName, properties);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ROOT_FUNCTION_NAME = "root";
	public SearchedElement root() {
		return null;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_REGEX_FUNCTION_NAME = "rgx";
	public RegexpTransformer rgx(String patt, int group) {
		return new RegexpTransformer(patt, group);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_DATE_FUNCTION_NAME = "dte";
	public DateTransformer dte(String ... data) {
		return new DateTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TIME_FUNCTION_NAME = "tim";
	public TimeTransformer tim(String ... data) {
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

	public static final String JAVA_MOUSE_FUNCTION_NAME = "mouse";
	public Mouse mouse(String type) {
		return new Mouse(type);
	}

	public Mouse mouse(String type, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new Mouse(type, hpos, vpos);
	}

	public MouseKey mouse(String type, Keys key, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseKey(type, key, hpos, vpos);
	}

	public MouseKey mouse(String type, Keys key) {
		return new MouseKey(type, key);
	}

	public MouseScroll mouse(int scroll, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseScroll(scroll, hpos, vpos);
	}

	public MouseScroll mouse(int scroll) {
		return new MouseScroll(scroll);
	}

	public MouseSwipe mouse(int hdir, int vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseSwipe(hdir, vdir, hpos, vpos);
	}

	public MouseSwipe mouse(int hdir, int vdir) {
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
		getChannelManager().startChannel(name, app);
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
	public void startRecorder(ScriptHeader info, boolean visual, boolean pdf, boolean xml) {
		if(recorder == null) {
			recorder = new RecorderThread(info, projectData, visual, pdf, xml);
		}
	}

	public void stopRecorder() {
		if(recorder != null) {
			recorder.terminate();
			recorder = null;
		}
	}

	public void pauseRecorder(boolean value) {
		if(recorder != null) {
			recorder.setPause(value);
		}
	}

	public void updateVisualImage() {
		if(recorder != null) {
			recorder.updateVisualImage(getCurrentChannel().getScreenShot());
		}
	}

	public void updateVisualElement(TestElement to) {
		if(recorder != null) {
			recorder.updateVisualElement(to);
		}
	}

	public void updateVisualValue(String value) {
		if(recorder != null) {
			recorder.updateVisualValue(value);
		}
	}

	public void updateVisualValue(String value, String data) {
		if(recorder != null) {
			recorder.updateVisualValue(value, data);
		}
	}

	public void updateVisualStatus(boolean value) {
		if(recorder != null) {
			recorder.updateVisualStatus(value);
		}
	}	

	public void newVisual(Action action) {
		if(recorder != null && getCurrentChannel() != null) {
			recorder.addVisual(getCurrentChannel(), action);
		}
	}
}