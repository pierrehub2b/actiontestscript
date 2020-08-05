package com.ats.test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;
import com.ats.generator.parsers.*;

public class DateParserTest {	
	private static final String JAVA_DATE_START_FUNCTION = "LocalDate.now()";
	private static final String JAVA_DATE_END_FUNCTION = ".format(DateTimeFormatter.ofPattern(\"MM/dd/yyyy\"))";

	@Test
	public void getJavaCodeTest() throws IOException {
		final String[] values = new String[] {"&date(06/20/1990)", "&date(32/32/2000)", "azerty", "&date(06/20/1990)", "&date(20/06/1990)", "", null};
		final String[] expectedValues = new String[] {"LocalDate.parse(\"06/20/1990\")", "LocalDate.parse(\"32/32/2000\")", JAVA_DATE_START_FUNCTION, "LocalDate.parse(\"06/20/1990\")", "LocalDate.parse(\"20/06/1990\")", JAVA_DATE_START_FUNCTION, JAVA_DATE_START_FUNCTION};
		ArrayList<String> dateParser = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			dateParser.add(DateParser.getJavaCode(values[i]));;
		}
		
		for (int i = 0; i < dateParser.size(); i++) {
			assertEquals(dateParser.get(i), expectedValues[i] + JAVA_DATE_END_FUNCTION);
		}
	}
	
	@Test
	public void dateIncrementTest() throws IOException {
		final String[] values = new String[] {"&date(06/20/1990)-5d", "&date(06/20/1990)-5m", "&date(06/20/1990)-5y", "&date(06/20/1990)+5y", "&date(06/20/1990)+5m", "&date(06/20/1990)+5d", "&date(06/20/1990)+y","&date(06/20/1990)y","&date(06/20/1990)+"};
		final String[] expectedValues = new String[] {"LocalDate.parse(\"06/20/1990\").minusDays(5)", "LocalDate.parse(\"06/20/1990\").minusMonths(5)", "LocalDate.parse(\"06/20/1990\").minusYears(5)", "LocalDate.parse(\"06/20/1990\").plusYears(5)", "LocalDate.parse(\"06/20/1990\").plusMonths(5)", "LocalDate.parse(\"06/20/1990\").plusDays(5)", "LocalDate.parse(\"06/20/1990\")","LocalDate.parse(\"06/20/1990\")","LocalDate.parse(\"06/20/1990\")"};
		ArrayList<String> dateParser = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			dateParser.add(DateParser.getJavaCode(values[i]));
		}
		
		for (int i = 0; i < dateParser.size(); i++) {
			assertEquals(dateParser.get(i), expectedValues[i] + JAVA_DATE_END_FUNCTION);
		}
	}
}