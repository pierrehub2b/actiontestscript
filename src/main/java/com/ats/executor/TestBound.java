package com.ats.executor;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

public class TestBound {

	private Double x;
	private Double y;
	private Double width;
	private Double height;
	
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
