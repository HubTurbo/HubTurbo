package ui.issuepanel.expanded;

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
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import service.ServiceManager;
import ui.UI;
import util.GitHubURL;
import util.IOUtilities;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;
import util.events.LoginEvent;
import util.events.LoginEventHandler;

/**
 * An abstraction for the functions of the Selenium web driver.
 */
public class BrowserComponent {
	
	private static final boolean USE_MOBILE_USER_AGENT = true;

	private static String HIDE_ELEMENTS_SCRIPT_PATH = USE_MOBILE_USER_AGENT
			? "ui/issuepanel/expanded/mobileHideUI.js"
			: "ui/issuepanel/expanded/hideUI.js";

	// Chrome, Android 4.2.2, Samsung Galaxy S4
	private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";
	
	static {
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
		driver = setupChromeDriver();
		ui.registerEvent(new LoginEventHandler() {
			@Override public void handle(LoginEvent e) {
				login();
			}
		});
		ui.registerEvent(new IssueSelectedEventHandler() {
			@Override public void handle(IssueSelectedEvent e) {
				if (!ui.isExpanded()) {
					ui.getBrowserComponent().showIssue(e.id);
				}
			}
		});

	}

	/**
	 * Called when application quits. Guaranteed to only happen once.
	 */
	public void quit() {
		assert driver != null;
		try {
			driver.close();
		} catch (WebDriverException e) {
			// Chrome was closed; do nothing
		}
	}
	
	/**
	 * Creates, initialises, and returns a ChromeDriver.
	 * @return
	 */
	private ChromeDriver setupChromeDriver() {
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
				try {
					if (!driver.getCurrentUrl().equals(GitHubURL.getPathForIssue(id))) {
						driver.get(GitHubURL.getPathForIssue(id));
						hidePageElements();
					}
				} catch (WebDriverException e) {
					// Chrome was closed; recreate it
					driver = setupChromeDriver();
					return call(); // Recurse and repeat
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
	
	/**
	 * Ensures that the chromedriver executable is in the project root before
	 * initialisation. Since executables are packaged for all platforms, this also
	 * picks the right version to use.
	 */
	private static void setupChromeDriverExecutable() {
		
		String osName = System.getProperty("os.name");
		String binaryFileName =
				osName.startsWith("Mac OS") ? "chromedriver"
				: osName.startsWith("Windows") ? "chromedriver.exe"
				: "chromedriver_linux";
		
		File f = new File(binaryFileName);
		if(!f.exists()) {
			InputStream in = BrowserComponent.class.getClassLoader().getResourceAsStream("ui/issuepanel/expanded/" + binaryFileName);
			OutputStream out;
			try {
				out = new FileOutputStream(binaryFileName);
				IOUtils.copy(in, out);
				out.close();
				f.setExecutable(true);
			} catch (IOException e) {
				System.out.println("Could not load Chrome driver binary!");
				e.printStackTrace();
			}
			System.out.println("Could not find " + binaryFileName + "; extracted it from jar");
		} else {
			System.out.println("Located " + binaryFileName);
		}
		
		System.setProperty("webdriver.chrome.driver", binaryFileName);
	}
}
