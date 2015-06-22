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

public class InvalidKeyboardShortcutsConfigTest extends UITest {

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(UITest.TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void invalidKeyboardShortcutsConfigTest() {
        Preferences testPref = new Preferences(true);
        Map<String, String> keyboardShortcuts = new HashMap<>();
        keyboardShortcuts.put("MARK_AS_READ", "E");
        testPref.setKeyboardShortcuts(keyboardShortcuts);
        testPref.saveGlobalConfig();
        testPref.loadGlobalConfig();
        Platform.runLater(() -> KeyboardShortcuts.loadKeyboardShortcuts(testPref));
        sleep(1000);
        assertNodeExists(hasText("Invalid number of shortcut keys specified"));
    }

}
