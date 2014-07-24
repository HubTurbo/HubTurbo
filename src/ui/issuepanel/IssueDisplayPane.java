package ui.issuepanel;

import java.lang.ref.WeakReference;

import command.TurboIssueAdd;
import command.TurboIssueCommand;
import command.TurboIssueEdit;
import ui.ColumnControl;
import ui.SidePanel;
import ui.SidePanel.IssueEditMode;
import ui.issuepanel.comments.IssueDetailsDisplay;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class IssueDisplayPane extends HBox {
	protected static final int DETAILS_WIDTH = 350;
	protected static final int ISSUE_WIDTH = 300;

	private final TurboIssue originalIssue;
	private final TurboIssue displayedIssue;
	private final Model model;
	private final Stage parentStage;
	private ColumnControl columns;
	
	private IssueDetailsDisplay issueDetailsDisplay;
	private IssueEditDisplay issueEditDisplay;
	private WeakReference<SidePanel> parentPanel;
	public boolean showIssueDetailsPanel = false;
	private boolean focusRequested;
	private IssueEditMode mode;
			
	public IssueDisplayPane(TurboIssue displayedIssue, Stage parentStage, Model model, ColumnControl columns, SidePanel parentPanel, boolean focusRequested, IssueEditMode mode) {
		this.displayedIssue = displayedIssue;
		this.originalIssue = new TurboIssue(displayedIssue);
		this.model = model;
		this.parentStage = parentStage;
		this.columns = columns;
		this.parentPanel = new WeakReference<SidePanel>(parentPanel);
		this.focusRequested = focusRequested;
		this.mode = mode;
		showIssueDetailsPanel = parentPanel.expandedIssueView;
		setup();
	}
	
	public boolean isExpandedIssueView(){
		return showIssueDetailsPanel;
	}
	
	public void handleCancelClicked(){
		columns.deselect();
		columns.refresh();
		showIssueDetailsDisplay(false);
		cleanup();
		parentPanel.get().displayTabs();
	}
	
	public void handleDoneClicked(){
		TurboIssueCommand command;
		if(mode == IssueEditMode.CREATE){
			command = new TurboIssueAdd(model, displayedIssue);
			command.execute();
		}else if(mode == IssueEditMode.EDIT){
			command = new TurboIssueEdit(model, originalIssue, displayedIssue);
			command.execute();
		}
		if(!showIssueDetailsPanel){
			cleanup();
			showIssueDetailsDisplay(false);
			parentPanel.get().displayTabs();
		}
	}

	private void setup() {
		setupIssueEditDisplay();
		this.getChildren().add(issueEditDisplay);
		showIssueDetailsDisplay(showIssueDetailsPanel);
	}
	
	private void setupIssueEditDisplay(){
		this.issueEditDisplay = new IssueEditDisplay(displayedIssue, parentStage, model, columns, this, focusRequested);
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
		showIssueDetailsPanel = show;
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
