# ActionTestScript

ActionTestScript is a structured testing language in order to create reliable and performant tests scripts.
This tests scripts are defined by a sequence of 'actions' executed on web or desktop application.
Scripts written in ATS can be generated in java classes and then be executed using Selenium and TestNG.

## Getting Started

After installation of ATS components, you can create a new ATS project, in the **src/main/ats** folder you can create ATS scripts, using notepad, with - *.ats* - extension.

Here is a simple example of ATS script :

```
channel-start -> myFirstChannel -> chrome
goto-url -> google.com
keyboard -> automated testing$key(ENTER) -> INPUT [id = lst-ib]
click -> A [id = , text = Test&sp;automation&sp;-&sp;Wikipedia]
scroll -> 300
channel-close -> myFirstChannel
```

### Prerequisites

You have to install a standard Java 9 JDK into the folder of your choice (JRE 9 server distribution is working too).
If you want to execute and compile ATS project wit Maven, you have to install it too.

### Installing

Download ATS components here : http://www.actiontestscript.com/ats.zip .

You can unzip archive into the folder of your choice, but if you do not install ATS on *[User-Home-Directory]/.actiontestscript* folder, you have to create an environment variable named **ATS_HOME** and set it's value to your ATS installation folder.

## Create ATS project

Download ATS simple project here : http://www.actiontestscript.com/ats-project.zip

Unzip this folder and now you can edit *.atsProjectProperties* and *pom.xml* files according to your needs. 

There is a simple ATS file in the zipped folder, you can edit this script with notpad or any ATS Editor application ...


## Execute project

Generate and compile ATS project using Maven and execute - *compile* - goal.
* Java files generated from ats files will be created into the *target/generated* folder of the project.
* Compiled classes will be created classes into the *target/classes* folder of the project.

```
cd [path-to-your-ats-project]
mvn compile
```

You can now use TestNG suite xml files to define your testing campaigns, please visit [TestNG](http://testng.org/doc/) to see how use TestNG with your favorite Java IDE or to create test suite executions.

## Thirdparty components

* [Selenium](https://www.seleniumhq.org/) - The testing framework used
* [TestNG](http://testng.org/doc/) - Test runner

## Authors

* **Pierre Huber** - *Initial work* - [Pierrehub2b](https://github.com/pierrehub2b)

See also the list of [contributors](https://github.com/pierrehub2b/actiontestscript/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License 2.0 License - see the [LICENSE.md](LICENSE.md) file for details

