package ui;

import java.util.Optional;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Represents an individual issue card not linked to any panel
 */
public class IssueCard extends VBox {

    private static final String OCTICON_COMMENT = "\uf02b";
    private static final String OCTICON_PULL_REQUEST = "\uf009";
    private static final int CARD_WIDTH = 350;
    private static final String OCTICON_ARROW_RIGHT = "\uf03e";

    protected final GuiElement guiElement;
    protected final FlowPane issueDetails;
    private final boolean isIssueWithNewComments;

    public IssueCard(GuiElement guiElement, boolean isFocus, boolean isIssueWithNewComments) {
        this.guiElement = guiElement;
        this.issueDetails = createDetailsPane();
        this.isIssueWithNewComments = isIssueWithNewComments;
        this.setFocused(isFocus);
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

        updateDetails();

        setPadding(new Insets(0, 0, 0, 0));
        setSpacing(1);

        getChildren().addAll(issueTitle, issueDetails);
    }

    /**
     * Sets background color when the card is focused or not
     * @param focusedBackground
     * @param defaultBackground
     * @return issue card
     */
    public IssueCard setBackgroundProperty(Background focusedBackground, Background defaultBackground) {
        this.backgroundProperty().bind(
            Bindings.when(this.focusedProperty()).then(focusedBackground).otherwise(defaultBackground));
        this.setStyle("-fx-border-color:black; -fx-border-style:hidden hidden solid hidden; ");
        return this;
    }

    private FlowPane createDetailsPane() {
        FlowPane detailsPane = new FlowPane();
        detailsPane.setMaxWidth(CARD_WIDTH);
        detailsPane.setPrefWrapLength(CARD_WIDTH);
        detailsPane.setHgap(3);
        detailsPane.setVgap(3);
        return detailsPane;
    }

    private void updateDetails() {
        issueDetails.getChildren().clear();
        TurboIssue issue = guiElement.getIssue();

        addPullRequestIcon(issue);
        addCommentIcon(issue);
        addMilestoneIcon(issue);

        createAuthorBox(issue);
        createAssigneeBox(issue);
        guiElement.getLabels().forEach(label -> issueDetails.getChildren().add(label.getNode()));
    }

    private void createAssigneeBox(TurboIssue issue) {
        if (issue.getAssignee().isPresent()) {
            HBox assigneeBox = createDisplayUserBox(guiElement.getAssignee(), issue.getAssignee().get());
            issueDetails.getChildren().add(assigneeBox);
        }
    }

    private void createAuthorBox(TurboIssue issue) {
        if (issue.isPullRequest()) {
            HBox authorBox = createDisplayUserBox(guiElement.getAuthor(), issue.getCreator());
            issueDetails.getChildren().add(authorBox);
            if (issue.getAssignee().isPresent()) {
                Label rightArrow = new Label(OCTICON_ARROW_RIGHT);
                rightArrow.getStyleClass().addAll("octicon", "pull-request-assign-icon");
                issueDetails.getChildren().add(rightArrow);
            }
        }
    }

    private void addMilestoneIcon(TurboIssue issue) {
        if (issue.getMilestone().isPresent() && guiElement.getMilestone().isPresent()) {
            TurboMilestone milestone = guiElement.getMilestone().get();
            issueDetails.getChildren().add(new Label(milestone.getTitle()));
        }
    }

    private void addCommentIcon(TurboIssue issue) {
        if (issue.getCommentCount() > 0) {
            Label commentIcon = new Label(OCTICON_COMMENT);
            commentIcon.getStyleClass().addAll("octicon", "comments-label-button");
            Label commentCount = new Label(Integer.toString(issue.getCommentCount()));

            if (isIssueWithNewComments) {
                commentIcon.getStyleClass().add("has-comments");
                commentCount.getStyleClass().add("has-comments");
            }

            issueDetails.getChildren().add(commentIcon);
            issueDetails.getChildren().add(commentCount);
        }
    }

    private void addPullRequestIcon(TurboIssue issue) {
        if (issue.isPullRequest()) {
            Label icon = new Label(OCTICON_PULL_REQUEST);
            icon.getStyleClass().addAll("octicon", "issue-pull-request-icon");
            issueDetails.getChildren().add(icon);
        }
    }

    /**
     * Creates a box that displays a label of userName
     * The avatar that belongs to the user will be prepended if TurboUser has it
     *
     * @param user
     * @param userName
     * @return
     */
    private HBox createDisplayUserBox(Optional<TurboUser> user, String userName) {
        HBox userBox = setupUserBox();
        Label authorNameLabel = new Label(userName);
        addAvatarIfPresent(userBox, user);
        userBox.getChildren().addAll(authorNameLabel);
        return userBox;
    }

    private void addAvatarIfPresent(HBox userBox, Optional<TurboUser> user) {
        if (!user.isPresent()) return;
        ImageView userAvatar = getAvatar(user.get());
        userBox.getChildren().add(userAvatar);
    }

    private HBox setupUserBox() {
        HBox userBox = new HBox();
        userBox.setAlignment(Pos.BASELINE_CENTER);
        return userBox;
    }

    /**
     * Attempts to get the TurboUser's avatar
     *
     * @param user
     * @return ImageView that contains the avatar image if it exists or an empty ImageView if it doesn't exist
     */
    private ImageView getAvatar(TurboUser user) {
        ImageView userAvatar = new ImageView();
        Image userAvatarImage = user.getAvatarImage();
        if (userAvatarImage != null) {
            userAvatar.setImage(userAvatarImage);
        }
        return userAvatar;
    }
}
