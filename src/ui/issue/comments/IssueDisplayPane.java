package ui.issue.comments;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

import ui.ColumnControl;
import ui.IssueEditDisplay;
import ui.SidePanel;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class IssueDisplayPane extends HBox {
	protected static final int DETAILS_WIDTH = 350;
	protected static final int ISSUE_WIDTH = 300;

	private final TurboIssue issue;
	private final Model model;
	private final Stage parentStage;
	private ColumnControl columns;
	
	private IssueDetailsDisplay issueDetailsDisplay;
	private IssueEditDisplay issueEditDisplay;
	private WeakReference<SidePanel> parentPanel;
	public boolean showIssueDetailsPanel = false;
			
	public IssueDisplayPane(TurboIssue displayedIssue, Stage parentStage, Model model, ColumnControl columns, SidePanel parentPanel) {
		this.issue = displayedIssue;
		this.model = model;
		this.parentStage = parentStage;
		this.columns = columns;
		this.parentPanel = new WeakReference<SidePanel>(parentPanel);
		showIssueDetailsPanel = parentPanel.expandedIssueView;
		setup();
	}
	
	public CompletableFuture<String> getResponse() {
		return issueEditDisplay.getResponse();
	}

	private void setup() {
		setupIssueEditDisplay();
		this.getChildren().add(issueEditDisplay);
		showIssueDetailsDisplay(showIssueDetailsPanel);
	}
	
	private void setupIssueEditDisplay(){
		this.issueEditDisplay = new IssueEditDisplay(issue, parentStage, model, columns, this);
		this.issueEditDisplay.setPrefWidth(ISSUE_WIDTH);
		this.issueEditDisplay.setMinWidth(ISSUE_WIDTH);
	}
	
	private void setupIssueDetailsDisplay(){
		this.issueDetailsDisplay = new IssueDetailsDisplay(issue);
		this.issueDetailsDisplay.setPrefWidth(DETAILS_WIDTH);
		this.issueDetailsDisplay.setMinWidth(DETAILS_WIDTH);
		this.issueDetailsDisplay.setMaxWidth(DETAILS_WIDTH);
	}
	
	public void showIssueDetailsDisplay(boolean show){
		parentPanel.get().expandedIssueView = show;
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
	
}
