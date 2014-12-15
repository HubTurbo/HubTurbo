package ui.issuepanel.expanded;

import java.awt.Rectangle;

import javafx.concurrent.Task;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import ui.UI;

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
	private SeleniumTask task;
	
	public void toggle() {
		boolean expanded = ui.toggleExpandedWidth();

		if (!expanded) {
			SeleniumTask task = new SeleniumTask();
			Thread seleniumThread = new Thread(task);
			seleniumThread.setDaemon(true);
			seleniumThread.start();
		} else {
//			seleniumThread.interrupt();
			task.stop();
		}
	}

	private class SeleniumTask extends Task<Boolean> {
		
		private WebDriver driver;
		
		@Override
		protected Boolean call() throws Exception {
			driver = new ChromeDriver();

			driver.manage().window()
					.setPosition(new Point((int) ui.getCollapsedX(), 0));
			Rectangle availableDimensions = ui.getAvailableDimensions();
			driver.manage()
					.window()
					.setSize(
							new Dimension((int) availableDimensions.getWidth(),
									(int) availableDimensions.getHeight()));

			driver.get("http://www.google.com/xhtml");
//			try {
//				// Let the user actually see something!
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				return false;
//			}
			WebElement searchBox = driver.findElement(By.name("q"));
			searchBox.sendKeys("ChromeDriver");
			System.out.println("sent keys");
			searchBox.submit();

//			try {
//				// Let the user actually see something!
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				return false;
//			}

//			driver.quit();
			System.out.println("returning");
			return true;
		}
		
		public void stop() {
			System.out.println("stopping " + driver);
			driver.quit();
			System.out.println("quit");
		}
	}
}
