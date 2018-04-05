# ActionTestScript

ActionTestScript is a structured and readable testing language used to create reliable and performant GUI automated tests campaigns.
Tests scripts are defined by a sequence of 'actions' executed on web or desktop application.
Scripts written in ATS are converted into java classes and then executed using Selenium and TestNG frameworks.

Only following browsers are available for the moment with ATS : *Chrome, Edge, Firefox and Opera*.

With ATS, tests designers are only focused on the functional actions to do and don't have to worried about technical considerations.

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

### Prerequisites

You have to install a standard Java 9 JDK into the folder of your choice (JRE 9 server distribution is working too).
If you want to execute and compile ATS project wit Apache Maven, you have to install it too.

### Installing

Download ATS components : http://www.actiontestscript.com/ats.zip .

You can unzip archive into the folder of your choice, but if you do not install ATS on *[User-Home-Directory]/.actiontestscript* folder, you have to create an environment variable named **ATS_HOME** and set it's value to your ATS installation folder.

After installation of ATS components you can create ATS projects with the following folder structure.

```
Project
	/libs/
	/src/
		/assets/
			/data/
			/resouces/
		/exec/
		/main/
			/ats/
				/subscripts/
			/java/
	/target/
	/*.atsProjectProperties*
	/*pom.xml*
```

In the **src/main/ats** folder you can create ATS scripts using notepad with - *.ats* - extension.

## Create ATS project

Download ATS simple project : http://www.actiontestscript.com/ats-project.zip

Unzip this folder and now you can edit *.atsProjectProperties* and *pom.xml* files according to your needs. 

There is a simple ATS file in the src/main/ats folder, you can edit this script with notpad or any ATS Editor application ...


## Execute project

Generate and compile ATS project using Maven and execute - *compile* - goal.
* Java files generated from ats scripts will be created into the *target/generated* folder of the project.
* Compiled classes will be created classes into the *target/classes* folder of the project.

```
cd [path-to-your-ats-project]
mvn compile
```

You can now use TestNG suite files to define your testing campaigns, please visit [TestNG](http://testng.org/doc/) to see how use TestNG with your favorite Java IDE or to create test suite executions.

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

## Thirdparty components

* [Selenium](https://www.seleniumhq.org/) - The testing framework used
* [TestNG](http://testng.org/doc/) - Test runner

## Authors

* **Pierre Huber** - *Initial work* - [Pierrehub2b](https://github.com/pierrehub2b)

See also the list of [contributors](https://github.com/pierrehub2b/actiontestscript/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details