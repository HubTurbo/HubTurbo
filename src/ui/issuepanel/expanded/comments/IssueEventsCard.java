package ui.issuepanel.expanded.comments;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import service.TurboIssueEvent;

public class IssueEventsCard extends VBox{
    
    private static int PREF_WIDTH = 300;
    private static int ELEMENTS_HORIZONTAL_SPACING = 10;
    private static int ELEMENTS_VERTICAL_SPACING = 5;
    private static int PADDING = 3;

    private HBox topBar;
    private VBox display;
    
    private TurboIssueEvent event;
    
    public IssueEventsCard(TurboIssueEvent item) {
        this.event = item;

        setSpacing(ELEMENTS_VERTICAL_SPACING);
        setPrefWidth(PREF_WIDTH);
        setPadding(new Insets(PADDING));

        initialiseUIComponents();
    }
    
    private void initialiseUIComponents() {
        String message = getMessage();

        topBar = initialiseTopBar();
        display = initialiseDisplay(message);

        getChildren().addAll(topBar, display);
    }

    private String getMessage() {
        String message = "";
        String actorName = event.getActor().getLogin();
        switch (event.getType()) {
        case Renamed:
            message = String.format("%s renamed the issue from '%s' to '%s'.",
                    actorName,
                    event.getRenamedFrom(),
                    event.getRenamedTo());
            break;
        case Milestoned:
            message = String.format("%s added the milestone '%s'.",
                    actorName,
                    event.getMilestoneTitle());
            break;
        case Demilestoned:
            message = String.format("%s removed the milestone '%s'.",
                    actorName,
                    event.getMilestoneTitle());
            break;
        case Labeled:
            message = String.format("%s added the label '%s'.",
                    actorName,
                    event.getLabelName());
            break;
        case Unlabeled:
            message = String.format("%s removed the label '%s'.",
                    actorName,
                    event.getLabelName());
            break;
        case Assigned:
            message = String.format("%s assigned %s to the issue.",
                    actorName,
                    event.getAssignedUser().getLogin());
            break;
        case Unassigned:
            message = String.format("%s unassigned %s from the issue.",
                    actorName,
                    event.getAssignedUser().getLogin());
            break;
        case Closed:
            message = String.format("%s closed the issue.",
                    actorName);
            break;
        case Reopened:
            message = String.format("%s reopened the issue.",
                    actorName);
            break;
        case Locked:
            message = String.format("%s locked the issue.",
                    actorName);
            break;
        case Unlocked:
            message = String.format("%s unlocked the issue.",
                    actorName);
            break;
        case Subscribed:
        case Merged:
        case HeadRefDeleted:
        case HeadRefRestored:
        case Referenced:
        case Mentioned:
        default:
            // Not yet implemented, or no events triggered
        }
        return message;
    }
    
    private HBox initialiseTopBar(){
        HBox topBar = new HBox();
        topBar.setPrefWidth(PREF_WIDTH);
        topBar.setSpacing(ELEMENTS_HORIZONTAL_SPACING);
		HBox details = new HBox();
		Text creationDate = new Text(formatDisplayedDate(event.getDate()));
		creationDate.getStyleClass().add("issue-comment-details");
		details.setAlignment(Pos.BOTTOM_LEFT);
		details.setSpacing(ELEMENTS_HORIZONTAL_SPACING);
		details.getChildren().addAll(creationDate);
        topBar.getChildren().add(details);
        return topBar;
    }
    
    private VBox initialiseDisplay(String message) {
        VBox display = new VBox();
        display.getStyleClass().add("issue-comment-events");
        display.getChildren().add(new Label(message));
        return display;
    }
    
    private String formatDisplayedDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat("d MMM yy, h:mm a");
        return format.format(date);
    }
}
