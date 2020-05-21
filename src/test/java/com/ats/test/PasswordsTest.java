package com.ats.test;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.crypto.Password;
import com.ats.crypto.Passwords;
import com.ats.executor.ActionTestScript;

public class PasswordsTest {
	
	private final static String[] names = new String[] {"passw1", "passw2", "specials", "numbers", "name1"};
	private final static String[] values = new String[] {"cryptedValue", "", "é&#')@à'", "1235456789", "very_long_password_value_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"};

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void createPasswordsFile() throws IOException {

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
}