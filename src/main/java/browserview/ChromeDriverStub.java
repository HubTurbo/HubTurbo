package browserview;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverStub extends ChromeDriver{

    public ChromeDriverStub(ChromeOptions options) {
        super();
    }

    @Override
    public void quit() {}

    @Override
    public void get(String url) {}
}
