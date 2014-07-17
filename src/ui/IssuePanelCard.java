package ui;

import java.lang.ref.WeakReference;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.TurboIssue;
import model.TurboUser;

public class IssuePanelCard extends VBox {

	/**
	 * A card that is constructed with an issue as argument. Its components
	 * are bound to the issue's fields and will update automatically.
	 */
	
	private final TurboIssue issue;
	public IssuePanelCard(TurboIssue issue) {
		this.issue = issue;
		setup();
	}
	
	private void setup() {
		Text issueTitle = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueTitle.setWrappingWidth(330);
		issueTitle.getStyleClass().add("issue-panel-name");
		if (!issue.getOpen()) issueTitle.getStyleClass().add("issue-panel-closed");
		issue.titleProperty().addListener(new WeakChangeListener<String>(createIssueTitleListener(issue, issueTitle)));

		LabelDisplayBox labels = new LabelDisplayBox(issue.getLabelsReference(), false, "");

		IssueIndicatorsDisplayBox indicators = new IssueIndicatorsDisplayBox(issue, false);
		
		TurboUser assignee = issue.getAssignee();
		HBox rightAlignBox = new HBox();
		rightAlignBox.setAlignment(Pos.BASELINE_RIGHT);
		HBox.setHgrow(rightAlignBox, Priority.ALWAYS);
		if (assignee != null) {
			Label assigneeName = new Label(assignee.getGithubName());
			assigneeName.getStyleClass().add("display-box-padding");
			rightAlignBox.getChildren().addAll(assigneeName);
		}

		HBox leftAlignBox = new HBox();
		leftAlignBox.setAlignment(Pos.BASELINE_LEFT);
		HBox.setHgrow(leftAlignBox, Priority.ALWAYS);
		leftAlignBox.getChildren().add(indicators);

		HBox bottom = new HBox();
		bottom.setSpacing(5);
		bottom.getChildren().add(leftAlignBox);
		if (assignee != null) bottom.getChildren().add(rightAlignBox);
		
		setMaxWidth(350);
		getStyleClass().addAll("borders", "rounded-borders");
		setPadding(new Insets(0,5,0,5));
		setSpacing(3);
		getChildren().addAll(issueTitle, labels, bottom);
	}
	
	private ChangeListener<String> titleChangeListener;
	private ChangeListener<String> createIssueTitleListener(TurboIssue issue, Text issueName){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		titleChangeListener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				TurboIssue issue = issueRef.get();
				if(issue != null){
					issueName.setText("#" + issue.getId() + " " + newValue);
				}
			}
		};
		
		return titleChangeListener;
	}
}
