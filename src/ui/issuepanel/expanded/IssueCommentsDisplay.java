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

import ui.UI;
import util.IOUtilities;

public class IssueCommentsDisplay {

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
			options.addArguments("user-agent=\"Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36\"");

			driver = new ChromeDriver(options);

			driver.manage().window()
					.setPosition(new Point((int) ui.getCollapsedX(), 0));
			Rectangle availableDimensions = ui.getAvailableDimensions();
			driver.manage()
					.window()
					.setSize(
							new Dimension((int) availableDimensions.getWidth(),
									(int) availableDimensions.getHeight()));

			driver.get("https://github.com/login");
			
			try {
				WebElement searchBox = driver.findElement(By.name("login"));
				// username
				searchBox = driver.findElement(By.name("password"));
				// password
				searchBox.submit();
			} catch (NoSuchElementException e) {
				// Do nothing
			}

			driver.get("https://github.com/hubturbo/hubturbo/issues/1");

			if (driver instanceof JavascriptExecutor) {
				Optional<String> file = IOUtilities.readResource("ui/issuepanel/expanded/hideUI.js");
				if (file.isPresent()) {
					((JavascriptExecutor) driver).executeScript(file.get());
				} else {
					System.out.println("Failed to read hideUI.js; did not execute");
				}
			}
			
			return true;
		}
	}
}
