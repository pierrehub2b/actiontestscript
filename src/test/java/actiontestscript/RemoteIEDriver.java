package actiontestscript;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteIEDriver {

	public static void main(String[] args) throws MalformedURLException {
		
		InternetExplorerOptions options = new InternetExplorerOptions();
		
		options.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, false);
		//cap.setCapability(CapabilityType.PROXY, proxy);
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
		options.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);

		RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:5555"), (MutableCapabilities)options);
		driver.get("http://google.fr");
		
	}

}
