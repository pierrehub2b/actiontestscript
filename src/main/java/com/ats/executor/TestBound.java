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

import java.util.ArrayList;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

public class TestBound {

	private Double x = 0.0;
	private Double y = 0.0;
	private Double width = 0.0;
	private Double height = 0.0;
	
	public TestBound() {}

	public TestBound(Double x, Double y) {
		this.x = x;
		this.y = y;
	}
	
	public TestBound(Double x, Double y, Double width, Double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Point getPoint(){
		return new Point(x.intValue(), y.intValue());
	}
	
	public Dimension getSize(){
		return new Dimension(width.intValue(), height.intValue());
	}
	
	public boolean isCollision(TestBound dimension){
		return getPoint().x == dimension.getPoint().x || getPoint().y == dimension.getPoint().y;
	}
	
	public void updateLocation(Double x, Double y) {
		this.x += x;
		this.y += y;		
	}
	

	public void update(ArrayList<Double> dim) {
		this.x = dim.get(0);
		this.y = dim.get(1);
		this.width = dim.get(2);
		this.height = dim.get(3);
	}
	
	//----------------------------------------------------------------------------------------------------------------------
	// Getter and setter for serialization
	//----------------------------------------------------------------------------------------------------------------------

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}
}
