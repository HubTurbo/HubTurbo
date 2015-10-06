# Development Environment

- [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.8u40 or later
- [Eclipse Luna](https://www.eclipse.org/downloads/) or later
    - For Eclipse, please install [Buildship Gradle Integration](http://marketplace.eclipse.org/content/buildship-gradle-integration)
    - (Alternative IDE) [IntelliJ IDEA 14](https://www.jetbrains.com/idea/) or later
- [Google Chrome](http://www.google.com/chrome/) 43 or later

## Tool Stack

Most of HubTurbo is written in Java. [ControlsFX](http://fxexperience.com/controlsfx/) extends JavaFX with lots of useful components. [EGit](https://github.com/eclipse/egit-github) helps us interface with GitHub. We also make use of [PrettyTime](https://github.com/ocpsoft/prettytime/), [Guava](https://github.com/google/guava), [Gson](https://github.com/google/gson), and [log4j](http://logging.apache.org/log4j/2.x/) for essential tasks, and [JUnit](http://junit.org/) and [TestFx](https://github.com/TestFX/TestFX) for testing.

We use a few platform-specific components: [Selenium](http://www.seleniumhq.org/) to instantiate and control a browser window, and [JNA](https://github.com/twall/jna) for Windows integration.

## Useful Background Knowledge

**Must have**: Basic Java programming skills, basic Git knowledge

**Good to have** (you may not need these at the beginning, but be prepared to learn these along the way):

- JavaFX
- Java threading
- Familiarity with Java 8 features: streams, lambda expressions

## Environment Setup

### Install prerequisites
Install the following (refer the previous section to find the correct version)
- JDK
- Eclipse and Buildship plugin for Eclipse <br> (alternatively, you may use IDEA)
- Chrome

### Check out current [`master`](https://github.com/HubTurbo/HubTurbo)

Development of HubTurbo moves fairly quickly. The [`master`](https://github.com/HubTurbo/HubTurbo) branch is generally always slightly ahead of the released version and contains features which will go into the next version, so you should check it out and build it to get started.

### Build the project

**Eclipse**

Import the project:

1. `File` > `Import` > `Gradle` > `Gradle Project`
2. `Next`
3. `Select root directory` > `Browse`
4. Locate the project
5. `Finish`

Run HubTurbo:

1. `HubTurbo` > `src/main/java` > `ui` > `UI.java`
2. `Right Click` > `Run As` > `1 Java Application`

**IntelliJ IDEA**

Import the project:

1. `File` > `Import project`
1. Select project directory
1. `Import project from existing model` > `Gradle`
1. `Next`
1. Choose where you want project files to be located
1. `Finish`

Run HubTurbo:

1. `Navigate` > `Class` > `UI`
1. `Run` > `Run`

**Gradle**

HubTurbo may also be built using [Gradle](https://gradle.org/). We use Gradle and [Travis](https://travis-ci.org/) for [Continuous Integration (CI)](http://www.thoughtworks.com/continuous-integration) purposes.

Given a fresh clone of the repository:

- `./gradlew.bat shadowJar` (for Windows) `./gradlew shadowJar` (for OS X/Linux) in the root directory. This will download a local copy of Gradle, then build the `jar` file.
    - On Windows Gradle may take up significant memory while running. Close the window when you are done to release it.
- The jar will then be available in `build/libs/HubTurbo-x.x.x-all.jar`. Simply double-click that file to run.

Gradle Tasks:

- `build` - Assembles and tests the project (includes building the `jar` file)
- `check` - Runs all checks, including `CheckStyle`
- `shadowJar` - Builds the full `jar` for running HubTurbo
- `test` - Runs all tests

If errors occur, trying running `clean` before the task (e.g. `./gradlew clean test`). 

## Start contributing

If you have not used HubTurbo before, the [User Guide](userGuide.md) has lots of detail on how to use HubTurbo. Be sure to give it a quick read and try out HubTrubo features first.

After that, check out the documentation on [design](design.md) and [decision rationales and guidelines](designRationalesAndGuidelines.md) 
for an overview of the codebase, and why it's the way it is. Read about the [development process](process.md) and [how we test our code](testing.md) once you are ready to contribute.

## Troubleshooting

**My IDE complains about diamond operator/lambda syntax not being supported!**

This is due to support for the required language level (8) not being present or not being correctly set. Check the project setup dialog in your IDE.

**My IDE complains about package names! It expects that the package names be prefixed with `main.java.` or something similar.**

This is likely due to the source folders not being recognized as such.

Open the project setup dialog in your IDE and register the following directories as source directories:

- `src/main/java`
- `src/main/resources`
- `src/test/java` (add this as a test directory if your IDE supports it)

**My problem isn't listed here!**

Feel free to ask for help on the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).