package tests;

import browserview.BrowserComponent;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import ui.UI;

import java.awt.Rectangle;

import static org.mockito.Mockito.*;

public class BrowserComponentResizeTest {

    /**
     * Tests whether the bView is placed next to the pView when it is at default size.
     */
    @Test
    public void resizeBrowserComponentDefaultpViewTest() {
        UI mockedUI = getMockedUI(200.0, 1600, 1400, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(mockedUI, false);
        testBrowserComponent.setWindowSize(mockedManage);

        verify(mockedWindow).setPosition(new Point(200, 0));
        verify(mockedWindow).setSize(new Dimension(1400, 2400));
    }

    /**
     * Tests whether the bView is placed next to the pView when the pView is almost at the resize
     * threshold.
     */
    @Test
    public void resizeBrowserComponentMediumpViewTest() {
        UI mockedUI = getMockedUI(1439.0, 1600, 161, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(mockedUI, false);
        testBrowserComponent.setWindowSize(mockedManage);

        verify(mockedWindow).setPosition(new Point(1439, 0));
        verify(mockedWindow).setSize(new Dimension(161, 2400));
    }

    /**
     * Tests whether the bView is maximised when the pView's size is past the resize threshold.
     */
    @Test
    public void resizeBrowserComponentOversizedpViewTest() {
        UI mockedUI = getMockedUI(1440.0, 1600, 160, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(mockedUI, false);
        testBrowserComponent.setWindowSize(mockedManage);

        verify(mockedWindow).setPosition(new Point(0, 0));
        verify(mockedWindow).setSize(new Dimension(1600, 2400));
    }

    /**
     * Tests whether the bView is maximised when the pView takes up the entire screen.
     */
    @Test
    public void resizeBrowserComponentFullscreenpViewTest() {
        UI mockedUI = getMockedUI(1600.0, 1600, 0, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(mockedUI, false);
        testBrowserComponent.setWindowSize(mockedManage);

        verify(mockedWindow).setPosition(new Point(0, 0));
        verify(mockedWindow).setSize(new Dimension(1600, 2400));
    }

    /**
     * Produces a mocked UI object that responds to getDimensions and getAvailableDimensions based
     * on information provided as parameters.
     *
     * @param pViewX The x-coordinate of the top right corner of the pView window.
     * @param maxWidth The width of the screen.
     * @param availableWidth The width of the space on the right of the pView window.
     * @param height The height of the screen.
     * @return The mocked UI object.
     */
    private static UI getMockedUI(double pViewX, int maxWidth, int availableWidth, int height) {
        UI mockedUI = mock(UI.class);
        when(mockedUI.getAvailableDimensions()).thenReturn(new Rectangle(availableWidth, height));
        when(mockedUI.getCollapsedX()).thenReturn(pViewX);
        when(mockedUI.getDimensions()).thenReturn(new Rectangle(0, 0, maxWidth, height));
        return mockedUI;
    }

    /**
     * Prepares the mocked options object that returns the window object passed in as argument.
     *
     * @param mockedWindow The window object, whose sizes will be set by BrowserComponent.setWindowSize.
     * @return The options object encapsulating the window object to be modified.
     */
    private static WebDriver.Options getMockedOptions(WebDriver.Window mockedWindow) {
        WebDriver.Options mockedManage = mock(WebDriver.Options.class);
        when(mockedManage.window()).thenReturn(mockedWindow);
        return mockedManage;
    }

}
