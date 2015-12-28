
# Workflow

Working on HubTurbo involves a number of common tasks.

Most are accessed via Gradle, which we will assume to be the lowest common denominator in terms of ways to do things, and which should work out of the box without any additional setup. Plugins providing graphical interfaces to these tasks in IDEs are generally also available and will be listed.

## Running Tests

```sh
./gradlew clean test -i
```

The `-i` flag enables logging. This command will run all tests, [stable and unstable](testing.md#unstable-tests).

To simulate testing in the CI environment, add the environment variable `CI=true`.

```sh
CI=true ./gradlew clean test -i
```

This will run only the stable tests. To run the unstable tests separately,

```sh
./gradlew clean unstableTests
```

If you've [set up a project for your IDE](settingUpDevEnvironment.md), there are also ways to run tests with the configuration specified in the project.

## Code Coverage

```sh
./gradlew clean jacocoRootReport
```

This will run tests, then generate coverage reports in `build/reports`.

If you've already just run tests,

```sh
./gradlew jacocoRootReport
```

should be sufficient. Adding `CI=true` will generate coverage only for stable tests.

[EclEmma](http://eclemma.org/) is a plugin for Eclipse which allows tests be to selectively run, and coverage measured with more granularity than the default Gradle task.

## Style Violations and Linting

```sh
./gradlew clean check
```

This will run all static analysis tools before running tests. Violations will cause the entire build to fail, so they must be resolved. Reports will be placed in `build/reports` if this happens. Configuration for these tools can be found in `config`.

If you're not sure how to resolve a violation and wish to make an exception for it, please highlight it for discussion in your pull request.

More information on tools can be found [here](staticAnalysis.md).

## Building a JAR

```sh
./gradlew clean shadowJar
```

Eclipse and IntelliJ have built-in ways to export JARs.
