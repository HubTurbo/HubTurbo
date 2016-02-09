package ui.listpanel;

import static ui.components.KeyboardShortcuts.*;
import static util.GithubPageElements.DISCUSSION_TAB;
import static util.GithubPageElements.COMMITS_TAB;
import static util.GithubPageElements.FILES_TAB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;

import org.apache.logging.log4j.Logger;

import ui.GUIController;
import ui.GuiElement;
import ui.UI;
import ui.components.IssueListView;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.GithubPageElements;
import util.HTLog;
import util.KeyPress;
import util.events.IssueSelectedEvent;
import util.events.ShowIssueCreatorEvent;
import util.events.ShowLabelPickerEvent;
import backend.resource.TurboIssue;
import filter.expression.Qualifier;

public class ListPanel extends FilterPanel {

    private static final Logger logger = HTLog.get(ListPanel.class);

    private final UI ui;
    private final GUIController guiController;
    private int issueCount;

    private final IssueListView listView;
    private final HashMap<Integer, Integer> issueCommentCounts = new HashMap<>();
    private final HashMap<Integer, Integer> issueNonSelfCommentCounts = new HashMap<>();

    // Context Menu
    private final ContextMenu contextMenu = new ContextMenu();

    private final MenuItem markAsReadUnreadMenuItem = new MenuItem();
    private static final String markAsReadMenuItemText = "Mark as read (E)";
    private static final String markAsUnreadMenuItemText = "Mark as unread (U)";

    private static final MenuItem changeLabelsMenuItem = new MenuItem();
    private static final String changeLabelsMenuItemText = "Change labels (L)";

    public ListPanel(UI ui, GUIController guiController, PanelControl parentPanelControl, int panelIndex) {
        super(ui, guiController, parentPanelControl, panelIndex);
        this.ui = ui;
        this.guiController = guiController;

        listView = new IssueListView();
        setupListView();
        getChildren().add(listView);
    }

    /**
     * Determines if an issue has had new comments added (or removed) based on
     * its last-known comment count in {@link #issueCommentCounts}.
     * @param issue
     * @return true if the issue has changed, false otherwise
     */

    private boolean issueHasNewComments(TurboIssue issue, boolean hasMetadata) {
        if (hasMetadata && Qualifier.hasUpdatedQualifier(currentFilterExpression)) {
            return issueNonSelfCommentCounts.containsKey(issue.getId()) &&
                    Math.abs(
                            issueNonSelfCommentCounts.get(issue.getId()) - issue.getMetadata().getNonSelfCommentCount()
                    ) > 0;
        } else {
            return issueCommentCounts.containsKey(issue.getId()) &&
                    Math.abs(issueCommentCounts.get(issue.getId()) - issue.getCommentCount()) > 0;
        }
    }

    /**
     * Updates {@link #issueCommentCounts} with the latest counts.
     * Returns a list of issues which have new comments.
     * @return
     */
    private HashSet<Integer> updateIssueCommentCounts(boolean hasMetadata) {
        HashSet<Integer> result = new HashSet<>();
        for (GuiElement guiElement : getElementsList()) {
            TurboIssue issue = guiElement.getIssue();
            if (issueCommentCounts.containsKey(issue.getId())) {
                // We know about this issue; check if it's been updated
                if (issueHasNewComments(issue, hasMetadata)) {
                    result.add(issue.getId());
                }
            } else {
                // We don't know about this issue, just put the current comment count.
                issueNonSelfCommentCounts.put(issue.getId(), issue.getMetadata().getNonSelfCommentCount());
                issueCommentCounts.put(issue.getId(), issue.getCommentCount());
            }
        }
        return result;
    }

    /**
     * Refreshes the list of issue cards shown to the user depending on the currently active filter expression
     * in the panel.
     */
    @Override
    public final void refreshItems() {
        final HashSet<Integer> issuesWithNewComments
                = updateIssueCommentCounts(Qualifier.hasUpdatedQualifier(getCurrentFilterExpression()));

        // Set the cell factory every time - this forces the list view to update
        listView.setCellFactory(list -> new ListPanelCell(this, panelIndex, issuesWithNewComments));
        listView.saveSelection();

        // Supposedly this also causes the list view to update - not sure
        // if it actually does on platforms other than Linux...
        listView.setItems(null);
        listView.setItems(getElementsList());
        issueCount = getElementsList().size();

        listView.restoreSelection();
        this.setId(guiController.getDefaultRepo() + "_col" + panelIndex);
    }

    private void setupListView() {
        setVgrow(listView, Priority.ALWAYS);
        setupKeyboardShortcuts();
        setupContextMenu();

        listView.setOnItemSelected(i -> {
            updateContextMenu(contextMenu);

            TurboIssue issue = listView.getItems().get(i).getIssue();
            ui.triggerEvent(
                    new IssueSelectedEvent(issue.getRepoId(), issue.getId(), panelIndex, issue.isPullRequest())
            );

            // Save the stored comment count as its own comment count.
            // The refreshItems(false) call that follows will remove the highlighted effect of the comment bubble.
            // (if it was there before)
            issueCommentCounts.put(issue.getId(), issue.getCommentCount());
            issueNonSelfCommentCounts.put(issue.getId(), issue.getMetadata().getNonSelfCommentCount());

            refreshItems();
        });
    }

    private void setupKeyboardShortcuts() {        
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // Temporary fix for now since markAsRead and Show Related Issue/PR have same keys.
            // Will only work if the key for markAsRead is not the default key E.
            if (KeyboardShortcuts.markAsRead.match(event) && !SHOW_RELATED_ISSUE_OR_PR.match(event)) {
                markAsRead();
            }
            if (KeyboardShortcuts.markAsUnread.match(event)) {
                markAsUnread();
            }
            if (SHOW_DOCS.match(event)) {
                ui.getBrowserComponent().showDocs();
            }
            if (SHOW_ISSUES.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().showIssues();
            }
            if (SHOW_PULL_REQUESTS.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().showPullRequests();
            }
            if (SHOW_HELP.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().showDocs();
            }
            if (SHOW_KEYBOARD_SHORTCUTS.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().showKeyboardShortcuts();
            }
            if (SHOW_CONTRIBUTORS.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().showContributors();
                event.consume();
            }
            if (KeyboardShortcuts.scrollToTop.match(event)) {
                ui.getBrowserComponent().scrollToTop();
            }
            if (KeyboardShortcuts.scrollToBottom.match(event)) {
                ui.getBrowserComponent().scrollToBottom();
            }
            if (KeyboardShortcuts.scrollUp.match(event) || KeyboardShortcuts.scrollDown.match(event)) {
                ui.getBrowserComponent().scrollPage(KeyboardShortcuts.scrollDown.match(event));
            }
            if (GOTO_MODIFIER.match(event)) {
                KeyPress.setLastKeyPressedCodeAndTime(event.getCode());
            }
            if (NEW_COMMENT.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().switchToTab(DISCUSSION_TAB);
                } else if (ui.getBrowserComponent().isCurrentUrlIssue()) {
                    ui.getBrowserComponent().switchToTab(DISCUSSION_TAB);
                    ui.getBrowserComponent().jumpToComment();
                }
            }
            if (PR_FILES_CHANGED.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().switchToTab(FILES_TAB);
                event.consume();
            }
            if (PR_COMMITS.match(event)
                && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {

                ui.getBrowserComponent().switchToTab(COMMITS_TAB);
                event.consume();
            }
            if (SHOW_LABELS.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().newLabel();
                } else {
                    changeLabels();
                }
            }
            if (SHOW_ISSUE_CREATOR.match(event)) {
                createIssue(Optional.empty());
            }
            if (SHOW_ISSUES.match(event)) {
                if (!getSelectedElement().isPresent()) {
                    createIssue(Optional.empty());
                } else {
                    createIssue(Optional.of(getSelectedElement().get().getIssue()));
                }
            }
            if (MANAGE_ASSIGNEES.match(event) && ui.getBrowserComponent().isCurrentUrlIssue()) {
                ui.getBrowserComponent().switchToTab(DISCUSSION_TAB);
                ui.getBrowserComponent().manageAssignees(event.getCode().toString());
            }
            if (SHOW_MILESTONES.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showMilestones();
                } else if (ui.getBrowserComponent().isCurrentUrlIssue()) {
                    ui.getBrowserComponent().switchToTab(DISCUSSION_TAB);
                    ui.getBrowserComponent().manageMilestones(event.getCode().toString());
                }
            }
            if (UNDO_LABEL_CHANGES.match(event)) {
                ui.triggerNotificationAction();
            }
            if (JUMP_TO_FIRST_ISSUE.match(event)) {
                listView.selectNthItem(1);
            }
            for (Map.Entry<Integer, KeyCodeCombination> entry : JUMP_TO_NTH_ISSUE_KEYS.entrySet()) {
                if (entry.getValue().match(event)){
                    event.consume();
                    listView.selectNthItem(entry.getKey());
                    break;
                }
            }
            if (SHOW_RELATED_ISSUE_OR_PR.match(event) && ui.getBrowserComponent().isCurrentUrlIssue()) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    showRelatedIssueOrPR();
                // only for default. can remove if default key for MARK_AS_READ_CHANGES
                } else if (KeyboardShortcuts.markAsRead.match(event)) {
                    markAsRead();
                }
            }
        });
    }

    private void showRelatedIssueOrPR() {
        if (!listView.getSelectedItem().isPresent()) return;
        TurboIssue issue = listView.getSelectedItem().get().getIssue();
        Optional<Integer> relatedIssueNumber = listView.getSelectedItem().get().getIssue().isPullRequest()
            ? GithubPageElements.extractIssueNumber(listView.getSelectedItem().get().getIssue().getDescription())
            : ui.getBrowserComponent().getPRNumberFromIssue();
        if (!relatedIssueNumber.isPresent()) return;
        ui.triggerEvent(
            new IssueSelectedEvent(issue.getRepoId(), relatedIssueNumber.get(), panelIndex, issue.isPullRequest())
        );
    }

    private ContextMenu setupContextMenu() {
        markAsReadUnreadMenuItem.setOnAction(e -> {
            String menuItemText = markAsReadUnreadMenuItem.getText();

            if (menuItemText.equals(markAsReadMenuItemText)) {
                markAsRead();
            } else if (menuItemText.equals(markAsUnreadMenuItemText)) {
                markAsUnread();
            }
        });

        changeLabelsMenuItem.setText(changeLabelsMenuItemText);
        changeLabelsMenuItem.setOnAction(e -> {
            changeLabels();
        });

        contextMenu.getItems().addAll(markAsReadUnreadMenuItem, changeLabelsMenuItem);
        contextMenu.setOnShowing(e -> updateContextMenu(contextMenu));
        listView.setContextMenu(contextMenu);

        return contextMenu;
    }

    private ContextMenu updateContextMenu(ContextMenu contextMenu) {
        updateMarkAsReadUnreadMenuItem();
        updateChangeLabelsMenuItem();

        return contextMenu;
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    private MenuItem updateChangeLabelsMenuItem() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            changeLabelsMenuItem.setDisable(false);
        } else {
            changeLabelsMenuItem.setDisable(true);
        }

        return changeLabelsMenuItem;
    }

    private MenuItem updateMarkAsReadUnreadMenuItem() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            markAsReadUnreadMenuItem.setDisable(false);
            TurboIssue selectedIssue = item.get().getIssue();

            if (selectedIssue.isCurrentlyRead()) {
                markAsReadUnreadMenuItem.setText(markAsUnreadMenuItemText);
            } else {
                markAsReadUnreadMenuItem.setText(markAsReadMenuItemText);
            }
        } else {
            markAsReadUnreadMenuItem.setDisable(true);
        }

        return markAsReadUnreadMenuItem;
    }

    public int getIssueCount() {
        return issueCount;
    }

    public Optional<GuiElement> getSelectedElement() {
        return listView.getSelectedItem();
    }

    /* Methods that perform user's actions under the context of this ListPanel */

    private void markAsRead() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            TurboIssue issue = item.get().getIssue();
            issue.markAsRead(UI.prefs);

            parentPanelControl.refresh();
            listView.selectNextItem();
        }
    }

    private void markAsUnread() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            TurboIssue issue = item.get().getIssue();
            issue.markAsUnread(ui.prefs);

            parentPanelControl.refresh();
        }
    }

    private void changeLabels() {
        if (getSelectedElement().isPresent()) {
            ui.triggerEvent(new ShowLabelPickerEvent(getSelectedElement().get().getIssue()));
        }
    }

    /**
     * Adds a style class to the listview which changes its background to contain a loading spinning gif
     */
    @Override
    protected void addPanelLoadingIndication() {
        logger.info("Preparing to add panel loading indication");
        listView.getStyleClass().add("listview-loading");

        // Remove the items in the issue list because they are not relevant to the filter anymore.
        // This also makes the listview background visible, since we intend to show a loading indicator on the
        // background.
        listView.setItems(null);
    }

    /**
     * Removes the style class that was added in addPanelLoadingIndicator() from the listview.
     */
    @Override
    protected void removePanelLoadingIndication() {
        logger.info("Preparing to remove panel loading indication");
        listView.getStyleClass().removeIf(cssClass -> cssClass.equals("listview-loading"));
    }

    private void createIssue(Optional<TurboIssue> issue) {
        ui.triggerEvent(new ShowIssueCreatorEvent(issue));
    }
}
