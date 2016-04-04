package ui.components;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

/**
 * This class handles the keyboard shortcuts used by the different UI components
 */
public final class KeyboardShortcuts {
    // ui.listpanel.ListPanel
    public static final KeyCodeCombination MARK_AS_READ =
            new KeyCodeCombination(KeyCode.E);
    public static final KeyCodeCombination MARK_AS_UNREAD =
            new KeyCodeCombination(KeyCode.U);

    public static final KeyCodeCombination CLOSE_ISSUE =
            new KeyCodeCombination(KeyCode.X);
    public static final KeyCodeCombination REOPEN_ISSUE =
            new KeyCodeCombination(KeyCode.O);

    public static final KeyCodeCombination SCROLL_TO_TOP =
            new KeyCodeCombination(KeyCode.I);
    public static final KeyCodeCombination SCROLL_TO_BOTTOM =
            new KeyCodeCombination(KeyCode.N);
    public static final KeyCodeCombination SCROLL_UP =
            new KeyCodeCombination(KeyCode.J);
    public static final KeyCodeCombination SCROLL_DOWN =
            new KeyCodeCombination(KeyCode.K);

    //ui.issuepanel.PanelControl
    public static final KeyCodeCombination LEFT_PANEL =
            new KeyCodeCombination(KeyCode.D);
    public static final KeyCodeCombination RIGHT_PANEL =
            new KeyCodeCombination(KeyCode.F);

    // ui.components.NavigableListView
    public static final KeyCodeCombination UP_ISSUE =
            new KeyCodeCombination(KeyCode.T);
    public static final KeyCodeCombination DOWN_ISSUE =
            new KeyCodeCombination(KeyCode.V);

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
    public static final KeyCodeCombination SHOW_ISSUES =
            new KeyCodeCombination(KeyCode.I);
    public static final KeyCodeCombination SHOW_ISSUE_PICKER =
            new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN);
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
    public static final KeyCodeCombination SHOW_MILESTONES =
            new KeyCodeCombination(KeyCode.M);
    public static final KeyCodeCombination SHOW_ASSIGNEES =
            new KeyCodeCombination(KeyCode.A);
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

    //ui.RepositorySelector
    public static final KeyCodeCombination REMOVE_FOCUS =
            new KeyCodeCombination(KeyCode.ESCAPE);
    public static final KeyCodeCombination SHOW_REPO_PICKER =
            new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

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

    private KeyboardShortcuts() {
    }

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
}
