package com.ats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.EnvironmentValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.transform.NumericTransformer;
import com.ats.generator.variables.transform.RegexpTransformer;

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
	public void transformValues() throws IOException {

		final String[] values = new String[] {"0.000001", "9.99999", "9.000001", "-99999.99999", "99", "0", "", "x", "999999999999999"};
		final String[] rounded = new String[] {"0.000", "10.000", "9.000", "-100000.000", "99.000", "0.000", "0", "NaN", "999999999999999.000"};

		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());

		for(int i = 0; i< values.length; i++) {
			Variable v = script.createVariable("transformedVariable" + i, new CalculatedValue(script, values[i]), new NumericTransformer(3));
			assertEquals(v.getCalculatedValue(), rounded[i]);
		}
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

	@Test
	public void multipleValues() throws IOException {

		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		final String value1 = "value1";
		final String value2 = "value2";

		CalculatedValue calc1 = new CalculatedValue(script, value1);
		script.createVariable("var1", calc1, null);
		
		CalculatedValue calc2 = new CalculatedValue(script, value2);
		script.createVariable("var2", calc2, null);
		
		CalculatedValue calc3 = new CalculatedValue(script, "$var(var1)$var(var2)");
		assertEquals(calc3.getCalculated(), value1 + value2);

		CalculatedValue calc4 = new CalculatedValue(script, "-- $var(var1) -- $var(var2) --");
		assertEquals(calc4.getCalculated(), "-- " + value1 + " -- " + value2 + " --");
	}
	
	@Test
	public void valuesInceptions() throws IOException {

		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		final String value1 = "value1";
		final String value2 = "value2";

		CalculatedValue calc1 = new CalculatedValue(script, value1);
		script.createVariable("var1", calc1, null);
		
		CalculatedValue calc2 = new CalculatedValue(script, value2);
		script.createVariable("var2", calc2, null);
		
		script.createVariable("var3", new CalculatedValue(script, "$var(var1)$var(var2)"), null);
		
		Variable v4 = script.createVariable("var4", new CalculatedValue(script, "$var(var1)$var(var2)$var(var3)"), null);

		assertEquals(v4.getCalculatedValue(), value1 + value2 + value1 + value2);

		Variable v5 = script.createVariable("var5", new CalculatedValue(script, "$var(var3)$var(var4)"), null);
		assertEquals(v5.getCalculatedValue(), value1 + value2 + value1 + value2 + value1 + value2);
	}
	
	@Test
	public void regexpTransformation() throws IOException {

		final String[] values = new String[] {"valuexxx56endvalue", "xxxsearched:value-end"};
		final String[] regexps = new String[] {".*(\\d\\d).*", ".*searched:(.*)-end"};
		final String[] asserts = new String[] {"56", "value"};
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(int i = 0 ; i < values.length; i++) {
			final CalculatedValue calc = new CalculatedValue(script, values[i]);
			final Variable var = script.createVariable("var" + i, calc, new RegexpTransformer(regexps[i], 1));
			assertEquals(var.getCalculatedValue(), asserts[i]);
		}
	}
	
	@Test
	public void envGroupExtract() throws IOException {
		
		Matcher mv = CalculatedValue.ENV_PATTERN.matcher("$env(br.alpha, ok)");
		mv.find();
		EnvironmentValue sp = new EnvironmentValue(mv);
		
		assertEquals(sp.getCode(), "(\"br.alpha\", \"ok\")");
		
		mv = CalculatedValue.ENV_PATTERN.matcher("$env(br, ok)");
		mv.find();
		sp = new EnvironmentValue(mv);
		
		assertEquals(sp.getCode(), "(\"br\", \"ok\")");
	}
	
	@Test
	public void rndTestLength() throws IOException {
		
		final String[] codes = new String[] {"$rnd(10)", "$rnd(100)", "$rnd(15,upp)", "$rnd(8,low)", "$rnd(6,num)", "$rnd(28,num)", "$rnd(12,abcdef)"};
		final String[] matches = new String[] {"[a-zA-Z]+", "[a-zA-Z]+", "[A-Z]+", "[a-z]+", "[0-9]+", "[0-9]+", "[a-f]+"};
		final int[] lengths = new int[] {10, 100, 15, 8, 6, 28, 12};
		
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(int i = 0 ; i < codes.length; i++) {
			final Variable rnd = script.createVariable("rnd"+i, new CalculatedValue(script, codes[i]), null);
			final String result = rnd.getCalculatedValue();
			assertTrue(result.length() == lengths[i] && result.matches(matches[i]));
		}
	}
}
