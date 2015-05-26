package ui.issuepanel;

import backend.resource.*;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import github.TurboIssueEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.eclipse.egit.github.core.Comment;
import ui.issuecolumn.IssueColumn;
import util.Utility;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class IssuePanelCard extends VBox {

	private static final String OCTICON_PULL_REQUEST = "\uf009";
	private static final int CARD_WIDTH = 350;
	private static final String OCTICON_COMMENT = "\uf02b";
	/**
	 * A card that is constructed with an issue as argument. Its components
	 * are bound to the issue's fields and will update automatically.
	 */
	
	private final TurboIssue issue;
	private final Model model;
	private FlowPane issueDetails = new FlowPane();
	private IssueColumn parentPanel;
	private final HashSet<Integer> issuesWithNewComments;

	public IssuePanelCard(Model model, TurboIssue issue, IssueColumn parentPanel, HashSet<Integer>
		issuesWithNewComments) {
		this.model = model;
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
			Node feed = getEventDisplay(issue, getUpdateFilterHours(parentPanel.getCurrentFilterExpression()));
			getChildren().add(feed);
		}
	}

	/**
	 * Creates a JavaFX node containing a graphical display of this issue's events.
	 * @param withinHours the number of hours to bound the returned events by
	 * @return the node
	 */
	public Node getEventDisplay(TurboIssue issue, final int withinHours) {
		final LocalDateTime now = LocalDateTime.now();

		List<TurboIssueEvent> eventsWithinDuration = issue.getMetadata().getEvents().stream()
			.filter(event -> {
				LocalDateTime eventTime = Utility.longToLocalDateTime(event.getDate().getTime());
				int hours = Utility.safeLongToInt(eventTime.until(now, ChronoUnit.HOURS));
				return hours < withinHours;
			})
			.collect(Collectors.toList());

		List<Comment> commentsWithinDuration = issue.getMetadata().getComments().stream()
			.filter(comment -> {
				LocalDateTime created = Utility.longToLocalDateTime(comment.getCreatedAt().getTime());
				int hours = Utility.safeLongToInt(created.until(now, ChronoUnit.HOURS));
				return hours < withinHours;
			})
			.collect(Collectors.toList());

		return layoutEvents(eventsWithinDuration, commentsWithinDuration);
	}

	/**
	 * Given a list of issue events, returns a JavaFX node laying them out properly.
	 * @param events
	 * @param comments
	 * @return
	 */
	private static Node layoutEvents(List<TurboIssueEvent> events, List<Comment> comments) {
		VBox result = new VBox();
		result.setSpacing(3);
		VBox.setMargin(result, new Insets(3, 0, 0, 0));

		// Events
		events.stream()
			.map(TurboIssueEvent::display)
			.forEach(e -> result.getChildren().add(e));

		// Comments
		if (comments.size() > 0) {
			String names = comments.stream()
				.map(comment -> comment.getUser().getLogin())
				.distinct()
				.collect(Collectors.joining(", "));
			HBox commentDisplay = new HBox();
			commentDisplay.getChildren().addAll(
				TurboIssueEvent.octicon(TurboIssueEvent.OCTICON_QUOTE),
				new javafx.scene.control.Label(String.format("%d comments since, involving %s.", comments.size(),
					names))
			);
			result.getChildren().add(commentDisplay);
		}

		return result;
	}

	/**
	 * Given a list of issue events, returns a textual representation of them,
	 * concatenated together with newlines.
	 * @param events
	 * @param width
	 * @return
	 */
	private static Node formatEventsText(List<TurboIssueEvent> events, int width) {
		String text = events.stream()
			.map(TurboIssueEvent::toString)
			.collect(Collectors.joining("\n"));

		Text display = new Text(text);
		display.setWrappingWidth(width);
		display.getStyleClass().add("issue-panel-feed");
		return display;
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

		for (TurboLabel label : model.getLabelsOfIssue(issue)) {
			issueDetails.getChildren().add(label.getNode());
		}

		if (issue.getMilestone().isPresent() && model.getMilestoneOfIssue(issue).isPresent()) {
			TurboMilestone milestone = model.getMilestoneOfIssue(issue).get();
			issueDetails.getChildren().add(new Label(milestone.getTitle()));
		}

		if (issue.getAssignee().isPresent() && model.getAssigneeOfIssue(issue).isPresent()) {
			TurboUser assignee = model.getAssigneeOfIssue(issue).get();
			Label assigneeNameLabel = new Label(issue.getAssignee().get());
			assigneeNameLabel.getStyleClass().add("display-box-padding");

			Image image = assignee.getAvatar();
			ImageView avatar = new ImageView();
			assert image != null;
			avatar.setImage(image);

			HBox assigneeBox = new HBox();
			assigneeBox.setAlignment(Pos.BASELINE_CENTER);
			assigneeBox.getChildren().addAll(avatar, assigneeNameLabel);
			issueDetails.getChildren().add(assigneeBox);
		}
		
	}
}
