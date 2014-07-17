package ui;

import java.util.concurrent.CompletableFuture;

import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class IssueDisplayPane extends HBox {
	protected static final String ISSUE_DETAILS_BTN_TXT = "Details >>";
	protected static final int LINE_HEIGHT = 18;
	protected static final int TITLE_ROW_NUM = 3;
	protected static final int DESC_ROW_NUM = 8;
	protected static final int DETAILS_WIDTH = 350;
	protected static final int ISSUE_WIDTH = 300;

	private final TurboIssue issue;
	private final Model model;
	private final Stage parentStage;
	private ColumnControl columns;
	
	private IssueDetailsDisplay issueDetailsDisplay;
	private IssueEditDisplay issueEditDisplay;
		
	public IssueDisplayPane(TurboIssue displayedIssue, Stage parentStage, Model model, ColumnControl columns) {
		this.issue = displayedIssue;
		this.model = model;
		this.parentStage = parentStage;
		this.columns = columns;
		setup();
	}
	
	public CompletableFuture<String> getResponse() {
		return issueEditDisplay.getResponse();
	}


	private void setup() {
		this.issueEditDisplay = new IssueEditDisplay(issue, parentStage, model, columns, this);
		this.issueEditDisplay.setPrefWidth(ISSUE_WIDTH);
		this.issueEditDisplay.setMinWidth(ISSUE_WIDTH);
		
		this.issueDetailsDisplay = new IssueDetailsDisplay(parentStage, issue);
		this.issueDetailsDisplay.setPrefWidth(DETAILS_WIDTH);
		this.issueDetailsDisplay.setMinWidth(DETAILS_WIDTH);
		this.getChildren().add(issueEditDisplay);
	}
	
	protected void showIssueDetailsDisplay(boolean show){
		if(show){
			this.getChildren().add(issueDetailsDisplay);
		}else{
			this.getChildren().remove(issueDetailsDisplay);
		}
	}
}
