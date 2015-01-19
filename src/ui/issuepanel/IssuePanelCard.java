package ui.issuepanel;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.TurboIssue;
import model.TurboLabel;
import ui.issuecolumn.IssueColumn;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;

public class IssuePanelCard extends VBox {

	private static final String OCTICON_PULL_REQUEST = "\uf009";
	private static final int CARD_WIDTH = 350;
	private static final int HOURS_AGO = 24;
	private static final int MINUTES_AGO = 0;
	private static final int SECONDS_AGO = 0;
	/**
	 * A card that is constructed with an issue as argument. Its components
	 * are bound to the issue's fields and will update automatically.
	 */
	
	private final TurboIssue issue;
	private FlowPane issueDetails = new FlowPane();
	private IssueColumn parentPanel;

	public IssuePanelCard(TurboIssue issue, IssueColumn parentPanel) {
		this.issue = issue;
		this.parentPanel = parentPanel;
		setup();
	}
	
	private void setup() {
		Text issueTitle = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueTitle.setWrappingWidth(CARD_WIDTH);
		issueTitle.getStyleClass().add("issue-panel-name");
		if (!issue.isOpen()) issueTitle.getStyleClass().add("issue-panel-closed");
	
		setupIssueDetailsBox();
		
		setPadding(new Insets(0,0,3,0));
		setSpacing(1);

		if (isUpdateFilter(parentPanel.getCurrentFilterExpression())) {
			String feed = issue.getFeeds(getUpdateFilterHours(parentPanel.getCurrentFilterExpression()), MINUTES_AGO, SECONDS_AGO);
			if (feed != null && !feed.isEmpty()) {
				Text issueFeed = new Text(feed);
				issueFeed.setWrappingWidth(CARD_WIDTH);
				issueFeed.getStyleClass().add("issue-panel-feed");
				getChildren().addAll(issueTitle, issueDetails, issueFeed);
			} else {
				getChildren().addAll(issueTitle, issueDetails);
			}
		} else {
			getChildren().addAll(issueTitle, issueDetails);
		}
	}
	
	private boolean isUpdateFilter(FilterExpression currentFilterExpression) {
		return currentFilterExpression.getQualifierNames().contains("updated");
	}
	
	private int getUpdateFilterHours(FilterExpression currentFilterExpression) {
		List<Qualifier> filters = currentFilterExpression.find(q -> q.getName().equals("updated"));
		assert filters.size() > 0 : "Problem with isUpdateFilter";

		// Return the first of the updated qualifiers, if there are multiple
		Qualifier qualifier = filters.get(0);
		
		// Currently we show 24 hours or less only, clamping ranges
		if (qualifier.getNumber().isPresent()) {
			return Math.min(qualifier.getNumber().get(), HOURS_AGO);
		} else {
			assert qualifier.getNumberRange().isPresent();
			if (qualifier.getNumberRange().get().getStart() == null) {
				return Math.min(qualifier.getNumberRange().get().getEnd(), HOURS_AGO);
			} else if (qualifier.getNumberRange().get().getEnd() == null) {
				return Math.min(qualifier.getNumberRange().get().getStart(), HOURS_AGO);
			} else {
				return Math.min(qualifier.getNumberRange().get().getStart(), Math.min(qualifier.getNumberRange().get().getEnd(), HOURS_AGO));
			}
		}
	}

	private void setupIssueDetailsBox() {
		issueDetails.setMaxWidth(CARD_WIDTH);
		issueDetails.setPrefWrapLength(CARD_WIDTH);
		issueDetails.setHgap(3);
		
		updateDetails();
	}
	
	private void updateDetails() {
		issueDetails.getChildren().clear();
		
		if (issue.isPullRequest()) {
			Label icon = new Label(OCTICON_PULL_REQUEST);
			icon.getStyleClass().addAll("octicon", "issue-pull-request-icon");
			issueDetails.getChildren().add(icon);
		}
		
		if (issue.getNumOfComments() > 0){
			Text commentCount = new Text(issue.getNumOfComments() + " comments");
			issueDetails.getChildren().add(commentCount);
		}
		
		for (TurboLabel label : issue.getLabels()) {
			Label labelText = new Label(label.getName());
			labelText.getStyleClass().add("labels");
			labelText.setStyle(label.getStyle());
			if (label.getGroup() != null) {
				Tooltip groupTooltip = new Tooltip(label.getGroup());
				labelText.setTooltip(groupTooltip);
			}
			issueDetails.getChildren().add(labelText);
		}
		
		if(issue.getParentIssue() >= 0){
			String parentString = "#" + issue.getParentIssue();
			Label parent = new Label(parentString);
			parent.getStyleClass().addAll("display-box-padding");
			issueDetails.getChildren().add(parent);
		}
		
		if (issue.getMilestone() != null) {
			Label milestone = new Label(issue.getMilestone().getTitle());
			issueDetails.getChildren().add(milestone);
		}

		if (issue.getAssignee() != null) {
			
			Label assigneeName = new Label((issue.getAssignee().getAlias()));
			assigneeName.getStyleClass().add("display-box-padding");
			Image image = issue.getAssignee().getAvatar();
			ImageView avatar = new ImageView();
			if(image != null){
				avatar.setImage(image);
			}
			HBox assignee = new HBox();
			assignee.setAlignment(Pos.BASELINE_CENTER);
			assignee.getChildren().addAll(avatar, assigneeName);
			issueDetails.getChildren().add(assignee);
		}
		
	}
}
