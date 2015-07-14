package ui.components;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import util.DialogMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * a central place to specify keyboard shortcuts
 *
 * Classes that currently have keyboard shortcut code:
 * ui.components.NavigableListView
 * ui.issuepanel.PanelControl
 * ui.listpanel.ListPanel
 * ui.MenuControl
 *
 * Utility Class:
 * util.KeyPress
 */
public class KeyboardShortcuts {

    private static final Logger logger = LogManager.getLogger(KeyboardShortcuts.class.getName());

    private static Map<String, String> keyboardShortcuts = null;
    private static Set<KeyCode> assignedKeys = null;

    // customizable keyboard shortcuts
    // ui.listpanel.ListPanel
    public static KeyCode MARK_AS_READ;
    public static KeyCode MARK_AS_UNREAD;

    public static KeyCode SCROLL_TO_TOP;
    public static KeyCode SCROLL_TO_BOTTOM;
    public static KeyCode SCROLL_UP;
    public static KeyCode SCROLL_DOWN;

    //ui.issuepanel.PanelControl
    public static KeyCode LEFT_PANEL;
    public static KeyCode RIGHT_PANEL;

    // ui.components.NavigableListView && ui.listpanel.ListPanel
    public static KeyCode UP_ISSUE;
    public static KeyCode DOWN_ISSUE;

    // non-customizable keyboard shortcuts
    // ui.listpanel.ListPanel
    public static final KeyCombination BOX_TO_LIST =
            new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination LIST_TO_BOX =
            new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination MAXIMIZE_WINDOW =
            new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination MINIMIZE_WINDOW =
            new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination DEFAULT_SIZE_WINDOW =
            new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination SWITCH_DEFAULT_REPO =
            new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);

    public static final KeyCode FIRST_ISSUE = KeyCode.HOME;
    public static final KeyCode LAST_ISSUE = KeyCode.END;

    public static final KeyCode REFRESH = KeyCode.F5;
    public static final KeyCode SHOW_DOCS = KeyCode.F1;

    public static final KeyCode GOTO_MODIFIER = KeyCode.G;
    public static final KeyCode SHOW_LABELS = KeyCode.L;
    public static final KeyCode SHOW_ISSUES = KeyCode.I;
    public static final KeyCode SHOW_MILESTONES = KeyCode.M;
    public static final KeyCode SHOW_PULL_REQUESTS = KeyCode.P;
    public static final KeyCode SHOW_HELP = KeyCode.H;
    public static final KeyCode SHOW_KEYBOARD_SHORTCUTS = KeyCode.K;
    public static final KeyCode SHOW_CONTRIBUTORS = KeyCode.D;

    // TODO decouple manage/show labels/milestones?
    public static final KeyCode NEW_COMMENT = KeyCode.C;
    public static final KeyCode MANAGE_LABELS = KeyCode.L;
    public static final KeyCode MANAGE_ASSIGNEES = KeyCode.A;
    public static final KeyCode MANAGE_MILESTONE = KeyCode.M;

    public static final KeyCode DOUBLE_PRESS = KeyCode.SPACE;

    // ui.MenuControl
    public static final KeyCombination NEW_ISSUE =
            new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination NEW_LABEL =
            new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination NEW_MILESTONE =
            new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);

    public static final KeyCombination CREATE_LEFT_PANEL =
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCombination CREATE_RIGHT_PANEL =
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination CLOSE_PANEL =
            new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);

    public static Map<String, String> getDefaultKeyboardShortcuts() {
        Map<String, String> defaultKeyboardShortcuts = new HashMap<>();
        defaultKeyboardShortcuts.put("MARK_AS_READ", "E");
        defaultKeyboardShortcuts.put("MARK_AS_UNREAD", "U");
        defaultKeyboardShortcuts.put("SCROLL_TO_TOP", "I");
        defaultKeyboardShortcuts.put("SCROLL_TO_BOTTOM", "N");
        defaultKeyboardShortcuts.put("SCROLL_UP", "J");
        defaultKeyboardShortcuts.put("SCROLL_DOWN", "K");
        defaultKeyboardShortcuts.put("LEFT_PANEL", "D");
        defaultKeyboardShortcuts.put("RIGHT_PANEL", "F");
        defaultKeyboardShortcuts.put("UP_ISSUE", "T");
        defaultKeyboardShortcuts.put("DOWN_ISSUE", "V");
        return defaultKeyboardShortcuts;
    }

    private static void addNonCustomizableShortcutKeys() {
        assignedKeys.add(KeyCode.F5); //REFRESH
        assignedKeys.add(KeyCode.F1); //SHOW_DOCS
        assignedKeys.add(KeyCode.G); //GOTO_MODIFIER
        assignedKeys.add(KeyCode.C); //NEW_COMMENT
        assignedKeys.add(KeyCode.A); //MANAGE_ASSIGNEES
        assignedKeys.add(KeyCode.SPACE); //DOUBLE_PRESS
    }

    private static void getKeyboardShortcutsFromHashMap() {
        MARK_AS_READ = getKeyCode("MARK_AS_READ");
        MARK_AS_UNREAD = getKeyCode("MARK_AS_UNREAD");
        SCROLL_TO_TOP = getKeyCode("SCROLL_TO_TOP");
        SCROLL_TO_BOTTOM = getKeyCode("SCROLL_TO_BOTTOM");
        SCROLL_UP = getKeyCode("SCROLL_UP");
        SCROLL_DOWN = getKeyCode("SCROLL_DOWN");
        LEFT_PANEL = getKeyCode("LEFT_PANEL");
        RIGHT_PANEL = getKeyCode("RIGHT_PANEL");
        UP_ISSUE = getKeyCode("UP_ISSUE");
        DOWN_ISSUE = getKeyCode("DOWN_ISSUE");
    }

    public static void loadKeyboardShortcuts(Preferences prefs) {
        assignedKeys = new HashSet<>();
        if (prefs.getKeyboardShortcuts().size() == 0) {
            logger.info("No user specified keyboard shortcuts found, using defaults. ");
            prefs.setKeyboardShortcuts(getDefaultKeyboardShortcuts());
        }
        if (prefs.getKeyboardShortcuts().size() != getDefaultKeyboardShortcuts().size()) {
            logger.warn("Invalid number of user specified keyboard shortcuts detected. ");
            if (DialogMessage.showYesNoWarningDialog(
                    "Warning",
                    "Invalid number of shortcut keys specified",
                    "Do you want to reset the shortcut keys to their defaults or quit?",
                    "Reset to default",
                    "Quit")) {
                keyboardShortcuts = getDefaultKeyboardShortcuts();
            } else {
                Platform.exit();
                System.exit(0);
            }
        } else {
            logger.info("Loading user specified keyboard shortcuts. ");
            keyboardShortcuts = prefs.getKeyboardShortcuts();
        }
        addNonCustomizableShortcutKeys();
        getKeyboardShortcutsFromHashMap();
        prefs.setKeyboardShortcuts(keyboardShortcuts);
    }

    private static KeyCode getKeyCode(String keyboardShortcut) {
        KeyCode keyCode = KeyCode.getKeyCode(getDefaultKeyboardShortcuts().get(keyboardShortcut));
        if (keyboardShortcuts.containsKey(keyboardShortcut)) {
            KeyCode userDefinedKeyCode = KeyCode.getKeyCode(keyboardShortcuts.get(keyboardShortcut).toUpperCase());
            if (userDefinedKeyCode != null && !assignedKeys.contains(userDefinedKeyCode)) {
                keyCode = userDefinedKeyCode;
            } else {
                logger.warn("Invalid key specified for " + keyboardShortcut +
                        " or it has already been used for some other shortcut. ");
                if (DialogMessage.showYesNoWarningDialog(
                        "Warning",
                        "Invalid key specified for " + keyboardShortcut +
                                " or it has already been used for some other shortcut. ",
                        "Do you want to use the default key <" +
                                getDefaultKeyboardShortcuts().get(keyboardShortcut) + "> or quit?",
                        "Use default key",
                        "Quit")) {
                    keyboardShortcuts.put(keyboardShortcut, getDefaultKeyboardShortcuts().get(keyboardShortcut));
                } else {
                    Platform.exit();
                    System.exit(0);
                }
            }
        } else {
            logger.warn("Could not find user defined keyboard shortcut for " + keyboardShortcut);
            if (DialogMessage.showYesNoWarningDialog(
                    "Warning",
                    "Could not find user defined keyboard shortcut for " + keyboardShortcut,
                    "Do you want to use the default key <" +
                            getDefaultKeyboardShortcuts().get(keyboardShortcut) + "> or quit?",
                    "Use default key",
                    "Quit")) {
                keyboardShortcuts.put(keyboardShortcut, getDefaultKeyboardShortcuts().get(keyboardShortcut));
            } else {
                Platform.exit();
                System.exit(0);
            }
        }
        logger.info("Assigning <" + keyCode + "> to " + keyboardShortcut);
        assignedKeys.add(keyCode);
        return keyCode;
    }

}
