# ActionTestScript

ActionTestScript is a structured testing language in order to create reliable and performant tests scripts.
This tests scripts are defined by a sequence of 'actions' executed on web or desktop application.
Scripts written in ATS can be generated in java classes and then be executed using Selenium and TestNG.

## Getting Started

After installation of ATS components, you can create a new ATS project, in the **src/main/ats** folder you can create ATS scripts, using notepad, with - *.ats* - extension.

Here is a simple example of ATS script and actions you can write :

```
channel-start -> myFirstChannel -> chrome
goto-url -> google.com
keyboard -> tesla car$key(ENTER) -> INPUT [id = lst-ib]
click -> A [index(1), href =~ https://www.tesla.com.*models]
click -> A [text = USA]
property -> text => textData -> DIV [class = text-content] -> ARTICLE [class = feature&sp;sc-hero&sp;ml-overlay&sp;hero]
channel-close -> myFirstChannel
```

### Prerequisites

You have to install a standard Java 9 JDK into the folder of your choice (JRE 9 server distribution is working too).

### Installing

Download ATS components here : http://www.actiontestscript.com/ats.zip .

You can unzip archive into the folder of your choice, but if you do not install ATS on *[User-Home-Directory]/.actiontestscript* folder, you have to create an environment variable named **ATS_HOME** and set it's value to your ATS installation folder.


## Execute project

You can generate and compile ATS project using Maven and execute - *compile* - goal, it will generate java files from ats files into the *target/generated* folder of the project, after that it will compile generated classes into the 'classes' folder.
After generate and compile the project you can use TestNG suite xml files to define your testing campaigns.

## Thirdparty components

* [Selenium](https://www.seleniumhq.org/) - The testing framework used
* [TestNG](http://testng.org/doc/) - Test runner

## Authors

* **Pierre Huber** - *Initial work* - [Pierrehub2b](https://github.com/pierrehub2b)

See also the list of [contributors](https://github.com/pierrehub2b/actiontestscript/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License 2.0 License - see the [LICENSE.md](LICENSE.md) file for details

