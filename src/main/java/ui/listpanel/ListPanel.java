package ui.listpanel;

import static ui.components.KeyboardShortcuts.*;
import util.GithubPageElements.PrTab;

import java.util.*;

import filter.expression.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import ui.GuiElement;
import ui.IdGenerator;
import ui.UI;
import ui.components.IssueListView;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import undo.actions.EditIssueStateAction;
import util.GithubPageElements;
import util.HTLog;
import util.KeyPress;
import util.events.*;
import backend.resource.TurboIssue;
import filter.expression.Qualifier;
import util.events.ShowMilestonePickerEvent;

public class ListPanel extends FilterPanel {

    private static final Logger logger = HTLog.get(ListPanel.class);

    public static final String WATCH_LIST_FILTER_REGEX = "^id:([^#]+#\\d+;)*([^#]+#\\d+)$";

    private final UI ui;
    private int issuesCount = 0;
    private int closedIssuesCount = 0;
    private int openIssuesCount = 0;

    private final Stage mainStage;

    private final IssueListView listView;
    private final HashMap<Integer, Integer> issueCommentCounts = new HashMap<>();
    private final HashMap<Integer, Integer> issueNonSelfCommentCounts = new HashMap<>();

    Text openIssueText;
    Text closedIssueText;
    Text totalIssueText;
    Text bracketOpenText;
    Text bracketCloseText;
    Text plusText;

    private static final String APPEND_TO_WATCH_LIST_FORMAT = "%s;%s#%d";
    private static final String CREATE_WATCH_LIST_FORMAT = "id:%s#%d";
    private static final String WATCH_LIST_PANEL_NAME_PROMPT_TITLE = "Watch List Panel Name";
    private static final String WATCH_LIST_PANEL_NAME_PROMPT_HEADER = "Please enter a name for the watch list panel.";
    private static final String DEFAULT_WATCH_LIST_PANEL_NAME = "New Watch List Panel";

    // Context Menu
    private final ContextMenu contextMenu = new ContextMenu();

    private final MenuItem markAsReadUnreadMenuItem = new MenuItem();
    private final MenuItem markAllBelowAsReadMenuItem = new MenuItem();
    private final MenuItem markAllBelowAsUnreadMenuItem = new MenuItem();
    private static final String MARK_AS_READ_MENU_ITEM_TEXT = "Mark as read (E)";
    private static final String MARK_AS_UNREAD_MENU_ITEM_TEXT = "Mark as unread (U)";
    public static final String MARK_ALL_AS_UNREAD_MENU_ITEM_TEXT = "Mark all below as unread";
    public static final String MARK_ALL_AS_READ_MENU_ITEM_TEXT = "Mark all below as read";
    private static final Boolean READ = true;
    private final MenuItem changeLabelsMenuItem = new MenuItem();
    private static final String CHANGE_LABELS_MENU_ITEM_TEXT = "Change labels (L)";
    private static final String ADD_TO_WATCH_LIST_MENU_TEXT = "Add to watch list";
    private final Menu addToWatchListMenu = new Menu();
    private static final String NEW_WATCH_LIST_MENU_ITEM_TEXT = "New watch list";
    private final MenuItem newWatchListMenuItem = new MenuItem();

    private static final MenuItem changeMilestoneMenuItem = new MenuItem();
    private static final String CHANGE_MILESTONE_MENU_ITEM_TEXT = "Change milestone (M)";
    private final MenuItem changeAssigneeMenuItem = new MenuItem();
    private static final String CHANGE_ASSIGNEE_MENU_ITEM_TEXT = "Change Assignee (A)";

    private final MenuItem closeReopenIssueMenuItem = new MenuItem();
    private static final String closeIssueMenuItemText = "Close issue (X)";
    private static final String reopenIssueMenuItemText = "Reopen issue (O)";

    public ListPanel(UI ui, Stage mainStage, PanelControl parentPanelControl, int panelIndex) {
        super(ui, parentPanelControl, panelIndex);
        this.ui = ui;
        this.mainStage = mainStage;

        listView = new IssueListView();
        setupListView();
        getChildren().add(listView);
        getChildren().add(createPanelFooter());
    }

    /**
     * Creates a Graphic element(HBox) containing a label that show the total no of issues in the ListPanel.
     *
     * @return HBox Instance to be added to the ListPanel.
     */
    private HBox createPanelFooter() {
        HBox bottomDisplay = new HBox();
        bottomDisplay.getChildren().add(createFooterIssueStats());
        return bottomDisplay;
    }

    private TextFlow createFooterIssueStats() {
        openIssueText = new Text(String.valueOf(openIssuesCount));
        closedIssueText = new Text(String.valueOf(closedIssuesCount));
        totalIssueText = new Text(String.valueOf(issuesCount));
        bracketOpenText = new Text(" (");
        bracketCloseText = new Text(")");
        plusText = new Text(" + ");
        openIssueText.setFill(Color.GREEN);
        closedIssueText.setFill(Color.RED);
        totalIssueText.setFill(Color.BLACK);
        TextFlow bottomPanelText = new TextFlow(totalIssueText, bracketOpenText,
                                                openIssueText, plusText, closedIssueText, bracketCloseText);
        return bottomPanelText;
    }

    /**
     * Determines if an issue has had new comments added (or removed) based on
     * its last-known comment count in {@link #issueCommentCounts}.
     *
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
     *
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
        issuesCount = getElementsList().size();
        closedIssuesCount = getClosedIssuesCount();
        openIssuesCount = issuesCount - closedIssuesCount;
        listView.restoreSelection();
        this.setId(IdGenerator.getPanelId(panelIndex));
        updateFooter();


    }

    private int getClosedIssuesCount() {
        return (int) getElementsList().stream().filter((element) -> !element.getIssue().isOpen()).count();
    }

    /**
     * This function updates the information in the panel footer.
     */
    private void updateFooter() {

        updateFooterPanelStatsDetails();
        if (issuesCount == 0) {
            hideFooterPanelStatsDetails(true);
        } else {
            hideFooterPanelStatsDetails(false);
        }

    }

    private void updateFooterPanelStatsDetails() {
        openIssueText.setText(String.valueOf(openIssuesCount));
        closedIssueText.setText(String.valueOf(closedIssuesCount));
        totalIssueText.setText(String.valueOf(issuesCount));
    }

    private void hideFooterPanelStatsDetails(boolean isHidden) {
        openIssueText.setVisible(!isHidden);
        closedIssueText.setVisible(!isHidden);
        plusText.setVisible(!isHidden);
        bracketCloseText.setVisible(!isHidden);
        bracketOpenText.setVisible(!isHidden);
    }

    private void setupListView() {
        setVgrow(listView, Priority.ALWAYS);
        setupKeyboardShortcuts();
        setupContextMenu();

        listView.setOnItemSelected((index, rightKey) -> {
            updateContextMenu(contextMenu);

            TurboIssue issue = listView.getItems().get(index).getIssue();
            if (!rightKey) {
                ui.triggerEvent(
                        new IssueSelectedEvent(issue.getRepoId(), issue.getId(), panelIndex, issue.isPullRequest())
                );
            }
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
            // Temporary fix for now since MARK_AS_READ and Show Related Issue/PR have same keys.
            // Will only work if the key for MARK_AS_READ is not the default key E.
            if (KeyboardShortcuts.MARK_AS_READ.match(event) && !SHOW_RELATED_ISSUE_OR_PR.match(event)) {
                markAsRead();
            }
            if (KeyboardShortcuts.MARK_AS_UNREAD.match(event)) {
                markAsUnread();
            }
            if (KeyboardShortcuts.CLOSE_ISSUE.match(event)) {
                closeIssue();
            }
            if (KeyboardShortcuts.REOPEN_ISSUE.match(event)) {
                reopenIssue();
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
            if (KeyboardShortcuts.SCROLL_TO_TOP.match(event)) {
                ui.getBrowserComponent().scrollToTop();
            }
            if (KeyboardShortcuts.SCROLL_TO_BOTTOM.match(event)) {
                ui.getBrowserComponent().scrollToBottom();
            }
            if (KeyboardShortcuts.SCROLL_UP.match(event) || KeyboardShortcuts.SCROLL_DOWN.match(event)) {
                ui.getBrowserComponent().scrollPage(KeyboardShortcuts.SCROLL_DOWN.match(event));
            }
            if (GOTO_MODIFIER.match(event)) {
                KeyPress.setLastKeyPressedCodeAndTime(event.getCode());
            }
            if (NEW_COMMENT.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    // goto PR discussion tab
                    ui.getBrowserComponent().openPrTab(getCurrentlySelectedIssue(), PrTab.DISCUSSION);
                } else { // focus new comment textbox
                    if (!ui.getBrowserComponent().isCurrentUrlDiscussion()) {
                        openIssuePageMainTab(getCurrentlySelectedIssue());
                    }
                    ui.getBrowserComponent().waitUntilDiscussionPageLoaded();
                    ui.getBrowserComponent().jumpToNewCommentBox();
                }

            }
            if (PR_FILES_CHANGED.match(event)
                    && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                ui.getBrowserComponent().openPrTab(getCurrentlySelectedIssue(), PrTab.FILES);
                event.consume();
            }
            if (PR_COMMITS.match(event)
                    && KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                ui.getBrowserComponent().openPrTab(getCurrentlySelectedIssue(), PrTab.COMMITS);
                event.consume();
            }
            if (SHOW_LABELS.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().newLabel();
                } else {
                    changeLabels();
                }
            }
            if (SHOW_ASSIGNEES.match(event)) {
                changeAssignee();
            }
            if (SHOW_MILESTONES.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showMilestones();
                } else {
                    getSelectedElement().ifPresent(this::changeMilestone);
                }
            }
            if (UNDO_LABEL_CHANGES.match(event)) {
                ui.triggerNotificationAction();
            }
            if (JUMP_TO_FIRST_ISSUE.match(event)) {
                listView.selectNthItem(1);
            }
            for (Map.Entry<Integer, KeyCodeCombination> entry : JUMP_TO_NTH_ISSUE_KEYS.entrySet()) {
                if (entry.getValue().match(event)) {
                    event.consume();
                    listView.selectNthItem(entry.getKey());
                    break;
                }
            }
            if (SHOW_RELATED_ISSUE_OR_PR.match(event) && ui.getBrowserComponent().isCurrentUrlIssue()) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    showRelatedIssueOrPR();
                    // only for default. can remove if default key for MARK_AS_READ_CHANGES
                } else if (KeyboardShortcuts.MARK_AS_READ.match(event)) {
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
                : ui.getBrowserComponent().getPrNumberFromIssue();
        if (!relatedIssueNumber.isPresent()) return;
        ui.triggerEvent(
                new IssueSelectedEvent(issue.getRepoId(), relatedIssueNumber.get(), panelIndex, issue.isPullRequest())
        );
    }

    private ContextMenu setupContextMenu() {
        markAsReadUnreadMenuItem.setOnAction(e -> {
            String menuItemText = markAsReadUnreadMenuItem.getText();

            if (menuItemText.equals(MARK_AS_READ_MENU_ITEM_TEXT)) {
                markAsRead();
            } else if (menuItemText.equals(MARK_AS_UNREAD_MENU_ITEM_TEXT)) {
                markAsUnread();
            }
        });

        closeReopenIssueMenuItem.setOnAction(e -> {
            String menuItemText = closeReopenIssueMenuItem.getText();

            if (menuItemText.equals(closeIssueMenuItemText)) {
                closeIssue();
            } else if (menuItemText.equals(reopenIssueMenuItemText)) {
                reopenIssue();
            }
        });

        changeLabelsMenuItem.setText(CHANGE_LABELS_MENU_ITEM_TEXT);
        changeLabelsMenuItem.setOnAction(e -> {
            changeLabels();
        });


        changeMilestoneMenuItem.setText(CHANGE_MILESTONE_MENU_ITEM_TEXT);
        changeMilestoneMenuItem.setOnAction(e -> {
            getSelectedElement().ifPresent(this::changeMilestone);
        });

        changeAssigneeMenuItem.setText(CHANGE_ASSIGNEE_MENU_ITEM_TEXT);
        changeAssigneeMenuItem.setOnAction(e -> {
            changeAssignee();
        });

        markAllBelowAsReadMenuItem.setText(MARK_ALL_AS_READ_MENU_ITEM_TEXT);
        markAllBelowAsReadMenuItem.setOnAction(e -> {
            markAllItemsBelow(READ);
        });

        markAllBelowAsUnreadMenuItem.setText(MARK_ALL_AS_UNREAD_MENU_ITEM_TEXT);
        markAllBelowAsUnreadMenuItem.setOnAction(e -> {
            markAllItemsBelow(!READ);
        });

        newWatchListMenuItem.setText(NEW_WATCH_LIST_MENU_ITEM_TEXT);
        newWatchListMenuItem.setOnAction(e -> {
            createNewWatchListPanel();
        });

        addToWatchListMenu.setText(ADD_TO_WATCH_LIST_MENU_TEXT);
        addToWatchListMenu.getItems().add(newWatchListMenuItem);

        contextMenu.getItems().addAll(markAsReadUnreadMenuItem, markAllBelowAsReadMenuItem,
                markAllBelowAsUnreadMenuItem, changeLabelsMenuItem, changeMilestoneMenuItem, closeReopenIssueMenuItem,
                changeAssigneeMenuItem, addToWatchListMenu);

        contextMenu.setOnShowing(e -> updateContextMenu(contextMenu));
        listView.setContextMenu(contextMenu);

        return contextMenu;
    }

    private ContextMenu updateContextMenu(ContextMenu contextMenu) {
        updateMarkAsReadUnreadMenuItem();
        updateCloseReopenIssueMenuItem();
        updateChangeLabelsMenuItem();
        updateChangeMilestoneMenuItem();
        updateChangeAssigneeMenuItem();
        updateAvailableWatchListPanels();

        return contextMenu;
    }

    private void updateAvailableWatchListPanels() {
        restoreDefaultAddToWatchListPanelMenu();
        Optional<GuiElement> item = listView.getSelectedItem();

        if (item.isPresent()) {
            addToWatchListMenu.setDisable(false);
        } else {
            addToWatchListMenu.setDisable(true);
        }

        parentPanelControl.getChildren().forEach(node -> {
            if (isWatchListPanel(node)) {
                MenuItem currentWatchPanel = createWatchListPanelMenuItem((FilterPanel) node);
                addToWatchListMenu.getItems().add(currentWatchPanel);
            }
        });
    }

    private void restoreDefaultAddToWatchListPanelMenu() {
        addToWatchListMenu.getItems().clear();
        addToWatchListMenu.getItems().add(newWatchListMenuItem);
    }

    /**
     * Creates a watch panel menu item for an existing watch list panel so that
     * the user can add an issue to that panel
     */
    private MenuItem createWatchListPanelMenuItem(FilterPanel watchListPanel) {
        MenuItem watchPanelMenu = new MenuItem();
        String watchListPanelTitle = watchListPanel.getCurrentInfo().getPanelName();
        watchPanelMenu.setText(watchListPanelTitle);
        watchPanelMenu.setOnAction(e -> addSelectedIssueToWatchListPanel(watchListPanel));
        return watchPanelMenu;
    }

    /**
     * @return true if the node is a filter panel and its filter is a 'watch list filter'
     */
    private boolean isWatchListPanel(Node node) {
        if (!(node instanceof FilterPanel)) {
            return false;
        }
        FilterPanel currentFilterPanel = (FilterPanel) node;
        return isWatchListFilter(currentFilterPanel.getFilterTextField().getText());
    }

    /**
     * A FilterExpression is a valid watch list filter if :
     *  - it is an empty string, OR
     *  - it is in the form of : id:repo1#id1;repo2#id2;repo3#id3;...
     */
    public static boolean isWatchListFilter(String filter) {
        return filter.isEmpty() || filter.matches(WATCH_LIST_FILTER_REGEX);
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    private MenuItem updateChangeAssigneeMenuItem() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            changeAssigneeMenuItem.setDisable(false);
        } else {
            changeAssigneeMenuItem.setDisable(true);
        }

        return changeAssigneeMenuItem;
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

    private MenuItem updateChangeMilestoneMenuItem() {
        Optional<GuiElement> selectedItem = listView.getSelectedItem();
        changeMilestoneMenuItem.setDisable(!selectedItem.isPresent());

        return changeMilestoneMenuItem;
    }

    private MenuItem updateMarkAsReadUnreadMenuItem() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            markAsReadUnreadMenuItem.setDisable(false);
            markAllBelowAsReadMenuItem.setDisable(false);
            markAllBelowAsUnreadMenuItem.setDisable(false);
            TurboIssue selectedIssue = item.get().getIssue();

            if (selectedIssue.isCurrentlyRead()) {
                markAsReadUnreadMenuItem.setText(MARK_AS_UNREAD_MENU_ITEM_TEXT);
            } else {
                markAsReadUnreadMenuItem.setText(MARK_AS_READ_MENU_ITEM_TEXT);
            }
        } else {
            markAsReadUnreadMenuItem.setDisable(true);
            markAllBelowAsReadMenuItem.setDisable(true);
            markAllBelowAsUnreadMenuItem.setDisable(true);
        }

        return markAsReadUnreadMenuItem;
    }

    public int getIssuesCount() {
        return issuesCount;
    }

    private MenuItem updateCloseReopenIssueMenuItem() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (!item.isPresent()) {
            closeReopenIssueMenuItem.setDisable(true);
            return closeReopenIssueMenuItem;
        }

        closeReopenIssueMenuItem.setDisable(false);
        TurboIssue selectedIssue = item.get().getIssue();

        if (selectedIssue.isOpen()) {
            closeReopenIssueMenuItem.setText(closeIssueMenuItemText);
        } else {
            closeReopenIssueMenuItem.setText(reopenIssueMenuItemText);
        }

        return closeReopenIssueMenuItem;
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

    /**
     * Creates a new watch list panel containing the currently selected issue
     */
    private void createNewWatchListPanel() {
        Optional<GuiElement> item = listView.getSelectedItem();
        assert item.isPresent();

        TurboIssue issue = item.get().getIssue();
        int panelCount = parentPanelControl.getPanelCount();
        FilterPanel newWatchListPanel = parentPanelControl.addPanelAt(panelCount);
        newWatchListPanel.setFilterByString(String.format(CREATE_WATCH_LIST_FORMAT,
                ui.logic.getDefaultRepo(), issue.getId()));
        String newWatchListPanelName = promptUserForNewWatchListPanelName();
        newWatchListPanel.setPanelName(newWatchListPanelName);
    }

    private String promptUserForNewWatchListPanelName() {
        TextInputDialog dialog = new TextInputDialog(DEFAULT_WATCH_LIST_PANEL_NAME);
        dialog.setTitle(WATCH_LIST_PANEL_NAME_PROMPT_TITLE);
        dialog.setHeaderText(WATCH_LIST_PANEL_NAME_PROMPT_HEADER);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(DEFAULT_WATCH_LIST_PANEL_NAME);
    }

    private void addSelectedIssueToWatchListPanel(FilterPanel watchListPanel) {
        Optional<GuiElement> item = listView.getSelectedItem();
        assert item.isPresent();

        TurboIssue issue = item.get().getIssue();
        String oldFilter = watchListPanel.getFilterTextField().getText();
        String newFilter;
        if (oldFilter.isEmpty()) {
            newFilter = String.format(CREATE_WATCH_LIST_FORMAT, issue.getRepoId(), issue.getId());
        } else {
            newFilter = String.format(APPEND_TO_WATCH_LIST_FORMAT, oldFilter, issue.getRepoId(), issue.getId());
        }
        watchListPanel.setFilterByString(newFilter);
    }

    /**
     * Marks the selected item and item below it in the list view as read/unread
     *
     * @param isRead Setting it to true marks all items on and below the selected issue as read.
     *               Setting it to false marks all items on and below the selected issue as unread.
     */
    private void markAllItemsBelow(boolean isRead) {
        if (!listView.getSelectedIndex().isPresent()) {
            return;
        }
        for (int i = listView.getSelectedIndex().get(); i < listView.getItems().size(); i++) {
            TurboIssue issue = listView.getItems().get(i).getIssue();
            if (isRead) {
                issue.markAsRead(UI.prefs);
            } else {
                issue.markAsUnread(UI.prefs);
            }
        }

        parentPanelControl.refresh();
    }

    private void markAsUnread() {
        Optional<GuiElement> item = listView.getSelectedItem();
        if (item.isPresent()) {
            TurboIssue issue = item.get().getIssue();
            issue.markAsUnread(UI.prefs);

            parentPanelControl.refresh();
        }
    }

    private void confirmCloseOrReopenIssue(GuiElement element, boolean isOpen) {
        ConfirmCloseOrReopenIssueDialog dialog = new ConfirmCloseOrReopenIssueDialog(mainStage, isOpen);
        Optional<ButtonType> response = dialog.showAndWait();
        response.ifPresent(res -> {
            if (res == ButtonType.OK) {
                ui.undoController.addAction(element.getIssue(),
                                            new EditIssueStateAction(ui.logic, isOpen));
            }
        });
    }

    private void closeIssue() {
        getSelectedElement().ifPresent(element -> {
            confirmCloseOrReopenIssue(element, false);
        });
    }

    private void reopenIssue() {
        getSelectedElement().ifPresent(element -> {
            confirmCloseOrReopenIssue(element, true);
        });
    }

    private void changeLabels() {
        if (getSelectedElement().isPresent()) {
            ui.triggerEvent(new ShowLabelPickerEvent(getSelectedElement().get().getIssue()));
        }
    }


    private void changeMilestone(GuiElement issueGuiElement) {
        ui.triggerEvent(new ShowMilestonePickerEvent(issueGuiElement.getIssue()));
    }

    private void changeAssignee() {
        if (getSelectedElement().isPresent()) {
            ui.triggerEvent(new ShowAssigneePickerEvent(getSelectedElement().get().getIssue()));
        }
    }

    @Override
    protected void startLoadingAnimationIfApplicable(PrimaryRepoOpeningEvent e) {
        // Repo is being opened by the repo selector, does not need to filter if repo is already specified for panel
        if (hasReposInFilter()) startLoadingAnimation();
    }

    @Override
    protected void stopLoadingAnimationIfApplicable(PrimaryRepoOpenedEvent e) {
        if (hasReposInFilter()) stopLoadingAnimation();
    }

    @Override
    protected void startLoadingAnimationIfApplicable(ApplyingFilterEvent e) {
        // Repo is being opened by a panel's filter, only need to show loading animation in the responsible panel
        if (e.panel == this) startLoadingAnimation();
    }

    @Override
    protected void stopLoadingAnimationIfApplicable(AppliedFilterEvent e) {
        if (e.panel == this) stopLoadingAnimation();
    }

    private void startLoadingAnimation() {
        setTranslucentCellFactory();
        showLoadingIndicator();
    }

    private void stopLoadingAnimation() {
        hideLoadingIndicator();
    }

    /**
     * Adds a style class to the listview which changes its background to contain a loading spinning gif
     */
    private void showLoadingIndicator() {
        logger.info("Preparing to add panel loading indication");
        listView.getStyleClass().add("listview-loading");
    }

    /**
     * Removes the style class that was added in showLoadingIndicator() from the listview.
     */
    private void hideLoadingIndicator() {
        logger.info("Preparing to remove panel loading indication");
        listView.getStyleClass().removeIf(cssClass -> cssClass.equals("listview-loading"));
    }

    private void setTranslucentCellFactory() {
        if (getElementsList() == null) return;
        final HashSet<Integer> issuesWithNewComments
                = updateIssueCommentCounts(Qualifier.hasUpdatedQualifier(getCurrentFilterExpression()));
        listView.setCellFactory(list -> {
            ListPanelCell cell = new ListPanelCell(this, panelIndex, issuesWithNewComments);
            cell.setStyle(cell.getStyle() + "-fx-opacity: 40%;");
            return cell;
        });
    }

    private boolean hasReposInFilter() {
        HashSet<String> allReposInFilterExpr =
                Qualifier.getMetaQualifierContent(getCurrentFilterExpression(), QualifierType.REPO);
        return allReposInFilterExpr.isEmpty();
    }

    private TurboIssue getCurrentlySelectedIssue() {
        return getSelectedElement().get().getIssue();
    }

    private void openIssuePageMainTab(TurboIssue issue) {
        ui.getBrowserComponent().showIssueMainTab(issue.getRepoId(), issue.getId(), issue.isPullRequest(), false);
    }

    @Override
    public void close() {
        // To be implemented if action needed to be taken after panel is deselected
    }
}
