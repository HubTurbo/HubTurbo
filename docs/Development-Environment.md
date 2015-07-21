# Development Environment

## Requirements

- [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.8u40 or later
- [Eclipse Luna](https://www.eclipse.org/downloads/) or later
    - For Eclipse, please install [Buildship Gradle Integration](http://marketplace.eclipse.org/content/buildship-gradle-integration)
    - (Alternative IDE) [IntelliJ IDEA 14](https://www.jetbrains.com/idea/) or later
- [Google Chrome](http://www.google.com/chrome/) 43 or later

## Environment Setup

### Check out current `[master](https://github.com/HubTurbo/HubTurbo)`

Development of HubTurbo moves fairly quickly. The `[master](https://github.com/HubTurbo/HubTurbo)` branch is generally always slightly ahead of the released version and contains features which will go into the next version, so you should check it out and build it to get started.

### Eclipse

Please install [Buildship Gradle Integration](http://marketplace.eclipse.org/content/buildship-gradle-integration) first. 

After cloning the project,

- `File` > `Import` > `Gradle` > `Gradle Project`
- `Next`
- `Select root directory` > `Browse`
- Locate the project
- `Finish`

**Afterwards**

Navigate to the main class
- `HubTurbo` > `src/main/java` > `ui` > `UI.java`
- `Right Click` > `Run As` > `1 Java Application`

### IntelliJ IDEA

**Importing Gradle setup**

- `File` > `Import project`
- Select project directory
- `Import project from existing model` > `Gradle`
- `Next`
- Choose where you want project files to be located
- `Finish`

**Afterwards**

- `Navigate` > `Class` > `UI`
- `Run` > `Run`

## Gradle

HubTurbo may also be built using [Gradle](https://gradle.org/). We use Gradle and [Travis](https://travis-ci.org/) for [Continuous Integration (CI)](http://www.thoughtworks.com/continuous-integration) purposes.

Given a fresh clone of the repository:

- `./gradlew.bat shadowJar` (for Windows) `./gradlew shadowJar` (for OS X/Linux) in the root directory. This will download a local copy of Gradle, then build the `jar` file.
    - On Windows Gradle may take up significant memory while running. Close the window when you are done to release it.
- The jar will then be available in `build/libs/HubTurbo-x.x.x-all.jar`. Simply double-click that file to run.

### Gradle Tasks

- `build` - Assembles and tests the project (includes building the `jar` file)
- `check` - Runs all checks, including `CheckStyle`
- `shadowJar` - Builds the full `jar` for running HubTurbo
- `test` - Runs all tests

If errors occur, trying running `clean` before the task (e.g. `./gradlew clean test`). 

## What now?

The [User Guide](Getting-Started.md) has lots of detail on how to use HubTurbo. Be sure to give it a quick read!

After that, check out the documentation on [architecture](Architecture.md) and [design](Design-Decisions-and-Guidelines.md) for an overview of the codebase, and why it's the way it is. Read about the [workflow](Workflow.md) once you are ready to contribute.

## Troubleshooting

**My IDE complains about diamond operator/lambda syntax not being supported!**

This is due to support for the required language level (8) not being present or not being correctly set. Check the project setup dialog in your IDE.

**My IDE complains about package names! It expects that the package names be prefixed with `main.java.` or something similar.**

This is likely due to the source folders not being recognised as such.

Open the project setup dialog in your IDE and register the following directories as source directories:

- `src/main/java`
- `src/main/resources`
- `src/test/java` (add this as a test directory if your IDE supports it)

**My problem isn't listed here!**

Feel free to ask for help on the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).