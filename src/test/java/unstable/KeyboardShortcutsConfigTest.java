package unstable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.loadui.testfx.controls.Commons.hasText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import guitests.UITest;
import javafx.application.Platform;
import prefs.Preferences;
import ui.TestController;
import ui.components.KeyboardShortcuts;

public class KeyboardShortcutsConfigTest extends UITest {

    private Map<String, String> keyboardShortcuts;
    private Preferences testPref;

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(UITest.TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void invalidNumberOfKeyboardShortcuts() {
        testPref = TestController.createTestPreferences();
        keyboardShortcuts = new HashMap<>();
        keyboardShortcuts.put("MARK_AS_READ", "E");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        reloadPrefs(testPref);
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        waitUntilNodeAppears(hasText("Invalid number of shortcut keys specified"));
        click("Reset to default");
        reloadPrefs(testPref);
        assertEquals(KeyboardShortcuts.getDefaultKeyboardShortcuts().size(), testPref.getKeyboardShortcuts().size());

        testPref = TestController.createTestPreferences();
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.put("BLAH", "Z");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        reloadPrefs(testPref);
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        waitUntilNodeAppears(hasText("Invalid number of shortcut keys specified"));
        click("Reset to default");
        reloadPrefs(testPref);
        assertEquals(KeyboardShortcuts.getDefaultKeyboardShortcuts().size(), testPref.getKeyboardShortcuts().size());
    }

    @Test
    public void invalidKeySpecified() {
        testPref = TestController.createTestPreferences();
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.remove("MARK_AS_READ");
        keyboardShortcuts.put("MARK_AS_READ", "eee");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        reloadPrefs(testPref);
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        waitUntilNodeAppears(hasText("Invalid key specified for MARK_AS_READ" +
            " or it has already been used for some other shortcut. "));
        click("Use default key");
        reloadPrefs(testPref);
        assertEquals(KeyboardShortcuts.getDefaultKeyboardShortcuts().get("MARK_AS_READ"),
            testPref.getKeyboardShortcuts().get("MARK_AS_READ"));
    }

    @Test
    public void noKeySpecified() {
        testPref = TestController.createTestPreferences();
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.remove("MARK_AS_READ");
        keyboardShortcuts.put("BLAH", "Z");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        reloadPrefs(testPref);
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        waitUntilNodeAppears(hasText("Could not find user defined keyboard shortcut for MARK_AS_READ"));
        click("Use default key");
        reloadPrefs(testPref);
        assertEquals(KeyboardShortcuts.getDefaultKeyboardShortcuts().get("MARK_AS_READ"),
            testPref.getKeyboardShortcuts().get("MARK_AS_READ"));
    }

    @Test
    public void repeatedKeySpecified() {
        testPref = TestController.createTestPreferences();
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.remove("MARK_AS_UNREAD");
        keyboardShortcuts.put("MARK_AS_UNREAD", "E");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        reloadPrefs(testPref);
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        waitUntilNodeAppears(hasText("Invalid key specified for MARK_AS_UNREAD" +
            " or it has already been used for some other shortcut. "));
        click("Use default key");
        reloadPrefs(testPref);
        assertEquals(KeyboardShortcuts.getDefaultKeyboardShortcuts().get("MARK_AS_UNREAD"),
            testPref.getKeyboardShortcuts().get("MARK_AS_UNREAD"));
    }

    private static void reloadPrefs(Preferences prefs) {
        prefs.saveGlobalConfig();
        String loadGlobalConfig = "loadGlobalConfig";
        try {
            Method method = Preferences.class.getDeclaredMethod(loadGlobalConfig);
            method.setAccessible(true);
            method.invoke(prefs);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(String.format("Problem invoking private method; does %s still exist?", loadGlobalConfig));
        }
    }
}
