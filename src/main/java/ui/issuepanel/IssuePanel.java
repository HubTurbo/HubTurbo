package ui.issuepanel;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.expression.Qualifier;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import ui.UI;
import ui.components.IssueListView;
import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import util.KeyPress;
import util.events.IssueSelectedEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class IssuePanel extends IssueColumn {

    private final IModel model;
    private final UI ui;
    private int issueCount;

    private IssueListView listView;
    private final KeyCombination keyCombBoxToList =
        new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyCombListToBox =
        new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    private final KeyCombination maximizeWindow =
        new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
    private final KeyCombination minimizeWindow =
        new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    private final KeyCombination defaultSizeWindow =
        new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);

    // IssueCommentCounts and issueNonSelfCommentCounts are accessed whenever the view is updated
    // via listView.setItems in refreshItems. However, an overwrite of the count only happens upon
    // an IssueSelectedEvent.
    private HashMap<Integer, Integer> issueCommentCounts = new HashMap<>();
    private HashMap<Integer, Integer> issueNonSelfCommentCounts = new HashMap<>();

    public IssuePanel(UI ui, IModel model, ColumnControl parentColumnControl, int columnIndex) {
        super(ui, model, parentColumnControl, columnIndex);
        this.model = model;
        this.ui = ui;

        listView = new IssueListView();
        setupListView();
        getChildren().add(listView);

        refreshItems(true);
    }

    /**
     * Determines if an issue has had new comments added (or removed) based on
     * its last-known comment count in {@link #issueCommentCounts}.
     * @param issue
     * @return true if the issue has changed, false otherwise
     */
    private boolean issueHasNewComments(TurboIssue issue, boolean hasMetadata) {
        if (currentFilterExpression.getQualifierNames().contains(Qualifier.UPDATED) && hasMetadata) {
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
        for (TurboIssue issue : getIssueList()) {
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

    @Override
    public void refreshItems(boolean hasMetadata) {
//        super.refreshItems(hasMetadata);
//
//        boolean hasUpdatedQualifier = currentFilterExpression.getQualifierNames().contains(Qualifier.UPDATED);
//
//        // Only update filter if filter does not contain UPDATED (does not need to wait for metadata)
//        // or if hasMetadata is true (metadata has arrived), or if getIssueList is empty (if filter does
//        // have UPDATED, but there are no issues whose metadata require retrieval causing hasMetadata to
//        // never be true)
//
//        if (!hasUpdatedQualifier // not waiting for metadata, just update
//                || hasMetadata // metadata has arrived, update
//                || getIssueList().size() == 0 // checked only when above two not satisfied
//                ) {
            final HashSet<Integer> issuesWithNewComments = updateIssueCommentCounts(hasMetadata);

            // Set the cell factory every time - this forces the list view to update
            listView.setCellFactory(list ->
                    new IssuePanelCell(model, IssuePanel.this, columnIndex, issuesWithNewComments));
            listView.saveSelection();

            // Supposedly this also causes the list view to update - not sure
            // if it actually does on platforms other than Linux...
            listView.setItems(null);
            listView.setItems(getIssueList());
            issueCount = getIssueList().size();

            listView.restoreSelection();
            this.setId(model.getDefaultRepo() + "_col" + columnIndex);
        }
    }

    private void setupListView() {
        setVgrow(listView, Priority.ALWAYS);
        setupKeyboardShortcuts();

        listView.setOnItemSelected(i -> {
            TurboIssue issue = listView.getItems().get(i);
            ui.triggerEvent(
                    new IssueSelectedEvent(issue.getRepoId(), issue.getId(), columnIndex, issue.isPullRequest())
            );

            // Save the stored comment count as its own comment count.
            // The refreshItems(false) call that follows will remove the highlighted effect of the comment bubble.
            // (if it was there before)
            issueCommentCounts.put(issue.getId(), issue.getCommentCount());
            issueNonSelfCommentCounts.put(issue.getId(), issue.getMetadata().getNonSelfCommentCount());
            // We assume we already have metadata, so we pass true to avoid refreshItems from trying to get
            // metadata after clicking.
            refreshItems(true);
        });
    }

    private void setupKeyboardShortcuts() {
        filterTextField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (keyCombBoxToList.match(event)) {
                event.consume();
                listView.selectFirstItem();
            }
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
            }
            if (KeyPress.isDoublePress(KeyCode.SPACE, event.getCode())) {
                event.consume();
                listView.selectFirstItem();
            }
            if (maximizeWindow.match(event)) {
                ui.maximizeWindow();
            }
            if (minimizeWindow.match(event)) {
                ui.minimizeWindow();
            }
            if (defaultSizeWindow.match(event)) {
                ui.setDefaultWidth();
            }
        });

        addEventHandler(KeyEvent.KEY_RELEASED, event -> {

            if (event.getCode() == KeyCode.E) {
                Optional<TurboIssue> item = listView.getSelectedItem();
                if (!item.isPresent()) {
                    return;
                }
                TurboIssue issue = item.get();
                LocalDateTime now = LocalDateTime.now();
                ui.prefs.setMarkedReadAt(issue.getRepoId(), issue.getId(), now);
                issue.setMarkedReadAt(Optional.of(now));
                issue.setIsCurrentlyRead(true);
                parentColumnControl.refresh();
            }
            if (event.getCode() == KeyCode.U) {
                Optional<TurboIssue> item = listView.getSelectedItem();
                if (!item.isPresent()) {
                    return;
                }
                TurboIssue issue = item.get();
                ui.prefs.clearMarkedReadAt(issue.getRepoId(), issue.getId());
                issue.setMarkedReadAt(Optional.empty());
                issue.setIsCurrentlyRead(false);
                parentColumnControl.refresh();
            }
            if (event.getCode() == KeyCode.F5) {
                ui.logic.refresh();
            }
            if (event.getCode() == KeyCode.F1) {
                ui.getBrowserComponent().showDocs();
            }
            if (keyCombListToBox.match(event)) {
                setFocusToFilterBox();
            }
            if (event.getCode() == KeyCode.SPACE
                && KeyPress.isDoublePress(KeyCode.SPACE, event.getCode())) {

                setFocusToFilterBox();
            }
            if (event.getCode() == KeyCode.I) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().showIssues();
                }
            }
            if (event.getCode() == KeyCode.P) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().showPullRequests();
                }
            }
            if (event.getCode() == KeyCode.H) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().showDocs();
                }
            }
            if (event.getCode() == KeyCode.K) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().showKeyboardShortcuts();
                }
            }
            if (event.getCode() == KeyCode.D) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().showContributors();
                    event.consume();
                }
            }
            if (event.getCode() == KeyCode.U) {
                ui.getBrowserComponent().scrollToTop();
            }
            if (event.getCode() == KeyCode.N) {
                if (!minimizeWindow.match(event)) {
                    ui.getBrowserComponent().scrollToBottom();
                }
            }
            if (event.getCode() == KeyCode.J || event.getCode() == KeyCode.K) {
                ui.getBrowserComponent().scrollPage(event.getCode() == KeyCode.K);
            }
            if (event.getCode() == KeyCode.G) {
                KeyPress.setLastKeyPressedCodeAndTime(event.getCode());
            }
            if (event.getCode() == KeyCode.C && ui.getBrowserComponent().isCurrentUrlIssue()) {
                ui.getBrowserComponent().jumpToComment();
            }
            if (event.getCode() == KeyCode.L) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().newLabel();
                } else if (ui.getBrowserComponent().isCurrentUrlIssue()) {
                    ui.getBrowserComponent().manageLabels(event.getCode().toString());
                }
            }
            if (event.getCode() == KeyCode.A && ui.getBrowserComponent().isCurrentUrlIssue()) {
                ui.getBrowserComponent().manageAssignees(event.getCode().toString());
            }
            if (event.getCode() == KeyCode.M) {
                if (KeyPress.isValidKeyCombination(KeyCode.G, event.getCode())) {
                    ui.getBrowserComponent().showMilestones();
                } else if (ui.getBrowserComponent().isCurrentUrlIssue()) {
                    ui.getBrowserComponent().manageMilestones(event.getCode().toString());
                }
            }
            if (maximizeWindow.match(event)) {
                ui.maximizeWindow();
            }
            if (minimizeWindow.match(event)) {
                ui.minimizeWindow();
            }
            if (defaultSizeWindow.match(event)) {
                ui.setDefaultWidth();
            }
        });
    }

    private void setFocusToFilterBox() {
        filterTextField.requestFocus();
        filterTextField.setText(filterTextField.getText().trim());
        filterTextField.positionCaret(filterTextField.getLength());

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.V || event.getCode() == KeyCode.T) {
                listView.selectFirstItem();
            }
        });
    }

    public int getIssueCount() {
        return issueCount;
    }

    public TurboIssue getSelectedIssue() {
        return listView.getSelectedItem().get();
    }
}
