package guitests;

import static com.google.common.io.Files.getFileExtension;

import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.loadui.testfx.FXScreenController;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.exceptions.NoNodesVisibleException;
import org.loadui.testfx.utils.FXTestUtils;

import com.google.common.util.concurrent.SettableFuture;

import backend.interfaces.RepoStore;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import prefs.Preferences;
import ui.UI;
import util.PlatformEx;
import util.PlatformSpecific;

public class UITest extends GuiTest {

    private static final Logger logger = LogManager.getLogger(UITest.class.getName());
    protected static final SettableFuture<Stage> STAGE_FUTURE = SettableFuture.create();
    private static final Map<Character, KeyCode> specialCharsMap = getSpecialCharsMap();

    private final Robot robot;
    private final FXScreenController screenController;

    protected static class TestUI extends UI {
        public TestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            STAGE_FUTURE.set(primaryStage);
        }
    }

    public UITest() {
        super();
        screenController = getScreenController();
        robot = getRobot();
    }

    private static Map<Character, KeyCode> getSpecialCharsMap() {
        Map<Character, KeyCode> specialChars = new HashMap<Character, KeyCode>();
        specialChars.put('~', KeyCode.BACK_QUOTE);
        specialChars.put('!', KeyCode.DIGIT1);
        specialChars.put('@', KeyCode.DIGIT2);
        specialChars.put('#', KeyCode.DIGIT3);
        specialChars.put('$', KeyCode.DIGIT4);
        specialChars.put('%', KeyCode.DIGIT5);
        specialChars.put('^', KeyCode.DIGIT6);
        specialChars.put('&', KeyCode.DIGIT7);
        specialChars.put('*', KeyCode.DIGIT8);
        specialChars.put('(', KeyCode.DIGIT9);
        specialChars.put(')', KeyCode.DIGIT0);
        specialChars.put('_', KeyCode.MINUS);
        specialChars.put('+', KeyCode.EQUALS);
        specialChars.put('{', KeyCode.OPEN_BRACKET);
        specialChars.put('}', KeyCode.CLOSE_BRACKET);
        specialChars.put(':', KeyCode.SEMICOLON);
        specialChars.put('"', KeyCode.QUOTE);
        specialChars.put('<', KeyCode.COMMA);
        specialChars.put('>', KeyCode.PERIOD);
        specialChars.put('?', KeyCode.SLASH);
        return Collections.unmodifiableMap(specialChars);
    }

    public void beforeStageStarts() {
        // method to be overridden if anything needs to be done (e.g. to the json) before the stage starts
    }

    public static void clearTestFolder() {
        try {
            if (Files.exists(Paths.get(RepoStore.TEST_DIRECTORY))) {
                Files.walk(Paths.get(RepoStore.TEST_DIRECTORY), 1)
                        .filter(Files::isRegularFile)
                        .filter(p ->
                                getFileExtension(String.valueOf(p.getFileName())).equalsIgnoreCase("json") ||
                                getFileExtension(String.valueOf(p.getFileName())).equalsIgnoreCase("json-err")
                        )
                        .forEach(p -> new File(p.toAbsolutePath().toString()).delete());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    @Override
    public void setupStage() throws Throwable {
        // delete test.json if it exists
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        if (testConfig.exists() && testConfig.isFile()) {
            assert testConfig.delete();
        }
        clearTestFolder();
        beforeStageStarts();

        if (stage == null) {
            launchApp();
        }
        try {
            stage = targetWindow(STAGE_FUTURE.get(25, TimeUnit.SECONDS));
            FXTestUtils.bringToFront(stage);
        } catch (Exception e) {
            throw new RuntimeException("Unable to show stage", e);
        }
    }

    // override this to change launch arguments
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--bypasslogin=true");
    }

    @Override
    protected Parent getRootNode() {
        return stage.getScene().getRoot();
    }

    private FXScreenController getScreenController() {
        try {
            return (FXScreenController) FieldUtils.readField(this, "controller", true);
        } catch (IllegalAccessException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    private Robot getRobot() {
        try {
            return (Robot) FieldUtils.readField(screenController, "robot", true);
        } catch (IllegalAccessException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private int getCode(KeyCode key) {
        return key.impl_getCode();
    }

    private void pressNoWait(KeyCode key) {
        robot.keyPress(getCode(key));
    }

    private void releaseNoWait(KeyCode key) {
        robot.keyRelease(getCode(key));
    }

    private List<KeyCode> getKeyCodes(KeyCodeCombination combination) {
        List<KeyCode> keys = new ArrayList<>();
        if (combination.getAlt() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.ALT);
        }
        if (combination.getShift() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.SHIFT);
        }
        if (combination.getMeta() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.META);
        }
        if (combination.getControl() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.CONTROL);
        }
        if (combination.getShortcut() == KeyCombination.ModifierValue.DOWN) {
            // Fix bug with internal method not having a proper code for SHORTCUT.
            // Dispatch manually based on platform.
            if (PlatformSpecific.isOnMac()) {
                keys.add(KeyCode.META);
            } else {
                keys.add(KeyCode.CONTROL);
            }
        }
        keys.add(combination.getCode());
        return keys;
    }

    public void push(KeyCode keyCode, int times) {
        assert times > 0;
        for (int i = 0; i < times; i++) {
            push(keyCode);
        }
    }

    public void pushKeys(KeyCodeCombination combination) {
        pushKeys(getKeyCodes(combination));
    }

    public void pushKeys(KeyCode... keys) {
        pushKeys(Arrays.asList(keys));
    }

    private void pushKeys(List<KeyCode> keys) {
        keys.forEach(this::pressNoWait);
        for (int i = keys.size() - 1; i >= 0; i--) {
            releaseNoWait(keys.get(i));
        }
        PlatformEx.waitOnFxThread();
    }

    public void press(KeyCodeCombination combination) {
        press(getKeyCodes(combination));
    }

    private void press(List<KeyCode> keys) {
        keys.forEach(this::press);
        for (int i = keys.size() - 1; i >= 0; i--) {
            release(keys.get(i));
        }
        PlatformEx.waitOnFxThread();
    }

    public void waitUntilNodeAppears(Node node) {
        waitUntil(node, n -> n.isVisible() && n.getParent() != null);
    }

    public void waitUntilNodeDisappears(Node node) {
        waitUntil(node, n -> !n.isVisible() || n.getParent() == null);
    }

    public void waitUntilNodeAppears(String selector) {
        while (!existsQuiet(selector)) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public void waitUntilNodeDisappears(String selector) {
        while (existsQuiet(selector)) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public void waitUntilNodeAppears(Matcher<Object> matcher) {
        while (!findQuiet(matcher).isPresent()) { // no `exists` for matcher, so using find
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public void waitUntilNodeDisappears(Matcher<Object> matcher) {
        while (findQuiet(matcher).isPresent()) { // no `exists` for matcher, so using find
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public <T extends Node> void waitUntil(String selector, Predicate<T> condition) {
        while (!condition.test(find(selector))) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public <T extends Node> T findOrWaitFor(String selector) {
        waitUntilNodeAppears(selector);
        return find(selector);
    }

    public <T extends Node> Optional<T> findQuiet(String selectorOrText) {
        try {
            return Optional.of(find(selectorOrText));
        } catch (NoNodesFoundException | NoNodesVisibleException e) {
            return Optional.empty();
        }
    }

    private <T extends Node> Optional<T> findQuiet(Matcher<Object> matcher) {
        try {
            return Optional.of(find(matcher));
        } catch (NoNodesFoundException | NoNodesVisibleException e) {
            return Optional.empty();
        }

    }

    public boolean existsQuiet(String selector) {
        try {
            return exists(selector);
        } catch (NoNodesFoundException | NoNodesVisibleException e) {
            return false;
        }
    }

    /**
     * Like drag(from).to(to), but does not relocate the mouse if the target moves.
     */
    public void dragUnconditionally(Node from, Node to) {
        Point2D start = pointFor(from);
        Point2D end = pointFor(to);

        move(start.getX(), start.getY());
        press(MouseButton.PRIMARY);
        move(end.getX(), end.getY());
        release(MouseButton.PRIMARY);
    }

    /**
     * Allows test threads to busy-wait on some condition.
     *
     * Taken from org.loadui.testfx.utils, but modified to synchronise with
     * the JavaFX Application Thread, with a lower frequency.
     *
     * The additional synchronisation prevents bugs where
     *
     * awaitCondition(a);
     * awaitCondition(b);
     *
     * sometimes may not be equivalent to
     *
     * awaitCondition(a && b);
     *
     * The lower frequency is a bit more efficient, since a frequency of 10 ms
     * just isn't necessary for GUI interactions, and we're bottlenecked by the FX
     * thread anyway.
     */
    public void awaitCondition(Callable<Boolean> condition) {
        awaitCondition(condition, 5);
    }

    private void awaitCondition(Callable<Boolean> condition, int timeoutInSeconds) {
        long timeout = System.currentTimeMillis() + timeoutInSeconds * 1000;
        try {
            while (!condition.call()) {
                Thread.sleep(100);
                PlatformEx.waitOnFxThread();
                if (System.currentTimeMillis() > timeout) {
                    throw new TimeoutException();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs logout from File -> Logout on HubTurbo's pView.
     */
    public void logout(){
        clickMenu("File", "Logout");
    }

    /**
     * Performs UI login on the login dialog box.
     * @param owner The owner of the repo.
     * @param repoName The repository name
     * @param username The Github username
     * @param password The Github password
     */
    public void login(String owner, String repoName, String username, String password){
        selectAll();
        type(owner).push(KeyCode.TAB);
        type(repoName).push(KeyCode.TAB);
        type(username).push(KeyCode.TAB);
        type(password);
        click("Sign in");
    }

    /**
     * Automate menu traversal by clicking them in order of input parameter
     *
     * @param menuNames array of strings of menu item names in sequence of traversal
     */
    public void clickMenu(String... menuNames) {
        for (String menuName : menuNames) {
            click(menuName);
        }
    }

    public <T> void waitForValue(ComboBoxBase<T> comboBoxBase) {
        waitUntil(comboBoxBase, c -> c.getValue() != null);
    }
    
    @Override
    public GuiTest type(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (specialCharsMap.containsKey(text.charAt(i))){
                press(KeyCode.SHIFT).press(specialCharsMap.get(text.charAt(i)))
                .release(specialCharsMap.get(text.charAt(i))).release(KeyCode.SHIFT);
             
            } else {
                type(text.charAt(i));
            }
        }
        return this;
    }

    /**
     * Used to select the whole filter text so that it can be replaced
     */
    public void selectAll() {
        pushKeys(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN));
    }
}
