package ui.issuepanel.expanded;

import java.awt.Rectangle;
import java.util.Optional;

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
	private WebDriver driver;
	private boolean loggedIn = false;
	private boolean expanded = true;

	public IssueCommentsDisplay(UI ui) {
		this.ui = ui;
		this.driver = setupDriver();
		
		System.out.println("created issue comments display");
		
		ui.registerEvent((IssueSelectedEvent e) -> {
			if (!expanded) {
				driverShowIssue(e.id);
			}
		});
	}

	public void toggle() {
		expanded = ui.toggleExpandedWidth();

		// Show the driver
		if (!expanded) {
			// TODO needs ref to current issue to do stuff
			driverShowIssue(1);
		} else {
			// Do nothing; leave the driver in the background
		}
	}

	private void driverLoginTest() {
		if (!loggedIn) {
			driverLogin();
			loggedIn = true;
		}
	}

	private void hidePageElements() {
		Optional<String> file = IOUtilities.readResource(HIDE_ELEMENTS_SCRIPT_PATH);
		if (file.isPresent()) {
			driverExecuteJavaScript(file.get());
		} else {
			System.out.println("Failed to read script for hiding elements; did not execute");
		}
	}

	private void driverShowIssue(int id) {
		driverLoginTest();
		driver.get(GitHubURL.getPathForIssue(id));
		hidePageElements();
	}
	
	private void driverLogin() {
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
	}

	private void driverExecuteJavaScript(String script) {
		if (driver instanceof JavascriptExecutor) {
			((JavascriptExecutor) driver).executeScript(script);
		} else {
			assert false : "Driver cannot execute JS";
		}
	}

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
