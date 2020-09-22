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

package com.ats.tools.performance.filters;

import com.ats.tools.performance.proxy.AtsProxy;
import com.browserup.harreader.model.HarEntry;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UrlBaseFilter {

	public static Predicate<UrlPattern> isMatching(String value) {
        return p -> p.match(value);
    }
	
	private String pageId = AtsProxy.CHANNEL_STARTED_PAGEID;
	
	protected List<UrlPattern> listUrls;
	
	public UrlBaseFilter() {
	}
	
	public UrlBaseFilter(List<String> list) {
		listUrls = list.stream().map(s -> new UrlPattern(s)).collect(Collectors.toList());
	}
	
	public void setPageId(String value) {
		this.pageId = value;
	}

	public void filter(List<HarEntry> logEntries, final List<HarEntry> savedEntries) {
		while(logEntries.size() > 0) {
			process(logEntries.remove(0), savedEntries);
		}
	}
	
	protected void saveEntry(List<HarEntry> savedEntries, HarEntry entry) {
		entry.setPageref(pageId);
		savedEntries.add(entry);
	}
	
	protected void process(HarEntry entry, List<HarEntry> savedEntries) {
		saveEntry(savedEntries, entry);
	}
}