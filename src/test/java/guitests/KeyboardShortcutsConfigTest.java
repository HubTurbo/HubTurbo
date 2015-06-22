package guitests;

import javafx.application.Platform;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.components.KeyboardShortcuts;

import java.util.HashMap;
import java.util.Map;

import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

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
        testPref = new Preferences(true);
        keyboardShortcuts = new HashMap<>();
        keyboardShortcuts.put("MARK_AS_READ", "E");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        testPref.saveGlobalConfig();
        testPref.loadGlobalConfig();
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        sleep(1000);
        assertNodeExists(hasText("Invalid number of shortcut keys specified"));
        click("Reset to default");

        testPref = new Preferences(true);
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.put("BLAH", "Z");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        testPref.saveGlobalConfig();
        testPref.loadGlobalConfig();
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        sleep(1000);
        assertNodeExists(hasText("Invalid number of shortcut keys specified"));
        click("Reset to default");
    }

    @Test
    public void invalidKeySpecified() {
        testPref = new Preferences(true);
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.remove("MARK_AS_READ");
        keyboardShortcuts.put("MARK_AS_READ", "eee");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        testPref.saveGlobalConfig();
        testPref.loadGlobalConfig();
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        sleep(1000);
        assertNodeExists(hasText("Invalid key specified for MARK_AS_READ" +
                " or it has already been used for some other shortcut. "));
        click("Use default key");
    }

    @Test
    public void noKeySpecified() {
        testPref = new Preferences(true);
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.remove("MARK_AS_READ");
        keyboardShortcuts.put("BLAH", "Z");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        testPref.saveGlobalConfig();
        testPref.loadGlobalConfig();
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        sleep(1000);
        assertNodeExists(hasText("Could not find user defined keyboard shortcut for MARK_AS_READ"));
        click("Use default key");
    }

    @Test
    public void repeatedKeySpecified() {
        testPref = new Preferences(true);
        keyboardShortcuts = new HashMap<>(KeyboardShortcuts.getDefaultKeyboardShortcuts());
        keyboardShortcuts.remove("MARK_AS_UNREAD");
        keyboardShortcuts.put("MARK_AS_UNREAD", "E");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        testPref.saveGlobalConfig();
        testPref.loadGlobalConfig();
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        sleep(1000);
        assertNodeExists(hasText("Invalid key specified for MARK_AS_UNREAD" +
                " or it has already been used for some other shortcut. "));
        click("Use default key");
    }

}
