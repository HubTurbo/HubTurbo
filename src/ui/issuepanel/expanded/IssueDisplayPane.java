package ui.issuepanel.expanded;

import java.lang.ref.WeakReference;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ui.StatusBar;
import ui.UI;
import ui.issuecolumn.ColumnControl;
import ui.issuepanel.expanded.comments.IssueDetailsDisplay;
import ui.sidepanel.SidePanel;
import ui.sidepanel.SidePanel.IssueEditMode;
import util.DialogMessage;

import command.TurboIssueAdd;
import command.TurboIssueCommand;
import command.TurboIssueEdit;

/**
 * Represents the form which comes up when an issue is created or edited.
 * Mainly handles the expanding and collapsing of the issue comments form, IssueDetailsDisplay.
 * Operations involving the editing of issues are delegated to IssueEditDisplay.
 */
public class IssueDisplayPane extends HBox {
	private static final Logger logger = LogManager.getLogger(IssueDisplayPane.class.getName());
	protected static final int DETAILS_WIDTH = 350;
	protected static final int ISSUE_WIDTH = 300;

	private final TurboIssue originalIssue;
	private final TurboIssue displayedIssue;
	private final Model model;
	private final Stage parentStage;
	private final UI ui;
	private final ColumnControl columns;
	
	private IssueDetailsDisplay issueDetailsDisplay;
	private IssueEditDisplay issueEditDisplay;
	private WeakReference<SidePanel> parentPanel;
	public boolean expandedIssueView = false;
	private boolean focusRequested;
	private IssueEditMode mode;
			
	public IssueDisplayPane(UI ui, TurboIssue displayedIssue, Stage parentStage, Model model, ColumnControl columns, SidePanel parentPanel, boolean focusRequested, IssueEditMode mode) {
		this.ui = ui;
		this.displayedIssue = displayedIssue;
		this.originalIssue = new TurboIssue(displayedIssue);
		this.model = model;
		this.parentStage = parentStage;
		this.columns = columns;
		this.parentPanel = new WeakReference<SidePanel>(parentPanel);
		this.focusRequested = focusRequested;
		this.mode = mode;
		this.expandedIssueView = parentPanel.expandedIssueView;
		setup();
	}
	
	public boolean isExpandedIssueView(){
		return expandedIssueView;
	}
	
	public void handleCancelClicked(){
		cleanup();
		columns.deselect();
		showIssueDetailsDisplay(false);
		parentPanel.get().displayTabs();
	}
	
	private void updateStateAfterSuccessfulAdd(TurboIssue addedIssue){
		Platform.runLater(() -> {
			displayedIssue.copyValues(addedIssue);
			originalIssue.copyValues(addedIssue);
			issueEditDisplay.updateIssueId(displayedIssue.getId());
		});
		mode = IssueEditMode.EDIT;
	}
	
	private void updateStateAfterSuccessfulEdit(TurboIssue editedIssue){
		Platform.runLater(() -> {
			displayedIssue.copyValues(editedIssue);
			originalIssue.copyValues(editedIssue);
		});
	}
	
	private boolean handleIssueCreate(){
		String message = "";
		TurboIssueCommand command = new TurboIssueAdd(model, displayedIssue);
		boolean success = command.execute();
		if(success){
			updateStateAfterSuccessfulAdd(((TurboIssueAdd)command).getAddedIssue());
			message = "Issue successfully created!";
		}else{
			message = "An error occured while creating the issue";
		}
		StatusBar.displayMessage(message);
		return success;
	}
	
	private boolean handleIssueEdit(){
		String message = "";
		TurboIssueCommand command = new TurboIssueEdit(model, originalIssue, displayedIssue);
		boolean success = command.execute();
		if(success){
			updateStateAfterSuccessfulEdit(((TurboIssueEdit)command).getEditedIssue());
			message = "Issue successfully edited!";
		}else{
			message = "An error occured while editing the issue. Changes have not been saved.";
		}
		StatusBar.displayMessage(message);
		return success;
	}
	
	public void handleSaveClicked(){
		Task<Boolean> bgTask = new Task<Boolean>(){

			@Override
			protected Boolean call() throws Exception {
				boolean success = false;
				if(mode == IssueEditMode.CREATE){
					success = handleIssueCreate();
				}else if(mode == IssueEditMode.EDIT){
					success = handleIssueEdit();
				}
				return success;
			}
			
		};
		
		bgTask.setOnSucceeded(e -> {
			try {
				boolean success = bgTask.get();
				if(success){
					if(expandedIssueView){
						issueDetailsDisplay.refresh(); //Refresh comments so change log can be seen
					}else{
						showIssueDetailsDisplay(false);
						cleanup();
						parentPanel.get().displayTabs();
					}
				}
			} catch (Exception e1) {
				logger.error(e1.getLocalizedMessage(), e1);
			}

		});
				
		DialogMessage.showProgressDialog(bgTask, "Saving issue...");
		Thread thread = new Thread(bgTask);
		thread.start();
	}

	private void setup() {
		setupIssueEditDisplay();
		this.getChildren().add(issueEditDisplay);
		showIssueDetailsDisplay(expandedIssueView);
	}
	
	private void setupIssueEditDisplay(){
		this.issueEditDisplay = new IssueEditDisplay(ui, displayedIssue, parentStage, model, this, focusRequested);
		this.issueEditDisplay.setPrefWidth(ISSUE_WIDTH);
		this.issueEditDisplay.setMinWidth(ISSUE_WIDTH);
	}
	
	private void setupIssueDetailsDisplay(){
		this.issueDetailsDisplay = new IssueDetailsDisplay(displayedIssue);
		this.issueDetailsDisplay.setPrefWidth(DETAILS_WIDTH);
		this.issueDetailsDisplay.setMinWidth(DETAILS_WIDTH);
		this.issueDetailsDisplay.setMaxWidth(DETAILS_WIDTH);
	}
	
	public void showIssueDetailsDisplay(boolean show){
		parentPanel.get().expandedIssueView = show;
		expandedIssueView = show;
		if(show){
			if(issueDetailsDisplay == null){
				setupIssueDetailsDisplay();
			}
			this.getChildren().add(issueDetailsDisplay);
			issueDetailsDisplay.show();
		}else{
			if(issueDetailsDisplay != null){
				this.getChildren().remove(issueDetailsDisplay);
				issueDetailsDisplay.hide();
			}
		}
	}
	public void cleanup(){
		if(issueDetailsDisplay != null){
			issueDetailsDisplay.cleanup();
		}
	}
}
