# Testing

HubTurbo uses both GUI tests and unit tests to prevent regressions.

They are separated into two packages: [`guitests`](../src/test/java/guitests) and [`tests`](../src/test/java/tests), respectively.

Currently all tests are run sequentially, with a new `JVM` started for each new test class (mainly because you can't run more than one GUI test at once).

## Unstable Tests

There is a third test package for *unstable* tests: those which fail intermittently when run on Travis. These must be run locally by developers when working on new features.

Tests which are not in the `unstable` package are considered *stable*, i.e. they fail infrequently enough that re-running the CI build one or twice will get everything to pass.

As far as possible we try not to add new tests to this package, and work towards moving tests out of it. Discuss new candidates for the `unstable` package in your PRs.

## GUI Tests

For GUI Tests, HubTurbo supports a few launch arguments/flags to aid in testing.

E.g. `--test=true --bypasslogin=true --closeonquit=true` (in program arguments) which simulates a test mode.
- `test` - used to enable test mode which by default doesn't read/write to any external `json` file (everything is kept in memory) and uses stubs for certain components like `BrowserComponent`
- `testconfig` - used to enable reading/writing of the `settings/test.json` file during test mode (for user preferences)
- `bypasslogin` - used to bypass the login dialog (username and password will be left empty)
- `testjson` - used to enable reading/writing to the repository `json` files during test mode
- `testchromedriver` - used to test `BrowserComponent` and `ChromeDriverEx`
- `closeonquit` - used for test mode to shutdown `JVM` on quit (mostly for manual testing, not used for ci/tests because that will cause tests to fail)

Most GUI Tests extend [`UITest`](../src/test/java/guitests/UITest.java) which in turn extends `GuiTest` which is part of `TestFX`.
- Override `launchApp` to define your own program arguments for the test
- Override `setupMethod` to add any pre-test configuration/setup before the stage is launched

**Interacting with the `UI`**

The [`TestController`](../src/main/java/ui/TestController.java) class allows tests to call the `UI` instance through `TestController.getUI()`.

**Interacting with the `Stage`**

Subclasses of the JavaFX [`Node`](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html) can be referenced via their id (similar to a HTML id attribute). A `Node`'s id is set using `setId("<id>")`, and it can then be selected using `find("#<id>")`, and interacted with using `click("#<id>")`.

Follow the conventions for existing ids when assigning ids to new elements.

**Concurrency**

Any code that modifies GUI objects from a thread other than the JavaFX Application Thread (aka the UI thread) must be wrapped in `Platform.runLater` (see guidelines on [thread safety](codingGuidelines.md#thread-safety), specifically the part on thread confinement).

Note that `Platform.runLater` does not provide any guarantees as to *when* your code actually executes. If it is important that your threads are synchronised, try the following:

- If another thread is doing something on the UI thread, then waiting until it is done, `PlatformEx.runAndWait` is the solution. There are a few other flavours of it in [`PlatformEx`](https://github.com/HubTurbo/HubTurbo/blob/master/src/main/java/util/PlatformEx.java).
- [`UITest`](https://github.com/HubTurbo/HubTurbo/blob/master/src/test/java/guitests/UITest.java) contains many useful synchronisation methods, such as `waitUntilNodeAppears` and `awaitCondition`. These are generally what is required.
- Otherwise, use one of the synchronisation aids in [java.util.concurrent](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/package-summary.html), possibly to implement your own abstraction.
- If you absolutely have to [busy-wait](https://en.wikipedia.org/wiki/Busy_waiting) (for example, to implement an abstraction), `PlatformEx.waitOnFxThread` helps throttle the rate at which the UI thread must be queried. It is for implementing lower-level operations and generally shouldn't be used directly in tests.

*Do not use `Thread.sleep` to sequence the execution of threads with respect to each other.* It is unreliable due to the semantics of `Platform.runLater` and can result in [slow, brittle, and/or nondeterministic code](http://googletesting.blogspot.sg/2008/08/tott-sleeping-synchronization.html) as tests get more complex.

In general, ensure as far as possible that tests are deterministic. Keep tests stateless and make all their inputs (such as global state) explicit.

**Events**

To test for events, you can create new events for the test in [`util.events.testevents`](../src/main/java/util/events/testevents), ensure that you also create the corresponding event handler. You can then test for event triggering using [`UI.events.registerEvent((EventHandler))`](../src/main/java/ui/UI.java).

## Headless Mode

Unlike running in GUI tests in headful mode (GUI components are spawned and cursor moves by itself), in headless mode, you will not see any GUI components being initialized on your screen (for Mac users, the focus may constantly shifts to HubTurbo icon). To run in headless mode:

### Mac & Linux
```sh
CI=true && ./gradlew clean test -i
```

### Windows
```sh
set CI=true && ./gradlew clean test -i
```

## CI Quirks

HubTurbo runs GUI tests on Travis as well. As its testing environment may differ from your development environment, tests which pass on your end may fail on Travis. Do troubleshoot with the following points, and feel free to add on to the list if you encounter problems.

- In order to interact with a text field within a pop-up/dialog window on the CI, the main stage must be hidden first. See [`LabelPickerTests`](../src/test/java/guitests/LabelPickerTests.java) for an example of this.
- Builds may fail with JavaFX exceptions, such as `NoNodesVisibleException`. Try restarting the build in this case.

**Restarting Builds**

- If you do not have the proper permissions to restart the build, you can:
    + Push an empty commit, i.e. `git commit --allow-empty -m "Trigger CI"` to get the build to trigger. Squash these later.
    + Open and close the pull request. The downside is that this leaves traces on GitHub.
- Alternatively, you can comment on the PR to signal to the dev team that the PR is ready to be reviewed. We will ascertain if the build failed due to the aforementioned quirks and/or nondeterminism, and if yes restart the CI build until it is successful.

**Linters**

Certain things we do in tests are unidiomatic and may require us to make exceptions for them with Findbugs or CheckStyle. Be as specific as possible when targeting code to exclude from checks.

## Unit Tests

Unit tests are meant to extensively test the functionality of a HubTurbo component. In most cases, this should be done without the use of File I/O. When testing file I/O components, however, do remember to include code to clean up the project directory at the end of the test, such as through [`UITest.clearTestFolder()`](../src/test/java/guitests/UITest.java).

## Additional Tools

[mockito](http://mockito.org/) is used in HubTurbo's tests suite to create clean and verifiable stubbed classes.
A common use case is to isolate a small component under test while mocking other required components without having to write complex stubbing logic and mixing between application and testing code. Refer to usage of *mockito* in our tests suite and [mockito's docs](http://mockito.github.io/mockito/docs/current/org/mockito/Mockito.html) for more information, guides and caveats when using *mockito*.

[MockServer](http://www.mock-server.com) is used in HubTurbo' tests suite to emulate and verify interactions with GitHub API services.
Note that when passing in `hostname` other than `api.github.com` (usually `"localhost"` when MockServer is used) to `egit`'s `GitHubClient` constructor, subsequent request paths will be appended to the `/api/v3` prefix.
Recorded data from GitHub API to be used with MockServer should be placed inside the `test/resources/tests` directory.
Refer to existing usage of MockServer in our tests suite and guides on [MockServer' website](http://www.mock-server.com) for more information.

**404 error from MockServer**

If you receive a 404 for a valid request path and parameters, a possible cause is that the MockServer process may take while to build the expectations when a large amount of request or response data is involved.
This typically happens when we only get the headers from multiple requests i.e. HEAD requests, which take very short time, while the body is still building up.
This error should not happen if we request for the body (GET, POST etc.) as the request will block until the body is available.
As a result, this situation can be avoided by omitting body data in expectations that only concern headers or making a full request.
