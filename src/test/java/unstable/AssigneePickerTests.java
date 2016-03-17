package unstable;

import backend.resource.TurboIssue;
import guitests.UITest;
import javafx.application.Platform;
import ui.UI;
import util.events.ShowAssigneePickerEvent;

public class AssigneePickerTests extends UITest {

    private static final String QUERY_FIELD_ID = "#queryField";
    private static final String DEFAULT_ISSUECARD_ID = "#dummy/dummy_col0_9";

    private void triggerAssigneePicker(TurboIssue issue) {
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowAssigneePickerEvent(issue));
        waitUntilNodeAppears(QUERY_FIELD_ID);
    }
}
