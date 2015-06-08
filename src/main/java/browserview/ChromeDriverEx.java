package browserview;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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
        if(!isTestChromeDriver) driver = new ChromeDriver(options);
    }

    public WebDriver.Options manage() {
        return !isTestChromeDriver ? driver.manage() : null;
    }

    public void quit() {
        if(!isTestChromeDriver) driver.quit();
    }

    public void get(String url) {
        if (!isTestChromeDriver) driver.get(url);
    }

    public String getCurrentUrl() {
        return !isTestChromeDriver ? driver.getCurrentUrl() :
                "https://github.com/HubTurbo/HubTurbo/issues/1";
    }

    public WebElement findElementById(String id) {
        return !isTestChromeDriver ? driver.findElementById(id) : null;
    }

    public WebElement findElementByTagName(String tag) {
        return !isTestChromeDriver ? driver.findElementByTagName(tag) : null;
    }

    public WebDriver.TargetLocator switchTo() {
        return !isTestChromeDriver ? driver.switchTo() : null;
    }

    public String getWindowHandle() {
        return !isTestChromeDriver ? driver.getWindowHandle() : "";
    }

    public WebElement findElement(By by) {
        return !isTestChromeDriver ? driver.findElement(by) : null;
    }

    public Object executeScript(String script) {
        return !isTestChromeDriver ? driver.executeScript(script) : null;
    }
}
