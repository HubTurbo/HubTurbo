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


}