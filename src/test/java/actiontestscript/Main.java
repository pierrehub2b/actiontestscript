package actiontestscript;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.ats.executor.drivers.desktop.DesktopResponse;
import com.ats.executor.drivers.desktop.DesktopData;
import com.ats.executor.drivers.desktop.DesktopDriver.CommandType;
import com.ats.executor.drivers.desktop.DesktopDriver.RecordType;
import com.ats.tools.Utils;
import com.ats.tools.logger.NullExecutionLogger;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Serializer;

public class Main {

	public static void main(String[] argsx) throws ParserConfigurationException, TransformerException, InterruptedException, IOException {


		Utils.createXmlReport(Paths.get("C:\\Users\\huber\\Desktop\\cimpa"), "check", new NullExecutionLogger());

		System.exit(0);
		
		

		final CloseableHttpClient downloadClient = HttpClients.createDefault();

		/*DesktopData obj = new DesktopData();
obj.setName("oo");
obj.setValue("rr");


		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AMF3Serializer data = new AMF3Serializer(baos);
		data.writeObject(obj);*/

		final HttpPost request = new HttpPost(
				new StringBuilder("http://localhost:9988")
				.append("/")
				.append(2)
				.append("/")
				.append(2)
				.append("/")
				.append(123456)
				.toString());

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", "&é''((-èè_ç_èç"));

		request.setEntity(new StringEntity("&é'('(-'(-è(-è-èàà)\nhgjhghjsss&&&ééé\ng&&&&&\"fffgh0@@_", ContentType.create("application/x-www-form-urlencoded", Consts.UTF_8)));



		try {

			final HttpResponse response = downloadClient.execute(request);

			final AMF3Deserializer amf3 = new AMF3Deserializer(response.getEntity().getContent());
			final DesktopResponse desktopResponse = (DesktopResponse) amf3.readObject();

			amf3.close();


		} catch (IOException e) {

		}





		//Utils.createXmlReport(Paths.get("D:\\agilitestWorkspace\\ats_test\\test-output\\suite"), "inputs.HtmlInputs", new NullExecutionLogger());

		System.exit(0);
		System.out.println(System.getProperty("user.home"));

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

		//Utils.createXmlReport(Paths.get("D:\\agilitestWorkspace\\ats_test\\target\\report\\subscripts"), "subscripts.CheckoutForm");
	}
}