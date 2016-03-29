package browserview;

import backend.resource.TurboIssue;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.os.Kernel32;
import ui.ScreenManager;
import ui.UI;
import util.GitHubURL;
import util.GithubPageElements;
import util.PlatformSpecific;
import util.events.testevents.JumpToNewCommentBoxEvent;
import util.events.testevents.SendKeysToBrowserEvent;

import java.awt.Rectangle;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An abstraction for the functions of the Selenium web driver.
 * It depends minimally on UI for width adjustments.
 */
public class BrowserComponent {

    private static final Logger logger = LogManager.getLogger(BrowserComponent.class.getName());

    private static final String CHROMEDRIVER_VERSION = "2-18";
    private static final boolean USE_MOBILE_USER_AGENT = false;
    private final boolean isTestChromeDriver;

    // Chrome, Android 4.2.2, Samsung Galaxy S4
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39)" +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";

    private static final String CHROME_DRIVER_LOCATION = "browserview/";
    private static final String CHROME_DRIVER_BINARY_NAME = determineChromeDriverBinaryName();

    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_NOACTIVATE = 0x0010;

    /**
     * Specifies how many tries to wait for a web page to load
     */
    private static final int WAIT_PAGE_LOAD_MAX_RETRY = 20;
    /**
     * Specifies the duration between tries to wait for a web page to load
     */
    private static final int WAIT_PAGE_LOAD_SLEEP_TIME = 10;

    private static HWND browserWindowHandle;
    private static User32 user32;

    private final UI ui;
    private final ScreenManager screenManager;
    private ChromeDriverEx driver = null;

    private String pageContentOnLoad = "";

    // We want browser commands to be run on a separate thread, but not to
    // interfere with each other. This executor is limited to a single instance,
    // so it ensures that browser commands are queued and executed in sequence.

    // The alternatives would be to:
    // - allow race conditions
    // - interrupt the blocking WebDriver::get method

    // The first is not desirable and the second does not seem to be possible
    // at the moment.
    private final Executor executor;

    public BrowserComponent(UI ui, ScreenManager screenManager, boolean isTestChromeDriver) {
        this.ui = ui;
        this.screenManager = screenManager;

        executor = Executors.newSingleThreadExecutor();
        this.isTestChromeDriver = isTestChromeDriver;
        setupJNA();
        setupChromeDriverExecutable();
    }

    /**
     * Called on application startup. Blocks until the driver is created.
     * Guaranteed to only happen once.
     */
    public void initialise() {
        assert driver == null;
        executor.execute(() -> {
            driver = createChromeDriver();
            logger.info("Successfully initialised browser component and ChromeDriver");
        });
        login();
    }

    /**
     * Called when application quits. Guaranteed to only happen once.
     */
    public void onAppQuit() {
        quit();
        removeChromeDriverIfNecessary();
    }

    /**
     * Quits the browser component.
     */
    private void quit() {
        logger.info("Quitting browser component");
        // The application may quit before the browser is initialised.
        // In that case, do nothing.
        if (driver != null) {
            try {
                driver.quit();
            } catch (WebDriverException e) {
                // Chrome was closed; do nothing
                logger.info("Chrome already closed");
            }
        }
    }

    /**
     * Creates, initialises, and returns a ChromeDriver.
     *
     * @return
     */
    private ChromeDriverEx createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        if (USE_MOBILE_USER_AGENT) {
            options.addArguments(String.format("user-agent=\"%s\"", MOBILE_USER_AGENT));
        }
        ChromeDriverEx driver = new ChromeDriverEx(options, isTestChromeDriver);
        WebDriver.Options driverOptions = driver.manage();
        if (!isTestChromeDriver) {
            setWindowBounds(driverOptions);
            initialiseJNA();
        }
        return driver;
    }

    /**
     * Positions the browser component according to the main window's position and size.
     *
     * @param driverOptions The options object belonging to the WebDriver managing the current Chrome window.
     */
    public void setWindowBounds(WebDriver.Options driverOptions) {
        Rectangle windowBounds = screenManager.getBrowserComponentBounds();

        driverOptions.window().setPosition(new Point((int) windowBounds.getX(),
                                                     (int) windowBounds.getY()));
        driverOptions.window().setSize(new Dimension((int) windowBounds.getWidth(),
                                                     (int) windowBounds.getHeight()));
    }

    private void removeChromeDriverIfNecessary() {
        if (ui.getCommandLineArgs().containsKey(UI.ARG_UPDATED_TO)) {
            boolean success = new File(CHROME_DRIVER_BINARY_NAME).delete();
            if (!success) {
                logger.warn("Failed to delete chromedriver");
            }
        }
    }

    /**
     * Executes Javascript in the currently-active driver window.
     * Run on the UI thread (will block until execution is complete,
     * i.e. change implementation if long-running scripts must be run).
     *
     * @param script
     */
    private void executeJavaScript(String script) {
        driver.executeScript(script);
        logger.info("Executed JavaScript " + script.substring(0, Math.min(script.length(), 10)));
    }

    /**
     * Navigates to the New Label page on GitHub.
     * Run on a separate thread.
     */
    public void newLabel() {
        logger.info("Navigating to New Label page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForNewLabel(ui.logic.getDefaultRepo()), false));
        bringToTop();
    }

    /**
     * Navigates to the New Milestone page on GitHub.
     * Run on a separate thread.
     */
    public void newMilestone() {
        logger.info("Navigating to New Milestone page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForNewMilestone(ui.logic.getDefaultRepo()), false));
        bringToTop();
    }

    /**
     * Navigates to the New Issue page on GitHub.
     * Run on a separate thread.
     */
    public void newIssue() {
        logger.info("Navigating to New Issue page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForNewIssue(ui.logic.getDefaultRepo()), false));
        bringToTop();
    }

    /**
     * Navigates to the HubTurbo documentation page.
     * Run on a separate thread.
     */
    public void showDocs() {
        logger.info("Showing documentation page");
        runBrowserOperation(() -> driver.get(GitHubURL.DOCS_PAGE, false));
    }

    /**
     * Navigates to HubTurbo filters doc page, run on separate thread.
     */
    public void showFilterDocs() {
        logger.info("Showing filters documentation page");
        runBrowserOperation(() -> driver.get(GitHubURL.FILTERS_PAGE, false));
    }

    /**
     * Navigates to the GitHub changelog page.
     * Run on a separate thread.
     */
//  public void showChangelog(String version) {
//      logger.info("Showing changelog for version " + version);
//      runBrowserOperation(() -> driver.get(GitHubURL.getChangelogForVersion(version)));
//  }

    /**
     * Navigates to the GitHub page for the given issue in the currently-active
     * driver window. Ignores current tab if it's a PR.
     * Run on a separate thread.
     */
    public void showIssue(String repoId, int id, boolean isPullRequest, boolean isForceRefresh) {
        if (isPullRequest) {
            logger.info("Showing pull request #" + id);
            if (!isCurrentUrlPrPageIgnoreTabs(repoId, id)) {
                runBrowserOperation(() -> driver.get(GitHubURL.getPathForPullRequest(repoId, id), isForceRefresh));
            } else {
                logger.info("Already at PR page: " + getCurrentUrl() + " will not reload.");
            }
        } else {
            logger.info("Showing issue #" + id);
            runBrowserOperation(() -> driver.get(GitHubURL.getPathForIssue(repoId, id), isForceRefresh));
        }
        runBrowserOperation(() -> scrollToBottom());
    }

    /**
     * Checks if current URL is the specified PR's page, regardless of the PR tab the URL is in.
     *
     * @return true if the url of current page points to the PR identified by the arguments
     */
    private boolean isCurrentUrlPrPageIgnoreTabs(String repoId, int id) {
        return getCurrentUrl().matches(
            "\\Q" + GitHubURL.getPathForPullRequest(repoId, id) + "\\E(?:/|/commits|/files)?"
        );
    }

    /**
     * Navigates to the main tab of the given issue in the currently-active driver window.
     * Runs on a separate thread.
     */
    public void showIssueMainTab(String repoId, int id, boolean isPullRequest, boolean isForceRefresh) {
        if (isPullRequest) {
            logger.info("Showing main tab for pull request #" + id);
            runBrowserOperation(() -> driver.get(GitHubURL.getPathForPullRequest(repoId, id), isForceRefresh));
            runBrowserOperation(() -> scrollToBottom());
        } else {
            showIssue(repoId, id, isPullRequest, isForceRefresh);
        }
    }

    public void jumpToNewCommentBox(){
        if (isTestChromeDriver) {
            UI.events.triggerEvent(new JumpToNewCommentBoxEvent());
        }
        try {
            WebElement comment = driver.findElementById(GithubPageElements.NEW_COMMENT);
            comment.click();
            bringToTop();
        } catch (Exception e) {
            logger.warn("Unable to reach jump to comments. ");
        }
    }

    private boolean isBrowserActive() {
        if (driver == null) return false;
        try {
            // Throws an exception if unable to switch to original HT tab
            // which then triggers a browser reset when called from runBrowserOperation
            WebDriver.TargetLocator switchTo = driver.switchTo();
            String windowHandle = driver.getWindowHandle();
            if (!isTestChromeDriver) switchTo.window(windowHandle);
            // When the HT tab is closed (but the window is still alive),
            // a lot of the operations on the driver (such as getCurrentURL)
            // will hang (without throwing an exception, the thread will just freeze the UI forever),
            // so we cannot use getCurrentURL/getTitle to check if the original HT tab
            // is still open. The above line does not hang the driver but still throws
            // an exception, thus letting us detect that the HT tab is not active any more.
            return true;
        } catch (WebDriverException e) {
            logger.warn("Unable to reach bview. ");
            return false;
        }
    }

    //  A helper function for reseting browser.
    private void resetBrowser() {
        logger.info("Relaunching chrome.");
        quit(); // if the driver hangs
        driver = createChromeDriver();
        login();
    }

    /**
     * A helper function for running browser operations.
     * Takes care of running it on a separate thread, and normalises error-handling across
     * all types of code.
     */
    private void runBrowserOperation (Runnable operation) {
        executor.execute(() -> {
            if (isBrowserActive()) {
                try {
                    operation.run();
                    pageContentOnLoad = getCurrentPageSource();
                } catch (WebDriverException e) {
                    switch (BrowserComponentError.fromErrorMessage(e.getMessage())) {
                    case NoSuchWindow:
                        resetBrowser();
                        runBrowserOperation(operation); // Recurse and repeat
                        break;
                    case NoSuchElement:
                        logger.info("Warning: no such element! " + e.getMessage());
                        break;
                    default:
                        break;
                    }
                }
            } else {
                logger.info("Chrome window not responding.");
                resetBrowser();
                runBrowserOperation(operation);
            }
        });
    }

    /**
     * Logs in the currently-active driver window using the credentials
     * supplied by the user on login to the app.
     * Run on a separate thread.
     */
    public void login() {
        logger.info("Logging in on GitHub...");
        focus(ui.getMainWindowHandle());
        runBrowserOperation(() -> {
            driver.get(GitHubURL.LOGIN_PAGE, false);
            try {
                WebElement searchBox = driver.findElement(By.name(GithubPageElements.LOGIN_FIELD));
                searchBox.sendKeys(ui.logic.loginController.credentials.username);
                searchBox = driver.findElement(By.name(GithubPageElements.PASSWORD_FIELD));
                searchBox.sendKeys(ui.logic.loginController.credentials.password);
                searchBox.submit();
            } catch (Exception e) {
                // Already logged in; do nothing
                logger.info("Unable to login, may already be logged in. ");
            }
        });
    }

    /**
     * One-time JNA setup.
     */
    private static void setupJNA() {
        if (PlatformSpecific.isOnWindows()) user32 = User32.INSTANCE;
    }

    /**
     * JNA initialisation. Should happen whenever the Chrome window is recreated.
     */
    private void initialiseJNA() {
        if (PlatformSpecific.isOnWindows()) {
            browserWindowHandle = user32.FindWindow(null, "data:, - Google Chrome");
        }
    }

    public static String determineChromeDriverBinaryName() {
        if (PlatformSpecific.isOnMac()) {
            logger.info("Using chrome driver binary: chromedriver_" + CHROMEDRIVER_VERSION);
            return "chromedriver_" + CHROMEDRIVER_VERSION;
        } else if (PlatformSpecific.isOnWindows()) {
            logger.info("Using chrome driver binary: chromedriver_" + CHROMEDRIVER_VERSION + ".exe");
            return "chromedriver_" + CHROMEDRIVER_VERSION + ".exe";
        } else if (PlatformSpecific.isOn32BitsLinux()) {
            logger.info("Using chrome driver binary: chromedriver_linux_" + CHROMEDRIVER_VERSION);
            return "chromedriver_linux_" + CHROMEDRIVER_VERSION;
        } else if (PlatformSpecific.isOn64BitsLinux()) {
            logger.info("Using chrome driver binary: chromedriver_linux_x86_64_" + CHROMEDRIVER_VERSION);
            return "chromedriver_linux_x86_64_" + CHROMEDRIVER_VERSION;
        } else {
            logger.error("Unable to determine platform for chrome driver");
            logger.info("Using chrome driver binary: chromedriver_linux_" + CHROMEDRIVER_VERSION);
            return "chromedriver_linux_" + CHROMEDRIVER_VERSION;
        }
    }

    /**
     * Ensures that the chromedriver executable is in the project root before
     * initialisation. Since executables are packaged for all platforms, this also
     * picks the right version to use.
     */
    private static void setupChromeDriverExecutable() {
        File f = new File(CHROME_DRIVER_BINARY_NAME);
        if (!f.exists()) {
            InputStream in = BrowserComponent.class.getClassLoader()
                    .getResourceAsStream(CHROME_DRIVER_LOCATION +
                                                 CHROME_DRIVER_BINARY_NAME);
            assert in != null : "Could not find " + CHROME_DRIVER_BINARY_NAME + " at "
                    + CHROME_DRIVER_LOCATION + "; this path must be updated if the executables are moved";
            OutputStream out;
            try {
                out = new FileOutputStream(CHROME_DRIVER_BINARY_NAME);
                IOUtils.copy(in, out);
                out.close();
                f.setExecutable(true);
            } catch (IOException e) {
                logger.error("Could not load Chrome driver binary! " + e.getLocalizedMessage(), e);
            }
            logger.info("Could not find " + CHROME_DRIVER_BINARY_NAME + "; extracted it from jar");
        } else {
            logger.info("Located " + CHROME_DRIVER_BINARY_NAME);
        }

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_BINARY_NAME);
    }

    private void bringToTop() {
        if (PlatformSpecific.isOnWindows()) {
            user32.ShowWindow(browserWindowHandle, WinUser.SW_RESTORE);
            user32.SetForegroundWindow(browserWindowHandle);
        }
    }

    public void focus(HWND mainWindowHandle) {
        if (PlatformSpecific.isOnWindows()) {
            // Restores browser window if it is minimized / maximized
            user32.ShowWindow(browserWindowHandle, WinUser.SW_SHOWNOACTIVATE);
            // SWP_NOMOVE and SWP_NOSIZE prevents the 0,0,0,0 parameters from taking effect.
            logger.info("Bringing bView to front");
            boolean success = user32.SetWindowPos(browserWindowHandle, mainWindowHandle, 0, 0, 0, 0,
                                                  SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE);
            if (!success) {
                logger.info("Failed to bring bView to front.");
                logger.info(Kernel32.INSTANCE.GetLastError());
            }
            user32.SetForegroundWindow(mainWindowHandle);
        }
    }

    private String getCurrentPageSource() {
        return StringEscapeUtils.escapeHtml4(
                (String) driver.executeScript("return document.documentElement.outerHTML"));
    }

    public boolean hasBviewChanged() {
        if (isTestChromeDriver) return true;
        if (isBrowserActive()) {
            if (getCurrentPageSource().equals(pageContentOnLoad)) return false;
            pageContentOnLoad = getCurrentPageSource();
            return true;
        }
        return false;
    }

    public void scrollToTop() {
        String script = GithubPageElements.SCROLL_TO_TOP;
        executeJavaScript(script);
    }

    public void scrollToBottom() {
        String script = GithubPageElements.SCROLL_TO_BOTTOM;
        executeJavaScript(script);
    }

    public void scrollPage(boolean isDownScroll) {
        String script;
        if (isDownScroll) {
            script = GithubPageElements.SCROLL_DOWN;
        } else {
            script = GithubPageElements.SCROLL_UP;
        }
        executeJavaScript(script);
    }

    private void sendKeysToBrowser(String keyCode) {
        if (isTestChromeDriver) {
            UI.events.triggerEvent(new SendKeysToBrowserEvent(keyCode));
        }
        WebElement body;
        try {
            body = driver.findElementByTagName(GithubPageElements.BODY);
            body.sendKeys(keyCode);
        } catch (Exception e) {
            logger.error("No such element");
        }
    }

    public void manageAssignees(String keyCode) {
        sendKeysToBrowser(keyCode.toLowerCase());
        bringToTop();
    }

    public void showIssues() {
        logger.info("Navigating to Issues page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForAllIssues(ui.logic.getDefaultRepo()), false));
    }

    public void showPullRequests() {
        logger.info("Navigating to Pull requests page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForPullRequests(ui.logic.getDefaultRepo()), false));
    }

    public void showKeyboardShortcuts() {
        logger.info("Navigating to Keyboard Shortcuts");
        runBrowserOperation(() -> driver.get(GitHubURL.KEYBOARD_SHORTCUTS_PAGE, false));
    }

    public void showMilestones() {
        logger.info("Navigating to Milestones page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForMilestones(ui.logic.getDefaultRepo()), false));
    }

    public void showContributors() {
        logger.info("Navigating to Contributors page");
        runBrowserOperation(() -> driver.get(GitHubURL.getPathForContributors(ui.logic.getDefaultRepo()), false));
    }

    public boolean isCurrentUrlIssue() {
        return driver != null && GitHubURL.isUrlIssueOrPr(driver.getCurrentUrl());
    }


    /**
     * Checks if current url is a valid pull request page
     */
    public boolean isCurrentUrlPr() {
        return driver != null && GitHubURL.isUrlPullRequest(driver.getCurrentUrl());
    }
    /**
     * Checks if current URL is GitHub issue or PR discussion page
     */
    public boolean isCurrentUrlDiscussion() {
        return driver != null && GitHubURL.isUrlIssueOrPrDiscussionPage(driver.getCurrentUrl());
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Opens a specified tab in PR's GitHub page.
     * Does nothing if specified issue is not a PR.
     *
     * @param pr TurboIssue of PR whose page is to be opened.
     * @param tab Either GithubPageElements.DISCUSSION_TAB, GithubPageElements.COMMITS_TAB
     *                or GithubPageElements.FILES_TAB
     */
    public void openPrTab(TurboIssue pr, GithubPageElements.PrTab tab) {
        if (!pr.isPullRequest()) {
            return;
        }
        String suffix;
        switch (tab) {
        case DISCUSSION :
            suffix = GitHubURL.PR_TAB_SUFFIX_DISCUSSION;
            break;
        case COMMITS :
            suffix = GitHubURL.PR_TAB_SUFFIX_COMMITS;
            break;
        case FILES :
            suffix = GitHubURL.PR_TAB_SUFFIX_FILES_CHANGED;
            break;
        default:
            assert false;
            return;
        }
        logger.info("Showing " + tab + " tab for pull request #" + pr.getId());
        runBrowserOperation(() -> driver.get(
            GitHubURL.getPathForPullRequest(pr.getRepoId(), pr.getId()) + suffix,
            false
        ));
    }

    public Optional<Integer> getPrNumberFromIssue() {
        // will only get the first PR number if multiple PR's are mentioned
        try {
            WebElement element = driver.findElement(By.xpath("//div[contains(@id, 'ref-pullrequest')]"));
            return Optional.of(Integer.parseInt(element.findElement(By.xpath(".."))
                    .findElement(By.xpath(".//h3/a/span"))
                    .getAttribute("innerHTML").trim().substring(1)));
        } catch (NoSuchElementException e) {
            logger.info("no PR mention found");
        }
        return Optional.empty();
    }

    /**
     * Wait for discussion page (for issue or PR) to be loaded.
     */
    public void waitUntilDiscussionPageLoaded() {
        for (int i = 0; i < WAIT_PAGE_LOAD_MAX_RETRY && !isCurrentUrlDiscussion(); i++) {
            try {
                Thread.sleep(WAIT_PAGE_LOAD_SLEEP_TIME);
            } catch (IllegalArgumentException | InterruptedException e) {
                logger.warn(e);
            }
        }
    }

    public void minimizeWindow() {
        if (PlatformSpecific.isOnWindows()) {
            user32.ShowWindow(browserWindowHandle, WinUser.SW_MINIMIZE);
        }
    }
}
