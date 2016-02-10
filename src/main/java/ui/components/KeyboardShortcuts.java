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
import java.util.Collections;
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
public final class KeyboardShortcuts {

    private static final Logger logger = LogManager.getLogger(KeyboardShortcuts.class.getName());

    private static Map<String, String> keyboardShortcuts = null;
    private static Set<KeyCodeCombination> assignedKeys = null;

    // customizable keyboard shortcuts
    // ui.listpanel.ListPanel
    public static KeyCodeCombination markAsRead;
    public static KeyCodeCombination markAsUnread;

    public static KeyCodeCombination closeIssue;
    public static KeyCodeCombination reopenIssue;

    public static KeyCodeCombination scrollToTop;
    public static KeyCodeCombination scrollToBottom;
    public static KeyCodeCombination scrollUp;
    public static KeyCodeCombination scrollDown;

    //ui.issuepanel.PanelControl
    public static KeyCodeCombination leftPanel;
    public static KeyCodeCombination rightPanel;

    // ui.components.NavigableListView
    static KeyCodeCombination upIssue;
    static KeyCodeCombination downIssue;

    // non-customizable keyboard shortcuts
    // ui.listpanel.ListPanel
    public static final KeyCodeCombination JUMP_TO_FIRST_ISSUE =
            new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination JUMP_TO_FILTER_BOX =
            new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination MAXIMIZE_WINDOW =
            new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination MINIMIZE_WINDOW =
            new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination DEFAULT_SIZE_WINDOW =
            new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination SWITCH_DEFAULT_REPO =
            new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination SWITCH_BOARD = 
            new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination UNDO_LABEL_CHANGES =
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);

    public static final KeyCodeCombination FIRST_ISSUE =
            new KeyCodeCombination(KeyCode.HOME);
    public static final KeyCodeCombination LAST_ISSUE =
            new KeyCodeCombination(KeyCode.END);

    public static final KeyCodeCombination REFRESH =
            new KeyCodeCombination(KeyCode.F5);
    public static final KeyCodeCombination SHOW_DOCS =
            new KeyCodeCombination(KeyCode.F1);

    public static final KeyCodeCombination GOTO_MODIFIER =
            new KeyCodeCombination(KeyCode.G);
    public static final KeyCodeCombination SHOW_LABELS =
            new KeyCodeCombination(KeyCode.L);
    public static final KeyCodeCombination SHOW_ISSUES =
            new KeyCodeCombination(KeyCode.I);
    public static final KeyCodeCombination SHOW_MILESTONES =
            new KeyCodeCombination(KeyCode.M);
    public static final KeyCodeCombination SHOW_PULL_REQUESTS =
            new KeyCodeCombination(KeyCode.P);
    public static final KeyCodeCombination SHOW_HELP =
            new KeyCodeCombination(KeyCode.H);
    public static final KeyCodeCombination SHOW_KEYBOARD_SHORTCUTS =
            new KeyCodeCombination(KeyCode.K);
    public static final KeyCodeCombination SHOW_CONTRIBUTORS =
            new KeyCodeCombination(KeyCode.D);
    public static final KeyCodeCombination SHOW_RELATED_ISSUE_OR_PR =
            new KeyCodeCombination(KeyCode.E);

    public static final Map<Integer, KeyCodeCombination> JUMP_TO_NTH_ISSUE_KEYS = populateJumpToNthIssueMap();

    public static final KeyCodeCombination PR_FILES_CHANGED =
            new KeyCodeCombination(KeyCode.F);
    public static final KeyCodeCombination PR_COMMITS =
            new KeyCodeCombination(KeyCode.C);
    public static final KeyCodeCombination NEW_COMMENT =
            new KeyCodeCombination(KeyCode.R);

    // TODO decouple manage/show labels/milestones?
    public static final KeyCodeCombination MANAGE_LABELS =
        new KeyCodeCombination(KeyCode.L);
    public static final KeyCodeCombination MANAGE_ASSIGNEES =
        new KeyCodeCombination(KeyCode.A);
    public static final KeyCodeCombination MANAGE_MILESTONE =
        new KeyCodeCombination(KeyCode.M);

    //ui.RepositorySelector
    public static final KeyCodeCombination REMOVE_FOCUS =
            new KeyCodeCombination(KeyCode.ESCAPE);

    // ui.MenuControl
    public static final KeyCodeCombination NEW_ISSUE =
            new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination NEW_LABEL =
            new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination NEW_MILESTONE =
            new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    public static final KeyCodeCombination CREATE_LEFT_PANEL =
            new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination CREATE_RIGHT_PANEL =
            new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCodeCombination CLOSE_PANEL =
            new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
    
    public static final String GLOBAL_HOTKEY = "control alt H";

    private KeyboardShortcuts() {}

    private static Map<Integer, KeyCodeCombination> populateJumpToNthIssueMap() {
        Map<Integer, KeyCodeCombination> result = new HashMap<>();
        result.put(1, new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN));
        result.put(2, new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN));
        result.put(3, new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN));
        result.put(4, new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN));
        result.put(5, new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN));
        result.put(6, new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN));
        result.put(7, new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.SHORTCUT_DOWN));
        result.put(8, new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.SHORTCUT_DOWN));
        result.put(9, new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.SHORTCUT_DOWN));
        return Collections.unmodifiableMap(result);
    }
    
    public static Map<String, String> getDefaultKeyboardShortcuts() {
        Map<String, String> defaultKeyboardShortcuts = new HashMap<>();
        defaultKeyboardShortcuts.put("MARK_AS_READ", "E");
        defaultKeyboardShortcuts.put("MARK_AS_UNREAD", "U");
        defaultKeyboardShortcuts.put("CLOSE_ISSUE", "C");
        defaultKeyboardShortcuts.put("REOPEN_ISSUE", "O");
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
        assignedKeys.add(REFRESH);
        assignedKeys.add(SHOW_DOCS);
        assignedKeys.add(GOTO_MODIFIER);
        assignedKeys.add(NEW_COMMENT);
        assignedKeys.add(MANAGE_ASSIGNEES);
    }

    private static void getKeyboardShortcutsFromHashMap() {
        markAsRead = getKeyCodeCombination("MARK_AS_READ");
        markAsUnread = getKeyCodeCombination("MARK_AS_UNREAD");
        closeIssue = getKeyCodeCombination("CLOSE_ISSUE");
        reopenIssue = getKeyCodeCombination("REOPEN_ISSUE");
        scrollToTop = getKeyCodeCombination("SCROLL_TO_TOP");
        scrollToBottom = getKeyCodeCombination("SCROLL_TO_BOTTOM");
        scrollUp = getKeyCodeCombination("SCROLL_UP");
        scrollDown = getKeyCodeCombination("SCROLL_DOWN");
        leftPanel = getKeyCodeCombination("LEFT_PANEL");
        rightPanel = getKeyCodeCombination("RIGHT_PANEL");
        upIssue = getKeyCodeCombination("UP_ISSUE");
        downIssue = getKeyCodeCombination("DOWN_ISSUE");
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

    private static KeyCodeCombination getKeyCodeCombination(String keyboardShortcut) {
        KeyCodeCombination keyCodeCombi = 
                new KeyCodeCombination(KeyCode.getKeyCode(getDefaultKeyboardShortcuts().get(keyboardShortcut))); 
        if (keyboardShortcuts.containsKey(keyboardShortcut)) {
            KeyCode keyCode = KeyCode.getKeyCode(keyboardShortcuts.get(keyboardShortcut).toUpperCase());
            if (keyCode != null && !assignedKeys.contains(new KeyCodeCombination(keyCode))) {
                keyCodeCombi = new KeyCodeCombination(keyCode);
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
        logger.info("Assigning <" + keyCodeCombi + "> to " + keyboardShortcut);
        assignedKeys.add(keyCodeCombi);
        return keyCodeCombi;
    }

}
