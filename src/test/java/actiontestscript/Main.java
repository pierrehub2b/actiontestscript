package actiontestscript;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Main {

	public static void main(String[] argsx) throws FileNotFoundException, ParserConfigurationException, TransformerException, InterruptedException, MalformedURLException {


		//System.setProperty("webdriver.gecko.driver", "C:\\Users\\huber\\.actiontestscript\\drivers\\geckodriver.exe"); 
				
		/*FirefoxOptions options = new FirefoxOptions();
		options.setCapability("marionette", false);
		options.setCapability("acceptSslCerts ","true");
		options.setCapability("acceptInsecureCerts ","true");
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);*/
		
		
		DesiredCapabilities caps = DesiredCapabilities.chrome(); 
		
		RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:9515"), caps);
		driver.get("http://google.com");
		String verif = (String) driver.executeAsyncScript("var result=arguments[0],index=arguments[1];var callbackResult=arguments[arguments.length-1];callbackResult(result);", "ok", 0);
		System.out.println(verif);
		
		
		/*Actions actions = new Actions(driver);

		driver.get("https://www.orange.fr/portail");
		driver.findElement(By.xpath("//a[@id='o-cookie-consent-ok']")).click();
		driver.findElement(By.xpath("//a[@data-rid='content,operateur,t_2_b_a']")).click();
		driver.findElement(By.xpath("//a[@title='Internet']")).click();
		driver.findElement(By.xpath("//a[@title='Mobile']")).click();
		driver.findElement(By.xpath("//a[@data-dcs-tab='Internet + Mobile']")).click();
		driver.findElement(By.xpath("//a[@title=\"TV d'Orange\"]")).click();

		driver.findElement(By.xpath("//a[@title=\"TV d'Orange\"]")).click();
		driver.findElement(By.xpath("//a[@title='Fixe']")).click();

		
		WebElement elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Accessoires']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Boutiques']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		

		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Accessoires']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Boutiques']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Accessoires']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Boutiques']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Accessoires']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Boutiques']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Accessoires']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Boutiques']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Accessoires']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();
		
		elem = driver.findElement(By.xpath("//a[@data-dcs-tab='Boutiques']"));
		actions.moveToElement(elem, 0, 0).perform();
		elem.click();*/

		driver.close();




		/*click [nofail] -> A [id = o-cookie-consent-ok]
		//click -> IMG [class = ico-spec dark] -> DIV [class = t t-w17 t-h50 pull-left topFixed]
		click -> A [title = Internet]
		click -> A [data-dcs-tab = Mobile, title = Mobile]
		click -> A [class = tms-tag dcs-tag black-txt-color normal-txt-weight, data-dcs-tab = Internet + Mobile]
		click -> A [class = tms-tag dcs-tag black-txt-color normal-txt-weight, title = TV d'Orange]
		click -> A [class = tms-tag dcs-tag black-txt-color normal-txt-weight, title = Fixe]
		click -> A [class = tms-tag dcs-tag black-txt-color normal-txt-weight, title = Accessoires]
		click -> A [class = tms-tag dcs-tag black-txt-color normal-txt-weight, title = Boutiques]*/













	}

}
