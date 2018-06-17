package com.ats.recorder;

import com.ats.executor.TestBound;

public class VisualElement {
	
	private TestBound bound;
	private int foundElements;
	private Long searchDuration;
	private String tag;
	private String criterias;
	
	public VisualElement() {}
	
	public TestBound getBound() {
		return bound;
	}

	public void setBound(TestBound bound) {
		this.bound = bound;
	}

	public int getFoundElements() {
		return foundElements;
	}

	public void setFoundElements(int foundElements) {
		this.foundElements = foundElements;
	}

	public Long getSearchDuration() {
		return searchDuration;
	}

	public void setSearchDuration(Long searchDuration) {
		this.searchDuration = searchDuration;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCriterias() {
		return criterias;
	}

	public void setCriterias(String criterias) {
		this.criterias = criterias;
	}
}