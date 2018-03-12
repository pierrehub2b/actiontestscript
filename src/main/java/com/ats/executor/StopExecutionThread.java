package com.ats.executor;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StopExecutionThread extends Thread{

	private InputStream inputStream;

	public StopExecutionThread(InputStream in) {
		super("ats-stop-execution");
		this.inputStream = in;
	}

	public void run() {
		Scanner scanner = new Scanner(inputStream);
		while (true) {
			try {
				String input = scanner.nextLine();
				if ("q".equals(input)) {
					break;
				}
			}catch(NoSuchElementException e) {

			}
		}
		scanner.close();
		System.exit(0);
	}
}