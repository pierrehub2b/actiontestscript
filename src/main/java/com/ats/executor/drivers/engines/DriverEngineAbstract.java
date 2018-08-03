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

package com.ats.executor.drivers.engines;

import java.awt.Rectangle;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ats.driver.ApplicationProperties;
import com.ats.executor.channels.Channel;
import com.ats.generator.objects.BoundData;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirection;
import com.ats.generator.objects.MouseDirectionData;

public abstract class DriverEngineAbstract {
	
	protected Channel channel;
	protected RemoteWebDriver driver;
	protected String application;
	protected String applicationPath;
	
	protected int currentWindow = 0;
	
	private int actionWait = -1;

	public DriverEngineAbstract(Channel channel, String application, ApplicationProperties props, int defaultWait){
		this.channel = channel;
		this.application = application;
		
		if(props != null) {
			actionWait = props.getWait();
			applicationPath = props.getPath();
		}
		
		if(actionWait == -1) {
			actionWait = defaultWait;
		}
	}
	
	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public String getApplication() {
		return application;
	}
	
	public String getApplicationPath() {
		return applicationPath;
	}
	
	public void actionWait() {
		channel.sleep(actionWait);
	}
	
	public int getActionWait() {
		return actionWait;
	}

	protected int getDirectionValue(int value, MouseDirectionData direction,Cartesian cart1, Cartesian cart2) {
		if(cart1.equals(direction.getName())) {
			return direction.getValue();
		}else if(cart2.equals(direction.getName())) {
			return value - direction.getValue();
		}
		return 0;
	}

	protected int getNoDirectionValue(int value) {
		return value / 2;
	}

	private int getCartesianOffset(int value, MouseDirectionData direction, Cartesian cart1, Cartesian cart2) {
		if(direction != null) {
			return getDirectionValue(value, direction, cart1, cart2);
		}else {
			return getNoDirectionValue(value);
		}
	}

	protected int getOffsetX(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.width, position.getHorizontalPos(), Cartesian.LEFT, Cartesian.RIGHT);
	}

	protected int getOffsetY(Rectangle rect, MouseDirection position) {
		return getCartesianOffset(rect.height, position.getVerticalPos(), Cartesian.TOP, Cartesian.BOTTOM);
	}
	
	public void setWindowBound(BoundData x, BoundData y, BoundData width, BoundData height) {

		if(width != null || height != null){
			int newWidth = channel.getDimension().getWidth().intValue();
			if(width != null) {
				newWidth = width.getValue();
			}

			int newHeight = channel.getDimension().getHeight().intValue();
			if(height != null) {
				newHeight = height.getValue();
			}

			setSize(new Dimension(newWidth, newHeight));
		}

		if(x != null || y != null){
			int newX = channel.getDimension().getX().intValue();
			if(x != null) {
				newX = x.getValue();
			}

			int newY = channel.getDimension().getY().intValue();
			if(y != null) {
				newY = y.getValue();
			}

			setPosition(new Point(newX, newY));
		}
	}

	protected void setPosition(Point pt) {} // no action by default
	protected void setSize(Dimension dim) {} // no action by default
}
