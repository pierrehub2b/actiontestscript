# ActionTestScript

ActionTestScript is a structured and readable testing language used to create reliable and performant GUI automated tests campaigns.
Tests scripts are defined by a sequence of 'actions' executed on web or desktop application.
Scripts written in ATS are converted into java classes and then executed using Selenium and TestNG frameworks.

Only following browsers are available for the moment with ATS : *Chrome, Edge, Firefox and Opera*.

With ATS, tests designers are only focused on the functional actions to do and don't have to worry about technical considerations.

## Getting Started

Here is a simple example of ATS script :

```
channel-start -> myFirstChannel -> chrome
goto-url -> google.com
keyboard -> automated testing$key(ENTER) -> INPUT [id = lst-ib]
click -> A [text = Test automation - Wikipedia]
scroll -> 300
scroll -> 0 -> A [font-style = italic, text = Graphical user interface testing]
over -> IMG [src =~ .*Test_Automation_Interface.png]
channel-close -> myFirstChannel
```

This script start Chrome browser, navigate to url 'google.com', enter 'automated test' string into input text field and hit 'enter' key.
The script click on the wikipedia link to open the page and do some mouse over and scroll actions.
At the end of the script the started channel called 'myFirstChannel' is closed and terminate the browser process.

Pretty simple no ?

### Prerequisites

* You have to install a standard Java 9 JDK into the folder of your choice (JRE 9 server distribution is working too).
* Apache Maven or Ant to generate and compile ATS project.
* Install TestNG plugin on your favorite java IDE (for Eclipse : http://testng.org/doc/eclipse.html).
* Read TestNG documentation about suite management.


### Installing

Download ATS components : http://www.actiontestscript.com/ats.zip .

You can unzip archive into the folder of your choice, but if you do not install ATS on *[User-Home-Directory]/.actiontestscript* folder, you have to create an environment variable named **ATS_HOME** and set it's value to your ATS installation folder.

After installation of ATS components you can create ATS projects with the following folder structure.

```
ATS Project folder
	/libs/
	/src/
		/assets/
			/data/
			/lang/
			/resources/
		/exec/
		/main/
			/ats/
				/subscripts/
			/java/
	/target/
	/.atsProjectProperties
	/pom.xml
```

In the **src/main/ats** folder you can create ATS scripts using notepad with - *.ats* - extension.

## Sample ATS project

You can download an ATS test project : https://github.com/pierrehub2b/ats_test/archive/master.zip

Unzip this folder and now you can edit *.atsProjectProperties* and *pom.xml* files according to your needs. 

You can launch test of the project using java command line, Ant target or Maven goal.


## Execute project

Generate and compile ATS project using Maven and execute - *compile* - goal.
* Java files generated from ats scripts will be created into the *target/generated* folder of the project.
* Compiled classes will be created classes into the *target/classes* folder of the project.

```
cd [path-to-your-ats-project]
mvn compile
```

You can now use TestNG suite files to define your testing campaigns, please visit [TestNG](http://testng.org/doc/) to see how use TestNG with your favorite Java IDE or to create test suite executions.

**Or**

You can generate and compile project using command line with java (see 'Installing' chapter for ATS distribution path)

* Generate java files

```
cd [path-to-your-ats-project]
java -cp [ats-distribution-path]\libs/*;libs/* com.ats.generator.Generator
```
Java files will be generated into project folder *target/generated* from scripts found in *src/main/ats* folder. 
Java files in *src/main/java* folder will be copied into the *target/generated* folder.

* Generate and compile project (for this command using JRE is not enough, you need to use a JDK to compile classes)

```
cd [path-to-your-ats-project]
java -cp [ats-distribution-path]\libs/*;libs/* com.ats.generator.Generator -comp
```
Generated and copied classes from *target/generated* folder will be compiled into *target/classes* folder and all files.
Contents of folder *src/assets/* will be copied into the *target/classes* folder.

Tests are ready to be launched with TestNG but you have to first create and define a TestNG suite xml file, in this file you can define groups, package or scripts you want to include or exclude from execution.
Here the command line to launch tests defined in 'suite.xml' file :

```
cd [path-to-your-ats-project]
java -cp [ats-distribution-path]\libs/*;libs/*;target/classes org.testng.TestNG src/exec/suite.xml
```

**Or**

You can generate, compile and execute project using command line with Java and Maven (see 'Installing' chapter for ATS distribution path)

* Execute Maven

```
cd [path-to-your-ats-project]
mvn clean test
```

The default TestNG suite in the pom.xml file will be executed, this suite is define with *'maven-surefire-plugin'* and *'suiteXmlFile'* property.

## Customize ATS on host machine

Each machine running ATS scripts has it's own performance or available resources to execute automated tests.
You can change configuration on the installed ATS distribution like wait action by browsers or tested application installation path.

Here is an example of global ATS configuration file (*.atsProperties* file in ATS root install folder)

```
<?xml version="1.0" encoding="UTF-8"?>
<execute>
	<proxy>
		<type>system</type>
	</proxy>
	<appBounding>
		<x>20</x>
		<y>20</y>
		<width>1300</width>
		<height>960</height>
	</appBounding>
	<maxTry>
		<searchElement>15</searchElement>
		<getProperty>10</getProperty>
		<webService>1</webService>		
	</maxTry>
	<timeOut>
		<script>60</script>
		<pageLoad>120</pageLoad>
		<watchDog>180</watchDog>
		<webService>20</webService>
	</timeOut>
	<browsers>
		<browser>
			<name>chrome</name>
			<options>
				<option>--disable-infobars</option>
				<option>--disable-web-security</option>
			</options>
		</browser>
		<browser>
			<name>opera</name>
			<path>C:\Program Files\Opera\52.0.2871.40\opera.exe</path>
			<waitAction>150</waitAction>
		</browser>
		<browser>
			<name>firefox</name>
			<waitAction>200</waitAction>
			<waitProperty>70</waitProperty>
		</browser>
	</browsers>
	<applications>
		<application>
			<name>notepad++</name>
			<path>C:\Program Files (x86)\Notepad++\notepad++.exe</path>
		</application>
		<application>
			<name>filezilla</name>
			<path>C:\Program Files\FileZilla FTP Client\filezilla.exe</path>
		</application>
	</applications>
	<mobiles>
		<mobile>
			<name>settings</name>
			<endpoint>192.168.0.6:8080</endpoint>
			<package>settings</package>
			<waitAction>100</waitAction>
		</mobile>
	</mobiles>
	<apis>
		<api>
			<name>fakerest</name>
			<url>https://fakerestapi.azurewebsites.net</url>
		</api>
	</apis>
</execute>
```
* You can define the proxy used by browsers during execution of the test.
Proxy types available are *'system'*, *'auto'*, *'direct'* and *'manual'*, if *'manual'* type is selected you have to define host and port of the proxy :
```
<proxy>
	<type>manual</type>
	<host>proxyhost</host>
	<port>8080</port>
</proxy>
```
* **[appBounding]** : initial window size of a tested application and it's initial position
* **[maxTry -> searchElement]** : action's maxTry to wait element exists or wait element is interactable before execute
* **[maxTry -> getProperty]** : action's maxTry get property to wait element's property exists and property is not null
* **[maxTry -> webService]** : api REST or SOAP webservice max try if request fail with timeout
* **[timeOut -> script]** : javascript execution timeout (in sec.) for web application only
* **[timeOut -> pageLoad]** : page load timeout (in sec.) for web application only
* **[timeOut -> watchDog]** : action's execution timeout (in sec.) to prevent browser or application infinite hangup
* **[timeOut -> webService]** : api REST or SOAP webservice requests execution timeout (in sec.)
* For each browser used you can define following parameters :
  - **[path]** : the application binary file path
  - **[waitAction]** : wait after each actions (in millisec.)
  - **[waitProperty]** : wait for double check attributes value (in millisec.) 
* You can add options to the browsers driver
* You can define application name and path of installed applications on the host machine
* You can define a chromium like browser in the available browsers list. In order to use a chromium like browser you have to define *'name'*, *'driver'* and *'path'* attributes of the browser element. The *'driver'* attribute is the driver file *(without extension)* in the ATS drivers folder and the *'path'* attribute is the executable file of the chromium browser :
```
<browser>
	<name>chromium</name>
	<driver>chromiumdriver</driver>
	<path>C:\Program\chromium\chrome.exe</path>
</browser>
```
* If you want to use JxBrowser based application, you have to define *'name'*, *'driver'* and *'path'* attributes of the browser element. The *'driver'* attribute is the driver file *(without extension)* in the ATS drivers folder and the *'path'* attribute is the executable file of the JxBrowser start script, the remote debug port used by default is 9222 (you have to enable debug mode to the JxBrowser based application).
You can find all chromium driver versions here : https://sites.google.com/a/chromium.org/chromedriver, you have to choose the version corresponding to your version of chromium engine in your JxBrowser based application:
```
<browser>
	<name>jx</name>
	<driver>jxbrowser</driver>
	<path>C:\Program\path_to_jxbrowser_based_application\app.exe</path>
</browser>
```
*Chromium binaries files and Chromium driver for Windows can be found here : http://commondatastorage.googleapis.com/chromium-browser-snapshots/index.html?prefix=Win_x64/650848/*
* ATS will automatically be updated with the last version of the supported browsers, in order to use another browser driver version you can define an executable driver name in the *'driver'* property of a named browser. If you want to use the version 73 of the chromedriver you have to download the right version of the driver, rename it, and copy it into the *'drivers'* folder :
```
<browser>
	<name>chrome</name>
	<driver>chromedriver_73</driver>
</browser>
```
* You can define a Neoload configuration to record and design load testing project with Neoload. ATS Neoload actions will enable Neoload UserPath recording and send commands to execute some design actions in a Neoload project. In order to enable Neoload recording you have to define Neoload proxy and service parameters :
```
<neoload>
	<recorder>
		<host>192.168.0.100</host>
		<port>8090</port>
	</recorder>
	<design>
		<api>Design/v1/Service.svc</api>
		<port>7400</port>
	</design>
</neoload>
```
## List of available ATS actions

```
channel-start : Start a new application channel with specified name
channel-switch : Switch to specified channel name (send application window to foreground)
channel-close : Close specified channel name
goto-url : Navigate to specified url
subscript : Call Ats or Java subscript
keyboard : Enter keyboard action
click : Simple click on found element
over : Make mouse over action on found element
drag : Mouse start drag action on found element
drop : Mouse drop action on found element
swipe : Execute mouse swipe gesture on found element
scroll : Execute mouse wheel action
check-property : Check property value of found element
check-count : Check number of element found with defined criterias
check-value : Make comparison between two values
property : Catch property value of found element into variable
window-resize : Resize or relocate current channel application window
window-switch : Switch between windows (or tabs) of current channel
window-close : Close indexed window
select : Select item by value, text or index of found select element
javascript : Execute javascript code on found element
comment : Add comment to script
```

## Thirdparty components

* [Neoload](https://www.neotys.com/) - Load testing platform
* [Selenium](https://www.seleniumhq.org/) - The testing framework used
* [TestNG](http://testng.org/doc/) - Test runner

## Authors

* **Pierre Huber** - *Initial work* - [Pierrehub2b](https://github.com/pierrehub2b)

See also the list of [contributors](https://github.com/pierrehub2b/actiontestscript/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details