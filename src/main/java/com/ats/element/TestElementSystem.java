package com.ats.element;

import java.util.function.Predicate;

import com.ats.executor.channels.Channel;

public class TestElementSystem extends TestElement {

	public TestElementSystem(Channel channel, int maxTry, Predicate<Integer> predicate, SearchedElement searchElement) {

		super(channel, maxTry, predicate, searchElement.getIndex());

		if(searchElement.getParent() != null){
			this.parent = new TestElementSystem(channel, maxTry, predicate, searchElement.getParent());
		}

		this.engine = channel.getDesktopDriverEngine();
		startSearch(true, searchElement.getTag(), searchElement.getCriterias());
	}
}