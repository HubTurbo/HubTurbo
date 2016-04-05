package browserview;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ui.UI;
import util.GitHubURL;
import util.events.testevents.ExecuteScriptEvent;
import util.events.testevents.NavigateToPageEvent;

import java.util.NoSuchElementException;

// acts as a middleman for ChromeDriver when not in testing mode,
// and as a stub when testing

public class ChromeDriverEx {

    private static final Logger logger = LogManager.getLogger(ChromeDriverEx.class.getName());

    private final boolean isTestChromeDriver;
    private ChromeDriver driver;

    ChromeDriverEx(ChromeOptions options, boolean isTestChromeDriver) {
        this.isTestChromeDriver = isTestChromeDriver;
        initialise(options);
    }

    private void initialise(ChromeOptions options) {
        if (!isTestChromeDriver) driver = new ChromeDriver(options);
    }

    public WebDriver.Options manage() {
        return !isTestChromeDriver ? driver.manage() : null;
    }

    public void quit() {
        if (!isTestChromeDriver) driver.quit();
    }

    public void get(String url, boolean isForceRefresh) throws WebDriverException {
        if (isTestChromeDriver) {
            if (!url.equalsIgnoreCase(GitHubURL.LOGIN_PAGE)) {
                UI.events.triggerEvent(new NavigateToPageEvent(url));
            }
            logger.info("Test loading page: " + url);
            testGet();
        } else {
            if (!isForceRefresh && driver.getCurrentUrl().equalsIgnoreCase(url)) {
                logger.info("Already on page: " + url + " will not load it again. ");
            } else {
                logger.info("Previous page was: " + driver.getCurrentUrl());
                logger.info("Loading page: " + url);
                driver.get(url);
            }
        }
    }

    public void testGet() throws WebDriverException {
        double chance = Math.random();
        if (chance < 0.25) {
            throw new WebDriverException("no such window");
        } else if (chance < 0.5) {
            throw new WebDriverException("no such element");
        } else if (chance < 0.75) {
            throw new WebDriverException("unexpected alert open");
        }
    }

    public String getCurrentUrl() {
        return !isTestChromeDriver ? driver.getCurrentUrl() :
                "https://github.com/HubTurbo/HubTurbo/issues/1";
    }

    public WebElement findElementById(String id) throws NoSuchElementException {
        if (!isTestChromeDriver) return driver.findElementById(id);
        throw new NoSuchElementException();
    }

    public WebElement findElementByTagName(String tag) throws NoSuchElementException {
        if (!isTestChromeDriver) return driver.findElementByTagName(tag);
        throw new NoSuchElementException();
    }

    public WebDriver.TargetLocator switchTo() throws WebDriverException {
        if (!isTestChromeDriver) {
            return driver.switchTo();
        } else {
            // ~25% chance to throw an exception which is used to test resetBrowser
            if (Math.random() < 0.25) {
                throw new WebDriverException();
            }
        }
        return null;
    }

    public String getWindowHandle() {
        return !isTestChromeDriver ? driver.getWindowHandle() : "";
    }

    public WebElement findElement(By by) throws NoSuchElementException {
        if (!isTestChromeDriver) return driver.findElement(by);
        throw new NoSuchElementException();
    }

    public Object executeScript(String script) {
        if (isTestChromeDriver) {
            UI.events.triggerEvent(new ExecuteScriptEvent(script));
        }
        return !isTestChromeDriver ? driver.executeScript(script) : "";
    }
}
