
# Development Environment

## Install prerequisites

- [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.8u40 or later
- [Google Chrome](http://www.google.com/chrome/)

[IntelliJ IDEA](https://www.jetbrains.com/idea/) (14 or later) is the recommended IDE.

## Check out [`master`](https://github.com/HubTurbo/HubTurbo)

Development of HubTurbo moves fairly quickly. The [`master`](https://github.com/HubTurbo/HubTurbo/tree/master) branch is generally always slightly ahead of the released version and contains features which will go into the next version, so you should check it out and build it to get started.

## Build the project

### IntelliJ IDEA

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

### Gradle

[Gradle](https://gradle.org/) is the default way to build HubTurbo.

Given a fresh clone of the repository, run

#### Windows

```bat
./gradlew.bat shadowJar
```

#### OS X/Linux

```sh
./gradlew shadowJar
```

in the root directory. This will download a local copy of Gradle, then build an executable jar file.

- On Windows, Gradle may take up significant memory while running. Close the window when the tasks are done to release it.

The executable jar will be in `build/libs/HubTurbo-x.x.x-all.jar`. Double-click that file to run it.

More details on Gradle usage [here](workflow.md).

## Start contributing

If you have not used HubTurbo before, the [User Guide](userGuide.md) details its ins and outs. Be sure to give it a quick read and try out its features first!

After that, check out the documentation on [design](design.md) for an overview of the codebase and why it's the way it is. You'll need information on day-to-day [workflow](workflow.md), as well as the [coding](codingGuidelines.md) and [testing](testing.md) guidelines, when you're ready to write some code.

To get your work integrated, check out how the [development process](process.md) works.
