package com.ats.executor.drivers.engines;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import com.ats.element.FoundElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.TestBound;
import com.ats.executor.TestElement;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.variables.CalculatedProperty;

public interface IDriverEngine{
	public void close();
	public boolean isDesktop();
	public WebDriver getWebDriver();
	public String getApplication();
	public int switchWindow(int index);
	public void resizeWindow(int width, int height);
	public int closeWindow(int index);
	public Object executeScript(ActionStatus status, String script, Object ... params);
	public void goToUrl(URL url, boolean newWindow);
	public ArrayList<FoundElement> findWebElement(Channel channel, TestElement testObject, String tagName, String[] attributes, Predicate<Map<String, Object>> searchPredicate);
	public void waitAfterAction();
	public TestBound[] getDimensions();
	public FoundElement getElementFromPoint(Double x, Double y);
	public CalculatedProperty[] getAttributes(FoundElement element);
	public CalculatedProperty[] getAttributes(RemoteWebElement element);
	public CalculatedProperty[] getCssAttributes(FoundElement element);
	public CalculatedProperty[] getCssAttributes(RemoteWebElement element);
	public void loadParents(FoundElement hoverElement);
	public void switchToDefaultframe();
	public void scroll(FoundElement foundElement, int delta);
	public void middleClick(WebElement element);
	public WebElement getRootElement();
	public void mouseMoveToElement(ActionStatus status, FoundElement foundElement, MouseDirection position);
}