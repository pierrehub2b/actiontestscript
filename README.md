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

Tests are ready to be launched with TestNG.

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
	<proxy>system</proxy>
	<appBounding>
		<x>20</x>
		<y>20</y>
		<width>1300</width>
		<height>960</height>
	</appBounding>
	<maxTry>
		<searchElement>15</searchElement>
		<interactable>15</interactable>
	</maxTry>
	<timeOut>
		<script>60</script>
		<pageLoad>120</pageLoad>
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
</execute>
```
* You can define the proxy used by browsers during execution of the test.
* You can define initial window size of a tested application and it's initial position (appBounding).
* You can define default maxTry action in order to wait that an element exists or wait element is interactable before execution action on it.
* You can define default timeout (in sec.) for executing javascript or wait page loaded (web application).
* For each browser used you can define the time to wait after each actions in miliseconds and the path of the binaries of the browser if needed.
* You can add options to the browsers driver
* You can define application name and path of installed applications on the host machine.

## List of available ATS actions

```
*channel-start : Start a new application channel with specified name
channel-switch : *Switch to specified channel name, the channel application come in front of other channels*
channel-close : *Close specified channel name*
goto-url : *Navigate to specified url*
subscript : *Call Ats or Java subscript*
keyboard : *Enter keyboard data*
click : *Simple click on found element*
over : *Make a mouse over action on found element*
drag : *Start a drag action on a found element*
drop : *Drop action on found element*
swipe : *Execute a swipe gesture on found element*
scroll : *Execute mouse wheel action*
check-property : *Check property value of found element*
check-count : *Check number of element found with defined criterias*
check-value : *Make a comparison between two values*
property : *Catch property of found element to script variable*
window-resize : *Resize or relocate current channel application window*
window-switch : *Switch between channel opened windows (or tab)*
window-close : *Close indexed window*
select : *Select item by value, text or index on a found select element*
javascript : *Execute javascript code on found element*
comment
```

## Thirdparty components

* [Selenium](https://www.seleniumhq.org/) - The testing framework used
* [TestNG](http://testng.org/doc/) - Test runner

## Authors

* **Pierre Huber** - *Initial work* - [Pierrehub2b](https://github.com/pierrehub2b)

See also the list of [contributors](https://github.com/pierrehub2b/actiontestscript/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details