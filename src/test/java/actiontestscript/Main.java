package actiontestscript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.ats.tools.Utils;

public class Main {

	public static void main(String[] argsx) throws ParserConfigurationException, TransformerException, InterruptedException, IOException {

		/*try {
			//Utils.loadCsvData("file:///D:\\agilitestWorkspace\\ats_test\\src\\assets\\data\\FichierCSVtest.csv");
			Utils.loadCsvData("https://www.caipture.com/demo/browsers.csv");
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		//Utils.loadCsvData("https://www.caipture.com/demo/browsers.csv");
		
		System.exit(0);
		
		
		System.setProperty("webdriver.ie.driver", "C:\\Users\\huber\\.actiontestscript\\drivers\\IEDriverServer.exe");
		DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
		ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		RemoteWebDriver ieDriver = new InternetExplorerDriver(new InternetExplorerOptions(ieCapabilities));
		
		
		ieDriver.get("http://www.google.com");
				
		
		System.exit(0);
			
			
		EdgeOptions options = new EdgeOptions();
		options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
		options.setPageLoadStrategy("eager");
		
		RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:17556"), options);
		driver.get("https://www.w3schools.com/html/tryit.asp?filename=tryhtml_iframe");
		
		RemoteWebElement iframe = (RemoteWebElement) driver.findElementByName("iframeResult");
		
		String elementId = iframe.getId();
		
		driver.switchTo().defaultContent();
		
		RemoteWebElement rwe = new RemoteWebElement();
		rwe.setId(elementId);
		rwe.setParent(driver);
		
		String src = rwe.getAttribute("id");
		
		Object verif = driver.executeAsyncScript(ResourceContent.getElementAttributesJavaScript() + ";arguments[arguments.length-1](result);", rwe);
		
		
		driver.switchTo().frame(rwe);
		
		
		System.exit(0);*/
		
		String[] commands = new String[] {"explorer", "shell:appsFolder\\Microsoft.BingWeather_8wekyb3d8bbwe!App"};
		
		
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(commands);

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		System.out.println("Here is the standard output of the command:\n");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
		    System.out.println(s);
		}

		// read any errors from the attempted command
		System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
		    System.out.println(s);
		}

		Utils.createXmlReport(Paths.get("D:\\agilitestWorkspace\\ats_test\\target\\report\\subscripts"), "subscripts.CheckoutForm");
	}
}