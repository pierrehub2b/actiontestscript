package com.ats.tools;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.PointerType;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public class WindowsFolderPath {

	public static final String WINDOWS_DESKTOP_FILE_NAME = "Windows.Desktop.Driver.exe";
	
	public static final String CHROME_DRIVER_FILE_NAME = "chromedriver.exe";
	public static final String MICROSOFT_WEBDRIVER_FILE_NAME = "MicrosoftWebDriver.exe";
	public static final String OPERA_WEBDRIVER_FILE_NAME = "operadriver.exe";
	
	private static final String ACTION_TEST_SCRIPT_FOLDER = "/.ActionTestScript";
	private static final String ATS_DRIVERS_FOLDER = ACTION_TEST_SCRIPT_FOLDER + "/drivers/";
	
	public static String getChromeDriverPath(){
		return getDriversFolderPath(Shell32.CSIDL_PROFILE) + CHROME_DRIVER_FILE_NAME;
	}

	public static String getWindowsDesktopDriverPath(){
		return getDriversFolderPath(Shell32.CSIDL_PROFILE) + WINDOWS_DESKTOP_FILE_NAME;
	}

	public static String getEdgeDriverPath(){
		return getDriversFolderPath(Shell32.CSIDL_PROFILE) + MICROSOFT_WEBDRIVER_FILE_NAME;
	}

	public static String getOperaDriverPath(){
		return getDriversFolderPath(Shell32.CSIDL_PROFILE) + OPERA_WEBDRIVER_FILE_NAME;
	}

	public static String getDriversFolderPath(int folderCode) {

		String path = null;

		if (com.sun.jna.Platform.isWindows()) {
			HWND hwndOwner = null;
			HANDLE hToken = null;
			int dwFlags = Shell32.SHGFP_TYPE_CURRENT;
			char[] pszPath = new char[Shell32.MAX_PATH];
			int hResult = Shell32.INSTANCE.SHGetFolderPath(hwndOwner, folderCode, hToken, dwFlags, pszPath);
			if (Shell32.S_OK == hResult) {
				path = new String(pszPath);
				int len = path.indexOf('\0');
				path = path.substring(0, len);
			} else {
				//System.err.println("Error: " + hResult);
			}
		}

		return path + ATS_DRIVERS_FOLDER;
	}

	private static Map<String, Object> OPTIONS = new HashMap<String, Object>();
	static {
		OPTIONS.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
		OPTIONS.put(Library.OPTION_FUNCTION_MAPPER,
				W32APIFunctionMapper.UNICODE);
	}

	static class HANDLE extends PointerType implements NativeMapped {
	}

	static class HWND extends HANDLE {
	}

	static interface Shell32 extends Library {

		//http://www.installmate.com/support/im9/using/symbols/functions/csidls.htm

		public static final int MAX_PATH = 260;
		public static final int CSIDL_LOCAL_APPDATA = 0x001c;
		public static final int CSIDL_PROFILE = 0x0028;
		public static final int CSIDL_WINDOWS = 0x0024;
		public static final int SHGFP_TYPE_CURRENT = 0;
		public static final int SHGFP_TYPE_DEFAULT = 1;
		public static final int S_OK = 0;

		static Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32",
				Shell32.class, OPTIONS);

		/**
		 * see http://msdn.microsoft.com/en-us/library/bb762181(VS.85).aspx
		 * 
		 * HRESULT SHGetFolderPath( HWND hwndOwner, int nFolder, HANDLE hToken,
		 * DWORD dwFlags, LPTSTR pszPath);
		 */
		public int SHGetFolderPath(HWND hwndOwner, int nFolder, HANDLE hToken,
				int dwFlags, char[] pszPath);
	}
}