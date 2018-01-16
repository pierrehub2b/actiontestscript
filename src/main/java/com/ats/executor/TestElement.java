package com.ats.executor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.Select;

import com.ats.element.FoundElement;
import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.script.actions.ActionSelect;
import com.ats.tools.logger.MessageCode;

public class TestElement{

	public static final String DESKTOP_PREFIX = "desk:";

	private Actions action;
	private Channel channel;

	private long searchDuration = 0;
	private long totalSearchDuration = 0;
	private int count = 0;

	private TestElement parent;
	private ArrayList<FoundElement> foundElements = new ArrayList<FoundElement>();

	private int maxTry;
	private int index;

	private String criterias;

	private boolean isDesktop = false;

	public TestElement(Channel channel, int maxTry, TestElement parent, String tag, List<CalculatedProperty> criterias) {

		this.channel = channel;
		this.maxTry = maxTry;
		this.parent = parent;

		this.isDesktop = channel.isDesktop();
		if(tag.startsWith(DESKTOP_PREFIX)){
			this.isDesktop = true;
			tag = tag.substring(DESKTOP_PREFIX.length());
		}

		initSearch(tag, criterias);
	}
	
	public TestElement(Channel channel) {
		this.channel = channel;
		this.index = 0;
		this.foundElements = new ArrayList<FoundElement>();
		this.foundElements.add(new FoundElement(channel));
	}

	public TestElement(Channel channel, int maxTry, SearchedElement searchElement) {

		this.channel = channel;
		this.maxTry = maxTry;
		this.index = searchElement.getIndex();
		if(searchElement.getParent() != null){
			this.parent = new TestElement(channel, maxTry, searchElement.getParent());
		}

		this.isDesktop = searchElement.getTag().startsWith(DESKTOP_PREFIX) || channel.isDesktop();

		initSearch(searchElement.getTag(), searchElement.getCriterias());
	}

	public TestElement(FoundElement element, Channel currentChannel) {
		this.foundElements.add(element);
		this.channel = currentChannel;
		this.count = getElementsCount();

		this.action = new Actions(channel.getWebDriver());
	}
	
	public void dispose() {
		action = null;
		channel = null;
		
		if(parent != null) {
			parent.dispose();
			parent = null;
		}
		
		while(foundElements.size() > 0) {
			foundElements.remove(0).dispose();
		}
	}

	private void initSearch(String tag, List<CalculatedProperty> properties) {

		if(channel != null){
			
			channel.switchToDefaultframe();
			
			action = new Actions(channel.getWebDriver());

			criterias = tag;
			searchDuration = System.currentTimeMillis();

			if(parent == null || (parent != null && parent.isFound())){

				if(isDesktop){

					WebElement parentElement = null;
					if(parent != null) {
						parentElement = parent.getWebElement();
					}
					
					foundElements = channel.findWindowsElement(parentElement, tag, properties);

				}else{

					ArrayList<String> attributeList = new ArrayList<String>();

					Predicate<Map<String, Object>> fullPredicate = Objects::nonNull;

					if(properties != null){
						for (CalculatedProperty property : properties){

							criterias += "," + property.getName() + ":" + property.getValue().getCalculated();

							attributeList.add(property.getName());
							fullPredicate = property.getPredicate(fullPredicate);
						}
					}

					String[] attributes = attributeList.toArray(new String[attributeList.size()]);

					foundElements = channel.findWebElement(this, tag, attributes, fullPredicate, false);

					int trySearch = 0;
					while(getElementsCount() == 0 && trySearch < maxTry){

						channel.sendLog(MessageCode.OBJECT_TRY_SEARCH, "searching element", maxTry - trySearch);
						foundElements = channel.findWebElement(this, tag, attributes, fullPredicate, false);

						channel.sleep(100 + trySearch*30);
						trySearch++;
					}
				}
			}

			searchDuration = System.currentTimeMillis() - this.searchDuration;
			totalSearchDuration = getTotalDuration();
			count = getElementsCount();

			if(index > 0) {
				index--;
			}

			//if(count > 0) {
			//	channel.scroll(getFoundElements().get(index), 0);
			//}
		}
	}

	private int getElementsCount() {
		if(index == 0) {
			return foundElements.size();
		}else if(foundElements.size() >= index){
			return 1;
		}else {
			return 0;
		}
	}

	private Long getTotalDuration(){
		if(parent != null){
			return searchDuration + parent.getTotalDuration();
		}else{
			return searchDuration;
		}
	}
	
	public FoundElement getFoundElement() {
		return foundElements.get(index); 
	}
	
	public WebElement getWebElement() {
		return getFoundElement().getValue();
	}

	public Rectangle getWebElementRectangle() {
		return getFoundElement().getRectangle();
	}

	public boolean isFound() {
		return count > 0;
	}
	
	public boolean isIframe() {
		if(isFound()){
			return getFoundElement().isIframe();
		}else{
			return false;
		}
	}

	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public TestElement getParent() {
		return parent;
	}

	public void setParent(TestElement parent) {
		this.parent = parent;
	}

	public long getSearchDuration() {
		return searchDuration;
	}

	public void setSearchDuration(long searchDuration) {
		this.searchDuration = searchDuration;
	}

	public long getTotalSearchDuration() {
		return totalSearchDuration;
	}

	public void setTotalSearchDuration(long totalSearchDuration) {
		this.totalSearchDuration = totalSearchDuration;
	}

	public ArrayList<FoundElement> getFoundElements() {
		return foundElements;
	}

	public void setFoundElements(ArrayList<FoundElement> data) {
		this.foundElements = data;
	}	

	public String getCriterias() {
		return criterias;
	}

	public void setCriterias(String criterias) {
		this.criterias = criterias;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Actions ...
	//-------------------------------------------------------------------------------------------------------------------


	//-------------------------------------------------------------------------------------------------------------------
	// Text ...
	//-------------------------------------------------------------------------------------------------------------------

	private void clearText(ActionStatus status) {
		if(isDesktop) {
			click(status, false);
			getWebElement().sendKeys("");//TODO ctrl+a and backspace
		}else {
			channel.executeScript(status, "arguments[0].value=''", getWebElement());
		}
	}

	public void sendText(ActionStatus status, boolean clear, ArrayList<SendKeyData> textActionList) {

		if(clear) {
			clearText(status);
		}

		if(status.isPassed()) {
			for(SendKeyData sequence : textActionList) {
				action.sendKeys(sequence.getSequence());
			}

			action.perform();
			//channel.waitAfterAction();
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Select ...
	//-------------------------------------------------------------------------------------------------------------------

	public void select(ActionStatus status, CalculatedProperty selectProperty, boolean select, boolean ctrl) {
		if(isFound()){

			Select selectElement = new Select(getWebElement());

			if(ActionSelect.SELECT_INDEX.equals(selectProperty.getName())){

				int idx = 0;
				try {
					idx = Integer.parseInt(selectProperty.getValue().getCalculated());
				}catch(NumberFormatException e) {}

				if(select) {
					selectElement.selectByIndex(idx);
				}else {
					selectElement.deselectByIndex(idx);
				}

			}else if (ActionSelect.SELECT_VALUE.equals(selectProperty.getName())){
				if(select) {
					selectElement.selectByValue(selectProperty.getValue().getCalculated());
				}else {
					selectElement.deselectByValue(selectProperty.getValue().getCalculated());
				}
			}else {

				if(select) {
					selectElement.selectByVisibleText(selectProperty.getValue().getCalculated());
				}else {
					selectElement.deselectByVisibleText(selectProperty.getValue().getCalculated());
				}
			}

			status.setPassed(true);

		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot select index !");
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Mouse ...
	//-------------------------------------------------------------------------------------------------------------------

	private void moveOver(ActionStatus status, MouseDirection position) {

		//int loop = 20;

		//while(loop > 0){
		//	try {

				int elementWidth = getWebElementRectangle().width;
				int elementHeight = getWebElementRectangle().height;

				int xOffset = 0;
				int yOffset = 0;

				if(position.getHorizontalPos() != null) {
					if(Cartesian.LEFT.equals(position.getHorizontalPos().getName())) {
						xOffset = position.getHorizontalPos().getValue();
					}else if(Cartesian.RIGHT.equals(position.getHorizontalPos().getName())) {
						xOffset = elementWidth - position.getHorizontalPos().getValue();
					}

					if(channel.isFirefox()) {
						xOffset -= elementWidth/2;
					}

				}else {
					if(!channel.isFirefox()) {
						xOffset = elementWidth / 2;
					}
				}

				if(position.getVerticalPos() != null) {
					if(Cartesian.TOP.equals(position.getVerticalPos().getName())) {
						yOffset = position.getVerticalPos().getValue();
					}else if(Cartesian.BOTTOM.equals(position.getVerticalPos().getName())) {
						yOffset = elementHeight - position.getVerticalPos().getValue();
					}

					if(channel.isFirefox()) {
						yOffset -= elementHeight/2;
					}

				}else {
					if(!channel.isFirefox()) {
						yOffset = elementHeight / 2;
					}
				}

				action.moveToElement(getWebElement(), xOffset, yOffset).perform();

				status.setPassed(true);
				//channel.waitAfterAction();

			//	loop = 0;

			//}catch(WebDriverException e) {	
			//	if(loop == 1) {
			//		throw e;
			//	}
			//}

			channel.sleep(150);
			//loop--;
		//}
	}

	public void over(ActionStatus status, MouseDirection position) {
		if(isFound()){

			channel.scroll(getFoundElement(), 0);

			if(channel.waitElementIsVisible(getWebElement())) {
				moveOver(status, position);
			}else {	
				status.setPassed(false);
				status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
				status.setMessage("element not visible");
			}

		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot execute over action !");
		}
	}

	public void click(ActionStatus status, boolean hold) {

		int loop = 20;

		while(loop > 0){
			try {

				if(hold) {
					action.clickAndHold();
				}else {
					action.click();
				}

				action.perform();
				status.setPassed(true);

				loop = 0;

			}catch(ElementNotVisibleException e0) {	

				if(loop == 1) {
					status.setPassed(false);
					status.setCode(ActionStatus.OBJECT_NOT_VISIBLE);
				}

			}catch(WebDriverException e0) {	

				if(loop == 1) {
					status.setPassed(false);
					if(e0.getMessage().contains("is not clickable") || e0.getMessage().contains("Element is obscured")) {
						status.setCode(ActionStatus.OBJECT_NOT_CLICKABLE);
					}else {
						throw e0;
					}
				}
			}

			channel.sleep(200);
			loop--;
		}
	}

	public void drag(ActionStatus status) {
		click(status, true);
	}

	public void drop(ActionStatus status) {
		action.release().perform();
		status.setPassed(true);
		//channel.waitAfterAction();
	}

	public void swipe(ActionStatus status, int hDirection, int vDirection) {
		click(status, true);
		action.moveByOffset(hDirection, vDirection);
		drop(status);
	}

	public void mouseWheel(ActionStatus status, int delta) {
		if(isFound()){
			channel.scroll(getFoundElement(), delta);
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot execute wheel action !");
		}
	}

	public ActionStatus wheelClick(ActionStatus status) {
		if(isFound()){
			channel.middleClick(getWebElement());
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot execute wheel click action !");
		}
		return status;
	}

	public ActionStatus doubleClick(ActionStatus status) {
		if(isFound()){
			action.doubleClick().perform();
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot execute double click action !");
		}

		return status;
	}

	public ActionStatus rightClick(ActionStatus status) {
		if(isFound()){
			action.contextClick().perform();
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot execute right click action !");
		}

		return status;
	}

	public boolean dropTo() {
		// TODO Auto-generated method stub
		return false;
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Attributes
	//-------------------------------------------------------------------------------------------------------------------

	public String getAttribute(String name){
		String result = null;
		if(isFound()){
			result = getWebElement().getAttribute(name);
			//attributeData = getAttributeValueByName(name);
			if(result == null){
				result = getCssAttributeValueByName(name);
			}
		}
		return result;
	}

	private String getCssAttributeValueByName(String name) {
		return foundAttributeValue(name, getCssAttributes());
	}

	/*private String getAttributeValueByName(String name) {
		return foundAttributeValue(name, getAttributes());
	}*/

	private String foundAttributeValue(String name, CalculatedProperty[] properties) {
		Stream<CalculatedProperty> stream = Arrays.stream(properties);
		Optional<CalculatedProperty> calc = stream.parallel().filter(c -> c.getName().equals(name)).findFirst();
		if(calc.isPresent()) {
			return calc.get().getValue().getCalculated();
		}
		return null;
	}

	public CalculatedProperty[] getAttributes() {
		return channel.getAttributes((RemoteWebElement)getWebElement());
	}

	public CalculatedProperty[] getCssAttributes() {
		return channel.getCssAttributes((RemoteWebElement)getWebElement());
	}

	public Object executeScript(ActionStatus status, String script) {
		if(isFound()){
			return channel.executeScript(status, "arguments[0]." + script, getWebElement());
		}else{
			status.setPassed(false);
			status.setCode(ActionStatus.OBJECT_NOT_FOUND);
			status.setMessage("Object not found, cannot execute script action !");
		}
		return null;
	}
}