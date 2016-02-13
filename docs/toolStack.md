
# Tool Stack

## IDE

[IntelliJ IDEA](https://www.jetbrains.com/idea/) is the preferred development environment.

## Core

HubTurbo is written in Java. [ControlsFX](http://fxexperience.com/controlsfx/) extends JavaFX with lots of useful components. [EGit](https://github.com/eclipse/egit-github) helps us interface with GitHub. We also make use of [PrettyTime](https://github.com/ocpsoft/prettytime/), [Guava](https://github.com/google/guava), [Gson](https://github.com/google/gson), and [log4j](http://logging.apache.org/log4j/2.x/) for essential functionality.

Platform-specific components are used as well: [Selenium](http://www.seleniumhq.org/) for instantiating and controlling a browser window, [JNA](https://github.com/twall/jna) to manage window focus on Windows, and [jkeymaster](https://github.com/tulskiy/jkeymaster) for a global hotkey.

## Testing

Unit tests are written with [JUnit](http://junit.org/), GUI tests with [TestFx](https://github.com/TestFX/TestFX). [Mockito](http://mockito.org) helps keep tests clean.

## Continuous Integration

We use [Travis CI](https://travis-ci.org/), and [Gradle](http://gradle.org/) to build the project.
[JaCoCo](https://github.com/jacoco/jacoco) computes code coverage.

### Static Analysis


We use [CheckStyle](http://checkstyle.sourceforge.net/), [FindBugs](http://findbugs.sourceforge.net/), and [PMD](https://pmd.github.io/). More details on these can be found [here](staticAnalysis.md).

## Licenses

[Our current license](https://github.com/HubTurbo/HubTurbo/blob/release/LICENSE) should be compatible with the licenses for the above dependencies, which are listed in the following table:

Library | License
----------|------------
Selenium, log4j, Gson, Guava, PrettyTime, JNA, Gradle | Apache 2.0
ControlsFX, markdown4j | 3-Clause BSD
JavaFX | Oracle BCL for Java SE
EGit, JUnit, JaCoCo | Eclipse Public License (EPL)
jkeymaster, FindBugs | LGPL 3.0
CheckStyle | LGPL 2.1
PMD | BSD-style
TestFx | EUPL 1.1
Mockito | MIT
