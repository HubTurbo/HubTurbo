package browserview;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.NoSuchElementException;

// acts as a middleman for ChromeDriver when not in testing mode,
// and as a stub when testing

public class ChromeDriverEx {

    private boolean isTestChromeDriver;
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

    public void get(String url) {
        if (!isTestChromeDriver) driver.get(url);
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
            // ~50% chance to throw an exception which is used to test resetBrowser
            if (Math.random() < 0.5) {
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
        return !isTestChromeDriver ? driver.executeScript(script) : "";
    }
}
