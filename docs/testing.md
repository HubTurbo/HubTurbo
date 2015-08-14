# Testing

There are two kinds of tests for HubTurbo, GUI Tests and Unit Tests. They are separated into two packages in the [`test`](../src/test/java) folder, namely [`guitests`](../src/test/java/guitests) and [`tests`](../src/test/java/tests). Currently all tests are run sequentially, with a new `JVM` started for each new test class (mainly because you can't run more than one GUI Test at once).  

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

**Interacting with the `Stage`**

Subclasses of the JavaFX [`Node`](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html) can be referenced via their id (similar to a HTML id attribute). A `Node`'s id is set using `setId("<id>")`, and it can then be selected using `find("#<id>")`, and interacted with using `click("#<id>")`.

Follow the conventions for existing ids when assigning ids to new elements.

**Concurrency**

Any code that changes the UI from a thread other than the JavaFX Application Thread must be wrapped in `Platform.runLater` (see guidelines on [thread safety](designRationalesAndGuidelines.md#thread-safety), specifically the part on thread confinement).

Note, however, that `Platform.runLater` does not provide any guarantees as to *when* your code actually executes. If you need another thread to wait until a UI operation is finished, use `PlatformEx.runAndWait`, or one of the synchronisation aids in [java.util.concurrent](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/package-summary.html), if `PlatformEx.runAndWait` isn't what you need. A latch or barrier should work in most cases.

Do not use `Thread.sleep` to sequence the execution of threads with respect to each other. It is unreliable due to the semantics of `Platform.runLater` and can result in slow, brittle, and/or nondeterministic code as tests get more complex.

In general, ensure as far as possible that tests are deterministic. Make all inputs to tests (such as global state) explicit.

**Events**

To test for events, you can create new events for the test in [`util.events.testevents`](../src/main/java/util/events/testevents), ensure that you also create the corresponding event handler. You can then test for event triggering using [`UI.events.registerEvent((EventHandler))`](../src/main/java/ui/UI.java). 

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