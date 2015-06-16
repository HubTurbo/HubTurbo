package ui.components;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import prefs.Preferences;

import java.util.HashMap;
import java.util.Map;

/**
 * a central place to specify keyboard shortcuts
 *
 * Classes that currently have keyboard shortcut code:
 * ui.components.NavigableListView
 * ui.issuecolumn.ColumnControl
 * ui.issuepanel.IssuePanel
 * ui.MenuControl
 *
 * Utility Class:
 * util.KeyPress
 */
public class KeyboardShortcuts {

    private static Map<String, String> keyboardShortcuts = null;
    private static Preferences prefs;

    // customizable keyboard shortcuts
    // ui.issuepanel.IssuePanel
    public static KeyCode MARK_AS_READ;
    public static KeyCode MARK_AS_UNREAD;

    // non-customizable keyboard shortcuts
    // ui.issuepanel.IssuePanel
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

    public static final KeyCode SCROLL_TO_TOP = KeyCode.U;
    public static final KeyCode SCROLL_TO_BOTTOM = KeyCode.N;
    public static final KeyCode SCROLL_UP = KeyCode.J;
    public static final KeyCode SCROLL_DOWN = KeyCode.K;

    // TODO decouple manage/show labels/milestones?
    public static final KeyCode NEW_COMMENT = KeyCode.C;
    public static final KeyCode MANAGE_LABELS = KeyCode.L;
    public static final KeyCode MANAGE_ASSIGNEES = KeyCode.A;
    public static final KeyCode MANAGE_MILESTONE = KeyCode.M;

    public static final KeyCode DOUBLE_PRESS = KeyCode.SPACE;

    //ui.issuecolumn.ColumnControl
    public static final KeyCode LEFT_PANEL = KeyCode.D;
    public static final KeyCode RIGHT_PANEL = KeyCode.F;

    // ui.components.NavigableListView && ui.issuepanel.IssuePanel
    public static final KeyCode UP_ISSUE = KeyCode.T;
    public static final KeyCode DOWN_ISSUE = KeyCode.V;

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

    public static void loadKeyboardShortcuts(Preferences prefs) {
        KeyboardShortcuts.prefs = prefs;
        if (prefs.getKeyboardShortcuts().size() < getDefaultKeyboardShortcuts().size()) {
            prefs.setKeyboardShortcuts(getDefaultKeyboardShortcuts());
        } else {
            KeyboardShortcuts.keyboardShortcuts = prefs.getKeyboardShortcuts();
            MARK_AS_READ = KeyCode.getKeyCode(keyboardShortcuts.get("MARK_AS_READ"));
            MARK_AS_UNREAD = KeyCode.getKeyCode(keyboardShortcuts.get("MARK_AS_UNREAD"));
        }
    }

    public static Map<String, String> getDefaultKeyboardShortcuts() {
        Map<String, String> defaultKeyboardShortcuts = new HashMap<>();
        defaultKeyboardShortcuts.put("MARK_AS_READ", "E");
        defaultKeyboardShortcuts.put("MARK_AS_UNREAD", "U");
        return defaultKeyboardShortcuts;
    }
}
