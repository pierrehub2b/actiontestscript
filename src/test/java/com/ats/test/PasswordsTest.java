package com.ats.test;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.crypto.Passwords;

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
	public void testadd() {
		assertTrue("This will succeed.", true);
	}
}