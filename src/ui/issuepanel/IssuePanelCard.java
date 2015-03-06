package ui.issuepanel;

import java.util.HashSet;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
	private static final String OCTICON_COMMENT = "\uf02b";
	/**
	 * A card that is constructed with an issue as argument. Its components
	 * are bound to the issue's fields and will update automatically.
	 */
	
	private final TurboIssue issue;
	private FlowPane issueDetails = new FlowPane();
	private IssueColumn parentPanel;
	private final HashSet<Integer> issuesWithNewComments;

	public IssuePanelCard(TurboIssue issue, IssueColumn parentPanel, HashSet<Integer> issuesWithNewComments) {
		this.issue = issue;
		this.parentPanel = parentPanel;
		this.issuesWithNewComments = issuesWithNewComments;
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

		getChildren().addAll(issueTitle, issueDetails);

		if (isUpdateFilter(parentPanel.getCurrentFilterExpression())) {
			Node feed = issue.getEventDisplay(getUpdateFilterHours(parentPanel.getCurrentFilterExpression()));
			getChildren().add(feed);
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

		if (qualifier.getNumber().isPresent()) {
			return qualifier.getNumber().get();
		} else {
			// TODO support ranges properly. getEventDisplay only supports <
			assert qualifier.getNumberRange().isPresent();
			if (qualifier.getNumberRange().get().getStart() != null) {
				// TODO semantics are not exactly right
				return qualifier.getNumberRange().get().getStart();
			} else {
				assert qualifier.getNumberRange().get().getEnd() != null;
				// TODO semantics are not exactly right
				return qualifier.getNumberRange().get().getEnd();
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
		
		if (issue.getCommentCount() > 0){
			Label commentIcon = new Label(OCTICON_COMMENT);
			commentIcon.getStyleClass().addAll("octicon", "comments-label-button");
			Label commentCount = new Label(Integer.toString(issue.getCommentCount()));
			
			if (issuesWithNewComments.contains(issue.getId())) {
				commentIcon.getStyleClass().add("has-comments");
				commentCount.getStyleClass().add("has-comments");
			}
			
			issueDetails.getChildren().add(commentIcon);
			issueDetails.getChildren().add(commentCount);
		}
		
		for (TurboLabel label : issue.getLabels()) {
			issueDetails.getChildren().add(label.getNode());
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
