package com.ats.tools.jenkins;

import java.net.URI;
import com.offbytwo.jenkins.JenkinsServer;

public class InstallJenkinsServer {
	private static String scriptInstall = "import jenkins.model.*\r\n" + 
			"\r\n" + 
			"def pluginParameter=\"envinject github-pullrequest maven-invoker-plugin testng-plugin unleash\"\r\n" + 
			"def plugins = pluginParameter.split()\r\n" + 
			"println(plugins)\r\n" + 
			"def instance = Jenkins.getInstance()\r\n" + 
			"def pm = instance.getPluginManager()\r\n" + 
			"def uc = instance.getUpdateCenter()\r\n" + 
			"def installed = false\r\n" + 
			"\r\n" + 
			"plugins.each {\r\n" + 
			"  if (!pm.getPlugin(it)) {\r\n" + 
			"    def plugin = uc.getPlugin(it)\r\n" + 
			"    if (plugin) {\r\n" + 
			"      println(\"Installing \" + it)\r\n" + 
			"      plugin.deploy()\r\n" + 
			"      installed = true\r\n" + 
			"    }\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"instance.save()";
	
	private static JenkinsServer jenkins;
	public static void installJenkinsServer (){	
		try {
			jenkins = new JenkinsServer(new URI("http://localhost:9090/"));
			jenkins.runScript(scriptInstall);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}