package browserview;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

import service.ServiceManager;
import ui.UI;
import util.GitHubURL;
import util.IOUtilities;
import util.PlatformSpecific;

/**
 * An abstraction for the functions of the Selenium web driver.
 * It depends minimally on UI for width adjustments.
 */
public class BrowserComponent {
	
	private static final Logger logger = LogManager.getLogger(BrowserComponent.class.getName());
	
	private static final boolean USE_MOBILE_USER_AGENT = false;

	private static String HIDE_ELEMENTS_SCRIPT_PATH = USE_MOBILE_USER_AGENT
			? "ui/issuepanel/expanded/mobileHideUI.js"
			: "ui/issuepanel/expanded/hideUI.js";	

	// Chrome, Android 4.2.2, Samsung Galaxy S4
	private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";

	private static final String CHROME_DRIVER_LOCATION = "browserview/";
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
	
	public BrowserComponent(UI ui) {
		this.ui = ui;
		this.executor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Called on application startup. Blocks until the driver is created.
	 * Guaranteed to only happen once.
	 */
	public void initialise() {
		assert driver == null;
		driver = createChromeDriver();
		logger.info("Successfully initialised browser component and ChromeDriver");
	}
	
	/**
	 * Called when application quits. Guaranteed to only happen once.
	 */
	public void onAppQuit() {
		quit();
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
		ChromeDriver driver = new ChromeDriver(options);
		driver.manage().window().setPosition(new Point((int) ui.getCollapsedX(), 0));
		Rectangle availableDimensions = ui.getAvailableDimensions();
		driver.manage().window().setSize(new Dimension(
				(int) availableDimensions.getWidth(),
				(int) availableDimensions.getHeight()));
		initialiseJNA();
		return driver;
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
	private void hidePageElements() {
		Optional<String> file = IOUtilities.readResource(HIDE_ELEMENTS_SCRIPT_PATH);
		if (file.isPresent()) {
			executeJavaScript(file.get());
		} else {
			logger.info("Failed to read script for hiding elements; did not execute");
		}
	}

	/**
	 * Navigates to the New Label page on GitHub.
	 * Run on a separate thread.
	 */
	public void newLabel() {
		logger.info("Navigating to New Label page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForNewLabel())) {
				driver.get(GitHubURL.getPathForNewLabel());
			}
		});
		bringToTop();
	}

	/**
	 * Navigates to the New Milestone page on GitHub.
	 * Run on a separate thread.
	 */
	public void newMilestone() {
		logger.info("Navigating to New Milestone page");
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForNewMilestone())) {
				driver.get(GitHubURL.getPathForNewMilestone());
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
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForNewIssue())) {
				driver.get(GitHubURL.getPathForNewIssue());
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
		runBrowserOperation(() -> {
			driver.get(GitHubURL.getPathForDocsPage());
		});
	}

	/**
	 * Navigates to the GitHub changelog page.
	 * Run on a separate thread.
	 */
	public void showChangelog(String version) {
		logger.info("Showing changelog for version " + version);
		runBrowserOperation(() -> {
			driver.get(GitHubURL.getChangelogForVersion(version));
		});
	}

	/**
	 * Navigates to the GitHub page for the given issue in the currently-active
	 * driver window.
	 * Run on a separate thread.
	 */
	public void showIssue(int id) {
		logger.info("Showing issue #" + id);
		runBrowserOperation(() -> {
			if (!driver.getCurrentUrl().equals(GitHubURL.getPathForIssue(id))) {
				driver.get(GitHubURL.getPathForIssue(id));
			}
		});
	}
	
	public void jumpToComment(){
		WebElement comment = driver.findElementById("new_comment_field");
		comment.click();
		bringToTop();
	}
	
	private boolean isBrowserActive(){
		if (driver == null){
			logger.warn("chromedriver process was killed !");
			return false;
 		}
		try {
			String url = driver.getCurrentUrl();
			if(url.isEmpty() || url == null){
				return false;
			}
		} catch (WebDriverException e){
			logger.warn("Unable to read url from bview. Resetting.");
			return false;
		}
		return true;
	}

	//	A helper function for reseting browser.
	private void resetBrowser(){
		logger.info("Relaunching chrome.");
		quit(); // if the driver hangs
		driver = createChromeDriver();
		initialiseJNA();
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
		runBrowserOperation(() -> {
			driver.get(GitHubURL.LOGIN_PAGE);
			try {
				WebElement searchBox = driver.findElement(By.name("login"));
				searchBox.sendKeys(ServiceManager.getInstance().getUserId());
				searchBox = driver.findElement(By.name("password"));
				searchBox.sendKeys(ServiceManager.getInstance().getLastUsedPassword());
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
	
	/**
	 * Ensures that the chromedriver executable is in the project root before
	 * initialisation. Since executables are packaged for all platforms, this also
	 * picks the right version to use.
	 */
	private static void setupChromeDriverExecutable() {
		String binaryFileName =
				PlatformSpecific.isOnMac() ? "chromedriver"
				: PlatformSpecific.isOnWindows() ? "chromedriver.exe"
				: "chromedriver_linux";
		
		File f = new File(binaryFileName);
		if(!f.exists()) {
			InputStream in = BrowserComponent.class.getClassLoader().getResourceAsStream(CHROME_DRIVER_LOCATION + binaryFileName);
			assert in != null : "Could not find " + binaryFileName + " at " + CHROME_DRIVER_LOCATION + "; this path must be updated if the executables are moved";
			OutputStream out;
			try {
				out = new FileOutputStream(binaryFileName);
				IOUtils.copy(in, out);
				out.close();
				f.setExecutable(true);
			} catch (IOException e) {
				logger.error("Could not load Chrome driver binary! " + e.getLocalizedMessage(), e);
			}
			logger.info("Could not find " + binaryFileName + "; extracted it from jar");
		} else {
			logger.info("Located " + binaryFileName);
		}
		
		System.setProperty("webdriver.chrome.driver", binaryFileName);
	}

	/**
	 * Resizes the browser window based on the given width.
	 * Executed on another thread.
	 */
	public void resize(double width) {
		executor.execute(new Task<Void>() {
			@Override
			protected Void call() {
				driver.manage().window().setPosition(new Point((int) width, 0));
				Rectangle availableDimensions = ui.getAvailableDimensions();
				driver.manage().window().setSize(new Dimension(
						(int) availableDimensions.getWidth(),
						(int) availableDimensions.getHeight()));
				return null;
			}
		});
	}
	
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
		if (getCurrentPageSource().equals(pageContentOnLoad)){
			return false;
		}
		pageContentOnLoad = getCurrentPageSource();
		return true;
	}
}
