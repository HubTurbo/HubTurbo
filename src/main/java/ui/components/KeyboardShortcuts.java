package ui.components;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * a central place to specify keyboard shortcuts
 *
 * Classes that currently have keyboard shortcut code:
 * ui.components.NavigableListView
 * ui.issuecolumn.ColumnControl
 * ui.issuepanel.IssuePanel
 *
 * Utility Class:
 * util.KeyPress
 */
public class KeyboardShortcuts {

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

    public static final KeyCode MARK_AS_READ = KeyCode.R;
    public static final KeyCode MARK_AS_UNREAD = KeyCode.U;

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

    public static final KeyCode PREVIOUS_ISSUE = KeyCode.T;
    public static final KeyCode NEXT_ISSUE = KeyCode.V;

    public static final KeyCode DOUBLE_PRESS = KeyCode.SPACE;
}