package com.ats.tools.report;

import java.util.Arrays;
import java.util.stream.Stream;

public class SuitesReport {
	public String projectId;
	public SuitesReportItem[] suites;
	
	public SuitesReport(String id, SuitesReportItem suite) {
		this.projectId = id;
		this.suites = new SuitesReportItem[] {suite};
	}
	
	public void add(SuitesReportItem suite) {
		this.suites = Stream.concat(Arrays.stream(this.suites), Arrays.stream(new SuitesReportItem[] {suite})).toArray(SuitesReportItem[]::new);
	}
}
