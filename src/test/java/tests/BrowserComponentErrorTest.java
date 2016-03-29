package tests;

import browserview.BrowserComponentError;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrowserComponentErrorTest {

    @Test
    public void browserComponentErrorTest() {
        assertEquals(BrowserComponentError.NoSuchWindow,
                     BrowserComponentError.fromErrorMessage("no such window"));
        assertEquals(BrowserComponentError.NoSuchWindow,
                     BrowserComponentError.fromErrorMessage("chrome not reachable"));
        assertEquals(BrowserComponentError.NoSuchElement,
                     BrowserComponentError.fromErrorMessage("no such element"));
        assertEquals(BrowserComponentError.UnexpectedAlert,
                     BrowserComponentError.fromErrorMessage("unexpected alert open"));
        assertEquals(BrowserComponentError.Unknown,
                     BrowserComponentError.fromErrorMessage("unknown"));
    }

}
