package ui.issuepanel;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboIssue;
import service.ServiceManager;
import ui.UI;
import ui.components.NavigableListView;
import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import util.KeyPress;
import util.events.IssueSelectedEvent;
import command.TurboCommandExecutor;

public class IssuePanel extends IssueColumn {

	private final Model model;
	private final UI ui;

	private NavigableListView<TurboIssue> listView;
	private final KeyCombination keyCombBoxToList = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
	private final KeyCombination keyCombListToBox = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
	private HashMap<Integer, Integer> issueCommentCounts = new HashMap<>();;

	public IssuePanel(UI ui, Stage mainStage, Model model, ColumnControl parentColumnControl, int columnIndex, TurboCommandExecutor dragAndDropExecutor) {
		super(ui, mainStage, model, parentColumnControl, columnIndex, dragAndDropExecutor);
		this.model = model;
		this.ui = ui;

		listView = new NavigableListView<>();
		setupListView();
		getChildren().add(listView);

		refreshItems();
	}

	/**
	 * Determines if an issue has had new comments added (or removed) based on
	 * its last-known comment count in {@link #issueCommentCounts}.
	 * @param issue
	 * @return true if the issue has changed, false otherwise
	 */
	private boolean issueHasNewComments(TurboIssue issue) {
		if (!issueCommentCounts.containsKey(issue.getId())) {
			return false;
		}
		return Math.abs(issueCommentCounts.get(issue.getId()) - issue.getCommentCount()) > 0;
	}

	@Override
	public void deselect() {
	}

	/**
	 * Updates {@link #issueCommentCounts} with the latest counts.
	 * Returns a list of issues which have new comments.
	 * @return
	 */
	private HashSet<Integer> updateIssueCommentCounts() {
		HashSet<Integer> result = new HashSet<>();
		for (TurboIssue issue : getIssueList()) {
			if (issueCommentCounts.containsKey(issue.getId())) {
				// We know about this issue; check if it's been updated
				if (issueHasNewComments(issue)) {
					result.add(issue.getId());
				}
			} else {
				// We don't know about this issue
				issueCommentCounts.put(issue.getId(), issue.getCommentCount());
			}
		}
		return result;
	}

	@Override
	public void refreshItems() {
		super.refreshItems();
		WeakReference<IssuePanel> that = new WeakReference<IssuePanel>(this);
		
		final HashSet<Integer> issuesWithNewComments = updateIssueCommentCounts();
		
		// Set the cell factory every time - this forces the list view to update
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				if(that.get() != null){
					return new IssuePanelCell(ui, model, that.get(), columnIndex, issuesWithNewComments);
				} else{
					return null;
				}
			}
		});

		listView.saveSelection();

		// Supposedly this also causes the list view to update - not sure
		// if it actually does on platforms other than Linux...
		listView.setItems(null);
		listView.setItems(getIssueList());

		listView.restoreSelection();
	}

	private void setupListView() {
		setVgrow(listView, Priority.ALWAYS);
		setupKeyboardShortcuts();
		listView.setOnItemSelected(i -> {
			TurboIssue issue = listView.getItems().get(i);
			ui.triggerEvent(new IssueSelectedEvent(issue.getId(), columnIndex));
			if (issueHasNewComments(issue)) {
				issueCommentCounts.put(issue.getId(), issue.getCommentCount());
				refreshItems();
			}
		});
	}
	
	private void setupKeyboardShortcuts(){
		filterTextField.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (keyCombBoxToList.match(event)) {
					listView.selectFirstItem();
				}
				if(event.getCode() == KeyCode.SPACE){
					event.consume();
					if (KeyPress.isDoublePress(event.getCode())) {
						listView.selectFirstItem();
					}
				}
			}
		});
		listView.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.C) {
					ui.getBrowserComponent().jumpToComment();
				}
				if(event.getCode() == KeyCode.T) {
					ui.getBrowserComponent().scrollToTop();
				} 
				if(event.getCode() == KeyCode.B) {
					ui.getBrowserComponent().scrollToBottom();
				}
				if(event.getCode() == KeyCode.U || event.getCode() == KeyCode.D) {
					ui.getBrowserComponent().scrollPage(event.getCode() == KeyCode.D);
				}
			}
		});
		this.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.F5) {
					ServiceManager.getInstance().updateModelNow();
				}
				if (keyCombListToBox.match(event)) {
					filterTextField.requestFocus();
				}
				if(event.getCode() == KeyCode.SPACE){
					if (KeyPress.isDoublePress(event.getCode())) {
						filterTextField.requestFocus();
					}
				}
			}
		});
		
	}
}
