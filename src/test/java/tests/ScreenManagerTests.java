package tests;

import browserview.BrowserComponent;
import javafx.scene.layout.Region;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import ui.ScreenManager;

import java.awt.Rectangle;

import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScreenManagerTests {

    /**
     * Tests whether the bView is placed next to the pView when it is at default size.
     */
    @Test
    public void resizeBview_MinimumPViewSize_AdjacentBView() {
        ScreenManager mockedScreenManager = getMockedScreenManager(200.0, 1600, 1400, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(null, mockedScreenManager, false);
        testBrowserComponent.setWindowBounds(mockedManage);

        verify(mockedWindow).setPosition(new Point(200, 0));
        verify(mockedWindow).setSize(new Dimension(1400, 2400));
    }

    /**
     * Tests whether the bView is placed next to the pView when the pView is almost at the resize
     * threshold.
     */
    @Test
    public void resizeBview_MediumPViewSize_AdjacentBView() {
        ScreenManager mockedScreenManager = getMockedScreenManager(1439.0, 1600, 161, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(null, mockedScreenManager, false);
        testBrowserComponent.setWindowBounds(mockedManage);

        verify(mockedWindow).setPosition(new Point(1439, 0));
        verify(mockedWindow).setSize(new Dimension(161, 2400));
    }

    /**
     * Tests whether the bView is maximised when the pView's size is past the resize threshold.
     */
    @Test
    public void resizeBview_OversizedPView_MaximiseBView() {
        ScreenManager mockedScreenManager = getMockedScreenManager(1440.0, 1600, 160, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(null, mockedScreenManager, false);
        testBrowserComponent.setWindowBounds(mockedManage);

        verify(mockedWindow).setPosition(new Point(0, 0));
        verify(mockedWindow).setSize(new Dimension(1600, 2400));
    }

    /**
     * Tests whether the bView is maximised when the pView takes up the entire screen.
     */
    @Test
    public void resizeBview_FullscreenPView_MaximisedBView() {
        ScreenManager mockedScreenManager = getMockedScreenManager(1600.0, 1600, 0, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(null, mockedScreenManager, false);
        testBrowserComponent.setWindowBounds(mockedManage);

        verify(mockedWindow).setPosition(new Point(0, 0));
        verify(mockedWindow).setSize(new Dimension(1600, 2400));
    }

    /**
     * Tests whether the bView snaps to the correct position when pView is out of position.
     */
    @Test
    public void resizeBview_OutOfPositionPView_SnappedBView() {
        ScreenManager mockedScreenManager = getMockedScreenManager(800.0, 1600, 400, 2400);
        WebDriver.Window mockedWindow = mock(WebDriver.Window.class);
        WebDriver.Options mockedManage = getMockedOptions(mockedWindow);

        BrowserComponent testBrowserComponent = new BrowserComponent(null, mockedScreenManager, false);
        testBrowserComponent.setWindowBounds(mockedManage);

        verify(mockedWindow).setPosition(new Point(800, 0));
        verify(mockedWindow).setSize(new Dimension(400, 2400));
    }

    /**
     * Tests whether the bounds returned by UI.getStageBounds contain the Region.USE_COMPUTED_SIZE constant as
     * the value of the maxWidth, minHeight and maxHeight fields when the list of screen bounds is empty.
     */
    @Test
    public void getPviewBounds_NoIntersectingScreens_EmptyBounds() {
        List<Rectangle2D> emptyScreenBoundsList = new ArrayList<>();
        Region emptyBounds = ScreenManager.getStageBounds(emptyScreenBoundsList);

        assertEquals(Region.USE_COMPUTED_SIZE, emptyBounds.getMaxWidth(), 0.0);
        assertEquals(Region.USE_COMPUTED_SIZE, emptyBounds.getMaxHeight(), 0.0);
        assertEquals(Region.USE_COMPUTED_SIZE, emptyBounds.getMinHeight(), 0.0);
    }

    /**
     * Tests whether the bounds returned by UI.getStageBounds contain the same values as the single screen's
     * bounds passed as argument.
     */
    @Test
    public void getPviewBounds_OneIntersectingScreen_SameBounds() {
        Rectangle2D screenBound = new Rectangle2D(0, 0, 2400, 1600);
        List<Rectangle2D> screenBoundsList = new ArrayList<>();
        screenBoundsList.add(screenBound);

        Region stageBounds = ScreenManager.getStageBounds(screenBoundsList);

        assertEquals(screenBound.getWidth(), stageBounds.getMaxWidth(), 0.0);
        assertEquals(screenBound.getHeight(), stageBounds.getMaxHeight(), 0.0);
        assertEquals(screenBound.getHeight(), stageBounds.getMinHeight(), 0.0);
    }

    /**
     * Tests whether the bounds returned by UI.getStageBounds contain the following values when passed
     * multiple screens:
     * - minHeight: minimum height among screen bounds
     * - maxHeight: maximum height among screen bounds
     * - maxWidth: total width of screen bounds
     */
    @Test
    public void getPviewBounds_MultipleIntersectingScreens_StretchedBounds() {
        Rectangle2D bigScreenBound = new Rectangle2D(0, 0, 2400, 1600);
        Rectangle2D mediumScreenBound = new Rectangle2D(0, 0, 1440, 900);
        Rectangle2D smallScreenBound = new Rectangle2D(0, 0, 1024, 768);
        List<Rectangle2D> screenBoundsList = new ArrayList<>();
        screenBoundsList.add(bigScreenBound);
        screenBoundsList.add(mediumScreenBound);
        screenBoundsList.add(smallScreenBound);

        Region stageBounds = ScreenManager.getStageBounds(screenBoundsList);

        assertEquals(bigScreenBound.getWidth() + mediumScreenBound.getWidth() + smallScreenBound.getWidth(),
                     stageBounds.getMaxWidth(), 0.0);
        assertEquals(bigScreenBound.getHeight(), stageBounds.getMaxHeight(), 0.0);
        assertEquals(smallScreenBound.getHeight(), stageBounds.getMinHeight(), 0.0);
    }

    @Test(timeout = 5000)
    public void testGettingLookAndFeelOnLinux() {
        assertTrue(ScreenManager.getUsableScreenDimensions().isPresent());
        assertTrue(ScreenManager.getScreenDimensions() != null);
    }

    /**
     * Produces a mocked ScreenManager object that responds to getDimensions and getAvailableDimensions based
     * on information provided as parameters.
     *
     * @param pViewX         The x-coordinate of the top right corner of the pView window.
     * @param maxWidth       The width of the screen.
     * @param availableWidth The width of the space on the right of the pView window.
     * @param height         The height of the screen.
     * @return The mocked ScreenManager object.
     */
    private static ScreenManager getMockedScreenManager(double pViewX, int maxWidth, int availableWidth, int height) {
        ScreenManager mockedScreenManager = mock(ScreenManager.class);
        when(mockedScreenManager.getAvailableDimensions()).thenReturn(new Rectangle(availableWidth, height));
        when(mockedScreenManager.getCollapsedX()).thenReturn(pViewX);
        when(mockedScreenManager.getDimensions()).thenReturn(new Rectangle(0, 0, maxWidth, height));
        when(mockedScreenManager.getBrowserComponentBounds()).thenCallRealMethod();
        return mockedScreenManager;
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
