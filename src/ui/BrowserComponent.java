package ui;

import java.awt.Rectangle;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import service.ServiceManager;
import util.GitHubURL;
import util.IOUtilities;
import util.events.IssueSelectedEvent;
import util.events.LoginEvent;

/**
 * An abstraction for the functions of the Selenium web driver.
 */
public class BrowserComponent {
	
	private static final boolean USE_MOBILE_USER_AGENT = true;

	private static String HIDE_ELEMENTS_SCRIPT_PATH = USE_MOBILE_USER_AGENT
			? "ui/issuepanel/expanded/hideUI.js"
			: "ui/issuepanel/expanded/mobileHideUI.js";

	// Chrome, Android 4.2.2, Samsung Galaxy S4
	private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";
	
	static {
		System.setProperty("webdriver.chrome.driver", "/Users/darius/Downloads/chromedriver");
	}
	
	private final UI ui;
	private WebDriver driver = null;
	
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
		driver = setupChromeDriver();
		ui.registerEvent((LoginEvent e) -> {
			login();
		});
		ui.registerEvent((IssueSelectedEvent e) -> {
			// Triggers error logging from EventBus for an unknown reason.
			// No functionality seems to be affected, however... this block
			// always runs.
			if (!ui.isExpanded()) {
				ui.getBrowserComponent().showIssue(e.id);
			}
		});

	}
	
	/**
	 * Creates, initialises, and returns a ChromeDriver.
	 * @return
	 */
	private ChromeDriver setupChromeDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments(String.format("user-agent=\"%s\"", MOBILE_USER_AGENT));
		ChromeDriver driver = new ChromeDriver(options);
		driver.manage().window().setPosition(new Point((int) ui.getCollapsedX(), 0));
		Rectangle availableDimensions = ui.getAvailableDimensions();
		driver.manage().window().setSize(new Dimension(
				(int) availableDimensions.getWidth(),
				(int) availableDimensions.getHeight()));
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
	}
	

	/**
	 * Runs a script in the currently-active driver window to hide GitHub UI elements.
	 */
	private void hidePageElements() {
		Optional<String> file = IOUtilities.readResource(HIDE_ELEMENTS_SCRIPT_PATH);
		if (file.isPresent()) {
			executeJavaScript(file.get());
		} else {
			System.out.println("Failed to read script for hiding elements; did not execute");
		}
	}

	/**
	 * Navigates to the GitHub page for the given issue in the currently-active
	 * driver window.
	 * Run on a separate thread.
	 */
	public void showIssue(int id) {
		executor.execute(new Task<Void>() {
			@Override
			protected Void call() {
				if (!driver.getCurrentUrl().equals(GitHubURL.getPathForIssue(id))) {
					driver.get(GitHubURL.getPathForIssue(id));
					hidePageElements();
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
		executor.execute(new Task<Void>() {
			@Override
			protected Void call() {
				driver.get(GitHubURL.LOGIN_PAGE);
				// driver.getCurrentUrl()
				// driver.close(); // what do?
				try {
					WebElement searchBox = driver.findElement(By.name("login"));
					searchBox.sendKeys(ServiceManager.getInstance().getUserId());
					searchBox = driver.findElement(By.name("password"));
					searchBox.sendKeys(ServiceManager.getInstance().getPassword());
					searchBox.submit();
				} catch (NoSuchElementException e) {
					// Already logged in; do nothing
				}
				return null;
			}
		});
	}
	
}
