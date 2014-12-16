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

public class IssueCommentsDisplay {

	private static final boolean USE_MOBILE_USER_AGENT = true;
	
	private static String HIDE_ELEMENTS_SCRIPT_PATH = USE_MOBILE_USER_AGENT
			? "ui/issuepanel/expanded/hideUI.js"
			: "ui/issuepanel/expanded/mobileHideUI.js";
	
	// Chrome, Android 4.2.2, Samsung Galaxy S4
	private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";

	private UI ui;

	static {
		System.setProperty("webdriver.chrome.driver",
				"/Users/darius/Downloads/chromedriver");
	}

	public IssueCommentsDisplay(UI ui) {
		this.ui = ui;
	}
	
//	private Thread seleniumThread;
//	private SeleniumTask task;
	
	public void toggle() {
		boolean expanded = ui.toggleExpandedWidth();

		if (!expanded) {
			SeleniumTask task = new SeleniumTask();
//			Thread seleniumThread = new Thread(task);
//			seleniumThread.setDaemon(true);
//			seleniumThread.start();
			try {
				task.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
//			seleniumThread.interrupt();
//			task.stop();
		}
	}

	private class SeleniumTask extends Task<Boolean> {
		
		private WebDriver driver;
		
		@Override
		protected Boolean call() throws Exception {
			ChromeOptions options = new ChromeOptions();
			options.addArguments(String.format("user-agent=\"%s\"", MOBILE_USER_AGENT));

			driver = new ChromeDriver(options);

			driver.manage().window()
					.setPosition(new Point((int) ui.getCollapsedX(), 0));
			Rectangle availableDimensions = ui.getAvailableDimensions();
			driver.manage()
					.window()
					.setSize(
							new Dimension((int) availableDimensions.getWidth(),
									(int) availableDimensions.getHeight()));

			driver.get(GitHubURL.LOGIN_PAGE);
//			driver.getCurrentUrl()
//			driver.close(); // what do?
			
			try {
				WebElement searchBox = driver.findElement(By.name("login"));
				searchBox.sendKeys(ServiceManager.getInstance().getUserId());
				searchBox = driver.findElement(By.name("password"));
				searchBox.sendKeys(ServiceManager.getInstance().getPassword());
				searchBox.submit();
			} catch (NoSuchElementException e) {
				// Already logged in; do nothing
			}

			driver.get(GitHubURL.getPathForIssue(1));

			if (driver instanceof JavascriptExecutor) {
				Optional<String> file = IOUtilities.readResource(HIDE_ELEMENTS_SCRIPT_PATH);
				if (file.isPresent()) {
					((JavascriptExecutor) driver).executeScript(file.get());
				} else {
					System.out.println("Failed to read script for hiding elements; did not execute");
				}
			}
			
			return true;
		}

	}
}
