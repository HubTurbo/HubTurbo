package ui.listpanel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.eclipse.egit.github.core.Comment;

import ui.GuiElement;
import ui.issuepanel.FilterPanel;
import util.Utility;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import github.TurboIssueEvent;

public class ListPanelCard extends VBox {

    private static final String OCTICON_PULL_REQUEST = "\uf009";
    private static final int CARD_WIDTH = 350;
    private static final String OCTICON_COMMENT = "\uf02b";

    /**
     * A card that is constructed with an issue as argument. Its components
     * are bound to the issue's fields and will update automatically.
     */

    private final GuiElement guiElement;
    private final FlowPane issueDetails = new FlowPane();
    private final HBox authorAssigneeBox = new HBox();
    private final FilterPanel parentPanel;
    private final HashSet<Integer> issuesWithNewComments;

    /**
     * The constructor is the only method called from ListPanelCard. The rest of the methods in this class
     * are auxiliary methods called from the constructor so that the code is easier to understand.
     *
     * @param guiElement
     * @param parentPanel
     * @param issuesWithNewComments
     */
    public ListPanelCard(GuiElement guiElement, FilterPanel parentPanel,
                         HashSet<Integer> issuesWithNewComments) {
        this.guiElement = guiElement;
        this.parentPanel = parentPanel;
        this.issuesWithNewComments = issuesWithNewComments;
        setup();
    }

    private void setup() {
        TurboIssue issue = guiElement.getIssue();
        Label issueTitle = new Label("#" + issue.getId() + " " + issue.getTitle());
        issueTitle.setMaxWidth(CARD_WIDTH);
        issueTitle.setWrapText(true);
        issueTitle.getStyleClass().add("issue-panel-name");

        if (issue.isCurrentlyRead()) {
            issueTitle.getStyleClass().add("issue-panel-name-read");
        }

        if (!issue.isOpen()) {
            issueTitle.getStyleClass().add("issue-panel-closed");
        }

        setupIssueDetailsBox();
        setupAuthorAssigneeBox();
        updateDetails();

        setPadding(new Insets(0, 0, 0, 0));
        setSpacing(1);

        getChildren().addAll(issueTitle, issueDetails, authorAssigneeBox);

        if (Qualifier.hasUpdatedQualifier(parentPanel.getCurrentFilterExpression())) {
            getChildren().add(getEventDisplay(issue,
                getUpdateFilterHours(parentPanel.getCurrentFilterExpression())));
        }
    }

    /**
     * Creates a JavaFX node containing a graphical display of this issue's events.
     * @param withinHours the number of hours to bound the returned events by
     * @return the node
     */
    private Node getEventDisplay(TurboIssue issue, final int withinHours) {
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

        return layoutEvents(guiElement, eventsWithinDuration, commentsWithinDuration);
    }

    /**
     * Given a list of issue events, returns a JavaFX node laying them out properly.
     * @param events
     * @param comments
     * @return
     */
    private static Node layoutEvents(GuiElement guiElement,
                                     List<TurboIssueEvent> events, List<Comment> comments) {
        TurboIssue issue = guiElement.getIssue();

        VBox result = new VBox();
        result.setSpacing(3);
        VBox.setMargin(result, new Insets(3, 0, 0, 0));

        // Label update events
        List<TurboIssueEvent> labelUpdateEvents =
                        events.stream()
                        .filter(TurboIssueEvent::isLabelUpdateEvent)
                        .collect(Collectors.toList());
        List<Node> labelUpdateEventNodes =
                TurboIssueEvent.createLabelUpdateEventNodes(guiElement, labelUpdateEvents);
        labelUpdateEventNodes.forEach(node -> result.getChildren().add(node));

        // Other events beside label updates
        events.stream()
            .filter(e -> !e.isLabelUpdateEvent())
            .map(e -> e.display(guiElement, issue))
            .forEach(e -> result.getChildren().add(e));

        // Comments
        if (!comments.isEmpty()) {
            String names = comments.stream()
                .map(comment -> comment.getUser().getLogin())
                .distinct()
                .collect(Collectors.joining(", "));
            HBox commentDisplay = new HBox();
            commentDisplay.getChildren().addAll(
                TurboIssueEvent.octicon(TurboIssueEvent.OCTICON_QUOTE),
                new javafx.scene.control.Label(
                    String.format("%d comments since, involving %s.", comments.size(), names))
            );
            result.getChildren().add(commentDisplay);
        }

        return result;
    }

    private int getUpdateFilterHours(FilterExpression currentFilterExpression) {
        List<Qualifier> filters = currentFilterExpression.find(Qualifier::isUpdatedQualifier);
        assert !filters.isEmpty() : "Problem with isUpdateFilter";

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
        issueDetails.setVgap(3);
    }

    private void setupAuthorAssigneeBox() {
        authorAssigneeBox.setPrefWidth(CARD_WIDTH);
        authorAssigneeBox.setPadding(new Insets(0, 0, 1, 0));
        //authorAssigneeBox.setSpacing(1);
    }

    private void updateDetails() {
        issueDetails.getChildren().clear();
        TurboIssue issue = guiElement.getIssue();

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

        for (TurboLabel label : guiElement.getLabels()) {
            issueDetails.getChildren().add(label.getNode());
        }

        if (issue.getMilestone().isPresent() && guiElement.getMilestone().isPresent()) {
            TurboMilestone milestone = guiElement.getMilestone().get();
            issueDetails.getChildren().add(new Label(milestone.getTitle()));
        }

        if (guiElement.getAuthor().isPresent()) {
            TurboUser author = guiElement.getAuthor().get();
            Label authorLabel = new Label("Author: ");
            Label authorNameLabel = new Label(issue.getCreator());

            ImageView authorAvatar = new ImageView();
            if (author.getAvatarURL().length() != 0) {
                Image image = author.getAvatar();
                assert image != null;
                authorAvatar.setImage(image);
            }

            HBox authorBox = new HBox();
            authorBox.setAlignment(Pos.BASELINE_CENTER);
            authorBox.getChildren().addAll(authorLabel, authorAvatar, authorNameLabel);
            authorBox.setPrefWidth(CARD_WIDTH / 2);
            authorAssigneeBox.getChildren().add(authorBox);
        }

        if (issue.getAssignee().isPresent() && guiElement.getAssignee().isPresent()) {
            TurboUser assignee = guiElement.getAssignee().get();
            Label assigneeLabel = new Label("Assignee: ");
            Label assigneeNameLabel = new Label(issue.getAssignee().get());

            ImageView assigneeAvatar = new ImageView();
            if (assignee.getAvatarURL().length() != 0) {
                Image image = assignee.getAvatar();
                assert image != null;
                assigneeAvatar.setImage(image);
            }

            HBox assigneeBox = new HBox();
            assigneeBox.setAlignment(Pos.BASELINE_CENTER);
            assigneeBox.getChildren().addAll(assigneeLabel, assigneeAvatar, assigneeNameLabel);
            assigneeBox.setPrefWidth(CARD_WIDTH/2);
            authorAssigneeBox.getChildren().add(assigneeBox);
        }
    }

}
