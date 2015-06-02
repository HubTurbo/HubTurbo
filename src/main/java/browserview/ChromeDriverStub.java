package browserview;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

public class ChromeDriverStub extends ChromeDriver{

    public ChromeDriverStub(ChromeOptions options) {
        super();
    }

    @Override
    public void quit() {}

    @Override
    public void get(String url) {}

    @Override
    public WebElement findElement(By by) {
        return new WebElement() {
            @Override
            public void click() {

            }

            @Override
            public void submit() {

            }

            @Override
            public void sendKeys(CharSequence... keysToSend) {

            }

            @Override
            public void clear() {

            }

            @Override
            public String getTagName() {
                return null;
            }

            @Override
            public String getAttribute(String name) {
                return null;
            }

            @Override
            public boolean isSelected() {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public String getText() {
                return null;
            }

            @Override
            public List<WebElement> findElements(By by) {
                return null;
            }

            @Override
            public WebElement findElement(By by) {
                return null;
            }

            @Override
            public boolean isDisplayed() {
                return false;
            }

            @Override
            public Point getLocation() {
                return null;
            }

            @Override
            public Dimension getSize() {
                return null;
            }

            @Override
            public String getCssValue(String propertyName) {
                return null;
            }
        };
    }
}
