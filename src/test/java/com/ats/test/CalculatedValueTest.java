package com.ats.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;

public class CalculatedValueTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/*
	 * @Before public void setUp() { }
	 * 
	 * @After public void tearDown() { }
	 */

	@Test
	public void dateValues() throws IOException {
		
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		final CalculatedValue calc = new CalculatedValue(script, "$today xx $now");
		
		assertTrue(calc.getCalculated().matches("\\d\\d\\d\\d-\\d\\d-\\d\\d xx \\d\\d:\\d\\d:\\d\\d"));
		assertTrue(calc.getCalculated().matches("\\d\\d\\d\\d-\\d\\d-\\d\\d xxx \\d\\d:\\d\\d:\\d\\d")==false);
	}
	
	@Test
	public void variableValues() throws IOException {
		
		final String expectedValue1 = UUID.randomUUID().toString();
		final String expectedValue2 = UUID.randomUUID().toString();
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());

		script.createVariable("newVarName1", new CalculatedValue(script, expectedValue1), null);
		final CalculatedValue calc1 = new CalculatedValue(script, "$var(newVarName1)");
		assertEquals(calc1.getCalculated(), expectedValue1);
		
		script.createVariable("newVarName2", new CalculatedValue(script, expectedValue2), null);
		final CalculatedValue calc2 = new CalculatedValue(script, "$var(newVarName2)");
		assertEquals(calc2.getCalculated(), expectedValue2);

		final CalculatedValue calc3 = new CalculatedValue(script, "$var(newVarName1) -- $var(newVarName2)");
		assertEquals(calc3.getCalculated(), expectedValue1 + " -- " + expectedValue2);
	}
}
