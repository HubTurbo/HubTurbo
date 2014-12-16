package ui.issuepanel.expanded;

import java.awt.Rectangle;
import java.util.Optional;

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
import ui.UI;
import util.GitHubURL;
import util.IOUtilities;
import util.events.IssueSelectedEvent;

public class IssueCommentsDisplay {

	private static final boolean USE_MOBILE_USER_AGENT = true;

	private static String HIDE_ELEMENTS_SCRIPT_PATH = USE_MOBILE_USER_AGENT
			? "ui/issuepanel/expanded/hideUI.js"
			: "ui/issuepanel/expanded/mobileHideUI.js";

	// Chrome, Android 4.2.2, Samsung Galaxy S4
	private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";

	static {
		System.setProperty("webdriver.chrome.driver", "/Users/darius/Downloads/chromedriver");
	}

	// Component state

	private UI ui;

	// Only one instance of the WebDriver is used, and its lifetime is equal to that
	// of the app. This component may be arbitrarily recreated by anything that owns
	// it, so the driver (and all of its state) is static.
	
	private static WebDriver driver = null;
	private static boolean loggedIn = false;

	public IssueCommentsDisplay(UI ui) {
		this.ui = ui;
		
		if (driver == null) {
			driver = setupDriver();
			ui.registerEvent((IssueSelectedEvent e) -> {
				if (!ui.isExpanded()) {
					driverShowIssue(e.id);
				}
			});
		}
	}

	public void toggle() {
		// Show the driver
		if (!ui.toggleExpandedWidth()) {
			// TODO needs ref to current issue to do stuff
			driverShowIssue(1);
		} else {
			// Do nothing; leave the driver in the background
		}
	}

	/**
	 * Runs a script in the currently-active driver window to hide GitHub UI elements.
	 */
	private void hidePageElements() {
		Optional<String> file = IOUtilities.readResource(HIDE_ELEMENTS_SCRIPT_PATH);
		if (file.isPresent()) {
			driverExecuteJavaScript(file.get());
		} else {
			System.out.println("Failed to read script for hiding elements; did not execute");
		}
	}

	/**
	 * Navigates to the GitHub page for the given issue in the currently-active
	 * driver window.
	 * Run on a separate thread.
	 */
	private void driverShowIssue(int id) {
		Thread th = new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				driverLoginTest();
				driver.get(GitHubURL.getPathForIssue(id));
				hidePageElements();
				return null;
			}
		});
		th.setDaemon(true);
		th.start();
	}
	
	/**
	 * Logs in in the currently-active driver window, if that hasn't been done. 
	 */
	private void driverLoginTest() {
		if (!loggedIn) {
			driverLogin();
			loggedIn = true;
		}
	}

	/**
	 * Logs in the currently-active driver window using the credentials
	 * supplied by the user on login to the app.
	 * Run on a separate thread.
	 */
	private void driverLogin() {
		Thread th = new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
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
		th.setDaemon(true);
		th.start();
	}

	/**
	 * Executes Javascript in the currently-active driver window.
	 * Run on the UI thread (will block until execution is complete,
	 * i.e. change implementation if long-running scripts must be run).
	 * @param script
	 */
	private void driverExecuteJavaScript(String script) {
		if (driver instanceof JavascriptExecutor) {
			((JavascriptExecutor) driver).executeScript(script);
		} else {
			assert false : "Driver cannot execute JS";
		}
	}

	/**
	 * Creates, initialises, and returns a ChromeDriver.
	 * @return
	 */
	private ChromeDriver setupDriver() {
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
}
