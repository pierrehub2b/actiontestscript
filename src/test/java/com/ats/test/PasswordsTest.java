package com.ats.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.crypto.Password;
import com.ats.crypto.Passwords;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;

public class PasswordsTest {
	

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/*
	 * @Before public void setUp() { }
	 * 
	 * @After public void tearDown() { }
	 */

	@Test
	public void createPasswordsFile() throws IOException {

		final String[] names = new String[] {"passw1", "passw2", "specials", "numbers", "name1"};
		final String[] values = new String[] {"cryptedValue", "", "é&#')@à'", "1235456789", "very_long_password_value_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"};

		final File folder = tempFolder.newFolder();
		final Passwords setPasswords = new Passwords(folder.toPath());
		for(int i = 0; i< names.length; i++) {
			setPasswords.setPassword(names[i], values[i]);
		}
				
		final Passwords readPasswords = new Passwords(folder);
		for(int i = 0; i< names.length; i++) {
			assertEquals(readPasswords.getPassword(names[i]), values[i]);
		}
		
		folder.deleteOnExit();
	}
	
	@Test
	public void checkPasswordValue() throws IOException {
		
		final String passwordName = "passw1";
		String passwordValue = UUID.randomUUID().toString();
		
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		final Passwords passwords = script.getPasswords();
		passwords.setPassword(passwordName, passwordValue);
		
		Password pass = new Password(script, passwordName);
		
		assertEquals(pass.getValue(), passwordValue);
		assertEquals(script.getPassword(passwordName), passwordValue);
				
		passwordValue = UUID.randomUUID().toString();
		passwords.setPassword(passwordName, passwordValue);
		
		assertEquals(pass.getValue(), passwordValue);
		assertEquals(script.getPassword(passwordName), passwordValue);
	}
	
	@Test
	public void passwordInVariable() throws IOException {
		
		final String passwordName = "passw1";
		String passwordValue = UUID.randomUUID().toString();
		
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		final Passwords passwords = script.getPasswords();
		passwords.setPassword(passwordName, passwordValue);
		
		CalculatedValue calc1 = new CalculatedValue(script, "$pass(passw1)");
		script.createVariable("var1", calc1, null);
		
		CalculatedValue calc2 = new CalculatedValue(script, "$var(var1)");
		
		assertEquals(calc2.getCalculatedText(script).get(0).getData(), passwordValue);

	}
}