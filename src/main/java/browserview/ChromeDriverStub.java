package browserview;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverStub extends ChromeDriver{

    public ChromeDriverStub(ChromeOptions options) {
        super();
    }

    @Override
    public void get(String url) {}

    @Override
    public String getCurrentUrl() {
        return "https://github.com/HubTurbo/HubTurbo/issues/1";
    }

}
