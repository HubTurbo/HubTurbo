# Testing

There are two kinds of tests for HubTurbo, GUI Tests and Unit Tests. They are separated into two packages in the `test` folder, namely `guitests` and `tests`. Currently all tests are run sequentially, with a new `JVM` started for each new test class (mainly because you can't run more than one GUI Test at once).  

## GUI Tests

For GUI Tests, HubTurbo supports a few launch arguments/flags to aid in testing. 

E.g. `--test=true --bypasslogin=true --closeonquit=true` (in program arguments) which simulates a test mode. 
- `test` - used to enable test mode which by default doesn't read/write to any external `json` file (everything is kept in memory) and uses stubs for certain components like `BrowserComponent`
- `testconfig` - used to enable reading/writing of the `settings/test.json` file during test mode (for user preferences)
- `bypasslogin` - used to bypass the login dialog (username and password will be left empty)
- `testjson` - used to enable reading/writing to the repository `json` files during test mode
- `testchromedriver` - used to test `BrowserComponent` and `ChromeDriverEx`
- `closeonquit` - used for test mode to shutdown `JVM` on quit (mostly for manual testing, not used for ci/tests because that will cause tests to fail)

Most GUI Tests extend `UITest` which in turn extends `GuiTest` which is part of `TestFX`. 
- Override `launchApp` to define your own program arguments for the test
- Override `setupMethod` to add any pre-test configuration/setup before the stage is launched

Ensure that anything that causes the UI to change is wrapped in a `Platform.runLater` (if not you will get a `not on FX thread` error). You may need to specify a delay (using `sleep(X)`) to ensure that the wrapped command has been executed before proceeding on to the next line. Alternatively you can consider using `PlatformEx.runAndWait`. 

To test for events, you can create new events for the test in `util.events.testevents`, ensure that you also create the corresponding event handler. You can then test for event triggering using `UI.events.registerEvent((EventHandler))`. 

When testing certain GUI components, if they are children of the JavaFX `Node` class, they can be interacted directly within the test using their identifier. Make sure that the component's identifier has been set using `setId(<identifier>)` and then you can use `find(#<identifier>)` select it. Any method that uses identifiers can also be used such as `click(#<identifier>)`. 

In order to interact with a text field within a pop-up/dialog window within the CI, the main stage must be hidden first. See `LabelPickerTests` for an example of this. 

## CI Quirks

HubTurbo uses Travis as a CI and even the GUI Tests are run on it. This does expose a few unintended behaviours. If a build fails on the CI with certain errors from the GUI portion such as `NoNodesVisibleException` and so on, try restarting the build. If you do not have the proper permissions to restart the build, you can always try pushing another commit that does not change any functional code (e.g. refactor, write more comments, etc) to get the CI to run the build again. Alternatively, you can comment on the PR to signal to the dev team that the PR is ready to be reviewed. We will ascertain if the build fails due to the aforementioned quirks, and if yes restart the build on the CI until it is successful.

## Unit Tests

Unit tests are meant to extensively test the functionality of a HubTurbo component. In most cases, this should be done without the use of File I/O. When testing file I/O components, however, do remember to include code to clean up the project directory at the end of the test, such as through `UITest.clearTestFolder`.