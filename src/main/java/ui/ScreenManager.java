package ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Region;
import javafx.stage.*;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.PlatformSpecific;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles interfacing with the graphics environment to get screen-related data, such as screen width/size.
 * At the same time, this class also carries out computations to resize the browser component based on the
 * main program window's size and position onscreen.
 */
public class ScreenManager {

    private static final Logger logger = LogManager.getLogger(ScreenManager.class.getName());

    /**
     * If available width on screen (from getAvailableDimensions) is less than or equal to (total
     * screen width * maximiseThreshold), maximise browser window instead of placing it next to the
     * program window.
     */
    private static final double maximiseThreshold = 0.1;

    private final Window mainStage;

    public ScreenManager(Window mainStage) {
        this.mainStage = mainStage;
    }

    /**
     * Calculates initial stage dimensions using graphics environment calculations, and then
     * attempts to set stage dimensions using screen information.
     *
     * @param stage The stage whose bounds will be set.
     */
    public void setupStageDimensions(Stage stage, double minWidth) {
        // Reasonable defaults.
        Rectangle dimensions = getDimensions();
        stage.setMinWidth(minWidth);
        stage.setMaxWidth(dimensions.getWidth());
        stage.setMinHeight(dimensions.getHeight());
        stage.setMaxHeight(dimensions.getHeight());
        stage.setX(0);
        stage.setY(0);
        // Then calculate sizes using screen information.
        setBoundsFromScreen(stage);
    }

    /**
     * Configures setBoundsFromScreen to trigger whenever the x-coordinate of the given stage changes.
     *
     * @param stage The stage which contains the x-coordinate to listen.
     */
    public void setupPositionListener(Stage stage) {
        stage.xProperty().addListener((observableValue, oldX, newX) -> setBoundsFromScreen(stage));
    }

    /**
     * Searches for all screens that the given stage is in, and then sets the following bounds for
     * the stage's dimensions:
     * - Minimum height: Minimum height of the screens
     * - Maximum height: Maximum height of the screens
     * - Maximum width: Total width of the screens
     *
     * @param stage The stage whose bounds are to be set.
     */
    private static void setBoundsFromScreen(Stage stage) {
        List<Rectangle2D> intersectingScreenBounds = Screen.getScreensForRectangle(
                stage.getX(),
                stage.getY(),
                stage.getWidth(),
                stage.getHeight()
        ).stream().map(Screen::getVisualBounds).collect(Collectors.toList());

        if (!intersectingScreenBounds.isEmpty()) {
            Region stageBounds = getStageBounds(intersectingScreenBounds);
            stage.setMinHeight(stageBounds.getMinHeight());
            stage.setMaxHeight(stageBounds.getMaxHeight());
            stage.setMaxWidth(stageBounds.getMaxWidth());
        }
    }

    /**
     * Calculates a stage's boundaries based on the screen information given as argument. If the list of screens
     * is empty, then the boundaries will have the default value of Region.USE_COMPUTED_SIZE (-1.0).
     *
     * @param intersectingScreenBounds A list of screens, which should be non-empty
     * @return A Region object containing relevant information in the maxHeight, minHeight and maxWidth fields.
     */
    public static Region getStageBounds(List<Rectangle2D> intersectingScreenBounds) {
        DoubleSummaryStatistics occupyingHeights = intersectingScreenBounds.stream()
                .mapToDouble(Rectangle2D::getHeight)
                .summaryStatistics();

        double maxHeight = occupyingHeights.getMax();
        double minHeight = occupyingHeights.getMin();
        double sumWidths = intersectingScreenBounds.stream().mapToDouble(Rectangle2D::getWidth).sum();

        Region stageBounds = new Region();
        if (maxHeight >= 0 && minHeight >= 0) {
            stageBounds.setMaxHeight(maxHeight);
            stageBounds.setMinHeight(minHeight);
            stageBounds.setMaxWidth(sumWidths);
        }

        return stageBounds;
    }

    public Rectangle getBrowserComponentBounds() {
        Rectangle availableDimensions = getAvailableDimensions();
        Rectangle screenDimensions = getDimensions();

        if (availableDimensions.getWidth() > maximiseThreshold * screenDimensions.getWidth()) {
            return new Rectangle((int) getCollapsedX(),
                                 0,
                                 (int) availableDimensions.getWidth(),
                                 (int) availableDimensions.getHeight());
        } else {
            return new Rectangle(0,
                                 0,
                                 (int) screenDimensions.getWidth(),
                                 (int) screenDimensions.getHeight());
        }
    }

    /**
     * Returns the X position of the edge of the collapsed window.
     * This function may be called before the main stage is initialised, in
     * which case it simply returns a reasonable default.
     */
    public double getCollapsedX() {
        return mainStage.getWidth() + mainStage.getX();
    }

    /**
     * Returns the maximum usable size of the stage, or to the screen size if this fails.
     */
    public Rectangle getDimensions() {
        Optional<Rectangle> dimensions = getUsableScreenDimensions();
        if (dimensions.isPresent()) {
            return dimensions.get();
        } else {
            return getScreenDimensions();
        }
    }

    /**
     * Returns the dimensions of the screen available for use when
     * the main window is in a collapsed state.
     * This function may be called before the main stage is initialised, in
     * which case it simply returns a reasonable default.
     */
    public Rectangle getAvailableDimensions() {
        Rectangle dimensions = getDimensions();

        return new Rectangle(
                (int) (dimensions.getWidth() - mainStage.getWidth() - mainStage.getX()),
                (int) dimensions.getHeight());
    }

    public static Rectangle getScreenDimensions() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle((int) screenSize.getWidth(), (int) screenSize.getHeight());
    }

    public static Optional<Rectangle> getUsableScreenDimensions() {
        try {
            if (PlatformSpecific.isOnLinux()) {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            }

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            return Optional.of(ge.getMaximumWindowBounds());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return Optional.empty();
        }
    }
}
