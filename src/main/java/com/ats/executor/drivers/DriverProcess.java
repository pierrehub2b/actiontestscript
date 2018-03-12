package com.ats.executor.drivers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;

public class DriverProcess {

	private int port = 4444;
	private Process process;
	private DriverManager manager;

	public DriverProcess(DriverManager manager, Path driverFolderPath, String driverFileName) {

		this.manager = manager;
		
		File driverFile = driverFolderPath.resolve(driverFileName).toFile();

		if(driverFile.exists()){

			port = findFreePort();

			ProcessBuilder builder = new ProcessBuilder(driverFile.getAbsolutePath(), "--port=" + port);
            builder.redirectErrorStream(true);
            
            try {
				process = builder.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            
			int maxTry = 50;
			while(maxTry > 0 && waitServerStarted(port)) {
				maxTry--;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}
			}
		}
	}

	private boolean waitServerStarted(int port) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("localhost", port), 100);
			socket.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public int getPort() {
		return port;
	}

	public URL getDriverServerUrl(){
		try {
			return new URL("http://localhost:" + port);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	private static Integer findFreePort() {
		try (ServerSocket socket = new ServerSocket(0);) {
			return socket.getLocalPort();
		} catch (IOException e) {
			return 2106;
		}
	}

	//--------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------

	public void close(){
		if(process != null){

			process.descendants().forEach(p -> p.destroy());
			process.destroy();

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			process = null;
		}
		
		manager.processTerminated(this);
	}
}