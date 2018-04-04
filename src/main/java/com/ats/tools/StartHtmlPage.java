package com.ats.tools;

import java.io.IOException;
import java.util.Base64;

import com.google.common.io.Resources;

public final class StartHtmlPage {

	private static final String atsBrowserTitle = "ats-automation-enabled";

    public static String getAtsBrowserTitle() {
    	return atsBrowserTitle;
    }
    
    public static byte[] getAtsBrowserContent() {

    	StringBuilder htmlContent = new StringBuilder();
    	htmlContent.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>");
    	htmlContent.append(atsBrowserTitle);
    	htmlContent.append("</title></head><body bgcolor=\"#f2f2f2\"><img src=\"data:image/png;base64, ");
    	
    	try {
    		htmlContent.append(Base64.getEncoder().encodeToString(Resources.toByteArray(ResourceContent.class.getResource("/icon/ats_power.png"))));
		} catch (IOException e1) {}
    	
    	htmlContent.append("\" alt=\"ActionTestScript\" /></body></html>");
		    	
    	return htmlContent.toString().getBytes();
    }
}
