package github;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import org.eclipse.egit.github.core.User;
import org.ocpsoft.prettytime.PrettyTime;

import ui.GuiElement;
import util.Utility;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;

/**
 * Models an event that could happen to an issue.
 */
public class TurboIssueEvent {
    private static final String OCTICON_TAG = "\uf015";
    private static final String OCTICON_MILESTONE = "\uf075";
    private static final String OCTICON_ISSUE_CLOSED = "\uf028";
    private static final String OCTICON_ISSUE_OPENED = "\uf027";
    private static final String OCTICON_PENCIL = "\uf058";
    private static final String OCTICON_PERSON = "\uf018";
    public static final String OCTICON_QUOTE = "\uf063";


    // Maximum time difference in seconds between label update events in the same group
    private static final long MAX_TIME_DIFF = 60;

    private final Date date;
    private final IssueEventType type;
    private final User actor;

    private String labelName, labelColour;
    private String milestoneTitle;
    private String renamedFrom, renamedTo;
    private User assignedUser;

    public TurboIssueEvent(User actor, IssueEventType type, Date date) {
        this.type = type;
        this.actor = actor;
        this.date = new Date(date.getTime());
    }

    public IssueEventType getType() {
        return type;
    }

    public User getActor() {
        return actor;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    // Mutable fields

    public boolean isLabelUpdateEvent() {
        return type == IssueEventType.Labeled || type == IssueEventType.Unlabeled;
    }

    public String getLabelName() {
        assert isLabelUpdateEvent();
        return labelName;
    }

    public TurboIssueEvent setLabelName(String labelName) {
        assert isLabelUpdateEvent();
        this.labelName = labelName;
        return this;
    }

    public String getLabelColour() {
        assert isLabelUpdateEvent();
        return labelColour;
    }

    public TurboIssueEvent setLabelColour(String labelColour) {
        assert isLabelUpdateEvent();
        this.labelColour = labelColour;
        return this;
    }

    public String getMilestoneTitle() {
        assert type == IssueEventType.Milestoned || type == IssueEventType.Demilestoned;
        return milestoneTitle;
    }

    public TurboIssueEvent setMilestoneTitle(String milestoneTitle) {
        assert type == IssueEventType.Milestoned || type == IssueEventType.Demilestoned;
        this.milestoneTitle = milestoneTitle;
        return this;
    }

    public String getRenamedFrom() {
        assert type == IssueEventType.Renamed;
        return renamedFrom;
    }

    public void setRenamedFrom(String renamedFrom) {
        assert type == IssueEventType.Renamed;
        this.renamedFrom = renamedFrom;
    }

    public String getRenamedTo() {
        assert type == IssueEventType.Renamed;
        return renamedTo;
    }

    public void setRenamedTo(String renamedTo) {
        assert type == IssueEventType.Renamed;
        this.renamedTo = renamedTo;
    }

    public User getAssignedUser() {
        assert type == IssueEventType.Assigned || type == IssueEventType.Unassigned;
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        assert type == IssueEventType.Assigned || type == IssueEventType.Unassigned;
        this.assignedUser = assignedUser;
    }

    public static Label octicon(String which) {
        Label label = new Label(which);
        HBox.setMargin(label, new Insets(0, 2, 0, 0));
        label.getStyleClass().addAll("octicon", "issue-event-icon");
        return label;
    }

    /**
     * Could be generalised to include other types of formatting in future.
     *
     * @param bold
     * @param text
     * @return
     */
    private Text conditionallyBold(boolean bold, Text text) {
        if (bold) {
            text.getStyleClass().add("bold");
        }
        return text;
    }

    public Node display(GuiElement guiElement, TurboIssue issue) {
        String actorName = getActor().getLogin();
        String time = new PrettyTime().format(getDate());

        boolean bold = issue.getMarkedReadAt().isPresent()
                && issue.getMarkedReadAt().get().isBefore(Utility.dateToLocalDateTime(getDate()));

        switch (getType()) {
        case Renamed: {
            HBox display = new HBox();
            display.getChildren().addAll(octicon(OCTICON_PENCIL),
                                         conditionallyBold(bold,
                                                           new Text(String.format("%s renamed this issue %s.",
                                                                                  actorName, time))));
            return display;
        }
        case Milestoned: {
            HBox display = new HBox();
            display.getChildren().addAll(octicon(OCTICON_MILESTONE),
                                         conditionallyBold(bold, new Text(String.format(
                                                 "%s added milestone %s %s.", actorName, getMilestoneTitle(), time))));
            return display;
        }
        case Demilestoned: {
            HBox display = new HBox();
            display.getChildren().addAll(octicon(OCTICON_MILESTONE),
                                         conditionallyBold(bold, new Text(String.format(
                                                 "%s removed milestone %s %s.", actorName, getMilestoneTitle(), time)
                                         )));
            return display;
        }
        // Labeled and Unlabeled are not invoked as createLabelUpdateEventNodes is invoked
        // instead for these event types (see ListPanelCard.layoutEvents)
        case Labeled: {
            Optional<TurboLabel> label = guiElement.getLabelByActualName(getLabelName());
            HBox display = new HBox();
            display.getChildren().addAll(
                    octicon(OCTICON_TAG),
                    conditionallyBold(bold, new Text(String.format("%s added label ", actorName))),
                    label.isPresent()
                            ? label.get().getNode()
                            : new Label(getLabelName()),
                    conditionallyBold(bold, new Text(String.format(" %s.", time)))
            );
            return display;
        }
        case Unlabeled: {
            Optional<TurboLabel> label = guiElement.getLabelByActualName(getLabelName());
            HBox display = new HBox();
            display.getChildren().addAll(
                    octicon(OCTICON_TAG),
                    conditionallyBold(bold, new Text(String.format("%s removed label ", actorName))),
                    label.isPresent()
                            ? label.get().getNode()
                            : new Label(getLabelName()),
                    conditionallyBold(bold, new Text(String.format(" %s.", time)))
            );
            return display;
        }
        case Assigned: {
            HBox display = new HBox();
            display.getChildren().addAll(
                    octicon(OCTICON_PERSON),
                    conditionallyBold(bold,
                                      new Text(String.format(
                                              "%s was assigned to this issue %s.", actorName, time)))
            );
            return display;
        }
        case Unassigned: {
            HBox display = new HBox();
            display.getChildren().addAll(
                    octicon(OCTICON_PERSON),
                    conditionallyBold(bold, new Text(String.format(
                            "%s was unassigned from this issue %s.", actorName, time)))
            );
            return display;
        }
        case Closed: {
            HBox display = new HBox();
            display.getChildren().addAll(
                    octicon(OCTICON_ISSUE_CLOSED),
                    conditionallyBold(bold,
                                      new Text(String.format("%s closed this issue %s.", actorName, time)))
            );
            return display;
        }
        case Reopened: {
            HBox display = new HBox();
            display.getChildren().addAll(
                    octicon(OCTICON_ISSUE_OPENED),
                    conditionallyBold(bold,
                                      new Text(String.format("%s reopened this issue %s.", actorName, time)))
            );
            return display;
        }
        case Locked:
            return conditionallyBold(bold, new Text(
                    String.format("%s locked issue %s.", actorName, time)));
        case Unlocked:
            return conditionallyBold(bold, new Text(
                    String.format("%s unlocked this issue %s.", actorName, time)));
        case Referenced:
            return conditionallyBold(bold, new Text(
                    String.format("%s referenced this issue %s.", actorName, time)));
        case Subscribed:
            return conditionallyBold(bold, new Text(
                    String.format("%s subscribed to receive notifications for this issue %s.",
                                  actorName, time)));
        case Unsubscribed:
            return conditionallyBold(bold, new Text(
                    String.format("%s unsubscribed from notifications for this issue %s.",
                                  actorName, time)));
        case Mentioned:
            return conditionallyBold(bold, new Text(
                    String.format("%s was mentioned %s.", actorName, time)));
        case Merged:
            return conditionallyBold(bold, new Text(
                    String.format("%s merged this issue %s.", actorName, time)));
        case HeadRefDeleted:
            return conditionallyBold(bold, new Text(
                    String.format("%s deleted the pull request's branch %s.", actorName, time)));
        case HeadRefRestored:
            return conditionallyBold(bold, new Text(
                    String.format("%s restored the pull request's branch %s.", actorName, time)));
        default:
            // Not yet implemented, or no events triggered
            return conditionallyBold(bold, new Text(
                    String.format("%s %s %s.", actorName, getType(), time)));
        }
    }

    /**
     * Create a list of JavaFX nodes to display label update events
     *
     * @param labelUpdateEvents
     * @return list of Node corresponding to groups of label update events
     */
    public static List<Node> createLabelUpdateEventNodes(
            GuiElement guiElement, List<TurboIssueEvent> labelUpdateEvents) {

        assert labelUpdateEvents != null : "Error: Received null list of events";

        List<List<TurboIssueEvent>> groupedEvents = groupLabelUpdateEvents(labelUpdateEvents);
        List<Node> result = new ArrayList<>();

        groupedEvents.forEach(group -> {
            TurboIssueEvent firstEvent = group.get(0);
            String actorName = firstEvent.getActor().getLogin();
            String time = new PrettyTime().format(firstEvent.getDate());

            HBox box = new HBox();
            box.setSpacing(3);

            box.getChildren().addAll(
                    octicon(OCTICON_TAG),
                    new Text(String.format("%s :", actorName)));

            group.forEach(e -> {
                Node node = createLabelNode(guiElement, e);
                box.getChildren().add(node);
            });

            box.getChildren().add(new Text(String.format("%s.", time)));

            result.add(box);
        });

        return result;
    }

    private static Node createLabelNode(GuiElement guiElement, TurboIssueEvent e) {
        // Unlabeled/Labeled events use data from the TurboIssueEvent itself (see GuiElement.getLabels documentation)
        TurboLabel label = new TurboLabel(guiElement.getIssue().getRepoId(),
                                          e.getLabelColour(),
                                          e.getLabelName());
        Node node = label.getNode();
        if (e.getType() == IssueEventType.Unlabeled) {
            node.getStyleClass().add("labels-removed");
        }
        return node;
    }

    /**
     * Groups label update events into a list of sub-lists of events.
     * Events in the same sub-list will have the same author
     * and with time-stamps less than MAX_TIME_DIFF minutes of each other
     *
     * @param labelUpdateEvents
     * @return list of sub-lists of label update events
     */
    public static List<List<TurboIssueEvent>> groupLabelUpdateEvents(
            List<TurboIssueEvent> labelUpdateEvents) {

        assert labelUpdateEvents != null : "Error: Received null list of events";

        List<TurboIssueEvent> events = new ArrayList<>(labelUpdateEvents);
        List<List<TurboIssueEvent>> result = new ArrayList<>();
        List<TurboIssueEvent> currentSubList = new ArrayList<>();

        Collections.sort(events, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
        Collections.sort(events, (e1, e2) -> e1.getActor().getLogin().compareTo(e2.getActor().getLogin()));

        for (TurboIssueEvent e : events) {
            if (currentSubList.isEmpty() ||
                    e.isInSameLabelUpdateEventGroup(currentSubList.get(0))) {
                currentSubList.add(e);
            } else {
                result.add(currentSubList);
                currentSubList = new ArrayList<>();
                currentSubList.add(e);
            }
        }
        if (!currentSubList.isEmpty()) {
            result.add(currentSubList);
        }

        Collections.sort(result, (l1, l2) -> l1.get(0).getDate().compareTo(l2.get(0).getDate()));

        return result;
    }

    /**
     * Checks if this label update event in the same group
     * as another label update event e
     *
     * @param e another label update event
     * @return true if this event and e have same author and times within
     * MAX_TIME_DIFF from each other.
     * false otherwise
     */
    public boolean isInSameLabelUpdateEventGroup(TurboIssueEvent e) {
        long timeDiffMs = Math.abs(getDate().getTime() - e.getDate().getTime());
        long timeDiffSec = TimeUnit.MILLISECONDS.toSeconds(timeDiffMs);

        return getActor().getLogin().equals(e.getActor().getLogin()) &&
                timeDiffSec <= MAX_TIME_DIFF;
    }

    @Override
    public String toString() {
        String actorName = getActor().getLogin();
        String time = new PrettyTime().format(getDate());

        switch (getType()) {
        case Renamed:
            return String.format("%s renamed this issue %s.", actorName, time);
        case Milestoned:
            return String.format("%s added milestone %s %s.", actorName,
                                 getMilestoneTitle(), time);
        case Demilestoned:
            return String.format("%s removed milestone %s %s.", actorName,
                                 getMilestoneTitle(), time);
        case Labeled:
            return String.format("%s added label %s %s.", actorName, getLabelName(), time);
        case Unlabeled:
            return String.format("%s removed label %s %s.", actorName, getLabelName(), time);
        case Assigned:
            return String.format("%s was assigned to this issue %s.", actorName, time);
        case Unassigned:
            return String.format("%s was unassigned from this issue %s.", actorName, time);
        case Closed:
            return String.format("%s closed this issue %s.", actorName, time);
        case Reopened:
            return String.format("%s reopened this issue %s.", actorName, time);
        case Locked:
            return String.format("%s locked issue %s.", actorName, time);
        case Unlocked:
            return String.format("%s unlocked this issue %s.", actorName, time);
        case Referenced:
            return String.format("%s referenced this issue %s.", actorName, time);
        case Subscribed:
            return String.format("%s subscribed to receive notifications for this issue %s.",
                                 actorName, time);
        case Mentioned:
            return String.format("%s was mentioned %s.", actorName, time);
        case Merged:
            return String.format("%s merged this issue %s.", actorName, time);
        case HeadRefDeleted:
            return String.format("%s deleted the pull request's branch %s.", actorName, time);
        case HeadRefRestored:
            return String.format("%s restored the pull request's branch %s.", actorName, time);
        default:
            // Not yet implemented, or no events triggered
            return String.format("%s %s %s.", actorName, getType(), time);
        }
    }
}
