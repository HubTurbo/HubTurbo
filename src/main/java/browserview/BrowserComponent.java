package browserview;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import javafx.concurrent.Task;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ui.UI;
import util.GitHubURL;
import util.PlatformSpecific;

import java.awt.*;
import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An abstraction for the functions of the Selenium web driver.
 * It depends minimally on UI for width adjustments.
 */
public class BrowserComponent {

	private static final Logger logger = LogManager.getLogger(BrowserComponent.class.getName());

	private static final boolean USE_MOBILE_USER_AGENT = false;
	private static boolean isTestChromeDriver;

	private static String HIDE_ELEMENTS_SCRIPT_PATH = USE_MOBILE_USER_AGENT
			? "browserview/expanded/mobileHideUI.js"
			: "browserview/expanded/hideUI.js";

	// Chrome, Android 4.2.2, Samsung Galaxy S4
	private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";

	private static final String CHROME_DRIVER_LOCATION = "browserview/";
	private static final String CHROME_DRIVER_BINARY_NAME = determineChromeDriverBinaryName();

	private String pageContentOnLoad = "";

	private static final int SWP_NOSIZE = 0x0001;
	private static final int SWP_NOMOVE = 0x0002;
	private static HWND browserWindowHandle;
	private static User32 user32;

	static {
		setupJNA();
		setupChromeDriverExecutable();
	}

	private final UI ui;
	private ChromeDriver driver = null;

	// We want browser commands to be run on a separate thread, but not to
	// interfere with each other. This executor is limited to a single instance,
	// so it ensures that browser commands are queued and executed in sequence.

	// The alternatives would be to:
	// - allow race conditions
	// - interrupt the blocking WebDriver::get method

	// The first is not desirable and the second does not seem to be possible
	// at the moment.
	private Executor executor;

	public BrowserComponent(UI ui, boolean isTestChromeDriver) {
		this.ui = ui;
		this.executor = Executors.newSingleThreadExecutor();
		this.isTestChromeDriver = isTestChromeDriver;
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
		if (driver == null) {
			return;
		}

		try {
			driver.quit();
		} catch (WebDriverException e) {
			// Chrome was closed; do nothing
		}
	}

	/**
	 * Creates, initialises, and returns a ChromeDriver.
	 * @return
	 */
	private ChromeDriver createChromeDriver() {
		ChromeOptions options = new ChromeOptions();
		if (USE_MOBILE_USER_AGENT) {
			options.addArguments(String.format("user-agent=\"%s\"", MOBILE_USER_AGENT));
		}
		ChromeDriver driver;
		if (isTestChromeDriver) {
			driver = new ChromeDriverStub(options);
			logger.info("Creating ChromeDriverStub");
		} else {
			driver = new ChromeDriver(options);
			logger.info("Creating ChromeDriver");
		}
		driver.manage().window().setPosition(new Point((int) ui.getCollapsedX(), 0));
		Rectangle availableDimensions = ui.getAvailableDimensions();
		driver.manage().window().setSize(new Dimension(
				(int) availableDimensions.getWidth(),
				(int) availableDimensions.getHeight()));
		initialiseJNA();
		return driver;
	}

	private void removeChromeDriverIfNecessary() {
		if (ui.getCommandLineArgs().containsKey(UI.ARG_UPDATED_TO)) {
			new File(CHROME_DRIVER_BINARY_NAME).delete();
		}
	}

	/**
	 * Executes Javascript in the currently-active driver window.
	 * Run on the UI thread (will block until execution is complete,
	 * i.e. change implementation if long-running scripts must be run).
	 * @param script
	 */
	private void executeJavaScript(String script) {
		if (driver instanceof JavascriptExecutor) {
			((JavascriptExecutor) driver).executeScript(script);
		} else {
			assert false : "Driver cannot execute JS";
		}
		logger.info("Executed JavaScript " + script.substring(0, Math.min(script.length(), 10)));
	}

	/**
	 * Runs a script in the currently-active driver window to hide GitHub UI elements.
	 */
//	private void hidePageElements() {
//		Optional<String> file = IOUtilities.readResource(HIDE_ELEMENTS_SCRIPT_PATH);
//		if (file.isPresent()) {
//			executeJavaScript(file.get());
//		} else {
//			logger.info("Failed to read script for hiding elements; did not execute");
//		}
//	}

	/**
	 * Navigates to the New Label page on GitHub.
	 * Run on a separate thread.
	 */
	public void newLabel() {
		logger.info("Navigating to New Label page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForNewLabel(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForNewLabel(ui.logic.getDefaultRepo()));
			}
		});
	}

	/**
	 * Navigates to the New Milestone page on GitHub.
	 * Run on a separate thread.
	 */
	public void newMilestone() {
		logger.info("Navigating to New Milestone page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForNewMilestone(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForNewMilestone(ui.logic.getDefaultRepo()));
			}
		});
		bringToTop();
	}

	/**
	 * Navigates to the New Issue page on GitHub.
	 * Run on a separate thread.
	 */
	public void newIssue() {
		logger.info("Navigating to New Issue page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForNewIssue(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForNewIssue(ui.logic.getDefaultRepo()));
			}
		});
		bringToTop();
	}

	/**
	 * Navigates to the HubTurbo documentation page.
	 * Run on a separate thread.
	 */
	public void showDocs() {
		logger.info("Showing documentation page");
		runBrowserOperation(() -> driver.get(GitHubURL.getPathForDocsPage()));
	}

	/**
	 * Navigates to the GitHub changelog page.
	 * Run on a separate thread.
	 */
//	public void showChangelog(String version) {
//		logger.info("Showing changelog for version " + version);
//		runBrowserOperation(() -> driver.get(GitHubURL.getChangelogForVersion(version)));
//	}

	/**
	 * Navigates to the GitHub page for the given issue in the currently-active
	 * driver window.
	 * Run on a separate thread.
	 */
	public void showIssue(String repoId, int id) {
		logger.info("Showing issue #" + id);
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForIssue(repoId, id))) {
				driver.get(GitHubURL.getPathForIssue(repoId, id));
			}
		});
	}

	public void jumpToComment(){
		WebElement comment = driver.findElementById("new_comment_field");
		comment.click();
		bringToTop();
	}

	private boolean isBrowserActive(){
		if (driver == null) {
			logger.info("Initializing ChromeDriver");
			return false;
		}
		try {
			// Throws an exception if unable to switch to original HT tab
			// which then triggers a browser reset when called from runBrowserOperation
			driver.switchTo().window(driver.getWindowHandle());
			// When the HT tab is closed (but the window is still alive),
			// a lot of the operations on the driver (such as getCurrentURL)
			// will hang (without throwing an exception, the thread will just freeze the UI forever),
			// so we cannot use getCurrentURL/getTitle to check if the original HT tab
			// is still open. The above line does not hang the driver but still throws
			// an exception, thus letting us detect that the HT tab is not active any more.
			return true;
		} catch (WebDriverException e) {
			logger.warn("Unable to reach bview. Resetting.");
			return false;
		}
	}

	//	A helper function for reseting browser.
	private void resetBrowser(){
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
		executor.execute(new Task<Void>() {
			@Override
			protected Void call() {
				if (isBrowserActive()) {
					try {
						operation.run();
						pageContentOnLoad = getCurrentPageSource();
					} catch (WebDriverException e) {
						switch (BrowserComponentError.fromErrorMessage(e.getMessage())) {
						case NoSuchWindow:
							resetBrowser();
							runBrowserOperation(operation); // Recurse and repeat
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
				return null;
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
			driver.get(GitHubURL.LOGIN_PAGE);
			try {
				WebElement searchBox = driver.findElement(By.name("login"));
				searchBox.sendKeys(ui.logic.credentials.username);
				searchBox = driver.findElement(By.name("password"));
				searchBox.sendKeys(ui.logic.credentials.password);
				searchBox.submit();
			} catch (NoSuchElementException e) {
				// Already logged in; do nothing
			}
		});
	}

	/**
	 * One-time JNA setup.
	 */
	private static void setupJNA() {
		if (PlatformSpecific.isOnWindows()) {
			user32 = User32.INSTANCE;
		}
	}

	/**
	 * JNA initialisation. Should happen whenever the Chrome window is recreated.
	 */
	private void initialiseJNA() {
		if (PlatformSpecific.isOnWindows()) {
			browserWindowHandle = user32.FindWindow(null, "data:, - Google Chrome");
		}
	}

	private static String determineChromeDriverBinaryName() {
		return PlatformSpecific.isOnMac() ? "chromedriver"
			: PlatformSpecific.isOnWindows() ? "chromedriver.exe"
			: "chromedriver_linux";
	}

	/**
	 * Ensures that the chromedriver executable is in the project root before
	 * initialisation. Since executables are packaged for all platforms, this also
	 * picks the right version to use.
	 */
	private static void setupChromeDriverExecutable() {
		File f = new File(CHROME_DRIVER_BINARY_NAME);
		if(!f.exists()) {
			InputStream in = BrowserComponent.class.getClassLoader().getResourceAsStream(CHROME_DRIVER_LOCATION + CHROME_DRIVER_BINARY_NAME);
			assert in != null : "Could not find " + CHROME_DRIVER_BINARY_NAME + " at " + CHROME_DRIVER_LOCATION + "; this path must be updated if the executables are moved";
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

	/**
	 * Resizes the browser window based on the given width.
	 * Executed on another thread.
	 */
//	public void resize(double width) {
//		executor.execute(new Task<Void>() {
//			@Override
//			protected Void call() {
//				driver.manage().window().setPosition(new Point((int) width, 0));
//				Rectangle availableDimensions = ui.getAvailableDimensions();
//				driver.manage().window().setSize(new Dimension(
//						(int) availableDimensions.getWidth(),
//						(int) availableDimensions.getHeight()));
//				return null;
//			}
//		});
//	}

	private void bringToTop(){
		if (PlatformSpecific.isOnWindows()) {
			user32.ShowWindow(browserWindowHandle, WinUser.SW_RESTORE);
			user32.SetForegroundWindow(browserWindowHandle);
		}
	}

	public void focus(HWND mainWindowHandle){
		if (PlatformSpecific.isOnWindows()) {
			user32.ShowWindow(browserWindowHandle, WinUser.SW_SHOWNOACTIVATE);
			user32.SetWindowPos(browserWindowHandle, mainWindowHandle, 0,0,0,0, SWP_NOMOVE | SWP_NOSIZE);
			user32.SetForegroundWindow(mainWindowHandle);
		}
	}

	private String getCurrentPageSource() {
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		String result = StringEscapeUtils.escapeHtml4((String) executor.executeScript("return document.documentElement.outerHTML"));
		return result;
	}

	public boolean hasBviewChanged() {
		if (isBrowserActive()) {
			if (getCurrentPageSource().equals(pageContentOnLoad)){
				return false;
			}
			pageContentOnLoad = getCurrentPageSource();
			return true;
		}
		return false;
	}

	public void scrollToTop() {
		String script = "window.scrollTo(0, 0)";
		executeJavaScript(script);
	}

	public void scrollToBottom() {
		String script = "window.scrollTo(0, document.body.scrollHeight)";
		executeJavaScript(script);
	}

	public void scrollPage(boolean isDownScroll) {
		String script;
		if (isDownScroll)
			script = "window.scrollBy(0,100)";
		else
			script = "window.scrollBy(0, -100)";
		executeJavaScript(script);
	}

	private void sendKeysToBrowser(String keyCode) {
		WebElement body;
		try {
			body = driver.findElementByTagName("body");
			body.sendKeys(keyCode);
		} catch (Exception e) {
			logger.error("No such element" + e.getLocalizedMessage(), e);
		}
	}

	public void manageLabels(String keyCode) {
		sendKeysToBrowser(keyCode.toLowerCase());
		bringToTop();
	}

	public void manageAssignees(String keyCode) {
		sendKeysToBrowser(keyCode.toLowerCase());
		bringToTop();
	}

	public void manageMilestones(String keyCode) {
		sendKeysToBrowser(keyCode.toLowerCase());
		bringToTop();
	}

	public void showIssues() {
		logger.info("Navigating to Issues page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForAllIssues(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForAllIssues(ui.logic.getDefaultRepo()));
			}
		});
	}

	public void showPullRequests() {
		logger.info("Navigating to Pull requests page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForPullRequests(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForPullRequests(ui.logic.getDefaultRepo()));
			}
		});
	}

	public void showKeyboardShortcuts() {
		logger.info("Navigating to Keyboard Shortcuts");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForKeyboardShortcuts())) {
				driver.get(GitHubURL.getPathForKeyboardShortcuts());
			}
		});
	}

	public void showMilestones() {
		logger.info("Navigating to Milestones page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForMilestones(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForMilestones(ui.logic.getDefaultRepo()));
			}
		});
	}

	public void showContributors() {
		logger.info("Navigating to Contributors page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForContributors(ui.logic.getDefaultRepo()))) {
				driver.get(GitHubURL.getPathForContributors(ui.logic.getDefaultRepo()));
			}
		});
	}

	public boolean isCurrentUrlIssue() {
		return GitHubURL.isUrlIssue(driver.getCurrentUrl());
	}

}
