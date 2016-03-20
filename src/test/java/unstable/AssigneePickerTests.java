package unstable;

import backend.resource.TurboIssue;
import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanelCell;
import util.events.ShowAssigneePickerEvent;

public class AssigneePickerTests extends UITest {

    private static final String QUERY_FIELD_ID = "#queryField";
    private static final String DEFAULT_ISSUECARD_ID = "#dummy/dummy_col0_9";

    // TODO implement test after implementing dummyrepostate assignee related mtd
    @Test
    public void showLabelPicker_typeQuery_displaysCorrectly() {
        triggerAssigneePicker(getIssueCard(DEFAULT_ISSUECARD_ID).getIssue());
        TextField assigneePickerTextField = find(QUERY_FIELD_ID);
        click(assigneePickerTextField);
//        type("world");
//        assertEquals("world", assigneePickerTextField.getText());
        exitCleanly();
    }

    private void exitCleanly() {
        push(KeyCode.ESCAPE);
        waitUntilNodeDisappears(QUERY_FIELD_ID);
    }

    private void triggerAssigneePicker(TurboIssue issue) {
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowAssigneePickerEvent(issue));
        waitUntilNodeAppears(QUERY_FIELD_ID);
    }

    private ListPanelCell getIssueCard(String issueCardId) {
        return find(issueCardId);
    }

}
