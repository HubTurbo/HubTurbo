package ui.listpanel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.Comment;

import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import github.TurboIssueEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.GuiElement;
import ui.IssueCard;
import ui.issuepanel.FilterPanel;
import util.Utility;

/**
 * A card that is constructed with an issue as argument. Its components
 * are bound to the issue's fields and will update automatically.
 */
public class ListPanelCard extends IssueCard {



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
        super(guiElement, false, issuesWithNewComments.contains(guiElement.getIssue().getId()));
        setupEventDisplay(guiElement, parentPanel);
    }

    private void setupEventDisplay(GuiElement guiElement, FilterPanel parentPanel) {
        if (Qualifier.hasUpdatedQualifier(parentPanel.getCurrentFilterExpression())) {
            getChildren().add(getEventDisplay(guiElement.getIssue(),
                                              getUpdateFilterHours(parentPanel.getCurrentFilterExpression())));
        }
    }

    /**
     * Creates a JavaFX node containing a graphical display of this issue's events.
     *
     * @param withinHours the number of hours to bound the returned events by
     * @return the node
     */
    private Node getEventDisplay(TurboIssue issue, final int withinHours) {
        final LocalDateTime now = LocalDateTime.now();

        List<TurboIssueEvent> eventsWithinDuration = issue.getMetadata().getEvents().stream()
                .filter(event -> {
                    LocalDateTime eventTime = Utility.longToLocalDateTime
                            (event.getDate().getTime());
                    int hours = Math.toIntExact(eventTime.until(now, ChronoUnit.HOURS));
                    return hours < withinHours;
                })
                .collect(Collectors.toList());

        List<Comment> commentsWithinDuration = issue.getMetadata().getComments().stream()
                .filter(comment -> {
                    LocalDateTime created = Utility.longToLocalDateTime(comment.getCreatedAt().getTime());
                    int hours = Math.toIntExact(created.until(now, ChronoUnit.HOURS));
                    return hours < withinHours;
                })
                .collect(Collectors.toList());

        return layoutEvents(guiElement, eventsWithinDuration, commentsWithinDuration);
    }

    /**
     * Given a list of issue events, returns a JavaFX node laying them out properly.
     *
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
}
